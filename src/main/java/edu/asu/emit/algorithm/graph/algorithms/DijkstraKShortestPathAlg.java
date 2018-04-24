package edu.asu.emit.algorithm.graph.algorithms;

import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import edu.asu.emit.algorithm.graph.BaseGraph;
import edu.asu.emit.algorithm.graph.EqualCostPaths;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Paths;
import edu.asu.emit.algorithm.graph.Vertex;

public class DijkstraKShortestPathAlg extends DijkstraShortestPathAlg {
	private LinkedList<Path> finalPaths;
	private PriorityQueue<EqualCostPaths> foundPaths;
	private HashMap<Integer,EqualCostPaths> visitedNodes;
	private int K;
	private double minDistance;
	public DijkstraKShortestPathAlg(BaseGraph graph,int k) {
		super(graph);
		K = k;
		visitedNodes = new HashMap<Integer,EqualCostPaths>();
		finalPaths = new LinkedList<Path>();
		foundPaths = new PriorityQueue<EqualCostPaths>(new Comparator<EqualCostPaths>() {

			@Override
			public int compare(EqualCostPaths p1, EqualCostPaths p2) {
				if(p1.getWeight() > p2.getWeight()) {
					return 1;
				}
				if(p1.getWeight() < p2.getWeight()) {
					return -1;
				}
				return 0;
			}
		});
		minDistance = Graph.DISCONNECTED;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void clear() {
		super.clear();
		foundPaths.clear();
		finalPaths.clear();
		visitedNodes.clear();
		minDistance = Graph.DISCONNECTED;
	}
	
	@Override
	public Paths getShortestPath(Vertex sourceVertex, Vertex sinkVertex) {
		determineShortestPaths(sourceVertex, sinkVertex, true);
		return new Paths(finalPaths);
		
	}
	
	@Override
	protected void determineShortestPaths(Vertex sourceVertex,
			Vertex sinkVertex, boolean isSource2sink)	{
		// 0. clean up variables
		clear();

		// 1. initialize members
		Vertex endVertex = isSource2sink ? sinkVertex : sourceVertex;
		Vertex startVertex = isSource2sink ? sourceVertex : sinkVertex;
		Path startPath = new Path(new LinkedList<Vertex>(Arrays.asList(startVertex)),0);
		EqualCostPaths startPaths = new EqualCostPaths(startPath,0,K);
		foundPaths.add(startPaths);
		visitedNodes.put(startVertex.getId(),startPaths);
		//startVertexDistanceIndex.put(startVertex, 0.0);
		//startVertex.setWeight(0.0);
		//vertexCandidateQueue.add(startVertex);
		// 2. start searching for the shortest path
		//System.out.println("starting " + startVertex.getId() + " to " + endVertex.getId());
		while (!foundPaths.isEmpty()) {
			EqualCostPaths curCandidate = foundPaths.poll();
			//System.out.println("trying path " + curCandidate);
			
			if(curCandidate.getWeight() > minDistance) {
				break;
			}
			
			if (curCandidate.endsWith(endVertex)) {
				//System.out.println();
				//System.out.println("adding path " + curCandidate);
				minDistance = curCandidate.getWeight();
				finalPaths.addAll(curCandidate.getPaths());
			}

			updatePath(curCandidate, isSource2sink);
		}
	}
	
	
	private void updatePath(EqualCostPaths curCandidate, boolean isSource2sink) {
		List<Vertex> neighborVertexList = getVertexNeighbours(curCandidate.getLastVertex(), isSource2sink);
		for (Vertex curAdjacentVertex : neighborVertexList) {
			
			double edgeWeight = graph.getEdgeWeight(curCandidate.getLastVertex(), curAdjacentVertex);
			EqualCostPaths pathsToNode = visitedNodes.get(curAdjacentVertex.getId());
			if(pathsToNode!=null) {
				if(pathsToNode.getWeight()<curCandidate.getWeight()+edgeWeight) {
					continue;
				}
			}
			EqualCostPaths newPaths = new EqualCostPaths(curCandidate);
			newPaths.addVertex(curAdjacentVertex,edgeWeight);
			if(pathsToNode!=null) {
				pathsToNode.addPaths(newPaths);
			}else {
				visitedNodes.put(curAdjacentVertex.getId(),newPaths);
				foundPaths.add(newPaths);
			}
		
		}
		
	}
}
