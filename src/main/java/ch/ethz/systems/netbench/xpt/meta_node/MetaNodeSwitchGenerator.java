package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;

public class MetaNodeSwitchGenerator extends NetworkDeviceGenerator {
    IntermediaryGenerator intermediaryGenerator;
    int numNodes;

    public MetaNodeSwitchGenerator(IntermediaryGenerator intermediaryGenerator, int numNodes, NBProperties configuration) {
        super(configuration);
        this.intermediaryGenerator = intermediaryGenerator;
        this.numNodes = numNodes;
    }

    @Override
    public NetworkDevice generate(int identifier) {
        return new MetaNodeSwitch(identifier,null,numNodes,intermediaryGenerator.generate(identifier),configuration);
    }

    @Override
    public NetworkDevice generate(int identifier, TransportLayer server) {
        return new MetaNodeSwitch(identifier,server,numNodes,intermediaryGenerator.generate(identifier),configuration);
    }
}
