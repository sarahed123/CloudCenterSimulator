package ch.ethz.systems.netbench.xpt.remotesourcerouting;

import java.util.Map;

import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.algorithms.DijkstraShortestPathAlg;

public class MockRemoteRouter extends XpanderRouter {

	public MockRemoteRouter(Map<Integer, NetworkDevice> idToNetworkDevice) {
		super(idToNetworkDevice);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void switchPath(int src,int dst, Path newPath,long flowId) {
		
		super.switchPath(src, dst, newPath, flowId);
	}
	
	protected Path generatePathFromGraph(int source,int dest) {
		
		return super.generatePathFromGraph(source, dest);
	}
	
	protected Path getPath(long flowId) {
		return  mPaths.get(flowId);
	}
	
	


}
