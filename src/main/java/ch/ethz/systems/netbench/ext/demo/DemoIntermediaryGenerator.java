package ch.ethz.systems.netbench.ext.demo;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;

public class DemoIntermediaryGenerator extends IntermediaryGenerator {

    public DemoIntermediaryGenerator(NBProperties configuration) {
    	super(configuration);
        SimulationLogger.logInfo("Network device intermediary", "DEMO");
    }

    @Override
    public Intermediary generate(int networkDeviceIdentifier) {
        return new DemoIntermediary();
    }

}
