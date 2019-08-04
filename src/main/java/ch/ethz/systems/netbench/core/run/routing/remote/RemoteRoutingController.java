package ch.ethz.systems.netbench.core.run.routing.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorNetController;

import ch.ethz.systems.netbench.core.config.exceptions.PropertyMissingException;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicController;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.DistributedController;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import ch.ethz.systems.netbench.xpt.xpander.SemiXpander;
import ch.ethz.systems.netbench.xpt.xpander.SemiXpanderServerOptics;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public abstract class RemoteRoutingController extends RoutingPopulator{
	protected long flowPathExistsCounter = 0;
	protected long noPathCounter = 0;
	public RemoteRoutingController(NBProperties configuration) {
		super(configuration);
		mFlowIdsOnCircuit = new HashMap<>();
		mTransmittingSources = new HashMap<>();
		mRecievingDestinations = new HashMap<>();
		mAllocateddPathsNum = 0;
		mDeAllocatedPathsNum = 0;
	}
	protected Map<Integer,Integer> mTransmittingSources;
	protected Map<Integer,Integer> mRecievingDestinations;
	private static RemoteRoutingController mInstance = null;
	protected HashMap<Pair<Integer,Integer>, Path> mPaths;
	protected HashMap<Pair<Integer,Integer>, Set<Long>> mFlowIdsOnCircuit;
	protected int mAllocateddPathsNum;
	protected int mDeAllocatedPathsNum;

	protected Graph mMainGraph;
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
		case "semi_Xpander":
			mInstance = new SemiXpander(idToNetworkDevice,configuration);
			break;
		case "semi_Xpander_server_optics":
			if(Simulator.getConfiguration().getBooleanPropertyWithDefault("distributed_protocol_enabled",false)){
				mInstance = new DistributedController(idToNetworkDevice,configuration);
			}else{
				mInstance = new SemiXpanderServerOptics(idToNetworkDevice,configuration);
			}
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

    public static void setRemoteRouter(RemoteRoutingController router) {
		mInstance = router;
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

		SimulationLogger.regiserPathActive(p,adding);

		
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
	 * @param jumboFlowId the jumbo flow id
	 */
	public void initRoute(int source,int dest, long jumboFlowId){
		this.initRoute(source,dest,source,dest,jumboFlowId, 0);
	}

	public void initRoute(int transimttingSource, int receivingDest, int sourceKey, int destKey, long jumboFlowId){
		this.initRoute(transimttingSource,receivingDest,sourceKey,destKey,jumboFlowId, 0);
	}


	/**
	 * this is the base method for circuit creation
	 * @param transimttingSource the actaul source device who will transmit
	 * @param receivingDest the actual destination device who will receiv
	 * @param sourceKey the source key to aggregate by
	 * @param destKey the dest key to aggregate by
	 * @param jumboFlowId the jumbo flow id
	 * @param sizeBit
	 */
	public void initRoute(int transimttingSource, int receivingDest, int sourceKey, int destKey, long jumboFlowId, long sizeBit){
		ImmutablePair pair = new ImmutablePair<>(sourceKey,destKey);

		if(mPaths.containsKey(pair)) { // no  no need to create a new circuit

			throw new FlowPathExists(jumboFlowId);
		}

		if(receivingDest==transimttingSource && !trivialPathAllowed()){

			throw new NoPathException(); // assuming the EPS will pick it up
		}

		//check that circuit limit has not been reached:
		if(mTransmittingSources.getOrDefault(transimttingSource,0) >= getCircuitFlowLimit() || mRecievingDestinations.getOrDefault(receivingDest,0)>=getCircuitFlowLimit()){
			SimulationLogger.increaseStatisticCounter("TOO_MANY_DESTS_OR_SOURCES_ON_TOR");
			throw new NoPathException(transimttingSource,receivingDest);
		}
		Path p;


		p = generatePathFromGraph(transimttingSource, receivingDest);
		updateForwardingTables(sourceKey,destKey,p,jumboFlowId);
		removePathFromGraph(p);
		onPathAllocation(transimttingSource,receivingDest);
		mAllocateddPathsNum++;
		mPaths.put(pair, p);
		flowCounter++;
		logRoute(p,transimttingSource,receivingDest,jumboFlowId,Simulator.getCurrentTime(),true);
	}

	/**
	 *
	 * @return true if there is a need to allocate trivial paths, else false
	 */
	protected boolean trivialPathAllowed() {
		return false;
	}


	protected Path allocateTrivialPath(int source,int dest){
		throw new NoPathException();
	}

	/**
	 * removes the path from the graph such that other requests will not be issued same edges.
	 * @param p
	 */
	protected abstract void removePathFromGraph(Path p);

	/**
	 * reconfigure the routing tables in switches according to source-dest pair
	 * @param source
	 * @param dest
	 * @param p
	 * @param flowId
	 */
	protected abstract void updateForwardingTables(int source, int dest, Path p, long flowId);

	/**
	 * generate a path from sourceToR to destToR
	 * @param sourceToR
	 * @param destToR
	 * @return
	 */
	protected abstract Path generatePathFromGraph(int sourceToR, int destToR);

	public abstract int getCircuitFlowLimit();

	/**
	 * resets the graph to its original state
	 */
	public abstract void reset();

	/**
	 * recover a path, returning all its edges to the graph
	 */
	public void recoverPath(int src, int dst, long jumboFlowId){
		this.recoverPath(src,dst,src,dst,jumboFlowId);
	}

	public void recoverPath(int transimttingSource,int receivingDest, int sourceKey, int destKey, long jumboFlowId){

		Pair<Integer, Integer> pair = new ImmutablePair<Integer, Integer>(sourceKey, destKey);
		Path p = mPaths.get(pair);

		if(p==null) {
			throw new NoPathException();
		}

		returnPathToGraph(p,sourceKey,destKey,transimttingSource,receivingDest,jumboFlowId);
		logRoute(mPaths.get(pair),transimttingSource,receivingDest,jumboFlowId, Simulator.getCurrentTime(),false);
		mPaths.remove(pair);
		onPathDeAllocation(transimttingSource,receivingDest);
		mDeAllocatedPathsNum--;
	}

	/**
	 * return path p to the graph
	 * @param p
	 * @param sourceKey
	 * @param destKey
	 * @param transimttingSource
	 * @param receivingDest
	 * @param jumboFlowId
	 */
	protected abstract void returnPathToGraph(Path p, int sourceKey, int destKey, int transimttingSource, int receivingDest, long jumboFlowId);

	/**
	 * public for testing but should be a private method to handle path switching
	 * @param src the id of the switch that will get the new path
	 * @param dst the destination of the path
	 */
	protected void switchPath(int src,int dst,Path p,long flowId) {
		
	}

	public abstract void dumpState(String dumpFolderName) throws IOException;

	public String getCurrentState() {
		// TODO Auto-generated method stub
//		int numEdges = 0;
//		HashMap<Integer,Integer> loadMap = new HashMap<Integer,Integer>();
//		for(Path p : mPaths.values()){
//			numEdges += p.getVertexList().size();
//			int load = loadMap.getOrDefault(p.getVertexList().size(),0);
//			loadMap.put(p.getVertexList().size(),load+1);
//		}
//
//		String state = "Allocated paths " + mPaths.size() + ". Flow dropps " + flowFailuresSample + ". Flow count " + flowCounter + "\n";
//		state+= "num edges " + numEdges + "\n";
//		for(int pathLen : loadMap.keySet()){
//			state +=  "paths of len " + pathLen + " have count " + loadMap.get(pathLen) + "\n";
//		}

		String state = "";
		int sum = 0;
		int transmitting = 0;
		for(int source: mTransmittingSources.keySet()){
			int t = mTransmittingSources.get(source);

			if(t!=0) transmitting++;
			sum+=t;
		}
		double avg = (double) sum/ (double) transmitting;
//		OpticElectronicHybrid ToR = (OpticElectronicHybrid) mIdToNetworkDevice.get(67).getEncapsulatingDevice();
		state += "Sum transmissions " + sum + ", Avg transmissions per node " + avg + ", Transmitting " + transmitting + "\n";
		state += "Allocated: " + mAllocateddPathsNum + ", Deallocated: " + mDeAllocatedPathsNum + "\n";
		SimulationLogger.printOldestPaths();
		//state += "flow path exists count: " + flowPathExistsCounter + "\n";
		//state += "no flow path count: " + noPathCounter + "\n";
		mDeAllocatedPathsNum = 0;
		mAllocateddPathsNum = 0;
		flowPathExistsCounter = 0;
		noPathCounter = 0;
		
		return state;
	}

	protected void onPathAllocation(int sourceToR, int destToR){
		int transmittingCounter = mTransmittingSources.getOrDefault(sourceToR,0);
		int receivingCounter = mRecievingDestinations.getOrDefault(destToR,0);
		transmittingCounter++;
		receivingCounter++;
		mTransmittingSources.put(sourceToR,transmittingCounter);
		mRecievingDestinations.put(destToR,receivingCounter);
	}

	protected void onPathDeAllocation(int source, int dest){
		int transmittingCounter = mTransmittingSources.get(source);
		int receivingCounter = mRecievingDestinations.get(dest);
		transmittingCounter--;
		receivingCounter--;
		mTransmittingSources.put(source,transmittingCounter);
		mRecievingDestinations.put(dest,receivingCounter);
	}

	public boolean hasRoute(Pair p) {
		// TODO Auto-generated method stub
		return mPaths.containsKey(p);
	}

}
