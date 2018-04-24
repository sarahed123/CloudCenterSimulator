package edu.asu.emit.algorithm.graph;

import java.util.NoSuchElementException;

public class PathsFilterFirst extends PathsFilter {

	public PathsFilterFirst(Graph g) {
		super(g);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Path filterPaths(Paths paths) {
		// TODO Auto-generated method stub
		try {
			return paths.getPaths().getFirst();
		}catch(NoSuchElementException e) {
			return new Path(0);
		}
		
	}

}
