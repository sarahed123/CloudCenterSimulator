package ch.ethz.systems.netbench.xpt.megaswitch.server_optic;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingOutputPortGenerator;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import ch.ethz.systems.netbench.xpt.megaswitch.JumboFlow;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.OpticElectronicHybrid;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.ReservationPacket;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * this class represents an optic server ToR
 */
public class OpticServerToR extends OpticElectronicHybrid {

    public OpticServerToR(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);

    }

    /**
     * receive a packet, encapsulate it, and send.
     * @param genericPacket
     */
    @Override
    public void receive(Packet genericPacket) {

        Encapsulatable packet = (Encapsulatable) genericPacket;
        int destinationToR = configuration.getGraphDetails().getTorIdOfServer(packet.getDestinationId());

        TcpPacket encapsulated = (TcpPacket) packet.encapsulate(this.identifier,destinationToR);
        routeThroughtPacketSwitch(encapsulated);
    }


    /**
     * will connect all hosts to its optic device and vice versa
     * @param networkDevice
     * @param networkConf
     */
    @Override
    public void extend(NetworkDevice networkDevice, NBProperties networkConf){
        super.extend(networkDevice,networkConf);
        String networkType = networkConf.getPropertyOrFail("network_type");
        if(networkType.equals("circuit_switch")){
            OutputPortGenerator g = new RemoteRoutingOutputPortGenerator(networkConf);
            LinkGenerator gl = new PerfectSimpleLinkGenerator(networkConf);

                for(int server : configuration.getGraphDetails().getServersOfTor(this.identifier)){
                    OpticServer serverDevice = (OpticServer) this.targetIdToOutputPort.get(server).getTargetDevice();
                    this.optic.addConnection(g.generate(this.optic,serverDevice,gl.generate(this.optic,serverDevice)));
                    serverDevice.createOpticConnection(this.optic);
                }



        }

    }



    /**
     * will tell the source server that flowId has finished
     * @param source
     * @param dest
     * @param serverSource
     * @param serverDest
     * @param flowId
     */
    @Override
    public void onFlowFinished(int source, int dest,int serverSource,int serverDest, long flowId) {

        ((MegaSwitch)targetIdToOutputPort.get(serverSource).getTargetDevice()).onFlowFinished(source, dest, serverSource, serverDest, flowId);
    }
}
