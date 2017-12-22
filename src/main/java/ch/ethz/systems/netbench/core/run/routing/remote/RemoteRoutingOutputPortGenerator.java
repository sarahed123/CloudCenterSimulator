package ch.ethz.systems.netbench.core.run.routing.remote;

import java.util.concurrent.LinkedBlockingQueue;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;

public class RemoteRoutingOutputPortGenerator extends OutputPortGenerator{

	
    public RemoteRoutingOutputPortGenerator() {
        SimulationLogger.logInfo("Port", "REMOTE_ROUTING_PORT");
    }

    
	@Override
	public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
		// TODO Auto-generated method stub
		return new RemoteRoutingOutputPort(ownNetworkDevice, towardsNetworkDevice, link, new LinkedBlockingQueue<Packet>());
	}

}