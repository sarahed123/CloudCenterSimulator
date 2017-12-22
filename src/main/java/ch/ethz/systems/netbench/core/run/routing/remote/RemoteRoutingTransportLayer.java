package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.bare.BareSocket;
import ch.ethz.systems.netbench.xpt.sourcerouting.RemoteSourceRoutingSwitch;

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

	public void continueFlow(long flowId) {
		RemoteRoutingSocket rrs = (RemoteRoutingSocket) flowIdToSocket.get(flowId);
		rrs.continueFlow();
		
	}
    

}
