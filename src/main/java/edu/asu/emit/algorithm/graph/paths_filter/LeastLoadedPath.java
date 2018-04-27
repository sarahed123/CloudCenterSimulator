package edu.asu.emit.algorithm.graph.paths_filter;

import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Paths;
import edu.asu.emit.algorithm.graph.Vertex;

public class LeastLoadedPath extends PathsFilter {

	public LeastLoadedPath(Graph g) {
		super(g);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Path filterPaths(Paths paths) {
		double weight = 0;
		Path ret = new Path(0);
		for(Path p : paths.getPaths()) {
			double tmp = 0;
			for(Vertex v : p.getVertexList()) {
				tmp+= G.getAdjacentVertices(v).size();
				
			}
			if(weight < tmp) {
				weight = tmp;
				ret = p;
			}
		}
		return ret;
	}

}
