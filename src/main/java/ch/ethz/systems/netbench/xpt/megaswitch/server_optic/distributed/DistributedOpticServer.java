package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.JumboFlow;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.OpticServer;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.semi.SemiRemoteRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Path;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

/**
 * the distributed optic server will initiate establishment of circuit request
 * when needed.
 */
public class DistributedOpticServer extends OpticServer {
	private DistributedOpticServerToR ToRDevice; // the ToR fo this device
	protected HashMap<Integer, ReservationPacket> mFlowReservation; // a map which contains information about reservation packets per destination
	private HashMap<Integer, Integer> mPendingRequests; // the number of pending request to a certain destination
	private long mConfigurationTime; // the internal switches reconfiguration time, typacally assuming this can be done in parallel
	private long mCircuitTeardowTimeout; // the amount of time before a circuit should be teared down due to inuse
	enum State{
		NO_CIRCUIT, // the destination does not have a circuit
		IN_PROCESS, // there are pending requests to establish a circuit to a destination
		HAS_CIRCUIT // the destination has a circuit
	}
	HashMap<Integer,State> mFlowState; // circuit states per destination
	static Random rand =  Simulator.selectIndependentRandom("semit_remote_paths_randomizer");
	final int NUM_PATH_TO_RANDOMIZE; // the number of paths to randomize
	private HashMap<Integer, TeardownEvent> mTeardownEventsMap; // a map from destinations to tear down events

	/**
	 *
	 *
	 * @param identifier     Network device identifier
	 * @param transportLayer Transport layer instance (null, if only router and not a server)
	 * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
	 * @param configuration
	 */
	public DistributedOpticServer(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
		super(identifier, transportLayer, intermediary, configuration);
		mFlowState = new HashMap<>();
		mFlowReservation = new HashMap<>();
		mPendingRequests = new HashMap<>();
		NUM_PATH_TO_RANDOMIZE = configuration.getIntegerPropertyOrFail("num_paths_to_randomize");
		mConfigurationTime = configuration.getLongPropertyOrFail("static_configuration_time_ns");
		mCircuitTeardowTimeout = configuration.getLongPropertyWithDefault("circuit_teardown_timeout_ns",5000);
		mTeardownEventsMap = new HashMap<>();
		//        NUM_COLORS_TO_RANDOMIZE = configuration.getIntegerPropertyOrFail("num_colors_to_randomize");

	}

	@Override
	public void addConnection(OutputPort o){
		super.addConnection(o);
		ToRDevice = (DistributedOpticServerToR) o.getTargetDevice();
	}

	@Override
	protected void routeThroughCircuit(IpPacket packet, JumboFlow jFlow){

		switch(mFlowState.getOrDefault(packet.getDestinationId(),State.NO_CIRCUIT)){

		case NO_CIRCUIT:
			try{
				assert(mPendingRequests.getOrDefault(packet.getDestinationId(),0)==0); // asert there are no pending requests
				initRoute(packet, packet.getFlowId());
			}catch (NoPathException e){
				routeThroughtPacketSwitch((TcpPacket)packet);
				break;
			}

		case IN_PROCESS:
			routeThroughtPacketSwitch((TcpPacket)packet);
			changeState(packet.getDestinationId(),State.IN_PROCESS);

			break;
		case HAS_CIRCUIT:
			TcpPacket tcpPacket = (TcpPacket) packet;
			ReservationPacket rp = mFlowReservation.get(packet.getDestinationId()); // get the underline reservation packet
			tcpPacket.color(rp.getColor()); // paint all packets with the reserved color
			tcpPacket.setPrevHop(this.identifier);
			JumboFlow jumbo = getJumboFlow(packet.getSourceId(),packet.getDestinationId());
			assert(((DistributedController) getRemoteRouter()).serverHasColor(this.identifier, tcpPacket.getColor(), false));
			tcpPacket.markOnCircuit(true);
			this.conversionUnit.enqueue(this.identifier,packet.getDestinationId(),packet);
			this.mTeardownEventsMap.get(rp.getOriginalServerDest()).reset(mCircuitTeardowTimeout);
			jumbo.onCircuitEntrance(packet.getFlowId());
			onCircuitEntrance(packet.getFlowId());
			SimulationLogger.increaseStatisticCounter("PACKET_ROUTED_THROUGH_CIRCUIT");
			break;
		}
	}

	private void changeState(int destinationId, State state) {
		State oldState = mFlowState.put(destinationId,state);
		if(state!=oldState) {
			SimulationLogger.distProtocolStateChange(new ImmutablePair<>(this.identifier, destinationId), state.toString());
		}

	}

	protected void onCircuitEntrance(long flowId) {
		// used for testing
	}

