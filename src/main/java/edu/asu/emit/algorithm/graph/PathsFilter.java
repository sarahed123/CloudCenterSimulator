package edu.asu.emit.algorithm.graph;

public abstract class PathsFilter {
	Graph G;
	public PathsFilter(Graph g) {
		G = g;
	}
	
	public abstract Path filterPaths(Paths paths);
	
	
}
