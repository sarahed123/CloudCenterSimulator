package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.demo.DemoPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;

public class MockDemoPacket extends TcpPacket implements Encapsulatable {

	boolean received;
	MockDemoPacket(long flowId, long dataSizeByte, int sourceId, int destinationId, int TTL, long ackSizeByte) {
		super(flowId, dataSizeByte, sourceId, destinationId, TTL, 0,0,0,0,
				false,false,false,false,false,false,false,false,false,0);
		// TODO Auto-generated constructor stub
	}

	public MockDemoPacket(MockDemoPacket mockDemoPacket) {
		super(mockDemoPacket);
		this.received = mockDemoPacket.received;
	}

	public void markReceived(){
		this.received = true;
	}

	@Override
	public Encapsulatable encapsulate(final int newDestination) {
		System.out.println("encapsulating");
		return new MockDemoPacket(this) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public int getDestinationId() {
				return newDestination;
			}
			
		};
	}

	@Override
	public Encapsulatable deEncapsualte() {
		System.out.println("dencapsulating");
		return new MockDemoPacket(this);
	}
}
