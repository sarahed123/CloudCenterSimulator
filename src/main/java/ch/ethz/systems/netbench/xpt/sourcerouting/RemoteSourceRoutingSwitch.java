package ch.ethz.systems.netbench.xpt.sourcerouting;

import java.util.List;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Vertex;

public class RemoteSourceRoutingSwitch extends SourceRoutingSwitch {

	RemoteSourceRoutingSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary) {
		super(identifier, transportLayer, n, intermediary);
	}
	
	@Override
	protected void passToIntermediary(TcpPacket packet, Path path) {
   	 	this.passToIntermediary(packet);
		RemoteRoutingController.getInstance().recoverPath(path);
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
        TcpPacket packet = (TcpPacket) genericPacket;
        RemoteRoutingController remoteRouter = RemoteRoutingController.getInstance();
	    // Retrieve possible valiant paths to choose from
        List<SourceRoutingPath> possibilities;
        SourceRoutingPath selectedPath;

        if (isWithinExtendedTopology) {

            // Determine source and destination ToR to which the source and destination servers are attached
            int sourceTor = Simulator.getConfiguration().getGraphDetails().getTorIdOfServer(packet.getSourceId());
            int destinationTor = Simulator.getConfiguration().getGraphDetails().getTorIdOfServer(packet.getDestinationId());
            // Create path
            selectedPath = new SourceRoutingPath();

            // If the servers are not on the same ToR
            if (sourceTor != destinationTor) {

                // Retrieve ToR to which it is attached
                SourceRoutingSwitch sourceTorDevice = (SourceRoutingSwitch) this.targetIdToOutputPort.get(sourceTor).getTargetDevice();

                
                // Retrieve the src-ToR to dst-ToR path possibilities, not needed for now
                //possibilities = sourceTorDevice.getPathsList().get(destinationTor);

                
                // right now all thats needed is a single path.
                selectedPath.addAll(remoteRouter.getRoute(sourceTor, destinationTor,this));

            } else {

                // If both servers are in the same ToR just create the single up-down path
                selectedPath.add(sourceTor);

            }

            // Now we add both the original server and the destination server to the path
            selectedPath.add(0, packet.getSourceId());
            selectedPath.add(packet.getDestinationId());

        } else {
        	
        	// right now all thats needed is a single path.
        	selectedPath = new SourceRoutingPath();
        	selectedPath.addAll(remoteRouter.getRoute(packet.getSourceId(), packet.getDestinationId(),this));
        }
        addPathToDestination(packet.getDestinationId(), selectedPath);
        // Create encapsulation to propagate through the network
        SourceRoutingEncapsulation encapsulation = new SourceRoutingEncapsulation(
                packet,
                selectedPath
        );

	    // Send to network
        receive(encapsulation);

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


}
