package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.demo.DemoPacket;

public class MockDemoPacket extends TcpPacket {

	boolean received;

	MockDemoPacket(long flowId, long dataSizeByte, int sourceId, int destinationId, int TTL, long ackSizeByte) {
		super(flowId, dataSizeByte, sourceId, destinationId, TTL, 0,0,0,0,
				false,false,false,false,false,false,false,false,false,0);
		// TODO Auto-generated constructor stub
	}

	public void markReceived(){
		this.received = true;
	}

}
