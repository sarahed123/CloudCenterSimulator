package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class EcnTailDropOutputPort extends OutputPort {

    protected final long ecnThresholdKBits;
    protected final long maxQueueSizeBits;
    protected final boolean mPrioritizeAcksOnCircuit;

    protected EcnTailDropOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes) {
        this(ownNetworkDevice, targetNetworkDevice, link,maxQueueSizeBytes, ecnThresholdKBytes, new LinkedList<>());
    }

    protected EcnTailDropOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes, Queue queue) {
        super(ownNetworkDevice, targetNetworkDevice, link, queue);
        this.maxQueueSizeBits = maxQueueSizeBytes * 8L;
        this.ecnThresholdKBits = ecnThresholdKBytes * 8L;
        NBProperties configs = ownNetworkDevice.getConfiguration();
        mPrioritizeAcksOnCircuit = configs.getBooleanPropertyWithDefault("prioritize_acks_on_circuit",false);
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

    @Override
    protected void addPacketToQueue(Packet packet){
        try{
            TcpPacket tcpPacket = (TcpPacket) packet;
            if(mPrioritizeAcksOnCircuit && tcpPacket.isOnCircuit() && tcpPacket.isACK()){
                ((LinkedList)queue).addFirst(tcpPacket);
                return;
            }

        }catch (ClassCastException e){

        }
        super.addPacketToQueue(packet);
    }

    protected void onPacketDropped(IpHeader ipHeader) {
    	String suffix = "";
        
        try {
        	TcpPacket tcpPacket = (TcpPacket) ipHeader;
        	ipHeader = tcpPacket.deEncapsualte();
        	if(tcpPacket.isACK()) {
        		suffix = "ACK_";
        	}
        }catch(ClassCastException e) {
        	
        }
        SimulationLogger.increaseStatisticCounter(suffix + "PACKETS_DROPPED");
        if (ipHeader.getSourceId() == this.getOwnId()) {
            SimulationLogger.increaseStatisticCounter(suffix + "PACKETS_DROPPED_AT_SOURCE");
        }
    }
    
    protected boolean hasBufferSpace(IpHeader packet) {
    	return (getBufferOccupiedBits() + packet.getSizeBit() <= maxQueueSizeBits);
    }
    

}
