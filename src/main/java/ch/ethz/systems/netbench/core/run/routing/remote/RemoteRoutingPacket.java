package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;

public class RemoteRoutingPacket extends IpPacket implements Encapsulatable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 520197092371233676L;
	private int encapsulatedDestination;
	public long flowRemainder;
	public RemoteRoutingPacket(long flowId, long payloadSizeBit, int sourceId, int destinationId, int TTL, long remainder) {
		super(flowId, payloadSizeBit, sourceId, destinationId, TTL,RemoteRoutingController.getHeaderSize());
		flowRemainder = remainder;
		encapsulatedDestination = -1;
		
	}

	public boolean isLast(){
		return flowRemainder==getSizeBit();
	}

	@Override
	public Encapsulatable encapsulate(int newDestionation) {
		RemoteRoutingPacket p =  new RemoteRoutingPacket(getFlowId(), getSizeBit(), getSourceId(), newDestionation, getTTL(), flowRemainder);
    	p.encapsulatedDestination = getDestinationId();
		return p;
    }

	@Override
	public Encapsulatable deEncapsualte() {
		// TODO Auto-generated method stub
		return new RemoteRoutingPacket(getFlowId(), getSizeBit(), getSourceId(), encapsulatedDestination, getTTL(), flowRemainder);
	}

}
