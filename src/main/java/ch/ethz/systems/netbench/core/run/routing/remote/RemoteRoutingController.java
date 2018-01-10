package ch.ethz.systems.netbench.core.run.routing.remote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.commons.lang3.tuple.ImmutablePair;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingPath;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;

public abstract class RemoteRoutingController extends RoutingPopulator{
	private static RemoteRoutingController mInstance;
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

}
