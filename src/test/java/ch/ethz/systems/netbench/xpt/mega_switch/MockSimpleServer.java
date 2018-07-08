package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.demo.DemoPacket;
import ch.ethz.systems.netbench.xpt.simple.simpleserver.SimpleServer;

public class MockSimpleServer extends SimpleServer {
    public MockSimpleServer(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }

    @Override
    public void receive(Packet p){
        MockDemoPacket packet = (MockDemoPacket) p;
        if(packet.getDestinationId() == this.identifier){
            packet.markReceived();
        }
        super.receive(p);
    }
}
