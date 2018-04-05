package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;

/**
 * Event for the complete arrival of a packet in its entirety.
 */
public class PacketArrivalEvent extends Event {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3494066931483272486L;
	private final int arrivalNetworkDeviceId;
    private final Packet packet;

    /**
     * Packet arrival event constructor.
     *
     * @param timeFromNowNs             Time in simulation nanoseconds from now
     * @param packet                    Packet instance which will arrive
     * @param arrivalNetworkDevice      Network device at which the packet arrives
     */
    PacketArrivalEvent(long timeFromNowNs, Packet packet, NetworkDevice arrivalNetworkDevice) {
        super(timeFromNowNs);
        this.packet = packet;
        this.arrivalNetworkDeviceId = arrivalNetworkDevice.getIdentifier();
    }

    @Override
    public void trigger() {
        getNetworkDevice().receive(packet);
    }
    
    protected NetworkDevice getNetworkDevice() {
		return BaseInitializer.getInstance().getNetworkDeviceById(arrivalNetworkDeviceId);
    	
    }

    @Override
    public String toString() {
        return "PacketArrivalEvent<" + arrivalNetworkDeviceId + ", " + this.getTime() + ", " + this.packet + ">";
    }

}
