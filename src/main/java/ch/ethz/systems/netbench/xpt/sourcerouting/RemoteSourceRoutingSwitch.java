package ch.ethz.systems.netbench.xpt.sourcerouting;

import java.util.List;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Vertex;

public class RemoteSourceRoutingSwitch extends SourceRoutingSwitch {

	RemoteSourceRoutingSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary) {
		super(identifier, transportLayer, n, intermediary);
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

    	SourceRoutingPath selectedPath;
    	try {
    		selectedPath = getPathToDestinationById(packet.getFlowId(), packet.getDestinationId());
    		 // Create encapsulation to propagate through the network
            SourceRoutingEncapsulation encapsulation = new SourceRoutingEncapsulation(
                    packet,
                    selectedPath
            );

    	    // Send to network
            receive(encapsulation);
    	}catch (NoPathException e) {
    		super.receiveFromIntermediary(genericPacket);
		}
        
       

    }
    
    @Override
    protected SourceRoutingPath getPathToDestination(SourceRoutingSwitch src, int dest, Packet packet) {
    	RemoteRoutingController remoteRouter = RemoteRoutingController.getInstance();
		SourceRoutingPath srp =  remoteRouter.getRoute(src.getIdentifier(), dest,(RemoteSourceRoutingSwitch)src,packet.getFlowId());
		addPathToDestination(dest, srp);

		return srp;
	}
    
    @Override
    public void switchPathToDestination(int destinationId, SourceRoutingPath oldPath, SourceRoutingPath newPath) {
    	super.switchPathToDestination(destinationId, oldPath, newPath);
    	RemoteRoutingController remoteRouter = RemoteRoutingController.getInstance();
    	remoteRouter.recoverPath(oldPath);
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

	public void releasePath(int dest, long id) {

		List<SourceRoutingPath> current = this.destinationToPaths.get(dest);
    	for(int i=0; i<current.size();i++) {
    		if(current.get(i).getIdentifier()==id) {
    			RemoteRoutingController.getInstance().recoverPath(current.get(i));
    			current.remove(i);
    			
    			return;
    		}
		
    	}
		
	}


}
