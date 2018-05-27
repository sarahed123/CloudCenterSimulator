package ch.ethz.systems.netbench.xpt.xpander;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.core.state.SimulatorStateSaver;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Paths;
import edu.asu.emit.algorithm.graph.VariableGraph;
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

public class XpanderRouter extends RemoteRoutingController{
	private int flowFailuresSample;
	enum PathAlgorithm{
		DIJKSTRA,
		STRICT_UP_DOWN_DIJKSTRA
	}
	DijkstraShortestPathAlg dijkstraAlg;
	PathsFilter pathsFilter;
	PathAlgorithm pathAlg;
	Map<Integer, NetworkDevice> mIdToNetworkDevice;
	public XpanderRouter(Map<Integer, NetworkDevice> idToNetworkDevice){
		mIdToNetworkDevice = idToNetworkDevice;
		mG =  Simulator.getConfiguration().getGraph();
		mG.resetCapcities();
		mPaths = new HashMap<Long,Path>();
		totalDrops = 0;
		flowCounter = 0;
		flowFailuresSample = 0;
		String pathsFilterKey = Simulator.getConfiguration().getPropertyWithDefault("paths_filter","filter_first");
		switch(pathsFilterKey) {
		case "filter_first":
			pathsFilter = new PathsFilterFirst(mG);
			break;
		case "by_lower_index":
			pathsFilter = new LowestIndexFilter(mG);
			break;
		case "least_loaded_path":
			pathsFilter = new LeastLoadedPath(mG);
			break;
		case "random_path" :
			pathsFilter = new RandomPathsFilter(mG);
			break;
		case "most_loaded_path":
			pathsFilter = new MostLoadedPathFilter(mG);
			break;
		default:
			throw new RuntimeException("Illegal argument for paths_filter " + pathsFilterKey);
		}
		
		String pathAlgorithm = Simulator.getConfiguration().getProperty("path_algorithm");
		if(pathAlgorithm==null) {
			pathAlgorithm = "dijkstra";
		}
		double max_weigh = Simulator.getConfiguration().getDoublePropertyWithDefault("maximum_path_weight", Double.MAX_VALUE);
		switch(pathAlgorithm) {
		case "dijkstra":

			dijkstraAlg = new DijkstraShortestPathAlg(mG,max_weigh);
			break;
		case "fat_tree_dijkstra":
			dijkstraAlg = new FatTreeShortestPathAlg(mG,8);
			break;
		case "k_shortest_paths":
			int K = Simulator.getConfiguration().getIntegerPropertyOrFail("k_shortest_paths_num");
			dijkstraAlg = new DijkstraKShortestPathAlg(mG, K,max_weigh);
			break;
		default:
			throw new RuntimeException("Illegal argument for path_algorithm " + pathAlgorithm);
		}
	}

	@Override
	public void initRoute(int source,int dest,long flowId){ 
		
		if(mPaths.containsKey(flowId)) {
			throw new FlowPathExists(flowId);
		}
		Path p = generatePathFromGraph(source, dest);
		
		updateForwardingTables(source,dest,p,flowId);
		removePathFromGraph(p);
		mPaths.put(flowId, p);
		flowCounter++;
		logRoute(p,source,dest,flowId,Simulator.getCurrentTime(),true);

	}
	
	

	protected Path generatePathFromGraph(int source,int dest) {
		Paths ps  = dijkstraAlg.getShortestPath(mG.getVertex(source), mG.getVertex(dest));
		//System.out.println(ps);
		Path p = pathsFilter.filterPaths(ps);
		//System.out.println(p);
		return p;
	}

	public void reset(){
		mG.resetCapcities();
		mPaths.clear();
	}

