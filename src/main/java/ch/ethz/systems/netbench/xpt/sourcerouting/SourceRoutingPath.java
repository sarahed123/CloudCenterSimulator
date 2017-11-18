package ch.ethz.systems.netbench.xpt.sourcerouting;



import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Vertex;


public class SourceRoutingPath extends Path {

	public SourceRoutingPath() {
		super(0);
		// TODO Auto-generated constructor stub
	}
    // No adaptations needed, it is only created for
    // semantic purpose.

	public SourceRoutingPath(Path p) {
		super(p.getVertexList(),0);
		// TODO Auto-generated constructor stub
	}

	
}
