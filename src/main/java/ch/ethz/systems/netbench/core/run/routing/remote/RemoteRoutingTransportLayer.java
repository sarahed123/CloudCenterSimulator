package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.bare.BareSocket;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.DeviceNotSourceException;

public class RemoteRoutingTransportLayer extends TransportLayer {

	RemoteRoutingTransportLayer(int identifier) {
        super(identifier);
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new RemoteRoutingSocket(this, flowId, identifier, destinationId, flowSizeByte);
    }

	public void releasePath(int destinationId, long flowId) {
		((RemoteSourceRoutingSwitch) networkDevice).releasePath(flowId);
		
	}

	public void continueFlow(RemoteRoutingPacket packet) {
    	
		RemoteRoutingSocket rrs = (RemoteRoutingSocket) flowIdToSocket.get(packet.getFlowId());
		if(rrs==null) {
			//this can happen in state reset
			rrs = (RemoteRoutingSocket) createSocket(packet.getFlowId(), packet.getDestinationId(), packet.flowRemainder);
			rrs.markAsSender();
			flowIdToSocket.put(packet.getFlowId(), rrs);
		}
		rrs.continueFlow(packet);
		
	}
    

}
