package ch.ethz.systems.netbench.core.run.routing.remote;

import java.util.HashSet;

import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.xpt.sourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingPath;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;

public abstract class RemoteRoutingController extends RoutingPopulator{
	private static RemoteRoutingController mInstance;
	protected HashSet<Path> mPaths;
	protected VariableGraph mG;
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

	/**
	 * does nothting here
	 */
	@Override
	public void populateRoutingTables() {
		
		
	}
	/**
	 * gets a route from source to dest, removing the corresponding
	 * edges from the graph
	 * @param source
	 * @param dest
	 * @return 
	 * @parma the switch that to return the path to.
	 */
	public abstract SourceRoutingPath getRoute(int source,int dest, RemoteSourceRoutingSwitch s);
	
	/**
	 * resets the graph to its original state
	 */
	public abstract void reset();

	/**
	 * recover a path, returning all its edges to the graph
	 * @param p the path to recover
	 */
	public abstract void recoverPath(Path p);
}
