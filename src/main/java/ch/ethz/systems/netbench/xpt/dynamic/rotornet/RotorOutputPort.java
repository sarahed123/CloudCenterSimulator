package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import java.util.Queue;

import ch.ethz.systems.netbench.core.network.InputPort;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.xpt.dynamic.device.DynamicOutuptPort;

public class RotorOutputPort extends DynamicOutuptPort {
	RotorSwitch mOriginalDevice;
	protected RotorOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link,
			long maxQueueSizeBytes, long ecnThresholdKBytes) {
		super(ownNetworkDevice, targetNetworkDevice, link, maxQueueSizeBytes, ecnThresholdKBytes);
		mOriginalDevice = (RotorSwitch) ownNetworkDevice;
		this.ownNetworkDevice = null;
	}

	public RotorSwitch getOriginalDevice(){
		return this.mOriginalDevice;
	}
	
	@Override
	protected InputPort getTargetInputPort() {
		return new InputPort(targetNetworkDevice,ownNetworkDevice,link);
	}

}
