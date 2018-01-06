package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.ext.basic.IpPacket;

public class RemoteRoutingPacket extends IpPacket {
	

	public RemoteRoutingPacket(long flowId, long payloadSizeBit, int sourceId, int destinationId, int TTL) {
		super(flowId, payloadSizeBit, sourceId, destinationId, TTL,RemoteRoutingController.getHeaderSize());
		// TODO Auto-generated constructor stub
	}


}
