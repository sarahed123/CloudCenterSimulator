package ch.ethz.systems.netbench.xpt.xpander;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingPath;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.DijkstraShortestPathAlg;

public class XpanderRouter extends RemoteRoutingController{

	Map<Integer, NetworkDevice> mIdToNetworkDevice;
	public XpanderRouter(Map<Integer, NetworkDevice> idToNetworkDevice){
		mIdToNetworkDevice = idToNetworkDevice;
		mG = new VariableGraph(Simulator.getConfiguration().getGraph());
		mPaths = new HashMap<Long,Path>();
	}
	
	@Override
	public void initRoute(int source,int dest,long flowId){ 
		if(mPaths.containsKey(flowId)) {
			throw new IllegalArgumentException("Already have path with this flow " + flowId);
		}
		DijkstraShortestPathAlg dijkstra = new DijkstraShortestPathAlg(mG);
	    
		Path p  = dijkstra.getShortestPath(mG.getVertex(source), mG.getVertex(dest));
		createPath(source,dest,p,flowId);
		mPaths.put(flowId, p);
		
	}
	
	public void reset(){
		mG.recoverDeletedEdges();
		mPaths.clear();
	}
	
	@Override
	public void recoverPath(long flowId){
		Path p = mPaths.get(flowId);
		for(int i=0; i< p.getVertexList().size() - 1;i++){
			Vertex v = p.getVertexList().get(i);
			Vertex u = p.getVertexList().get(i+1);
			mG.recoverDeletedEdge(new ImmutablePair<Integer,Integer>(v.getId(),u.getId()));
			
		}
		mPaths.remove(flowId);
	}
	
	@Override
	protected void switchPath(int src,int dst, Path newPath,long flowId) {
		
		createPath(src,dst,newPath,flowId);
	}

	/**
	 * updates switch routing tables and deletes the edges from the graph
	 * @param source
	 * @param dest
	 * @param p
	 * @param flowId 
	 */
	private void createPath(int source, int dest, Path p, long flowId) {
		List<Vertex> pathAsList = p.getVertexList();
		if(pathAsList.size()==0){
			throw new NoPathException(source,dest);
		}
		int curr = pathAsList.get(0).getId();
		for(int i = 1; i<pathAsList.size();i++){
			
			RemoteSourceRoutingSwitch rsrs = (RemoteSourceRoutingSwitch) mIdToNetworkDevice.get(curr);
			mG.deleteEdge(new ImmutablePair<Integer, Integer>(curr, pathAsList.get(i).getId()));
			curr = pathAsList.get(i).getId();
			rsrs.updateForwardingTable(flowId,curr);
		}
		
	}
}
