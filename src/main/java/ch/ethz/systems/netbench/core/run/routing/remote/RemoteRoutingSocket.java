package ch.ethz.systems.netbench.core.run.routing.remote;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.demo.DemoPacket;
import ch.ethz.systems.netbench.ext.demo.DemoSocket;
import com.google.common.base.Joiner;

public class RemoteRoutingSocket extends Socket{

	public RemoteRoutingSocket(RemoteRoutingTransportLayer transportLayer, long flowId, int sourceId, int destinationId,
			long flowSizeByte) {
		super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
		
		// TODO Auto-generated constructor stub
	}

    @Override
    protected void onAllFlowConfirmed() {
		if(isReceiver()){
			Simulator.registerFlowFinished(flowId);
		}
		transportLayer.removeSocket(flowId);
    }

    @Override
    public void start() {
        // Send out single data packet at start
         transportLayer.send(new RemoteRoutingPacket(flowId, getNextPayloadSizeByte(), sourceId, destinationId, 100,getRemainderToConfirmFlowSizeByte()));

      
    }

	@Override
	public void handle(Packet genericPacket) {
		if(isReceiver()){
			if(((RemoteRoutingPacket) genericPacket).isLast()){
				((RemoteRoutingTransportLayer) transportLayer).releasePath(destinationId,flowId);
				onAllFlowConfirmed();
			}
		}

		
	}
	
	protected long getNextPayloadSizeByte() {
        return Math.min(1000L, getRemainderToConfirmFlowSizeByte());
    }

	public void continueFlow(RemoteRoutingPacket packet) {
	
		if(packet.getSizeBit()==0) {

			throw new RuntimeException("packet size is 0");
		}
		confirmFlow(getNextPayloadSizeByte());
		long nextPayload = getNextPayloadSizeByte();

		if(!isAllFlowConfirmed()) {
			transportLayer.send(new RemoteRoutingPacket(flowId,nextPayload, sourceId, destinationId, 100,getRemainderToConfirmFlowSizeByte()));
		}

	}




}
