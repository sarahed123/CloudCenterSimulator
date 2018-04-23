package edu.asu.emit.algorithm.graph;

public class PathsFilterFirst extends PathsFilter {

	public PathsFilterFirst(Graph g) {
		super(g);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Path filterPaths(Paths paths) {
		// TODO Auto-generated method stub
		return paths.getPaths().getFirst();
	}

}
