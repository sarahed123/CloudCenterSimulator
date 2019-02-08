package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;

public class LightOutputPortGenerator extends OutputPortGenerator{

	
    public LightOutputPortGenerator(NBProperties configuration) {
    	super(configuration);
        SimulationLogger.logInfo("Port", "REMOTE_ROUTING_PORT");
    }

    
	@Override
	public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
		
		boolean isExtended = configuration.isExtendedTopology();
		return new LightOutputPort(ownNetworkDevice, towardsNetworkDevice, link,  configuration.getLongPropertyOrFail("output_port_max_queue_size_bytes"),
				configuration.getLongPropertyOrFail("output_port_ecn_threshold_k_bytes"),isExtended);
	}

}
