package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpleserver.SimpleServer;

public class ElectronicOpticHybridGenerator extends NetworkDeviceGenerator{
    IntermediaryGenerator intermediaryGenerator;
    public ElectronicOpticHybridGenerator(IntermediaryGenerator intermediaryGenerator, int numNodes, NBProperties configuration) {
        super(configuration);
        this.intermediaryGenerator = intermediaryGenerator;
    }

    @Override
    public NetworkDevice generate(int identifier) {
        return new OpticElectronicHybrid(identifier,null,null,configuration);
    }

    @Override
    public NetworkDevice generate(int identifier, TransportLayer tl) {
        return new SimpleServer(identifier,tl,intermediaryGenerator.generate(identifier),configuration);
    }
}
