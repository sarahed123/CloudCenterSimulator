package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import java.util.Random; 

import java.util.ArrayList;
import java.util.List;

public class EcmpSwitch extends NetworkDevice implements EcmpSwitchRoutingInterface {
    protected Random rand;
    // Routing table
    protected final List<List<Integer>> destinationToNextSwitch;

    /**
     * Constructor for ECMP switch.
     *
     * @param identifier        Network device identifier
     * @param transportLayer    Underlying server transport layer instance (set null, if none)
     * @param n                 Number of network devices in the entire network (for routing table size)
     * @param intermediary      Flowlet intermediary instance (takes care of hash adaptation for flowlet support)
     */
    public  EcmpSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary,NBProperties configuration) {
        super(identifier, transportLayer, intermediary,configuration);
        this.destinationToNextSwitch = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            this.destinationToNextSwitch.add(new ArrayList<>());
        }
	    this.rand = new Random();
    }

    @Override
    public void receive(Packet genericPacket) {
        IpPacket ipPacket = (IpPacket) genericPacket;



        // Check if it has arrived
        if (ipPacket.getDestinationId() == this.identifier) {
            if(isServer()){
                // Hand to the underlying server
                this.passToIntermediary(genericPacket); // Will throw null-pointer if this network device does not have a server attached to it
            }else {

                passToEncapsulatingDevice(genericPacket);
            }

        } else {
            forwardToNextSwitch(genericPacket);

        }

    }

    protected void  forwardToNextSwitch(Packet genericPacket){
        // Convert to TCP packet
        TcpHeader tcpHeader = (TcpHeader) genericPacket;
        // Forward to the next switch
        List<Integer> possibilities = destinationToNextSwitch.get(tcpHeader.getDestinationId());
        if(!configuration.getBooleanPropertyWithDefault("disable_ecmp_path_hashing", false)){
            int hash = tcpHeader.getHash(this.identifier);
            this.getTargetOuputPort(possibilities.get(hash % possibilities.size()), hash).enqueue(genericPacket);
        }else{
            int randomNext = possibilities.get(rand.nextInt(possibilities.size()));
            this.getTargetOuputPort(randomNext).enqueue(genericPacket);
        }
    }

    @Override
    public void receiveFromIntermediary(Packet genericPacket) {
        receive(genericPacket);
    }

    /**
     * Add another hop opportunity to the routing table for the given destination.
     *
     * @param destinationId     Destination identifier
     * @param nextHopId         A network device identifier where it could go to next (must have already been added
     *                          as connection via {@link #addConnection(OutputPort)}, else will throw an illegal
     *                          argument exception.
     */
    @Override
    public void addDestinationToNextSwitch(int destinationId, int nextHopId) {

        // Check for not possible identifier
        if (!connectedTo.contains(nextHopId)) {
            throw new IllegalArgumentException("Cannot add hop to a network device to which it is not connected (" + nextHopId + ")");
        }

        // Check for duplicate
        List<Integer> current = this.destinationToNextSwitch.get(destinationId);
        if (current.contains(nextHopId)) {
            throw new IllegalArgumentException("Cannot add a duplicate next hop network device identifier (" + nextHopId + ")");
        }

        // Add to current ones
        current.add(nextHopId);

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ValiantSwitch<id=");
        builder.append(getIdentifier());
        builder.append(", connected=");
        builder.append(connectedTo);
        builder.append(", routing: ");
        for (int i = 0; i < destinationToNextSwitch.size(); i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(i);
            builder.append("->");
            builder.append(destinationToNextSwitch.get(i));
        }
        builder.append(">");
        return builder.toString();
    }


}
