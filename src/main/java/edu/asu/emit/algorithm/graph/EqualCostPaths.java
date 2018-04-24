package edu.asu.emit.algorithm.graph;

import java.util.LinkedList;

public class EqualCostPaths extends Paths {
	double weight;
	Vertex endVertex;
	long limit;
	public EqualCostPaths(Path startPath, double w, int limit) {
		super(startPath);
		weight = w;
		endVertex = startPath.getLastVertex();
		this.limit = limit;
	}
	public EqualCostPaths(EqualCostPaths ecpaths) {
		super(ecpaths.getPaths());
		weight = ecpaths.getWeight();
		endVertex = ecpaths.endVertex;
		this.limit = ecpaths.limit;
	}
	public double getWeight() {
		// TODO Auto-generated method stub
		return weight;
	}
	public boolean endsWith(Vertex endVertex) {
		// TODO Auto-generated method stub
		return this.endVertex.getId() == endVertex.getId();
	}
	public Vertex getLastVertex() {
		// TODO Auto-generated method stub
		return endVertex;
	}
	public void addVertex(Vertex v, double w) {
		for(Path p: mPaths) {
			p.addVertex(v, w);
		}
		this.endVertex = v;
		this.weight += w;
		
	}
	public void addPaths(EqualCostPaths pathsToNode) {
		while(this.mPaths.size() <= limit) {
			Path p = pathsToNode.mPaths.pollFirst();
			if(p==null) {
				break;
			}
			this.mPaths.add(p);
		}
		
	}

}
