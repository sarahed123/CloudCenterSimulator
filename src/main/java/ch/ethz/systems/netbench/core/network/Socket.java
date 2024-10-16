package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.FlowLogger;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.xpt.megaswitch.MegaSwitch;

public abstract class Socket {

    protected final TransportLayer transportLayer;
    protected long flowId;
    protected final int sourceId;
    protected final int destinationId;
    protected final long flowSizeByte;
    protected long remainderToConfirmFlowSizeByte;
    protected boolean isReceiver;
    protected FlowLogger privateLogger;

    /**
     * Create a socket. By default, it should be the receiver.
     * Use the {@link #start() start} method to make the socket a
     * sender and initiate the communication handshake.
     *
     * @param transportLayer Transport layer
     * @param sourceId       Source network device identifier
     * @param destinationId  Target network device identifier
     * @param flowSizeByte   Size of the flow in bytes
     */
    public Socket(TransportLayer transportLayer, long flowId, int sourceId, int destinationId, long flowSizeByte) {
        // Initialize higher variables
        this.transportLayer = transportLayer;
        this.flowId = flowId;
        this.flowSizeByte = flowSizeByte;
        this.remainderToConfirmFlowSizeByte = flowSizeByte;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.isReceiver = true;
        initLogger();

    }

    protected void initLogger() {
        // Initialize logger
        this.privateLogger = new FlowLogger(flowId, sourceId, destinationId, flowSizeByte);
    }

    /**
     * Confirm the amount of flow given as argument.
     *
     * @param newlyConfirmedFlowByte Amount of flow (> 0) newly confirmed
     */
    protected void confirmFlow(long newlyConfirmedFlowByte) {
        // check the packet not recieve and there is flow byte
        assert (!this.isReceiver && this.remainderToConfirmFlowSizeByte >= newlyConfirmedFlowByte
                && newlyConfirmedFlowByte > 0);
        this.remainderToConfirmFlowSizeByte -= newlyConfirmedFlowByte;
        logSender(newlyConfirmedFlowByte);

        // Remove references to the socket after finish
        if (isAllFlowConfirmed()) {

            onAllFlowConfirmed();

        }

    }

    protected void logSender(long newlyConfirmedFlowByte) {
        this.privateLogger.logFlowAcknowledged(newlyConfirmedFlowByte);
    }

    protected void onAllFlowConfirmed() {
        // this is some block that should really be moved
        // but its the best way to tell if a flow is finished
        boolean exendedTopology = true;
        int destToRId = -1;
        int sourceToRId = -1;
        try {
            sourceToRId = transportLayer.getNetworkDevice().getConfiguration().getGraphDetails()
                    .getTorIdOfServer(sourceId);
            destToRId = transportLayer.getNetworkDevice().getConfiguration().getGraphDetails()
                    .getTorIdOfServer(destinationId);
        } catch (NullPointerException e) {
            exendedTopology = false;
        }
        if (exendedTopology) {
            try {
                MegaSwitch ms = (MegaSwitch) BaseInitializer.getInstance().getIdToNetworkDevice().get(sourceToRId);
                if (ms != null)
                    ms.onFlowFinished(sourceToRId, destToRId, sourceId, destinationId, flowId);
            } catch (ClassCastException e) {

            }
        }

        transportLayer.cleanupSockets(flowId);
        Simulator.registerFlowFinished(flowId);

    }

    /**
     * Check whether all flow has been confirmed via {@link #confirmFlow(long)
     * confirmFlow}.
     *
     * @return True iff all flow has been confirmed
     */
    protected boolean isAllFlowConfirmed() {

        return remainderToConfirmFlowSizeByte == 0;
    }

    /**
     * Get the remaining amount of flow to be confirmed.
     *
     * @return Remainder of flow not yet confirmed in bytes
     */
    protected long getRemainderToConfirmFlowSizeByte() {
        return remainderToConfirmFlowSizeByte;
    }

    /**
     * Initiate the connection from this socket.
     */
    public abstract void start();

    /**
     * Handle the reception of packet, passed on by the transport layer
     * to which this socket belongs- handle when the packet pass(which socket
     * belong)
     *
     * @param genericPacket Packet instance
     */
    public abstract void handle(Packet genericPacket);

    /**
     * Mark this socket as the sender. By default, when a
     * socket is constructed, it is assumed that it is the receiver.
     * Do not override.
     */
    public void markAsSender() {
        this.isReceiver = false;
    }

    /**
     * Check whether the socket is the receiver (or not, the sender).
     * Do not override.
     *
     * @return True iff the socket is a receiver
     */
    protected boolean isReceiver() {
        return isReceiver;
    }

    public void markAsReceiver() {
        this.isReceiver = true;
    }
}
