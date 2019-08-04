package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.InputPort;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.dynamic.device.DynamicOutuptPort;

public class RotorOutputPort extends DynamicOutuptPort {
	RotorSwitch mOriginalDevice;
	RotorMap mRotorMap;
	private long mDispatchTime;
	int mPacketSentCounter;

	protected RotorOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link,
			long maxQueueSizeBytes, long ecnThresholdKBytes) {
		super(ownNetworkDevice, targetNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
		mOriginalDevice = (RotorSwitch) ownNetworkDevice;
		this.ownNetworkDevice = ownNetworkDevice;
		mPacketSentCounter = 0;
	}

	public RotorSwitch getOriginalDevice(){
		return this.mOriginalDevice;
	}
	
	@Override
	protected InputPort getTargetInputPort() {
		return new InputPort(targetNetworkDevice,ownNetworkDevice,link);
	}

	public void setRotorMap(RotorMap rotorMap) {
		this.mRotorMap = rotorMap;
	}

	/**
	 * only send if the next reconfiguration event is not closer then the packet transmission time
	 * @param packet    Packet instance
	 */
	@Override
	public void enqueue(Packet packet) {
		IpPacket ipPacket = (IpPacket) packet;
		long totalBits = getBufferOccupiedBits() + ipPacket.getSizeBit();
		long time = totalBits/link.getBandwidthBitPerNs() +link.getDelayNs();
		if(isSending){
			time += (mDispatchTime - Simulator.getCurrentTime()); // mDispatchTime already includes current time
		}

		if(Simulator.getCurrentTime() + time > RotorNetController.sNextReconfigurationTime){
			onConfigurationTimeExceeded();
		}
		mPacketSentCounter++;
		super.enqueue(packet);
	}

	protected void onConfigurationTimeExceeded() {
		throw new ReconfigurationDeadlineException();
	}

	@Override
	protected long getDispatchTime(Packet packet){
		long time = packet.getSizeBit() / link.getBandwidthBitPerNs();
		mDispatchTime = Simulator.getTimeFromNow(time);

		return time;
	}

	@Override
	protected void log(Packet packet) {

	}


}
