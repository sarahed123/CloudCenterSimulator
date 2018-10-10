package ch.ethz.systems.netbench.xpt.remotesourcerouting;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.IpPacket;

public class RemoteSourceRoutingSwitch extends NetworkDevice {
	private Map<Pair<Integer,Integer>,OutputPort> forwardingTable;
	protected RemoteSourceRoutingSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
		super(identifier, transportLayer, intermediary,configuration);
		this.forwardingTable = new HashMap<Pair<Integer,Integer>,OutputPort>();
	}

	/**
     * Receives a TCP packet from the transport layer, which
     * is oblivious to the source routing happening underneath.
     * The TCP packet is then encapsulated to carry information of the
     * route it must take. The sequential hash of the packet is used
     * to determine the path it should be sent on.
     *
     * @param genericPacket     TCP packet instance
     */
    @Override
    public void receiveFromIntermediary(Packet genericPacket) {
		IpPacket packet = (IpPacket) genericPacket;
		initCircuit(packet);
    	
    	receive(packet);
       

    }
    
    protected void forwardToNextSwitch(IpPacket packet) {
    	IpPacket deEncapse = (IpPacket) (((Encapsulatable) packet).deEncapsualte());
    	forwardingTable.get(new ImmutablePair<>(deEncapse.getSourceId(),deEncapse.getDestinationId())).enqueue(packet);
		
	}
    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RemoteSourceRoutingSwitch<id=");
        builder.append(getIdentifier());
        builder.append(", connected=");
        builder.append(connectedTo);
        builder.append(", routing: ");
        builder.append(">");
        return builder.toString();
    }


	@Override
	public void receive(Packet genericPacket) {
		IpPacket packet = (IpPacket) genericPacket;


		if (packet.getDestinationId() == this.identifier) {
			if(isServer()){
				// Hand to the underlying server
				this.passToIntermediary(packet); // Will throw null-pointer if this network device does not have a server attached to it
			}else{

				passToEncapsulatingDevice(packet);
			}


		} else {
			
			// Forward to the next switch (automatically advances path progress)
			forwardToNextSwitch(packet);
			

		}
		
	}

	public void releasePath(int src,int dst, long flowId) {
		RemoteRoutingController.getInstance().recoverPath(src,dst, flowId);
		
	}

	public void updateForwardingTable(int src, int dest, int nextHop) {
		forwardingTable.put(new ImmutablePair<Integer,Integer>(src,dest), targetIdToOutputPort.get(nextHop));
		
	}

	public OutputPort getNextHop(int src, int dest) {
		// TODO Auto-generated method stub
		return forwardingTable.get(new ImmutablePair<Integer,Integer>(src,dest));
	}


}
