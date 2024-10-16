package ch.ethz.systems.netbench.xpt.xpander;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import edu.asu.emit.algorithm.graph.Graph;
import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Paths;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.DijkstraKShortestPathAlg;
import edu.asu.emit.algorithm.graph.algorithms.DijkstraShortestPathAlg;
import edu.asu.emit.algorithm.graph.algorithms.FatTreeShortestPathAlg;
import edu.asu.emit.algorithm.graph.paths_filter.LeastLoadedPath;
import edu.asu.emit.algorithm.graph.paths_filter.LowestIndexFilter;
import edu.asu.emit.algorithm.graph.paths_filter.MostLoadedPathFilter;
import edu.asu.emit.algorithm.graph.paths_filter.PathsFilter;
import edu.asu.emit.algorithm.graph.paths_filter.PathsFilterFirst;
import edu.asu.emit.algorithm.graph.paths_filter.RandomPathsFilter;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class is the base of all xpander routers.
 * Its main functionallity is to use dijkstra to find paths in graph.
 * Only used in initial experiments, since dijkstra is presumably
 * too long.
 */
public class XpanderRouter extends RemoteRoutingController{
	private final double dijkstra_max_weigh;
	private int flowFailuresSample;

	protected final int mMaxNumJFlowsOncircuit;

    enum PathAlgorithm{
		DIJKSTRA,
		STRICT_UP_DOWN_DIJKSTRA
	}
	DijkstraShortestPathAlg dijkstraAlg;
	PathsFilter pathsFilter;
	PathAlgorithm pathAlg;
	protected Graph[] mGraphs; // an array of graphs to simulate colors
	protected Map<Integer, NetworkDevice> mIdToNetworkDevice;
	boolean mIsServerOptics;
	public XpanderRouter(Map<Integer, NetworkDevice> idToNetworkDevice,NBProperties configuration){
		super(configuration);

		initGraphs(configuration,idToNetworkDevice);
		//experimental!

		// unused
		mIsServerOptics = configuration.getBooleanPropertyWithDefault("host_optics_enabled", false);

		// the max cuncurrent flows that a source can transmit optically
		mMaxNumJFlowsOncircuit = configuration.getIntegerPropertyWithDefault("max_num_flows_on_circuit",Integer.MAX_VALUE);
		if(mMaxNumJFlowsOncircuit==Integer.MAX_VALUE){
			System.out.println("WARNING: property max_num_flows_on_circuit is set to infinity. Is that what you want?");
		}

		mIdToNetworkDevice = idToNetworkDevice;
		// main graph should not be used for multiple wave lenghts
		mMainGraph =  configuration.getGraph();
		mMainGraph.resetCapcities(configuration.getBooleanPropertyWithDefault("servers_inifinite_capcacity",false)
				,idToNetworkDevice,configuration.getIntegerPropertyWithDefault("edge_capacity",1));
		mPaths = new HashMap<Pair<Integer,Integer>,Path>();
		totalDrops = 0;
		flowCounter = 0;
		flowFailuresSample = 0;
		String pathsFilterKey = configuration.getPropertyWithDefault("paths_filter","filter_first");

		// filters filter paths by some rule
		// not needed
		switch(pathsFilterKey) {
		case "filter_first":
			pathsFilter = new PathsFilterFirst(mMainGraph);
			break;
		case "by_lower_index":
			pathsFilter = new LowestIndexFilter(mMainGraph);
			break;
		case "least_loaded_path":
			pathsFilter = new LeastLoadedPath(mMainGraph);
			break;
		case "random_path" :
			pathsFilter = new RandomPathsFilter(mMainGraph);
			break;
		case "most_loaded_path":
			pathsFilter = new MostLoadedPathFilter(mMainGraph);
			break;
		default:
			throw new RuntimeException("Illegal argument for paths_filter " + pathsFilterKey);
		}
		
		String pathAlgorithm = configuration.getProperty("path_algorithm");
		if(pathAlgorithm==null) {
			pathAlgorithm = "dijkstra";
		}
		dijkstra_max_weigh = configuration.getDoublePropertyWithDefault("maximum_path_weight", Double.MAX_VALUE);
		switch(pathAlgorithm) {
		case "dijkstra":
			String vertexShuffle = configuration.getBooleanPropertyWithDefault("dijkstra_vertex_shuffle", true) ? "dijkstra_vertex_shuffle" : null;
			dijkstraAlg = new DijkstraShortestPathAlg(mMainGraph,dijkstra_max_weigh,vertexShuffle);
			break;
		case "fat_tree_dijkstra":
			boolean isInExtendedTopology = configuration.getPropertyWithDefault("scenario_topology_extend_with_servers","none").equals("regular");
			int ftDegree = configuration.getIntegerPropertyOrFail("fat_tree_degree");
			dijkstraAlg = new FatTreeShortestPathAlg(mMainGraph,ftDegree,isInExtendedTopology);
			break;
		case "k_shortest_paths":
			int K = configuration.getIntegerPropertyOrFail("k_shortest_paths_num");
			dijkstraAlg = new DijkstraKShortestPathAlg(mMainGraph, K,dijkstra_max_weigh,null);
			break;
		default:
			throw new RuntimeException("Illegal argument for path_algorithm " + pathAlgorithm);
		}
	}

