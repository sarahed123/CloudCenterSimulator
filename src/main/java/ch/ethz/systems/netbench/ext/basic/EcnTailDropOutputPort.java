package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Packet;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class EcnTailDropOutputPort extends OutputPort {

    protected final long ecnThresholdKBits;
    protected final long maxQueueSizeBits;

    protected EcnTailDropOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        super(ownNetworkDevice, targetNetworkDevice, link, new LinkedBlockingQueue<Packet>());
        this.maxQueueSizeBits = maxQueueSizeBytes * 8L;
        this.ecnThresholdKBits = ecnThresholdKBytes * 8L;
    }

    protected EcnTailDropOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes, Queue queue) {
        super(ownNetworkDevice, targetNetworkDevice, link, queue);
        this.maxQueueSizeBits = maxQueueSizeBytes * 8L;
        this.ecnThresholdKBits = ecnThresholdKBytes * 8L;
    }

    /**
     * Enqueue the given packet.
     * Drops it if the queue is full (tail drop).
     *
     * @param packet    Packet instance
     */
    @Override
    public void enqueue(Packet packet) {
        // Convert to IP packet
        IpHeader ipHeader = (IpHeader) packet;

        // Mark congestion flag if size of the queue is too big
        if (getBufferOccupiedBits() >= ecnThresholdKBits) {
            SimulationLogger.increaseStatisticCounter("MARK_CONGESTION_ECN_THRESHOLD");
            ipHeader.markCongestionEncountered();
        }

        // Tail-drop enqueue
        if (hasBufferSpace(ipHeader)) {
            guaranteedEnqueue(packet);
        } else {
            onPacketDropped(ipHeader);

        }

    }

    protected void onPacketDropped(IpHeader ipHeader) {
        SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED");
        if (ipHeader.getSourceId() == this.getOwnId()) {
            SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_AT_SOURCE");
        }
    }
    
    protected boolean hasBufferSpace(IpHeader packet) {
    	return (getBufferOccupiedBits() + packet.getSizeBit() <= maxQueueSizeBits);
    }
    

}
