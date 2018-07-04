package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.simple.simpleserver.SimpleServer;

public class MockSimpleServer extends SimpleServer {
    public MockSimpleServer(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }
}