	/**
	 * inits graph copies
	 * @param configuration
	 * @param idToNetworkDevice
	 */
	protected void initGraphs(NBProperties configuration, Map<Integer, NetworkDevice> idToNetworkDevice) {
		mGraphs = new Graph[configuration.getIntegerPropertyOrFail("circuit_wave_length_num")];
		for(int i = 0; i < mGraphs.length; i++){
			mGraphs[i] = configuration.getGraphCopy();
			mGraphs[i].resetCapcities(configuration.getBooleanPropertyWithDefault("servers_inifinite_capcacity",false)
					,idToNetworkDevice,configuration.getIntegerPropertyWithDefault("edge_capacity",1));
		}
	}





	@Override
	public int getCircuitFlowLimit() {
		return mMaxNumJFlowsOncircuit;
	}


	/**
	 * uses dijkstra to generate a path in the graph
	 * @param source
	 * @param dest
	 * @return
	 */
	protected Path generatePathFromGraph(int source,int dest) {
		Paths ps;
		Path p = null;
		for(int i = 0; i<this.mGraphs.length; i++){
			DijkstraShortestPathAlg dijkstra = new DijkstraShortestPathAlg(mGraphs[i],dijkstra_max_weigh,null);
			ps = dijkstra.getShortestPath(mGraphs[i].getVertex(source), mGraphs[i].getVertex(dest));
			p = pathsFilter.filterPaths(ps);
			p.setColor(i);
			if(p.getVertexList().size()>0){
				break;
			}

		}
		return p;
	}

	/**
	 * resets capacities and paths map
	 */
	public void reset(){
		for(int i = 0; i<mGraphs.length; i++) {
			mGraphs[i].resetCapcities(configuration.getBooleanPropertyWithDefault("servers_inifinite_capcacity",false)
					, mIdToNetworkDevice, configuration.getIntegerPropertyWithDefault("edge_capacity", 1));
		}
		mMainGraph.resetCapcities(configuration.getBooleanPropertyWithDefault("servers_inifinite_capcacity",false)
				,mIdToNetworkDevice, configuration.getIntegerPropertyWithDefault("edge_capacity", 1));
		mPaths.clear();
	}


	@Override
	protected void returnPathToGraph(Path p, int sourceKey, int destKey, int transimttingSource, int receivingDest, long jumboFlowId) {

		for(int i=0; i< p.getVertexList().size() - 1;i++){
			Vertex v = p.getVertexList().get(i);
			Vertex u = p.getVertexList().get(i+1);
			mGraphs[p.getColor()].increaseCapacity(new ImmutablePair<Integer,Integer>(v.getId(),u.getId()));
			RemoteSourceRoutingSwitch rsrs = (RemoteSourceRoutingSwitch) mIdToNetworkDevice.get(v.getId());
			rsrs.removeFromForwardingTable(jumboFlowId);
			// recover the opisite edge - currently not used
			//mMainGraph.increaseCapacity(new ImmutablePair<Integer,Integer>(u.getId(),v.getId()));


		}
	}

