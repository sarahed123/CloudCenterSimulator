package ch.ethz.systems.netbench.core.run.routing.remote;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;
import ch.ethz.systems.netbench.ext.bare.BareTransportLayer;

public class RemoteRoutingTransportLayerGenerator extends TransportLayerGenerator {

    public RemoteRoutingTransportLayerGenerator(NBProperties configuration) {
    	super(configuration);
    	SimulationLogger.logInfo("Transport layer", "REMOTE");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new RemoteRoutingTransportLayer(identifier,configuration);
    }

}
