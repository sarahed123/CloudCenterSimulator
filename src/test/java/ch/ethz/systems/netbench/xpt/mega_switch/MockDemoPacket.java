package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.ext.demo.DemoPacket;

public class MockDemoPacket extends DemoPacket {

	MockDemoPacket(long flowId, long dataSizeByte, int sourceId, int destinationId, int TTL, long ackSizeByte) {
		super(flowId, dataSizeByte, sourceId, destinationId, TTL, ackSizeByte);
		// TODO Auto-generated constructor stub
	}

}
