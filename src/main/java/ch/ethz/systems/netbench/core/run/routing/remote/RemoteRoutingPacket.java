package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.ext.basic.IpPacket;

public class RemoteRoutingPacket extends IpPacket {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 520197092371233676L;
	public long flowRemainder;
	public RemoteRoutingPacket(long flowId, long payloadSizeBit, int sourceId, int destinationId, int TTL, long remainder) {
		super(flowId, payloadSizeBit, sourceId, destinationId, TTL,RemoteRoutingController.getHeaderSize());
		flowRemainder = remainder;
		
	}


}
