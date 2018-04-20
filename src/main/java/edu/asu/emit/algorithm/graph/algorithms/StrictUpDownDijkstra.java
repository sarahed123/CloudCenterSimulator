package edu.asu.emit.algorithm.graph.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.asu.emit.algorithm.graph.BaseGraph;
import edu.asu.emit.algorithm.graph.Vertex;

public class StrictUpDownDijkstra extends DijkstraShortestPathAlg {
	private Map<Vertex, Boolean> isInDecsent = new HashMap<Vertex, Boolean>();

	public StrictUpDownDijkstra(BaseGraph graph) {
		super(graph);

		
	}
	
	@Override
	protected void addToPredecessorIndex(Vertex curAdjacentVertex, Vertex vertex) {
		if(vertex.getId() > curAdjacentVertex.getId()) {
			isInDecsent.put(curAdjacentVertex, true);
		}else {
			isInDecsent.put(curAdjacentVertex, false);
		}
		super.addToPredecessorIndex(curAdjacentVertex, vertex);
		
	}
	
	@Override
	protected List<Vertex> getVertexNeighbours(Vertex v, boolean isSource2sink){
		List<Vertex> neighborVertexList = super.getVertexNeighbours(v, isSource2sink);
		if(isInDecsent.getOrDefault(v, false)) {
			neighborVertexList.removeIf(entry -> entry.getId() > v.getId());

		}
		//System.out.println(v);
		//System.out.println(neighborVertexList);
		return neighborVertexList;
	}

}