	protected void initRoute(IpPacket packet, long jumboFlowiId) {
		SimulationLogger.registerFlowCircuitRequest(packet.getFlowId());
		int destToRId = getTransportLayer().getNetworkDevice().getConfiguration().getGraphDetails().getTorIdOfServer(packet.getDestinationId());
		int sourceToRId = getTransportLayer().getNetworkDevice().getConfiguration().getGraphDetails().getTorIdOfServer(getIdentifier());
		List<List<Integer>> paths = new LinkedList<>();
		if(destToRId==sourceToRId){ // trivail path
			List<Integer> path = new LinkedList<>();
			path.add(sourceToRId);
			paths.add(path);
		}else{
			paths = getAvailablePaths(destToRId); // these are the available pre computed paths

		}

		int p = Math.abs(rand.nextInt(paths.size())) % paths.size(); // randomize a starting index for the paths
		int pendingRequests = 0;
		for(int i = 0; i < NUM_PATH_TO_RANDOMIZE; i++){
			p = (p+i) % paths.size();
			List<Integer> path = paths.get(p);

			DistributedController controller = (DistributedController) getRemoteRouter();
			int numColorsAvailable = controller.getWaveLengthNum(); // number of wavelengths allowed
			//this is not the right way, get it from the ToR optic device
			int c = Math.abs(rand.nextInt(numColorsAvailable)) % numColorsAvailable;
			boolean hasAvailableColor = false;
			for(int j = 0; j<numColorsAvailable; j++){ // iterate over all colors starting at a certain index to see if one is available
				c = (c+j) % numColorsAvailable;
				try{
					controller.reserveServerColor(this.identifier,c,false);	
					hasAvailableColor = true;
					break;
				}catch (NoPathException e){

				}
			}
			if(hasAvailableColor){
				//                int pendingRequests = mPendingRequests.getOrDefault(packet.getDestinationId(),0);
				pendingRequests++; // count the number of pending requests
				mPendingRequests.put(packet.getDestinationId(),pendingRequests);

				sendReservationPackets(new LinkedList<Integer>(path),c,(TcpPacket)packet);
			}else{

			}

		}

		if(pendingRequests==0) {
			SimulationLogger.increaseStatisticCounter("DISTRIBUTED_SOURCE_ENDPOINT_NO_PATH");
			throw new NoPathException();
		}

	}

	/**
	 * get available paths from ToR
	 * @param destToRId
	 * @return
	 */
	protected List<List<Integer>>  getAvailablePaths(int destToRId) {
		SemiRemoteRoutingSwitch srrs = (SemiRemoteRoutingSwitch)ToRDevice.getEncapsulatedDevice("circuit_switch");
		return srrs.getPathsTo(destToRId);
	}

	/**
	 * send out reservation packet
	 * @param path
	 * @param color
	 * @param packet
	 */
	protected void sendReservationPackets(List<Integer> path, int color, TcpPacket packet) {
		ReservationPacket rp = new  ReservationPacket(packet,path.get(0),path.get(0),path,color,true);
		rp.setPrevHop(this.identifier);
		routeThroughtPacketSwitch(rp);
	}

