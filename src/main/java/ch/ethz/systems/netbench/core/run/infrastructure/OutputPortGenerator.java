package ch.ethz.systems.netbench.core.run.infrastructure;

import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.OutputPort;

public abstract class OutputPortGenerator {
	protected NBProperties configuration;
	public OutputPortGenerator(NBProperties configuration) {
		this.configuration = configuration;
	}
    public abstract OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link);
}
