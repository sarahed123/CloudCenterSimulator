package ch.ethz.systems.netbench.xpt.xpander;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.systems.netbench.core.Simulator;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.DijkstraShortestPathAlg;

public class XpanderRouter {
	private HashSet<Path> mPaths;
	
	public XpanderRouter(){
		mPaths = new HashSet<Path>();
	}
	public Path getRoute(int source,int dest){
		VariableGraph G = (VariableGraph) Simulator.getConfiguration().getGraph();
		DijkstraShortestPathAlg dijkstra = new DijkstraShortestPathAlg(G);
		Path p = dijkstra.getShortestPath(G.getVertex(source), G.getVertex(dest));
		List<Vertex> pathAsList = p.getVertexList();
		int curr = pathAsList.get(0).getId();
		for(int i = 1; i<pathAsList.size();i++){
			G.deleteEdge(new ImmutablePair<Integer, Integer>(curr, pathAsList.get(i).getId()));
			curr = pathAsList.get(i).getId();
		}
		mPaths.add(p);
		return p;
		
	}
}
