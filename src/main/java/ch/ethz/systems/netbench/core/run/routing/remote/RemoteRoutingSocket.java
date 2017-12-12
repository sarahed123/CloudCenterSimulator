package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.demo.DemoSocket;

public class RemoteRoutingSocket extends DemoSocket{

	public RemoteRoutingSocket(RemoteRoutingTransportLayer transportLayer, long flowId, int sourceId, int destinationId,
			long flowSizeByte) {
		super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
		// TODO Auto-generated constructor stub
	}

    @Override
    protected void onAllFlowConfirmed() {
    	((RemoteRoutingTransportLayer) transportLayer).releasePath(destinationId,flowId);
    }

}
