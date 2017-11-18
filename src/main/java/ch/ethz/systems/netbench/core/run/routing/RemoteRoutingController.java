package ch.ethz.systems.netbench.core.run.routing;

import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingPath;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import edu.asu.emit.algorithm.graph.Path;

public abstract class RemoteRoutingController extends RoutingPopulator{
	private static RemoteRoutingController mInstance;

	public static RemoteRoutingController getInstance() {
		
		return mInstance;
	}
	
	public static void initRemoteRouting(String type){
		switch(type) {
		case "Xpander":
			mInstance = new XpanderRouter();
			break;
		default:
			mInstance = null;
		}
	}

	@Override
	public void populateRoutingTables() {
		
		
	}
	
	public abstract SourceRoutingPath getRoute(int source,int dest);

}
