package ch.ethz.systems.netbench.xpt.remotesourcerouting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Vertex;

public class RemoteSourceRoutingSwitch extends NetworkDevice {
	private Map<Long,OutputPort> forwardingTable;
	RemoteSourceRoutingSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary) {
		super(identifier, transportLayer, intermediary);
		this.forwardingTable = new HashMap<Long,OutputPort>();
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
    	try {
    		RemoteRoutingController.getInstance().initRoute(packet.getSourceId(),packet.getDestinationId(),packet.getFlowId());
    	}catch(FlowPathExists e) {
    		
    	}catch(NoPathException e) {
    		SimulationLogger.increaseStatisticCounter("num_path_failures");
    		return;
    	}
    	
    	receive(packet);
       

    }
    
    protected void forwardToNextSwitch(IpPacket packet) {
    	
    	forwardingTable.get(packet.getFlowId()).enqueue(packet);
		
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

			// Hand to the underlying server
			this.passToIntermediary(packet); // Will throw null-pointer if this network device does not have a server attached to it

		} else {
			// Forward to the next switch (automatically advances path progress)
			forwardToNextSwitch(packet);
			

		}
		
	}

	public void releasePath(long flowId) {
		RemoteRoutingController.getInstance().recoverPath(flowId);
		
	}

	public void updateForwardingTable(long flowId, int nextHop) {
		forwardingTable.put(flowId, targetIdToOutputPort.get(nextHop));
		
	}

	public Object getNextHop(long flowId) {
		// TODO Auto-generated method stub
		return forwardingTable.get(flowId);
	}


}
