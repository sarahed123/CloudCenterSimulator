package ch.ethz.systems.netbench.xpt.remotesourcerouting.semi;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;

public class SemiRemoteRoutingSwitchGenerator extends NetworkDeviceGenerator{
    IntermediaryGenerator mIntermediaryGenerator;
    public SemiRemoteRoutingSwitchGenerator(IntermediaryGenerator intermediaryGenerator, NBProperties configuration) {
        super(configuration);
        mIntermediaryGenerator = intermediaryGenerator;
    }

    @Override
    public NetworkDevice generate(int identifier) {
        return new SemiRemoteRoutingSwitch(identifier,null,mIntermediaryGenerator.generate(identifier),configuration);
    }

    @Override
    public NetworkDevice generate(int identifier, TransportLayer server) {
        return new SemiRemoteRoutingSwitch(identifier,server,mIntermediaryGenerator.generate(identifier),configuration);
    }
}
