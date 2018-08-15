package ch.ethz.systems.netbench.core.run.routing.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorNetController;

import ch.ethz.systems.netbench.core.config.exceptions.PropertyMissingException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicController;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import org.apache.commons.lang3.tuple.Pair;

public abstract class RemoteRoutingController extends RoutingPopulator{
	public RemoteRoutingController(NBProperties configuration) {
		super(configuration);
	}

	private static RemoteRoutingController mInstance = null;
	protected HashMap<Pair<Integer,Integer>, Path> mPaths;
	protected Graph mG;
	private static long headerSize;
	protected long totalDrops;
	protected long flowCounter;
	public static RemoteRoutingController getInstance() {
		return mInstance;
	}
	
	public static void initRemoteRouting(String type, Map<Integer, NetworkDevice> idToNetworkDevice, long headerSize, NBProperties configuration){
		
		switch(type) {
		case "Xpander":
			mInstance = new XpanderRouter(idToNetworkDevice , configuration);
			break;
		case "dynamic":
			mInstance = new DynamicController(idToNetworkDevice, configuration);
			break;
			case "rotor_net":
			mInstance = new RotorNetController(idToNetworkDevice,configuration);
			break;
		default:
			throw new PropertyValueInvalidException(configuration,"centered_routing_type");
		}
		mInstance.setHeaderSize(headerSize);
		try {
			mInstance.reset_state(configuration);
		}catch(PropertyMissingException e) {
			
		}
	}

	protected abstract void reset_state(NBProperties configuration);

	private void setHeaderSize(long headerSize) {
		this.headerSize = headerSize;
		
	}
	
	public long getTotalDrops() {
		return totalDrops;
	}
	
	public long getTotalFlows() {
		return flowCounter;
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
			if(configuration.getBooleanPropertyWithDefault("log_remote_paths", false))
				SimulationLogger.logRemoteRoute(p,source,dest,flowId,time,adding);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void logCurrentState(int currentAllocatedPatsh, int flowFailuresSample, long flowCounter2){
		try {
			if(configuration.getBooleanPropertyWithDefault("log_remote_router_state", false))
				SimulationLogger.logRemoteRouterState(currentAllocatedPatsh,flowFailuresSample,flowCounter2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void logDrop(long flowId, int source,int dest,int allocPaths) {
		if(configuration.getBooleanPropertyWithDefault("log_remote_router_drops", false)) {
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
	 */
	public abstract void initRoute(int source,int dest, long flowId);
	
	/**
	 * resets the graph to its original state
	 */
	public abstract void reset();

	/**
	 * recover a path, returning all its edges to the graph
	 */
	public abstract void recoverPath(int src, int dst, long jumboFlowId);
	
	/**
	 * public for testing but should be a private method to handle path switching
	 * @param src the id of the switch that will get the new path
	 * @param dst the destination of the path
	 */
	protected void switchPath(int src,int dst,Path p,long flowId) {
		
	}

	public abstract void dumpState(String dumpFolderName) throws IOException;

}