	@Override
	public void receive(Packet genericPacket) {

		try{
			ReservationPacket ep = (ReservationPacket) genericPacket;
			if(ep.getOriginalServerDest()==this.identifier) { //we are receiver

				if(ep.idDeAllocation()) {
					assert(ep.isSuccess()); // this is a tear down of an existing circuit
					((DistributedController) getRemoteRouter()).deallocateServerColor(this.identifier,ep.getColor(), true);
					SimulationLogger.regiserPathActive(new Path(ep.getPath(),ep.getColor(),ep.getId()),false);
					ep.onFinishDeallocation();
	                ((DistributedController) getRemoteRouter()).onDeallocation();
					return;
				}
				try { // this is  an incoming request
	                ((DistributedController) getRemoteRouter()).reserveServerColor(this.identifier,ep.getColor(),true); // reserve end color
					ep.markSuccess();
					Path p = new Path(ep.getPath(),ep.getColor());
					ep.setId(p.getId());
					SimulationLogger.regiserPathActive(p,true);
					((DistributedController) getRemoteRouter()).onAallocation();
					Simulator.registerEvent(new Event(mConfigurationTime) {
						// send the packet after every one had time to reconfigure
						@Override
						public void trigger() {
							ep.markDelayed(mConfigurationTime);
							ep.reverse();
							routeThroughtPacketSwitch(ep);
						}
					});
					return;
					
				}catch(NoPathException e) {
		            SimulationLogger.increaseStatisticCounter("DISTRIBUTED_DEST_ENDPOINT_NO_PATH");
					ep.markFailure();
				}
				ep.reverse();
				routeThroughtPacketSwitch(ep);
				return;
			}
			
			JumboFlow jumbo = getJumboFlow(ep.getSourceId(), ep.getOriginalServerDest());
			//System.out.println(genericPacket.toString());
			int pendingRequests = mPendingRequests.get(ep.getOriginalServerDest());
			pendingRequests--;
			mPendingRequests.put(ep.getOriginalServerDest(),pendingRequests);
			if(ep.isSuccess()){
				// if the relevant flows have finsihed, or there already is a circuit, release resources.
				if(jumbo.getNumFlows() == 0 ||
						mFlowState.get(ep.getOriginalServerDest()) == State.HAS_CIRCUIT) {

					if(mFlowState.get(ep.getOriginalServerDest()) == State.IN_PROCESS){
						if(pendingRequests==0) {
							changeState(ep.getOriginalServerDest(), State.NO_CIRCUIT);
						}
					}
					SimulationLogger.increaseStatisticCounter("DISTRIBUTED_PATH_DOUBLE_SUCCESS_COUNT");
					ep.setDeallocation();
					ep.reverse();
					((DistributedController) getRemoteRouter()).deallocateServerColor(this.identifier,ep.getColor(), false);
					routeThroughtPacketSwitch(ep);
					return;
				}
				SimulationLogger.increaseStatisticCounter("DISTRIBUTED_PATH_SUCCESS_COUNT");
				//if weve made it this far then we are succeful
				assignCircuit(ep);
				
			}else{
				assert(ep.isFailure());

				((DistributedController) getRemoteRouter()).onPathFailure();
				//                System.out.println("failure for " + ep.toString());
				((DistributedController) getRemoteRouter()).deallocateServerColor(this.identifier,ep.getColor(), false); // deallocate color
				if(pendingRequests==0 && mFlowState.get(ep.getOriginalServerDest())!=State.HAS_CIRCUIT){
					SimulationLogger.increaseStatisticCounter("DISTRIBUTED_PATH_FAILURE_COUNT");
					if(mFlowState.get(ep.getOriginalServerDest())!=State.IN_PROCESS) {
						System.out.println(mFlowState.get(ep.getOriginalServerDest()));
						System.out.println(ep.toString());
						throw new RuntimeException();
					}
					changeState(ep.getOriginalServerDest(),State.NO_CIRCUIT); // if no more requests are pending, change state to NO_CIRCUIT
				}

			}
			return;
		}catch (ClassCastException e){

		}
		TcpPacket tcpPacket = (TcpPacket) genericPacket;
		if(tcpPacket.getColor()!=-1) {
			assert(((DistributedController) getRemoteRouter()).serverHasColor(this.identifier, tcpPacket.getColor(), true));
		}
		super.receive(genericPacket);
	}

	/**
	 * this is called when a circuit is fully assigned
	 * @param ep
	 */
	private void assignCircuit(ReservationPacket ep) {
		changeState(ep.getOriginalServerDest(),State.HAS_CIRCUIT); // change state
		mFlowReservation.put(ep.getOriginalServerDest(),ep);
		TeardownEvent tearDownEvent = new TeardownEvent(mCircuitTeardowTimeout,ep,this); // register auto tear down event
		mTeardownEventsMap.put(ep.getOriginalServerDest(),tearDownEvent);
		Simulator.registerEvent(tearDownEvent);
		
	}

	/**
	 * called to tear down circuit started with ep
	 * @param ep
	 */
	protected void teardownCircuit(ReservationPacket ep) {
//		JumboFlow jumbo = getJumboFlow(this.identifier,ep.getOriginalServerDest());
//		jumbo.resetFlow(ep.getFlowId()); // reset flow size
		DistributedController controller = (DistributedController) getRemoteRouter();
		controller.deallocateServerColor(this.identifier,ep.getColor(),false);
		ep.reverse();
		ep.setDeallocation();
		routeThroughtPacketSwitch(ep);
		changeState(ep.getOriginalServerDest(),State.NO_CIRCUIT);
		
	}


	@Override
	protected void recoverPath(JumboFlow jFlow) {
		//        System.out.println("trying to recover path from " + serverSource + " to " + serverDest);
		DistributedController controller = (DistributedController) getRemoteRouter();
		ReservationPacket rp = mFlowReservation.get(jFlow.getDest());
		if(rp==null){
			assert(mFlowState.get(jFlow.getDest()) != State.HAS_CIRCUIT);
			return;
		}
		if(mFlowState.get(jFlow.getDest()) != State.HAS_CIRCUIT){
			return;
		}
		teardownCircuit(rp);
//		controller.deallocateServerColor(this.identifier,rp.getColor(),false);
//		rp.setDeallocation();
//		rp.reverse();
//		routeThroughtPacketSwitch(rp);
		this.mTeardownEventsMap.get(rp.getOriginalServerDest()).finish();
//		changeState(rp.getOriginalServerDest(),State.NO_CIRCUIT);
	}


}
