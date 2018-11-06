package ch.ethz.systems.netbench.xpt.megaswitch.server_optic;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;

public class OpticServerGenerator extends NetworkDeviceGenerator{
    IntermediaryGenerator intermediaryGenerator;
    public OpticServerGenerator(NBProperties configuration) {
        super(configuration);
        intermediaryGenerator = new DemoIntermediaryGenerator(configuration);

    }

    public OpticServerGenerator(IntermediaryGenerator intermediaryGenerator, NBProperties configuration) {
        super(configuration);
        this.intermediaryGenerator = intermediaryGenerator;
    }

    @Override
    public NetworkDevice generate(int identifier) {
        return new OpticServerToR(identifier,null,intermediaryGenerator.generate(identifier),configuration);
    }

    @Override
    public NetworkDevice generate(int identifier, TransportLayer server) {
        return new OpticServer(identifier,server,intermediaryGenerator.generate(identifier),configuration);
    }
}
