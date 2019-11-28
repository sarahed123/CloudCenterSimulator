package ch.ethz.systems.netbench.xpt.simple.simpleudp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.FlowLogger;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.bare.BareSocket;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;

public class SimpleUDPSocket extends BareSocket {
    private final int mNetworkDeviceLinkBw;
    /**
     * Create a socket. By default, it should be the receiver.
     * Use the {@link #start() start} method to make the socket a
     * sender and initiate the communication handshake.
     *
     * @param transportLayer Transport layer
     * @param flowId
     * @param sourceId       Source network device identifier
     * @param destinationId  Target network device identifier
     * @param flowSizeByte   Size of the flow in bytes
     */
    public SimpleUDPSocket(TransportLayer transportLayer, long flowId, int sourceId, int destinationId, long flowSizeByte, NBProperties configuration) {
        super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
        mNetworkDeviceLinkBw = transportLayer.getNetworkDevice().getConfiguration().getIntegerPropertyOrFail("link_bandwidth_bit_per_ns");
    }

    @Override
    protected void initLogger() {
        // Initialize logger
        this.privateLogger = new UDPFlowLogger(flowId, sourceId, destinationId, flowSizeByte);
    }


    @Override
    public void start() {
        sendPacket();
    }

    private void sendPacket() {
        if(!isAllFlowConfirmed()) {
            final long next = Math.min(this.MAX_SEGMENT_SIZE, getRemainderToConfirmFlowSizeByte());
            Packet p = this.createPacket(next, 0, 0, false, false);

//            System.out.println("sending " + next + " for flow " + flowId + " at time " + Simulator.getCurrentTime());
            transportLayer.send(p);

            SimulationLogger.increaseStatisticCounter("UDP_PACKET_SENT");
            Simulator.registerEvent(new Event(p.getSizeBit() / mNetworkDeviceLinkBw) {
                @Override
                public void trigger() {
                    confirmFlow(next);
                    continueFlow();
                }
            });
        }

    }

    @Override
    public void handle(Packet genericPacket) {
        TcpPacket packet = (TcpPacket) genericPacket;
        privateLogger.logFlowAcknowledged(packet.getDataSizeByte());
    }

    public void continueFlow() {
        sendPacket();
    }

    @Override
    protected void logSender(long newlyConfirmedFlowByte) {

    }

}
