package ch.ethz.systems.netbench.xpt.dynamic.device;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;

public class DynamicOutputPortGenerator extends OutputPortGenerator {
	protected long mMaxQueueSizeBytes;
	protected long mEcnThresholdKBytes;
	public DynamicOutputPortGenerator(NBProperties configuration) {
		super(configuration);
		mEcnThresholdKBytes = configuration.getLongPropertyOrFail("output_port_ecn_threshold_k_bytes");
		mMaxQueueSizeBytes = configuration.getLongPropertyOrFail("output_port_max_queue_size_bytes");
	}

	@Override
	public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
		// TODO Auto-generated method stub
		return new DynamicOutuptPort(ownNetworkDevice, towardsNetworkDevice, link, mMaxQueueSizeBytes, mEcnThresholdKBytes);
	}

}
