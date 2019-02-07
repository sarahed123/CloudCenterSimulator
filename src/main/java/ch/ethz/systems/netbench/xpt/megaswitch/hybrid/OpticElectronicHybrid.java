package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import ch.ethz.systems.netbench.xpt.megaswitch.JumboFlow;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;

public class OpticElectronicHybrid extends NetworkDevice implements MegaSwitch {

    protected long circuitThreshold;
    protected NetworkDevice electronic;
    protected NetworkDevice optic;
    protected HashMap<Pair<Integer,Integer>,JumboFlow> mJumboFlowMap;
    private long mNumAllocatedFlows;
    private long mNumDeAllocatedFlows;
    protected ConversionUnit conversionUnit;
    public OpticElectronicHybrid(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary,configuration);
        circuitThreshold = configuration.getLongPropertyOrFail("hybrid_circuit_threshold_byte");
        mJumboFlowMap = new HashMap<>();
        mNumAllocatedFlows = 0;

        mNumDeAllocatedFlows = 0;

    }
    @Override
    public void receive(Packet genericPacket) {
       // boolean flowFinished = Simulator.isFlowFinished(genericPacket.getFlowId());

        Encapsulatable packet = (Encapsulatable) genericPacket;


        TcpPacket encapsulated = encapsulatePacket(packet);
        JumboFlow jumboFlow = getJumboFlow(getSourceToRWithDefault(packet.getSourceId(),encapsulated.getSourceId()),
                getDestToRWithDefault(packet.getDestinationId(),encapsulated.getDestinationId()),
                packet.getSourceId(),packet.getDestinationId());
        jumboFlow.onPacketDispatch(encapsulated);

        if(isCircuitable(jumboFlow,encapsulated)) {
        	try {
        		routeThroughCircuit(encapsulated,jumboFlow);
                jumboFlow.onCircuitEntrance(packet.getFlowId());
                SimulationLogger.increaseStatisticCounter("PACKET_ROUTED_THROUGH_CIRCUIT");
        		return;
        	}catch(NoPathException e) {
            }
        }
        routeThroughtPacketSwitch(encapsulated);
    }

    protected int getDestToRWithDefault(int serverId, int defToRId) {
        return defToRId;
    }

    protected int getSourceToRWithDefault(int serverId, int defToRId) {
        return defToRId;
    }

    protected TcpPacket encapsulatePacket(Encapsulatable packet) {
        int destinationToR = configuration.getGraphDetails().getTorIdOfServer(packet.getDestinationId());
        return (TcpPacket) packet.encapsulate(this.identifier,destinationToR);
    }

    protected JumboFlow getJumboFlow(int sourceToR, int destToR, int serverSource, int serverDest) {
        return getJumboFlow(serverSource,serverDest).setSourceToR(sourceToR).setDestToR(destToR);
    }

    protected JumboFlow getJumboFlow(int source, int dest){
        JumboFlow jumboFlow = mJumboFlowMap.get(new ImmutablePair<>(source,dest));
        if(jumboFlow==null) {
        	jumboFlow = new JumboFlow(source,dest);
        	mJumboFlowMap.put(new ImmutablePair<>(source,dest),jumboFlow);
        }
        return jumboFlow;
    }

    protected void initConversionUnit(){
        conversionUnit = new ConversionUnit(configuration,this,optic);
    }

    protected void routeThroughtPacketSwitch(TcpPacket packet) {
		this.electronic.receiveFromEncapsulating(packet);
	}

	protected void routeThroughCircuit(IpPacket packet, JumboFlow jFlow) {

		try {
		    int transmittingSource = getTransmittingSource(jFlow);
		    int receiveingDest = getReceivingDest(jFlow);
	    	getRemoteRouter().initRoute(transmittingSource,receiveingDest,jFlow.getSource(),jFlow.getDest(),jFlow.getId());
		}catch(FlowPathExists e) {

        }

        this.conversionUnit.enqueue(jFlow.getSource(),jFlow.getDest(),packet);


		
	}

    protected int getReceivingDest(JumboFlow jFlow) {
        return jFlow.getDestToR();
    }

    protected int getTransmittingSource(JumboFlow jFlow) {
        return jFlow.getSourceToR();
    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {
        throw new RuntimeException("Hybrid switch is not a server");
    }

    @Override
    public void extend(NetworkDevice networkDevice, NBProperties networkConf){
        String networkType = networkConf.getPropertyOrFail("network_type");
        switch (networkType){
            case "circuit_switch":
                this.optic = networkDevice;
                this.optic.setEncapsulatingDevice(this);
                initConversionUnit();
                break;
            case "packet_switch":
                this.electronic = networkDevice;
                this.electronic.setEncapsulatingDevice(this);
                break;
            default:
                throw new RuntimeException("bad network type " + networkType);
        }
    }

    @Override
    public void receiveFromEncapsulatedDevice(Packet packet) {
        Encapsulatable ipPacket = (Encapsulatable) packet;

        if (ipPacket.getDestinationId() == this.identifier) {
            TcpPacket deEncapse = (TcpPacket) ipPacket.deEncapsualte();
            if(deEncapse.isACK() && deEncapse.isFIN()){
            	//onFlowFinished(this.identifier,ipPacket.getSourceId(),(IpPacket)deEncapse);
//                onFlowFinished(ipPacket.getSourceId(),this.identifier,(IpPacket)deEncapse.encapsulate(deEncapse.getDestinationId(),deEncapse.getSourceId()));
            }
            targetIdToOutputPort.get(deEncapse.getDestinationId()).enqueue(deEncapse);
        }
    }

//    @Override
//    public void onFlowFinished(int sourceToRId, int destToRId, int sourceServer, int destServerId, long flowId) {
//        JumboFlow jumboFlow = getJumboFlow(sourceServer,destServerId);
//        boolean isOnCircuit = jumboFlow.isOnCircuit(flowId);
//        jumboFlow.onFlowFinished(flowId);
//        if(jumboFlow.getNumFlows()==0){
////        	recoverPath(source,dest,packet);
//            mJumboFlowMap.remove(new ImmutablePair<>(sourceServer,destServerId));
//
//        }
//        if(isOnCircuit){
//            conversionUnit.onFlowFinish(sourceServer,destServerId,jumboFlow.getId());
//            recoverPath(sourceToRId,destToRId,destServerId,sourceServer,flowId);
//        }
//
//    }

    protected void conversionUnitRecover(JumboFlow jumbo) {
        conversionUnit.onFlowFinish(jumbo.getSource(),jumbo.getDest(),jumbo.getId());
    }

    /**
     * called when flowId has finished
     * will recover the path if the corresponding jumbo flow finished
     * @param sourceToR
     * @param destToR
     * @param serverSource
     * @param serverDest
     * @param flowId
     */
    public void onFlowFinished(int sourceToR, int destToR,int serverSource,int serverDest, long flowId) {
        JumboFlow jumboFlow = getJumboFlow(sourceToR,destToR,serverSource,serverDest);
        jumboFlow.onFlowFinished(flowId);
        if(jumboFlow.getNumFlows()==0){
            conversionUnitRecover(jumboFlow);
            recoverPath(jumboFlow);
            mJumboFlowMap.remove(new ImmutablePair<>(jumboFlow.getSource(), jumboFlow.getDest()));
        }

    }


    
	protected void recoverPath(JumboFlow jFlow) {
		try {
            int transmittingSource = getTransmittingSource(jFlow);
            int receiveingDest = getReceivingDest(jFlow);
			getRemoteRouter().recoverPath(transmittingSource,receiveingDest,jFlow.getSource(),jFlow.getDest(),jFlow.getId());
		}catch(NoPathException e) {

		}
	}
	
	@Override
    public NetworkDevice getAsNetworkDevice() {
        return this;
    }

    @Override
    public NetworkDevice getEncapsulatedDevice(String type) {
        switch (type){
            case "circuit_switch":
                return optic;
            case "packet_switch":
                return electronic;
            default:
                throw new RuntimeException("bad network type " + type);
        }
    }

    @Override
    public boolean hadlePacketFromEncapsulating(Packet packet) {
        return false;
    }

    @Override
    public OutputPort getTargetOuputPort(int targetId, String technology) {
		if(technology == null) {
			 return this.getTargetOuputPort(targetId);
		}
    	switch (technology){
    	case "optic":
            return optic.getTargetOuputPort(targetId);
        case "electronic":
        	return electronic.getTargetOuputPort(targetId);
        default:
            throw new RuntimeException("bad technology " + technology);
    	}
	}

	@Override
    public InputPort getSourceInputPort(int sourceNetworkDeviceId, String technology){
        if(technology == null) {
            return this.getSourceInputPort(sourceNetworkDeviceId);
        }
        switch (technology){
            case "optic":
                return optic.getSourceInputPort(sourceNetworkDeviceId);
            case "electronic":
                return electronic.getSourceInputPort(sourceNetworkDeviceId);
            default:
                throw new RuntimeException("bad technology " + technology);
        }
    }
	
	protected RemoteRoutingController getRemoteRouter() {
		return RemoteRoutingController.getInstance();
	}

    public String getState() {

        return "";
    }

    protected boolean isCircuitable(JumboFlow jumboFlow, TcpPacket packet) {
        return jumboFlow.getSizeByte()>=circuitThreshold && !jumboFlow.isTrivial() && !(packet.isACK());
    }
}
