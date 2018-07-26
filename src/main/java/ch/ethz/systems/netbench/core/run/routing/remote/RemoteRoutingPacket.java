package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;

public class RemoteRoutingPacket extends IpPacket implements Encapsulatable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 520197092371233676L;

	public long flowRemainder;
	public RemoteRoutingPacket(long flowId, long payloadSizeBit, int sourceId, int destinationId, int TTL, long remainder) {
		super(flowId, payloadSizeBit, sourceId, destinationId, TTL,RemoteRoutingController.getHeaderSize());
		flowRemainder = remainder;
		
	}

	public RemoteRoutingPacket(RemoteRoutingPacket p) {
		super(p);
		flowRemainder = p.flowRemainder;


	}

	public boolean isLast(){
		return flowRemainder==getSizeBit();
	}

	@Override
	public Encapsulatable encapsulate(int newSource,int newDestination) {
		// TODO Auto-generated method stub
		return new RemoteRoutingPacket(this) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public int getDestinationId() {
				return newDestination;
			}

			@Override
			public int getSourceId() {
				return newSource;
			}

		};
    }

	@Override
	public Encapsulatable deEncapsualte() {
		// TODO Auto-generated method stub
		return new RemoteRoutingPacket(this);
	}

}
