package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.demo.DemoPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;

public class MockDemoPacket extends TcpPacket implements Encapsulatable {

	boolean received;
	int oldDestination = -1;
	MockDemoPacket(long flowId, long dataSizeByte, int sourceId, int destinationId, int TTL, long ackSizeByte) {
		super(flowId, dataSizeByte, sourceId, destinationId, TTL, 0,0,0,0,
				false,false,false,false,false,false,false,false,false,0);
		// TODO Auto-generated constructor stub
	}

	public void markReceived(){
		this.received = true;
	}

	@Override
	public IpPacket encapsulate(int newDestination) {
		MockDemoPacket p = new MockDemoPacket(getFlowId(),getDataSizeByte(),getSourceId(),newDestination,getTTL(),0);
		p.oldDestination = getDestinationId();
		return p;
	}

	@Override
	public IpPacket deEncapsualte() {
		return new MockDemoPacket(getFlowId(),getDataSizeByte(),getSourceId(),oldDestination,getTTL(),0);
	}
}