	/**
	 * removes a path from a graph, based on the path color,
	 * by decreasing the capacity.
	 * @param p
	 */
	protected void removePathFromGraph(Path p) {
		List<Vertex> pathAsList = p.getVertexList();
		int curr = pathAsList.get(0).getId();
		for(int i = 1; i<pathAsList.size();i++){
			int next = pathAsList.get(i).getId();
			mGraphs[p.getColor()].decreaseCapacity(new ImmutablePair<Integer, Integer>(curr, next));
			// delete the opisite edge - currently not used
			//mMainGraph.decreaseCapacity(new ImmutablePair<Integer, Integer>(next, curr));
			curr = next;
		}
		
	}

	/**
	 * not fully implemented and not used
	 * @param src the id of the switch that will get the new path
	 * @param dst the destination of the path
	 * @param newPath
	 * @param flowId
	 */
	@Override
	protected void switchPath(int src,int dst, Path newPath,long flowId) {
		//not tested!!!
		updateForwardingTables(src,dst,newPath,flowId);
		removePathFromGraph(newPath);
		mPaths.put(new ImmutablePair<>(src,dst),newPath);
	}

	/**
	 * updates switch routing tables and deletes the edges from the graph
	 * @param source
	 * @param dest
	 * @param p
	 * @param jumboFlowId
	 */
	protected void updateForwardingTables(int source, int dest, Path p, long jumboFlowId) {
		List<Vertex> pathAsList = p.getVertexList();
		if(pathAsList.size()==0){
			flowFailuresSample++;
			totalDrops++;
			SimulationLogger.increaseStatisticCounter("XPANDER_CONTROLLER_NO_PATH");
			//logDrop(flowId, source, dest, mPaths.size());
			throw new NoPathException(source,dest);	
		}
		int curr = pathAsList.get(0).getId();
		for(int i = 1; i<pathAsList.size();i++){

			RemoteSourceRoutingSwitch rsrs = (RemoteSourceRoutingSwitch) mIdToNetworkDevice.get(curr);
			curr = pathAsList.get(i).getId();
			rsrs.updateForwardingTable(jumboFlowId,curr);
		}


	}



	public void logCurrentState() {
		logCurrentState(mPaths.size(),flowFailuresSample,flowCounter);
		
		
	}

	public void resetCurrentState() {
		flowFailuresSample = 0;
		
	}
	


	@Override
	public void dumpState(String dumpFolderName) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dumpFolderName + "/" + "central_router_graph.ser")); 
		
		oos.writeObject(mMainGraph);
		System.out.println("Done writing graph");
		oos = new ObjectOutputStream(new FileOutputStream(dumpFolderName + "/" + "central_router_paths.ser")); 
		
		oos.writeObject(mPaths);
		System.out.println("Done writing paths");
		
	}

	@Override
	protected void reset_state(NBProperties configuration) {
		// needs to be fixed inorder to reallow state saving

		/*String dumpFolder = configuration.getPropertyOrFail("from_state");
		mMainGraph = (VariableGraph) SimulatorStateSaver.readObjectFromFile(dumpFolder + "/" + "central_router_graph.ser");
		mPaths = (HashMap<Pair<Integer,Integer>, Path>) SimulatorStateSaver.readObjectFromFile(dumpFolder + "/" + "central_router_paths.ser");
		for(Long flow : mPaths.keySet()) {
			int source = mPaths.get(flow).getVertexList().get(0).getId();
			int dest = mPaths.get(flow).getVertexList().get(mPaths.get(flow).getVertexList().size()-1).getId();
			//System.out.println("flow " + flow);
			//System.out.println("path " + mPaths.get(flow));
			updateForwardingTables(source, dest, mPaths.get(flow), flow);
		}
		*/
	}
}
