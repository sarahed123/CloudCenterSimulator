package ch.ethz.systems.netbench.core.run.routing.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingPath;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;

public abstract class RemoteRoutingController extends RoutingPopulator{
	private static RemoteRoutingController mInstance = null;
	protected HashMap<Long, Path> mPaths;
	protected VariableGraph mG;
	private static long headerSize;
	public static RemoteRoutingController getInstance() {
		return mInstance;
	}
	
	public static void initRemoteRouting(String type, Map<Integer, NetworkDevice> idToNetworkDevice, long headerSize){
		
		switch(type) {
		case "Xpander":
			mInstance = new XpanderRouter(idToNetworkDevice);
			break;
		default:
			throw new PropertyValueInvalidException(Simulator.getConfiguration(),"centered_routing_type");
		}
		mInstance.setHeaderSize(headerSize);
	}

	private void setHeaderSize(long headerSize) {
		this.headerSize = headerSize;
		
	}
	
	public static long getHeaderSize() {
		return headerSize;
	}
	
	/**
	 * does nothting here
	 */
	@Override
	public void populateRoutingTables() {
		
		
	}
	
	protected void logRoute(Path p, int source, int dest, long flowId, long time,boolean adding) {
		try {
			if(Simulator.getConfiguration().getBooleanPropertyWithDefault("log_remote_paths", false))
				SimulationLogger.logRemoteRoute(p,source,dest,flowId,time,adding);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void logCurrentState(int currentAllocatedPatsh, int flowFailuresSample, int flowCounter){
		try {
			if(Simulator.getConfiguration().getBooleanPropertyWithDefault("log_remote_router_state", false))
				SimulationLogger.logRemoteRouterState(currentAllocatedPatsh,flowFailuresSample,flowCounter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void logDrop(long flowId, int source,int dest,int allocPaths) {
		if(Simulator.getConfiguration().getBooleanPropertyWithDefault("log_remote_router_drops", false)) {
			try {
				SimulationLogger.logRemoteRouterDropStatistics(flowId, source, dest, allocPaths);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * gets a route from source to dest, removing the corresponding
	 * edges from the graph
	 * @param source
	 * @param dest
	 * @param flowId the flow id
	 * @param sourceSwitch the source switch for this path
	 */
	public abstract void initRoute(int source,int dest, long flowId);
	
	/**
	 * resets the graph to its original state
	 */
	public abstract void reset();

	/**
	 * recover a path, returning all its edges to the graph
	 * @param p the path to recover
	 */
	public abstract void recoverPath(long flowId);
	
	/**
	 * public for testing but should be a private method to handle path switching
	 * @param src the id of the switch that will get the new path
	 * @param dst the destination of the path
	 * @param old the  old path to switch from
	 * @param newPath the new path
	 */
	protected void switchPath(int src,int dst,Path p,long flowId) {
		
	}

	public abstract void dumpState(String dumpFolderName) throws IOException;

}
