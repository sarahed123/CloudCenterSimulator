package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.OpticElectronicHybrid;

public class MockOpticalHybrid extends OpticElectronicHybrid {
    public MockOpticalHybrid(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }

    @Override
    public void receive(Packet genericPacket) {
    	Encapsulatable packet = (Encapsulatable) genericPacket;


        int destinationToR = configuration.getGraphDetails().getTorIdOfServer(packet.getDestinationId());
        
        if (destinationToR == this.identifier) {
            targetIdToOutputPort.get(packet.getDestinationId()).enqueue(packet.deEncapsualte());
            return;
        }
        IpPacket p = packet.encapsulate(destinationToR);
        this.optic.initCircuit(this.identifier,destinationToR,packet.getFlowId());
        this.optic.receive(p);
    }
}
