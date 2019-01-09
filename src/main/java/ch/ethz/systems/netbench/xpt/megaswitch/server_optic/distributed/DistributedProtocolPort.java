package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class DistributedProtocolPort extends EcnTailDropOutputPort {
    public DistributedProtocolPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes, new LinkedList());
    }

    @Override
    protected void addPacketToQueue(Packet packet){
        try{
            ReservationPacket rp = (ReservationPacket) packet;
            ((LinkedList)queue).addFirst(packet);
            return;
        }catch (ClassCastException e){

        }
        super.addPacketToQueue(packet);
    }
}
