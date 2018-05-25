package edu.asu.emit.algorithm.graph.algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import edu.asu.emit.algorithm.graph.BaseGraph;
import edu.asu.emit.algorithm.graph.Vertex;

public class FatTreeShortestPathAlg extends DijkstraShortestPathAlg {
	private Map<Vertex, Boolean> isInDecsent = new HashMap<Vertex, Boolean>();

	public FatTreeShortestPathAlg(BaseGraph graph,double max_weigh) {
		super(graph, max_weigh);

		
	}
	
	/*@Override
	public void clear() {
		super.clear();
		isInDecsent.clear();
	}

	
	@Override
	protected void addToPredecessorIndex(Vertex curAdjacentVertex, Vertex vertex) {
		boolean isRightServer = BaseInitializer.getInstance().getNetworkDeviceById(vertex.getId()).isServer();
		boolean isLeftServer = BaseInitializer.getInstance().getNetworkDeviceById(curAdjacentVertex.getId()).isServer();
		if(!isLeftServer && !isRightServer){
			if(vertex.getId() > curAdjacentVertex.getId()) {
				isInDecsent.put(curAdjacentVertex, true);
			}else {
				isInDecsent.put(curAdjacentVertex, false);
			}
		}

		super.addToPredecessorIndex(curAdjacentVertex, vertex);
		
	}*/
	
	@Override
	protected List<Vertex> getVertexNeighbours(Vertex v, boolean isSource2sink){
		List<Vertex> neighborVertexList = super.getVertexNeighbours(v, isSource2sink);
		/*if(isInDecsent.getOrDefault(v, false)) {

			neighborVertexList.removeIf(entry ->  !BaseInitializer.getInstance().getNetworkDeviceById(entry.getId()).isServer() && entry.getId() > v.getId());

		}*/

		//System.out.println(v);
		//System.out.println(neighborVertexList);
		Collections.shuffle(neighborVertexList);
		return neighborVertexList;
	}

}
