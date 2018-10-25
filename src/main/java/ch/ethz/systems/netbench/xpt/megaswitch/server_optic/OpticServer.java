package ch.ethz.systems.netbench.xpt.megaswitch.server_optic;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
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
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.OpticElectronicHybrid;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;

public class OpticServer extends JumboOpticElectronicHybrid {
    private final int ownToR;
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
        this.ownToR = configuration.getGraphDetails().getTorIdOfServer(this.identifier);
        MegaSwitch megaSwitch = (MegaSwitch) this.targetIdToOutputPort.get(ownToR).getTargetDevice();

        conversionUnit = new ConversionUnit(configuration,this,megaSwitch.getEncapsulatedDevice("circuit_switch"));

    }

    @Override
    public void receive(Packet genericPacket) {
        TcpPacket tcpPacket = (TcpPacket) genericPacket;
        if (tcpPacket.getDestinationId() == this.identifier) {
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
        targetIdToOutputPort.get(ownToR).enqueue(genericPacket);
    }



    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {
        this.receive(genericPacket);
    }
}
