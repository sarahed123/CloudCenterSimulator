package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;

/**
 * Event for the complete arrival of a packet in its entirety.
 * Currently this does not support serialization:
 * To change this you will need to get the input port,
 * which for MegaSwitch means you will need to mark the packet
 * with the appropriate network interface. Previously this was done
 * with "technology" field, please see in network device.
 */
public class PacketArrivalEvent extends Event {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3494066931483272486L;
	private final int arrivalNetworkDeviceId;
	private final int sourceNetworkDeviceId;
    private final Packet packet;
    private final InputPort inputPort;
    /**
     * Packet arrival event constructor.
     *
     * @param timeFromNowNs             Time in simulation nanoseconds from now
     * @param packet                    Packet instance which will arrive
     * @param inputPort                 The input port the package is arriving to
     */
    PacketArrivalEvent(long timeFromNowNs, Packet packet, InputPort inputPort) {
        super(timeFromNowNs);
        this.packet = packet;
        this.arrivalNetworkDeviceId = inputPort.getOwnNetworkDevice().getIdentifier();
        this.sourceNetworkDeviceId = inputPort.getSourceNetworkDevice().getIdentifier();
        this.inputPort = inputPort;
    }

    @Override
    public void trigger() {
        inputPort.receive(packet);
    }
    
    protected NetworkDevice getNetworkDevice() {
		return BaseInitializer.getInstance().getNetworkDeviceById(arrivalNetworkDeviceId);
    	
    }

    @Override
    public String toString() {
        return "PacketArrivalEvent<" + arrivalNetworkDeviceId + ", " + this.getTime() + ", " + this.packet + ">";
    }

}
