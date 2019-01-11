package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.JumboFlow;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.OpticServer;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.semi.SemiRemoteRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.omg.CORBA.INV_POLICY;

import java.util.*;

public class DistributedOpticServer extends OpticServer {
    private DistributedOpticServerToR ToRDevice;
    private HashMap<Integer, ReservationPacket> mFlowReservation;
    private HashMap<Integer, Integer> mPendingRequests;

    enum State{
        NO_CIRCUIT,
        IN_PROCESS,
        HAS_CIRCUIT
    }
    HashMap<Integer,State> mFlowState;
    static Random rand =  Simulator.selectIndependentRandom("semit_remote_paths_randomizer");
    final int NUM_PATH_TO_RANDOMIZE;
//    final int NUM_COLORS_TO_RANDOMIZE;
    /**
     * Constructor of a network device.
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

//        NUM_COLORS_TO_RANDOMIZE = configuration.getIntegerPropertyOrFail("num_colors_to_randomize");

    }

    @Override
    public void addConnection(OutputPort o){
        super.addConnection(o);
        ToRDevice = (DistributedOpticServerToR) o.getTargetDevice();
    }

    @Override
    protected void routeThroughCircuit(IpPacket packet, long flowId){

        switch(mFlowState.getOrDefault(packet.getDestinationId(),State.NO_CIRCUIT)){

            case NO_CIRCUIT:
                try{
                	assert(mPendingRequests.getOrDefault(packet.getDestinationId(),0)==0);
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
//                System.out.println("routing on circuit " + packet);
                TcpPacket tcpPacket = (TcpPacket) packet;
                ReservationPacket rp = mFlowReservation.get(packet.getDestinationId());
                tcpPacket.color(rp.getColor());
                tcpPacket.setPrevHop(this.identifier);
                JumboFlow jumbo = getJumboFlow(packet.getSourceId(),packet.getDestinationId());
                this.conversionUnit.enqueue(this.identifier,packet.getDestinationId(),packet);
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

    }

    protected void initRoute(IpPacket packet, long jumboFlowiId) {
        //SemiRemoteRoutingSwitch srrs = (SemiRemoteRoutingSwitch)this.optic;
        int destToRId = getTransportLayer().getNetworkDevice().getConfiguration().getGraphDetails().getTorIdOfServer(packet.getDestinationId());
        int sourceToRId = getTransportLayer().getNetworkDevice().getConfiguration().getGraphDetails().getTorIdOfServer(getIdentifier());
        List<List<Integer>> paths = new LinkedList<>();
        if(destToRId==sourceToRId){
            List<Integer> path = new LinkedList<>();
            path.add(sourceToRId);
            paths.add(path);
        }else{
            paths = getAvailablePaths(destToRId);
//            if(packet.getFlowId()==246){
//                System.out.println(destToRId);
//                System.out.println(paths.toString());
//            }
        }

        int p = Math.abs(rand.nextInt(paths.size())) % paths.size();
        int pendingRequests = 0;
        for(int i = 0; i < NUM_PATH_TO_RANDOMIZE; i++){
            p = (p+i) % paths.size();
            List<Integer> path = paths.get(p);

            DistributedController controller = (DistributedController) getRemoteRouter();
            int numColorsAvailable = controller.getWaveLengthNum();
            //this is not the right way, get it from the ToR optic device
            int c = Math.abs(rand.nextInt(numColorsAvailable)) % numColorsAvailable;
            boolean hasAvailableColor = false;
            for(int j = 0; j<numColorsAvailable; j++){
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
                pendingRequests++;
                mPendingRequests.put(packet.getDestinationId(),pendingRequests);

                sendReservationPackets(new LinkedList<Integer>(path),c,(TcpPacket)packet);
            }else{
                
            }

        }
        if(pendingRequests==0) throw new NoPathException();
    }

    protected List<List<Integer>>  getAvailablePaths(int destToRId) {
        SemiRemoteRoutingSwitch srrs = (SemiRemoteRoutingSwitch)ToRDevice.getEncapsulatedDevice("circuit_switch");
        return srrs.getPathsTo(destToRId);
    }

    protected void sendReservationPackets(List<Integer> path, int color, TcpPacket packet) {
        ReservationPacket rp = new  ReservationPacket(packet,path.get(0),path.get(0),path,color,true);
        rp.setPrevHop(this.identifier);
        routeThroughtPacketSwitch(rp);
    }

    @Override
    public void receive(Packet genericPacket) {
//        if(genericPacket.getFlowId()==246){
//            System.out.println(this.identifier);
//            System.out.println(genericPacket.toString());
//        }
        try{
            ReservationPacket ep = (ReservationPacket) genericPacket;
            JumboFlow jumbo = getJumboFlow(ep.getSourceId(), ep.getOriginalServerDest());
            //System.out.println(genericPacket.toString());
            int pendingRequests = mPendingRequests.get(ep.getOriginalServerDest());
            pendingRequests--;
            mPendingRequests.put(ep.getOriginalServerDest(),pendingRequests);
            if(ep.isSuccess()){
            	// if the relevant flows have finsihed, or there already is a circuit, release resources.
            	if(jumbo.getNumFlows() == 0 ||
            			mFlowState.get(ep.getOriginalServerDest()) == State.HAS_CIRCUIT) {
            		ep.setDeallocation();
            		ep.reverse();
                    routeThroughtPacketSwitch(ep);
                    return;
            	}
            	changeState(ep.getOriginalServerDest(),State.HAS_CIRCUIT);
                mFlowReservation.put(ep.getOriginalServerDest(),ep);
//                System.out.println("success on reserving " + ep.toString() + " at time " + Simulator.getCurrentTime());
            }else{
                assert(ep.isFailure());
//                System.out.println("failure for " + ep.toString());

                if(pendingRequests==0 && mFlowState.get(ep.getOriginalServerDest())!=State.HAS_CIRCUIT){
                	if(mFlowState.get(ep.getOriginalServerDest())!=State.IN_PROCESS) {
                		System.out.println(mFlowState.get(ep.getOriginalServerDest()));
                		System.out.println(ep.toString());
                		throw new RuntimeException();
                	}
                    changeState(ep.getOriginalServerDest(),State.NO_CIRCUIT);
                }

            }
            return;
        }catch (ClassCastException e){

        }
        super.receive(genericPacket);
    }

    protected void conversionUnitRecover(int serverSource, int serverDest, long jumboFlowId, long flowId) {
        conversionUnit.onFlowFinish(serverSource,serverDest,flowId);
    }

    @Override
    protected void recoverPath(int source, int dest,int serverSource,int serverDest, long jumboFlowId) {
//        System.out.println("trying to recover path from " + serverSource + " to " + serverDest);
        DistributedController controller = (DistributedController) getRemoteRouter();
        ReservationPacket rp = mFlowReservation.get(serverDest);
        if(rp==null){
            assert(mFlowState.get(serverDest) != State.HAS_CIRCUIT);
            return;
        }
        if(mFlowState.get(serverDest) != State.HAS_CIRCUIT){
            return;
        }
        controller.deallocateServerColor(this.identifier,rp.getColor(),false);
        rp.setDeallocation();
        rp.reverse();
        routeThroughtPacketSwitch(rp);
        changeState(rp.getOriginalServerDest(),State.NO_CIRCUIT);
    }

}
