package ch.ethz.systems.netbench.xpt.megaswitch.server_optic;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import ch.ethz.systems.netbench.xpt.megaswitch.JumboFlow;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.ConversionUnit;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.JumboOpticElectronicHybrid;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * optic serve class
 */
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

    /**
     * called when flowId has finished
     * will recover the path if the corresponding jumbo flow finished
     * @param source
     * @param dest
     * @param serverSource
     * @param serverDest
     * @param flowId
     */
    public void onFlowFinished(int source, int dest,int serverSource,int serverDest, long flowId) {
        JumboFlow jumboFlow = getJumboFlow(serverSource,serverDest);
        jumboFlow.onFlowFinished(flowId);
        if(jumboFlow.getNumFlows()==0){
            conversionUnitRecover(serverSource,serverDest,jumboFlow.getId(),flowId);

            recoverPath(source,dest,serverSource,serverDest,jumboFlow.getId());
            mJumboFlowMap.remove(new ImmutablePair<>(serverSource, serverDest));
        }

    }

    protected void conversionUnitRecover(int serverSource, int serverDest, long jumboFlowId, long flowId) {
        conversionUnit.onFlowFinish(serverSource,serverDest,flowId);
    }

    /**
     * receive method here, should probably solve code duplication
     * @param genericPacket
     */
    @Override
    public void receive(Packet genericPacket) {
        TcpPacket tcpPacket = (TcpPacket) genericPacket;
        if (tcpPacket.getDestinationId() == this.identifier) {
            if(tcpPacket.isACK() && tcpPacket.isFIN()){
                int destToR = configuration.getGraphDetails().getTorIdOfServer(tcpPacket.getSourceId());
//                onFlowFinished(this.ownToRId,destToR,tcpPacket.getDestinationId(),tcpPacket.getSourceId(),tcpPacket.getFlowId());
            }
//            System.out.println("packet received at " + this.identifier + " : " + tcpPacket.toString());
            passToIntermediary(genericPacket);
            return;
        }
        super.receive(genericPacket);

    }

    protected TcpPacket encapsulatePacket(Encapsulatable packet) {
        return (TcpPacket) packet;
    }

    protected JumboFlow getJumboFlow(int sourceToR, int destToR, int serverSource, int serverDest) {
        return getJumboFlow(serverSource,serverDest);
    }

    /**
     * inits the route through the centralized controller
     * @param packet
     * @param jumboFlowiId
     */
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

    /**
     * recover path using the remote router
     * @param source
     * @param dest
     * @param serverSource
     * @param serverDest
     * @param jumboFlowId
     */
    @Override
    protected void recoverPath(int source, int dest,int serverSource,int serverDest, long jumboFlowId) {
        assert(this.identifier==serverSource);
        try {

            getRemoteRouter().recoverPath(source,dest,serverSource,serverDest,jumboFlowId);
        }catch(NoPathException e) {

        }
    }
}
