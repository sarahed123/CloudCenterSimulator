package ch.ethz.systems.netbench.xpt.simple.simpledctcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.DistributedTransportLayer;

public class SimpleDctcpTransportLayerGenerator extends TransportLayerGenerator {

    public SimpleDctcpTransportLayerGenerator(NBProperties configuration) {
    	super(configuration);
    	if(configuration.getBooleanPropertyWithDefault("distributed_protocol_enabled",false)){
    	    System.out.println("Using distributed transport layer");
        }
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "SIMPLE_DCTCP");
    }

    @Override
    public TransportLayer generate(int identifier) {
        if(configuration.getBooleanPropertyWithDefault("distributed_protocol_enabled",false) &&
                configuration.getBooleanPropertyWithDefault("enable_distributed_transport_layer",false)){
            return new DistributedTransportLayer(identifier,configuration);
        }
        return new SimpleDctcpTransportLayer(identifier,configuration);
    }

}
