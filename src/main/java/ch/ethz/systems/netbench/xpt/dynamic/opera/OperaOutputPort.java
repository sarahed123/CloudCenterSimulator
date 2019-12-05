package ch.ethz.systems.netbench.xpt.dynamic.opera;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.ReconfigurationDeadlineException;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorNetController;

import java.util.Queue;

public class OperaOutputPort extends EcnTailDropOutputPort {
    private long mBitsEnqueued;
    private final long mMaxBitsPerInterval;

    /**
     * Constructor.
     *
     * @param ownNetworkDevice    Source network device to which this output port is attached
     * @param targetNetworkDevice Target network device that is on the other side of the link
     * @param link                Link that this output ports solely governs
     * @param queue               Queue that governs how packet are stored queued in the buffer
     */
    protected OperaOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, Queue<Packet> queue) {
        super(ownNetworkDevice, targetNetworkDevice, link, Long.MAX_VALUE/8, Long.MAX_VALUE/8, queue);
        mBitsEnqueued = 0;
        mMaxBitsPerInterval = OperaController.getInstance().getRemainingTimeToReconfigure() * link.getBandwidthBitPerNs();
    }

    public void enqueue(Packet packet) {
        if(mBitsEnqueued + packet.getSizeBit() > mMaxBitsPerInterval){
            onConfigurationTimeExceeded();
        }
        mBitsEnqueued += packet.getSizeBit();
        super.enqueue(packet);
    }

    protected void onConfigurationTimeExceeded() {
        throw new ReconfigurationDeadlineException();
    }


    @Override
    protected void log(Packet packet) {

    }
}
