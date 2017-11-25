package ch.ethz.systems.netbench.xpt.xpander;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.run.routing.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingPath;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.DijkstraShortestPathAlg;

public class XpanderRouter extends RemoteRoutingController{
	private HashSet<Path> mPaths;
	private VariableGraph mG;
	
	public XpanderRouter(){
		mG = new VariableGraph(Simulator.getConfiguration().getGraph());
		mPaths = new HashSet<Path>();
	}
	
	@Override
	public SourceRoutingPath getRoute(int source,int dest){ 
		DijkstraShortestPathAlg dijkstra = new DijkstraShortestPathAlg(mG);
		
		Path p  = dijkstra.getShortestPath(mG.getVertex(source), mG.getVertex(dest));
		SourceRoutingPath srp = new SourceRoutingPath(p);
		List<Vertex> pathAsList = p.getVertexList();
		if(pathAsList.size()==0){
			throw new NoPathException(source,dest);
		}
		int curr = pathAsList.get(0).getId();
		for(int i = 1; i<pathAsList.size();i++){
			mG.deleteEdge(new ImmutablePair<Integer, Integer>(curr, pathAsList.get(i).getId()));
			curr = pathAsList.get(i).getId();
		}
		mPaths.add(p);
		return srp;
		
	}
}
