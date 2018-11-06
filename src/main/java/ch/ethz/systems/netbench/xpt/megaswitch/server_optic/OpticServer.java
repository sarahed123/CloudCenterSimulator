package ch.ethz.systems.netbench.xpt.megaswitch.server_optic;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.JumboFlow;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.ConversionUnit;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.JumboOpticElectronicHybrid;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class OpticServer extends JumboOpticElectronicHybrid {
    private final int ownToRId;
    /**
     * Constructor of a network device.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Transport layer instance (null, if only router and not a server)
     * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
     * @param configuration
     */
    protected OpticServer(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
        this.ownToRId = configuration.getGraphDetails().getTorIdOfServer(this.identifier);
//        MegaSwitch megaSwitch = (MegaSwitch) this.targetIdToOutputPort.get(ownToRId).getTargetDevice();
//
//        conversionUnit = new ConversionUnit(configuration,this,megaSwitch.getEncapsulatedDevice("circuit_switch"));

    }

    protected void onFlowFinished(int source, int dest,int serverSource,int serverDest, long flowId) {
        JumboFlow jumboFlow = getJumboFlow(serverSource,serverDest);
        jumboFlow.onFlowFinished(flowId);
        if(jumboFlow.getNumFlows()==0){
            conversionUnit.onFlowFinish(serverSource,serverDest,jumboFlow.getId());
            recoverPath(source,dest,serverSource,serverDest,jumboFlow.getId());
            mJumboFlowMap.remove(new ImmutablePair<>(serverSource, serverDest));
        }

    }

    @Override
    public void receive(Packet genericPacket) {
        TcpPacket tcpPacket = (TcpPacket) genericPacket;
        if (tcpPacket.getDestinationId() == this.identifier) {
            if(tcpPacket.isACK() && tcpPacket.isFIN()){
                int destToR = configuration.getGraphDetails().getTorIdOfServer(tcpPacket.getSourceId());
                onFlowFinished(this.ownToRId,destToR,tcpPacket.getDestinationId(),tcpPacket.getSourceId(),tcpPacket.getFlowId());
            }
            passToIntermediary(genericPacket);
            return;
        }
        JumboFlow jumboFlow = getJumboFlow(tcpPacket.getSourceId(),tcpPacket.getDestinationId());
        jumboFlow.onPacketDispatch(tcpPacket);
        assert(!jumboFlow.isTrivial());
        if(jumboFlow.getSizeByte()>=circuitThreshold && !tcpPacket.isACK()) {
            try {
                routeThroughCircuit(tcpPacket,jumboFlow.getId());
                return;
            }catch(NoPathException e) {
                //SimulationLogger.increaseStatisticCounter("num_path_failures");
            }
        }
        routeThroughtPacketSwitch((TcpPacket)genericPacket );

    }
    @Override
    protected void initRoute(IpPacket packet, long jumboFlowiId) {
        int destToRId = configuration.getGraphDetails().getTorIdOfServer(packet.getDestinationId());

        getRemoteRouter().initRoute(this.ownToRId,destToRId,this.identifier,packet.getDestinationId(),jumboFlowiId);
    }

    @Override
    protected void routeThroughtPacketSwitch(TcpPacket packet) {
        targetIdToOutputPort.get(ownToRId).enqueue(packet);
    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {
        this.receive(genericPacket);
    }


    public void createOpticConnection(NetworkDevice optic) {

        conversionUnit = new ConversionUnit(configuration,this,optic);
    }

    @Override
    protected void recoverPath(int source, int dest,int serverSource,int serverDest, long jumboFlowId) {
        assert(this.identifier==serverSource);
        try {

            getRemoteRouter().recoverPath(source,dest,serverSource,serverDest,jumboFlowId);
        }catch(NoPathException e) {

        }
    }
}
