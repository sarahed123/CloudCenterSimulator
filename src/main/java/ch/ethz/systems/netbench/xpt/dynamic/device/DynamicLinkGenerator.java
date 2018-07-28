package ch.ethz.systems.netbench.xpt.dynamic.device;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;

public class DynamicLinkGenerator extends LinkGenerator {
	NBProperties mConfiguration;
	long mDelayNs;
	long mBandwidthBitPerNs;
	public DynamicLinkGenerator(NBProperties conf) {
		this.mConfiguration = conf;
		 mDelayNs = conf.getLongPropertyOrFail("link_delay_ns");
		 mBandwidthBitPerNs = conf.getLongPropertyOrFail("link_bandwidth_bit_per_ns");
	}
	@Override
	public Link generate(NetworkDevice fromNetworkDevice, NetworkDevice toNetworkDevice) {
		// TODO Auto-generated method stub
		return new DynamicLink(mDelayNs, mBandwidthBitPerNs);
	}

}
