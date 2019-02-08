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

/**
 * this class transmits packets with zero delay, and allows no queueing
 */
public class LightOutputPort extends EcnTailDropOutputPort{
	boolean extendedTopology;
	protected LightOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, long maxQueueSizeBytes, long ecnThresholdKBytes, boolean isExtended) {
		super(ownNetworkDevice, targetNetworkDevice, link,maxQueueSizeBytes,ecnThresholdKBytes);
		extendedTopology = isExtended;
	}

	@Override
	public void enqueue(Packet packet) {
		this.registerPacketDispatchedEventNoDelay(packet);
	}

	/**
	 * no delay for packets transimtted here
	 * @param packet
	 */
	protected void registerPacketDispatchedEventNoDelay(Packet packet) {
		Simulator.registerEvent(new PacketDispatchedEvent(
				0,
				packet,
				this
		));
	}

	protected void onPacketDropped(IpHeader ipHeader) {
		super.onPacketDropped(ipHeader);
		SimulationLogger.increaseStatisticCounter("PACKETS_DROPPED_ON_CIRCUIT");
	}

//	@Override
//	protected void registerPacketDispatchedEvent(Packet packet) {
//		if(extendedTopology){
//			if(!ownNetworkDevice.isServer() && !targetNetworkDevice.isServer()){
//				Simulator.registerEvent(new PacketDispatchedEvent(
//						0,
//						packet,
//						this
//				));
//				return;
//			}
//		}
//		super.registerPacketDispatchedEvent(packet);
//
//	}
//
//	@Override
//	protected void dispatch(Packet packet) {
//
//
//		IpPacket ipPacket = (IpPacket) packet;
//
//
//
//		super.dispatch(packet);
//		try {
//			if(ownNetworkDevice.isServer() && ipPacket.getSourceId()==ownNetworkDevice.getIdentifier()){
//				RemoteRoutingPacket rrpacket = (RemoteRoutingPacket) packet;
//				RemoteRoutingTransportLayer tl = (RemoteRoutingTransportLayer) ownNetworkDevice.getTransportLayer();
//
//				tl.continueFlow(rrpacket);
//			}
//
//		}catch(DeviceNotSourceException e){
//
//		}
//
//	}

}
