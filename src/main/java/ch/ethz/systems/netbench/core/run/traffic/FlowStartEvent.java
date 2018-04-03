package ch.ethz.systems.netbench.core.run.traffic;

import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;

public class FlowStartEvent extends Event {

    /**
	 * 
	 */
	private static final long serialVersionUID = -953241448509175658L;
	private final int targetId;
    private final long flowSizeByte;
    private final int networkDeviceId; 
    /**
     * Create event which will happen the given amount of nanoseconds later.
     *
     * @param timeFromNowNs     Time it will take before happening from now in nanoseconds
     * @param transportLayer    Source transport layer that wants to send the flow to the target
     * @param targetId          Target network device identifier
     * @param flowSizeByte      Size of the flow to send in bytes
     */
    public FlowStartEvent(long timeFromNowNs, TransportLayer transportLayer, int targetId, long flowSizeByte) {
        super(timeFromNowNs);
        this.targetId = targetId;
        this.flowSizeByte = flowSizeByte;
        this.networkDeviceId = transportLayer.getNetworkDevice().getIdentifier();
    }

    @Override
    public void trigger() {
    	TransportLayer tl = BaseInitializer.getInstance().getIdToNetworkDevice().get(networkDeviceId).getTransportLayer();
    	tl.startFlow(targetId, flowSizeByte);
    }

}
