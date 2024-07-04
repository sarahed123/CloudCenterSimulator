package ch.ethz.systems.netbench.core.run.traffic;

import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.core.network.TransportLayer;

public class FlowStartEvent extends Event {

    /**
     * 
     */
    private static final long serialVersionUID = -953241448509175658L;
    protected final int targetId;
    protected final long flowSizeByte;
    protected int networkDeviceId;
    protected TransportLayer transportLayer;
    protected TransportLayer dstTransportLayer;

    /**
     * Create event which will happen the given amount of nanoseconds later.
     *
     * @param timeFromNowNs  Time it will take before happening from now in
     *                       nanoseconds
     * @param transportLayer Source transport layer that wants to send the flow to
     *                       the target
     * @param targetId       Target network device identifier
     * @param flowSizeByte   Size of the flow to send in bytes
     */
    public FlowStartEvent(long timeFromNowNs, TransportLayer transportLayer, int targetId, long flowSizeByte) {
        super(timeFromNowNs);
        this.targetId = targetId;
        this.flowSizeByte = flowSizeByte;
        this.transportLayer = transportLayer;
        // setNetworkDeviceId(transportLayer);
        // take the above out of comments to re-enable state saving support.
    }

    protected void setNetworkDeviceId(TransportLayer tl) {
        this.networkDeviceId = tl.getNetworkDevice().getIdentifier();
    }

    public int getNetWorkDeviceId() {
        return networkDeviceId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void run() {

    }

    @Override
    public void trigger() {
        // TransportLayer tl =
        // BaseInitializer.getInstance().getNetworkDeviceById(networkDeviceId).getTransportLayer();
        long flowId = transportLayer.startFlow(targetId, flowSizeByte);
        if (dstTransportLayer != null)
            dstTransportLayer.registerAsDest(flowId, flowSizeByte);
    }

    public void registerDstTransport(TransportLayer dstTransportLayer) {
        this.dstTransportLayer = dstTransportLayer;
    }
}
