package ch.ethz.systems.netbench.core.run.routing.remote;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;
import ch.ethz.systems.netbench.ext.basic.IpHeader;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.DeviceNotSourceException;

public class RemoteRoutingOutputPort extends EcnTailDropOutputPort{
	boolean extendedTopology;
	protected RemoteRoutingOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link,long maxQueueSizeBytes, long ecnThresholdKBytes, boolean isExtended) {
		super(ownNetworkDevice, targetNetworkDevice, link,maxQueueSizeBytes,ecnThresholdKBytes);
		extendedTopology = isExtended;
	}

	@Override
	public void enqueue(Packet packet) {
		IpPacket ipPacket = (IpPacket) packet;
		if(ipPacket.getSourceId()==this.getOwnDevice().getIdentifier()){
			super.enqueue(packet);
		}else{
			super.registerPacketDispatchedEvent(packet);
		}

	}

	protected void onPacketDropped(IpHeader ipHeader) {
		super.onPacketDropped(ipHeader);
		SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_ON_CIRCUIT");
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
		

		IpPacket ipPacket = (IpPacket) packet;
		


		super.dispatch(packet);
		try {
			if(ownNetworkDevice.isServer() && ipPacket.getSourceId()==ownNetworkDevice.getIdentifier()){
				RemoteRoutingPacket rrpacket = (RemoteRoutingPacket) packet;
				RemoteRoutingTransportLayer tl = (RemoteRoutingTransportLayer) ownNetworkDevice.getTransportLayer();

				tl.continueFlow(rrpacket);
			}

		}catch(DeviceNotSourceException e){

		}

	}

}
