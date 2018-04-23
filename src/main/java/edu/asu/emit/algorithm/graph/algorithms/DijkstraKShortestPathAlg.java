package edu.asu.emit.algorithm.graph.algorithms;

import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import edu.asu.emit.algorithm.graph.BaseGraph;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Paths;
import edu.asu.emit.algorithm.graph.Vertex;

public class DijkstraKShortestPathAlg extends DijkstraShortestPathAlg {
	private LinkedList<Path> finalPaths;
	private PriorityQueue<Path> foundPaths;
	private int K;
	private double minDistance;
	public DijkstraKShortestPathAlg(BaseGraph graph,int k) {
		super(graph);
		K = k;
		finalPaths = new LinkedList<Path>();
		foundPaths = new PriorityQueue<Path>(new Comparator<Path>() {

			@Override
			public int compare(Path p1, Path p2) {
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
		
		foundPaths.add(new Path(new LinkedList<Vertex>(Arrays.asList(startVertex)),0));
		//startVertexDistanceIndex.put(startVertex, 0.0);
		//startVertex.setWeight(0.0);
		//vertexCandidateQueue.add(startVertex);
		System.out.println(endVertex);
		// 2. start searching for the shortest path
		while (!foundPaths.isEmpty() && foundPaths.size()<K) {
			Path curCandidate = foundPaths.poll();
			System.out.println(curCandidate);
			
			if(curCandidate.getWeight() > minDistance) {
				break;
			}
			
			if (curCandidate.endsWith(endVertex)) {
				minDistance = curCandidate.getWeight();
				finalPaths.add(curCandidate);
			}

			updatePath(curCandidate, isSource2sink);
		}
	}
	
	
	private void updatePath(Path curCandidate, boolean isSource2sink) {
		List<Vertex> neighborVertexList = getVertexNeighbours(curCandidate.getLastVertex(), isSource2sink);
		for (Vertex curAdjacentVertex : neighborVertexList) {
			if(curCandidate.getVertexList().contains(curAdjacentVertex)) {
				continue;
			}
			Path newPath = new Path(curCandidate.getVertexList(),curCandidate.getWeight());
			newPath.addVertex(curAdjacentVertex,graph.getEdgeWeight(curCandidate.getLastVertex(), curAdjacentVertex));
			
			foundPaths.add(newPath);
		}
		
	}
}
