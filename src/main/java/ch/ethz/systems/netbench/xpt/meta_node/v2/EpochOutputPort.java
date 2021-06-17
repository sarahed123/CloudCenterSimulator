package ch.ethz.systems.netbench.xpt.meta_node.v2;

import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;

public class EpochOutputPort extends EcnTailDropOutputPort {
    protected EpochOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
    }

    @Override
    protected void dispatch(Packet packet){
        super.dispatch(packet);
        MNEpochPacket mnEpochPacket = (MNEpochPacket) packet;
        MetaNodeServer server = (MetaNodeServer) getOwnDevice();
        server.pullPacket(mnEpochPacket);
    }


}
