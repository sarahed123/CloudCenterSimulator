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
    private final Packet packet;
    private final InputPort inputPort;

    private static final int MAX_TRIGGER_COUNT = 3; 
    private int triggerCount = 0;

    /**
     * Packet arrival event constructor.
     *
     * @param timeFromNowNs Time in simulation nanoseconds from now
     * @param packet        Packet instance which will arrive
     * @param inputPort     The input port the package is arriving to
     */
    protected PacketArrivalEvent(long timeFromNowNs, Packet packet, InputPort inputPort) {
        super(timeFromNowNs);
        this.packet = packet;
        this.arrivalNetworkDeviceId = inputPort.getOwnNetworkDevice().getIdentifier();
        this.inputPort = inputPort;
    }

    public void run() {

    }

    public Packet getPacket() {
        return packet;
    }

    // @Override
    // public void trigger() {
    //     inputPort.receive(packet);
    // }
    public void trigger() {
        if (triggerCount < MAX_TRIGGER_COUNT) {
            inputPort.receive(packet);
            triggerCount++; // עדכון המונה לאחר הפעלת האירוע
        }
    }    

    /*
     * protected NetworkDevice getNetworkDevice() {
     * return
     * BaseInitializer.getInstance().getNetworkDeviceById(arrivalNetworkDeviceId);
     * 
     * }
     */

    @Override
    public String toString() {
        return "PacketArrivalEvent<" + arrivalNetworkDeviceId + ", " + this.getTime() + ", " + this.packet + ">";
    }

}