	@Override
	public void recoverPath(long flowId){
		Path p = mPaths.get(flowId);
		for(int i=0; i< p.getVertexList().size() - 1;i++){
			Vertex v = p.getVertexList().get(i);
			Vertex u = p.getVertexList().get(i+1);
			mG.increaseCapacity(new ImmutablePair<Integer,Integer>(v.getId(),u.getId()));
			// recover the opisite edge
			mG.increaseCapacity(new ImmutablePair<Integer,Integer>(u.getId(),v.getId()));


		}
		logRoute(p,p.getVertexList().get(0).getId(),p.getVertexList().get(p.getVertexList().size()-1).getId()
				,flowId,Simulator.getCurrentTime(),false);
		mPaths.remove(flowId);
	}

	protected void removePathFromGraph(Path p) {
		List<Vertex> pathAsList = p.getVertexList();
		int curr = pathAsList.get(0).getId();
		for(int i = 1; i<pathAsList.size();i++){
			int next = pathAsList.get(i).getId();
			mG.decreaseCapacity(new ImmutablePair<Integer, Integer>(curr, next));
			// delete the opisite edge
			mG.decreaseCapacity(new ImmutablePair<Integer, Integer>(next, curr));
			curr = next;
		}
		
	}

	@Override
	protected void switchPath(int src,int dst, Path newPath,long flowId) {

		updateForwardingTables(src,dst,newPath,flowId);
		removePathFromGraph(newPath);
		mPaths.put(flowId,newPath);
	}

	/**
	 * updates switch routing tables and deletes the edges from the graph
	 * @param source
	 * @param dest
	 * @param p
	 * @param flowId 
	 */
	protected void updateForwardingTables(int source, int dest, Path p, long flowId) {
		List<Vertex> pathAsList = p.getVertexList();
		if(pathAsList.size()==0){
			flowFailuresSample++;
			totalDrops++;
			logDrop(flowId, source, dest, mPaths.size());
			throw new NoPathException(source,dest);	
		}
		int curr = pathAsList.get(0).getId();
		for(int i = 1; i<pathAsList.size();i++){

			RemoteSourceRoutingSwitch rsrs = (RemoteSourceRoutingSwitch) mIdToNetworkDevice.get(curr);
			curr = pathAsList.get(i).getId();
			rsrs.updateForwardingTable(flowId,curr);
		}

	}

	public String getCurrentState() {
		// TODO Auto-generated method stub
		int numEdges = 0;
		HashMap<Integer,Integer> loadMap = new HashMap<Integer,Integer>();
		for(Path p : mPaths.values()){
			numEdges += p.getVertexList().size();
			int load = loadMap.getOrDefault(p.getVertexList().size(),0);
			loadMap.put(p.getVertexList().size(),load+1);
		}

		String state = "Allocated paths " + mPaths.size() + ". Flow dropps " + flowFailuresSample + ". Flow count " + flowCounter + "\n";
		state+= "num edges " + numEdges + "\n";
		for(int pathLen : loadMap.keySet()){
			state +=  "paths of len " + pathLen + " have count " + loadMap.get(pathLen) + "\n";
		}

		return state;
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
		
		oos.writeObject(mG);
		System.out.println("Done writing graph");
		oos = new ObjectOutputStream(new FileOutputStream(dumpFolderName + "/" + "central_router_paths.ser")); 
		
		oos.writeObject(mPaths);
		System.out.println("Done writing paths");
		
	}

	@Override
	protected void reset_state() {
		String dumpFolder = Simulator.getConfiguration().getPropertyOrFail("from_state");
		mG = (VariableGraph) SimulatorStateSaver.readObjectFromFile(dumpFolder + "/" + "central_router_graph.ser");
		mPaths = (HashMap<Long, Path>) SimulatorStateSaver.readObjectFromFile(dumpFolder + "/" + "central_router_paths.ser");
		for(Long flow : mPaths.keySet()) {
			int source = mPaths.get(flow).getVertexList().get(0).getId();
			int dest = mPaths.get(flow).getVertexList().get(mPaths.get(flow).getVertexList().size()-1).getId();
			//System.out.println("flow " + flow);
			//System.out.println("path " + mPaths.get(flow));
			updateForwardingTables(source, dest, mPaths.get(flow), flow);
		}
		
	}
}
