package ch.ethz.systems.netbench.xpt.xpander;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.sourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingPath;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.DijkstraShortestPathAlg;

public class XpanderRouter extends RemoteRoutingController{

	
	public XpanderRouter(){
		mG = new VariableGraph(Simulator.getConfiguration().getGraph());
		mPaths = new HashMap<ImmutablePair<Integer,Integer>,SourceRoutingPath>();
	}
	
	@Override
	public SourceRoutingPath getRoute(int source,int dest,RemoteSourceRoutingSwitch s){ 
		DijkstraShortestPathAlg dijkstra = new DijkstraShortestPathAlg(mG);
		
		Path p  = dijkstra.getShortestPath(mG.getVertex(source), mG.getVertex(dest));
		SourceRoutingPath srp = new SourceRoutingPath(p,s);
		List<Vertex> pathAsList = p.getVertexList();
		if(pathAsList.size()==0){
			throw new NoPathException(source,dest);
		}
		int curr = pathAsList.get(0).getId();
		for(int i = 1; i<pathAsList.size();i++){
			mG.deleteEdge(new ImmutablePair<Integer, Integer>(curr, pathAsList.get(i).getId()));
			curr = pathAsList.get(i).getId();
		}
		mPaths.put(new ImmutablePair<Integer, Integer>(source, dest), srp);
		return srp;
		
	}
	
	public void reset(){
		mG.recoverDeletedEdges();
	}
	
	public void recoverPath(Path p){
		for(int i=0; i< p.getVertexList().size() - 1;i++){
			Vertex v = p.getVertexList().get(i);
			Vertex u = p.getVertexList().get(i+1);
			mG.recoverDeletedEdge(new ImmutablePair<Integer,Integer>(v.getId(),u.getId()));
			
		}
	}
	
	@Override
	protected void switchPath(int src,int dst, SourceRoutingPath oldPath, SourceRoutingPath newPath) {
		
		newPath.getSourceSwitch().switchPathToDestination(dst, oldPath, newPath);
	}
}
