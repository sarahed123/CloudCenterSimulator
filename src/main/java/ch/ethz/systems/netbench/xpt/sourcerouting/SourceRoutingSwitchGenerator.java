package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;

public class SourceRoutingSwitchGenerator extends NetworkDeviceGenerator {

    protected final int numNodes;
    protected final IntermediaryGenerator intermediaryGenerator;

    public SourceRoutingSwitchGenerator(IntermediaryGenerator intermediaryGenerator, int numNodes, NBProperties configuration) {
    	super(configuration);
        log();

        // Standard fields
        this.numNodes = numNodes;
        this.intermediaryGenerator = intermediaryGenerator;

    }

    @Override
    public NetworkDevice generate(int identifier) {
        return this.generate(identifier, null);
    }
    
    protected void log(){
    	SimulationLogger.logInfo("Network device", "SOURCE_ROUTING_SWITCH(numNodes=" + numNodes + ")");
    }

    @Override
    public NetworkDevice generate(int identifier, TransportLayer transportLayer) {
        return new SourceRoutingSwitch(identifier, transportLayer, numNodes, intermediaryGenerator.generate(identifier),configuration);
    }

}
