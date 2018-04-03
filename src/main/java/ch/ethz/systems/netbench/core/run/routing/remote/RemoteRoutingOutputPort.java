package ch.ethz.systems.netbench.core.run.routing.remote;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.DeviceNotSourceException;

public class RemoteRoutingOutputPort extends OutputPort{

	protected RemoteRoutingOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, Queue<Packet> queue) {
		super(ownNetworkDevice, targetNetworkDevice, link,queue);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void enqueue(Packet packet) {
		
		guaranteedEnqueue(packet);

	}

	@Override
	protected void dispatch(Packet packet) {
		

		RemoteRoutingPacket rrpacket = (RemoteRoutingPacket) packet;
		


		super.dispatch(packet);
		try {
			if(ownNetworkDevice.isServer() && rrpacket.getSourceId()==ownNetworkDevice.getIdentifier()){
				RemoteRoutingTransportLayer tl = (RemoteRoutingTransportLayer) ownNetworkDevice.getTransportLayer();

				tl.continueFlow(rrpacket);
			}

		}catch(DeviceNotSourceException e){

		}

	}

}
