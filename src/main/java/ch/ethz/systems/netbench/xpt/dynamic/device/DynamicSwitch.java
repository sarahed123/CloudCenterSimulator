package ch.ethz.systems.netbench.xpt.dynamic.device;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.InputPort;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicDevice;

public class DynamicSwitch extends NetworkDevice implements DynamicDevice {

	long mMaxQueueSizeBytes;
	long mEcnThresholdKBytes;
	long mDelayNs;
	long mBandwidthBitPerNs;
	protected DynamicSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary,
			NBProperties configuration) {
		super(identifier, transportLayer, intermediary, configuration);
		 mDelayNs = configuration.getLongPropertyOrFail("link_delay_ns");
		 mBandwidthBitPerNs = configuration.getLongPropertyOrFail("link_bandwidth_bit_per_ns");
		 mEcnThresholdKBytes = configuration.getLongPropertyOrFail("output_port_ecn_threshold_k_bytes");
		 mMaxQueueSizeBytes = configuration.getLongPropertyOrFail("output_port_max_queue_size_bytes");
	}

	@Override
	public void receive(Packet genericPacket) {
		targetIdToOutputPort.get(((IpPacket)genericPacket).getDestinationId()).enqueue(genericPacket);

	}

	@Override
	public void addConnection(NetworkDevice source,NetworkDevice dest) {
		Link link = new DynamicLink(mDelayNs, mBandwidthBitPerNs);
		targetIdToOutputPort.put(dest.getIdentifier(), new DynamicOutuptPort(this, dest,link , mMaxQueueSizeBytes, mEcnThresholdKBytes));
		((DynamicSwitch)dest).setInputPort(new InputPort(dest, this,link));
	}

	@Override
	public void removeConnection(NetworkDevice source,NetworkDevice dest) {
		targetIdToOutputPort.remove(dest.getIdentifier());
		((DynamicSwitch)dest).removeInputPort(this.identifier);
		
	}


	private void removeInputPort(int identifier) {
		this.sourceIdToInputPort.remove(identifier);
		
	}

	@Override
	protected void receiveFromIntermediary(Packet genericPacket) {
		// TODO Auto-generated method stub
		
	}
	
	private void setInputPort(InputPort inputPort) {
		this.sourceIdToInputPort.put(inputPort.getSourceNetworkDevice().getIdentifier(), inputPort);
	}


	
	

}
