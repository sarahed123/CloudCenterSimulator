package ch.ethz.systems.netbench.core.run.routing.remote;

import java.util.LinkedList;
import java.util.Queue;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.bare.BareSocket;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.DeviceNotSourceException;

public class RemoteRoutingTransportLayer extends TransportLayer {
	Queue<Flow> flowsQueue;
	RemoteRoutingTransportLayer(int identifier,NBProperties configuration) {
        super(identifier,configuration);
        //flowsQueue = new LinkedList<RemoteRoutingTransportLayer.Flow>();
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new RemoteRoutingSocket(this, flowId, identifier, destinationId, flowSizeByte);
    }

	public void releasePath(int destinationId, long flowId) {
		((RemoteSourceRoutingSwitch) networkDevice).releasePath(flowId);
		
		/*Flow f = flowsQueue.poll();
		if(f!=null) {
			super.startFlow(f.destination, f.flowSizeByte);
		}*/
	}
	
	@Override
	public void startFlow(int destination, long flowSizeByte) {
		super.startFlow(destination, flowSizeByte);
		/*if(flowIdToSocket.size()==0) {
			super.startFlow(destination, flowSizeByte);
		}else {
			flowsQueue.add(new Flow(destination,flowSizeByte));
		}*/
	}

	public void continueFlow(RemoteRoutingPacket packet) {
    	
		RemoteRoutingSocket rrs = (RemoteRoutingSocket) flowIdToSocket.get(packet.getFlowId());
		if(rrs==null) {
			//this can happen in state reset
			rrs = (RemoteRoutingSocket) createSocket(packet.getFlowId(), packet.getDestinationId(), packet.flowRemainder);
			rrs.markAsSender();
			flowIdToSocket.put(packet.getFlowId(), rrs);
		}
		rrs.continueFlow(packet);
		
	}
	
	private class Flow{
		private int destination;
		private long flowSizeByte;
		private Flow(int dest,long size) {
			destination = dest;
			flowSizeByte = size;
		}
	}
    

}
