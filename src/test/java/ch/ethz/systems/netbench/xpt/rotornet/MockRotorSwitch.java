package ch.ethz.systems.netbench.xpt.rotornet;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorSwitch;

public class MockRotorSwitch extends RotorSwitch {
    protected MockRotorSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }
}
