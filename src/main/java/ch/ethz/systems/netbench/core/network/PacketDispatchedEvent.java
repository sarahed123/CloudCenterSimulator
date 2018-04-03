package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;

/**
 * Event for the dispatch of a packet, i.e. when all of the bits
 * of the packet have been written to the link.
 */
public class PacketDispatchedEvent extends Event {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8099553808355024992L;
	private final Packet packet;
    private final int deviceId;
    private final int targetId;

    /**
     * Packet dispatched event constructor.
     *
     * @param timeFromNowNs     Time in simulation nanoseconds from now
     * @param packet            Packet instance which is dispatched
     * @param dispatchPort      Port that has finished writing the packet to the link
     */
    PacketDispatchedEvent(long timeFromNowNs, Packet packet, OutputPort dispatchPort) {
        super(timeFromNowNs);
        this.packet = packet;
        this.targetId = dispatchPort.getTargetId();
        this.deviceId = dispatchPort.getOwnId();
    }

    @Override
    public void trigger() {
    	NetworkDevice nd = getOwnDevice();
    	getOutputPort(nd).dispatch(packet);

    }
    
    protected NetworkDevice getOwnDevice() {
    	return BaseInitializer.getInstance().getIdToNetworkDevice().get(this.deviceId); 
    }
    
    protected OutputPort getOutputPort(NetworkDevice nd){
    	return nd.targetIdToOutputPort.get(targetId);
    }

    @Override
    public String toString() {
        return "PacketDispatchedEvent<" + deviceId + " -> " + this.targetId + ", " + this.getTime() + ", " + this.packet + ">";
    }

}
