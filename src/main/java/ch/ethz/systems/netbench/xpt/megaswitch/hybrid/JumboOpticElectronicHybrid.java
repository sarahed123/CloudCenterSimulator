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

public class JumboOpticElectronicHybrid extends NetworkDevice implements MegaSwitch {

    protected long circuitThreshold;
    protected NetworkDevice electronic;
    protected NetworkDevice optic;
    HashMap<Pair<Integer,Integer>,JumboFlow> mJumboFlowMap;
    private long mNumAllocatedFlows;
    private long mNumDeAllocatedFlows;

    public JumboOpticElectronicHybrid(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary,configuration);
        circuitThreshold = configuration.getLongPropertyOrFail("hybrid_circuit_threshold_byte");
        mJumboFlowMap = new HashMap<>();
        mNumAllocatedFlows = 0;
        mNumDeAllocatedFlows = 0;

    }
    @Override
    public void receive(Packet genericPacket) {

        Encapsulatable packet = (Encapsulatable) genericPacket;

        int destinationToR = configuration.getGraphDetails().getTorIdOfServer(packet.getDestinationId());
        TcpPacket encapsulated = (TcpPacket) packet.encapsulate(this.identifier,destinationToR);
        JumboFlow jumboFlow = getJumboFlow(encapsulated.getSourceId(),encapsulated.getDestinationId());
        jumboFlow.onPacketDispatch(encapsulated);

        if(jumboFlow.getSizeByte()>=circuitThreshold && !jumboFlow.isTrivial()) {
            try {
                routeThroughCircuit(encapsulated,jumboFlow.getId());
                return;
            }catch(NoPathException e) {
                //SimulationLogger.increaseStatisticCounter("num_path_failures");
            }
        }
        routeThroughtPacketSwitch(encapsulated);
    }

    protected JumboFlow getJumboFlow(int source, int dest){
        JumboFlow jumboFlow = mJumboFlowMap.get(new ImmutablePair<>(source,dest));
        if(jumboFlow==null) {
            jumboFlow = new JumboFlow(source,dest);
            mJumboFlowMap.put(new ImmutablePair<>(source,dest),jumboFlow);
        }
        return jumboFlow;
    }

    protected void routeThroughtPacketSwitch(TcpPacket packet) {
        this.electronic.receiveFromEncapsulating(packet);

    }

    protected void routeThroughCircuit(IpPacket packet, long jumboFlowiId) {
        JumboFlow jumbo = getJumboFlow(packet.getSourceId(),packet.getDestinationId());

        try {
            getRemoteRouter().initRoute(this.identifier,packet.getDestinationId(),jumboFlowiId);
        }catch(FlowPathExists e) {

        }
        this.optic.receiveFromEncapsulating(packet);

        jumbo.onCircuitEntrance();
        SimulationLogger.increaseStatisticCounter("PACKET_ROUTED_THROUGH_CIRCUIT");

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
                onFlowFinished(this.identifier,ipPacket.getSourceId(),deEncapse.getFlowId());
            }
            targetIdToOutputPort.get(deEncapse.getDestinationId()).enqueue(deEncapse);
        }
    }

    protected void onFlowFinished(int source, int dest, long flowId) {
        JumboFlow jumboFlow = getJumboFlow(source,dest);
        jumboFlow.onFlowFinished(flowId);
        if(jumboFlow.getNumFlows()==0){
            recoverPath(source,dest,jumboFlow.getId());
            mJumboFlowMap.remove(new ImmutablePair<>(source, dest));
        }

    }

    protected void recoverPath(int source, int dest, long jumboFlowId) {
        try {

            getRemoteRouter().recoverPath(source,dest,jumboFlowId);
        }catch(NoPathException e) {

        }
    }

    @Override
    public NetworkDevice getAsNetworkDevice() {
        return this;
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
//        JumboFlow j = getJumboFlow(this.identifier,53);
//        String state = "Node " + this.identifier + " Allocated: " + this.mNumAllocatedFlows + ", deAllocated: " + this.mNumDeAllocatedFlows + ", total size: " +
//                j.getSizeByte() + ", on circuit " + j.isOnCircuit() + "\n";
//        mNumDeAllocatedFlows = 0;
//        mNumAllocatedFlows = 0;
        return "";
    }
}