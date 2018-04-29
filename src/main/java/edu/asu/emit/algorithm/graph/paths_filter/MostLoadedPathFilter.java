package edu.asu.emit.algorithm.graph.paths_filter;

import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Paths;
import edu.asu.emit.algorithm.graph.Vertex;

public class MostLoadedPathFilter extends PathsFilter {

	public MostLoadedPathFilter(Graph g) {
		super(g);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Path filterPaths(Paths paths) {
		// TODO Auto-generated method stub
		double capacity = Long.MAX_VALUE;
		
		Path ret = new Path(0);
		for(Path p : paths.getPaths()) {
			double tmp = 0;
			for(Vertex v : p.getVertexList()) {
				for(Vertex u : G.getAdjacentVertices(v)) {
					tmp+= G.getEdgeCapacity(v, u);
				}
				
				
			}
			if(capacity > tmp) {
				capacity = tmp;
				ret = p;
			}
		}
		return ret;
	}

}
