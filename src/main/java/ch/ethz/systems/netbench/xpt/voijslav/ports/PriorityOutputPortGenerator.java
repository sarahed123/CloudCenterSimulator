package ch.ethz.systems.netbench.xpt.voijslav.ports;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;

public class PriorityOutputPortGenerator  extends OutputPortGenerator {

    public PriorityOutputPortGenerator(NBProperties configuration) {
    	super(configuration);
        SimulationLogger.logInfo("Port", "PRIORITY_PORT");
    }

    @Override
    public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
        return new PriorityOutputPort(ownNetworkDevice, towardsNetworkDevice, link);
    }

}
