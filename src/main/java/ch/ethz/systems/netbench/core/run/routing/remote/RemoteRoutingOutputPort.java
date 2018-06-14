package ch.ethz.systems.netbench.core.run.routing.remote;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.DeviceNotSourceException;

public class RemoteRoutingOutputPort extends OutputPort{
	boolean extendedTopology;
	protected RemoteRoutingOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, Queue<Packet> queue, boolean isExtended) {
		super(ownNetworkDevice, targetNetworkDevice, link,queue);
		extendedTopology = isExtended;
	}

	@Override
	public void enqueue(Packet packet) {
			guaranteedEnqueue(packet);

	}

	@Override
	protected void registerPacketDispatchedEvent(Packet packet) {
		if(extendedTopology){
			if(!ownNetworkDevice.isServer() && !targetNetworkDevice.isServer()){
				Simulator.registerEvent(new PacketDispatchedEvent(
						0,
						packet,
						this
				));
				return;
			}
		}
		super.registerPacketDispatchedEvent(packet);

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
