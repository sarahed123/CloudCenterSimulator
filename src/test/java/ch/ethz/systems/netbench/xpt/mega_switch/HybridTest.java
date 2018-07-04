package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.infrastructure.*;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingOutputPortGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingTransportLayerGenerator;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.ElectronicOpticHybridGenerator;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitchGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpletcp.SimpleTcpTransportLayer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;

@RunWith(MockitoJUnitRunner.class)
public class HybridTest {

    private File tempRunConfig;

    @Before
    public void setup() throws IOException {
        tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter.write("scenario_topology_file=example/topologies/simple/simple_n2x2_v1.topology\n");
        runConfigWriter.close();
        NBProperties conf = new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );
        Simulator.setup(0,conf);
        BaseInitializer initializer = BaseInitializer.getInstance();
        OutputPortGenerator portGen = new OutputPortGenerator(conf) {
            @Override
            public OutputPort generate(NetworkDevice own, NetworkDevice target, Link link) {
                return new OutputPort(own,target,link,new PriorityQueue<Packet>()) {
                    @Override
                    public void enqueue(Packet packet) {
                        guaranteedEnqueue(packet);
                    }
                };
            }
        };
        NetworkDeviceGenerator ndg = new ElectronicOpticHybridGenerator(new DemoIntermediaryGenerator(conf),2,conf);
        LinkGenerator lg = new LinkGenerator() {
            @Override
            public Link generate(NetworkDevice networkDevice, NetworkDevice networkDevice1) {
                return new Link() {
                    @Override
                    public long getDelayNs() {
                        return 0;
                    }

                    @Override
                    public long getBandwidthBitPerNs() {
                        return 10;
                    }

                    @Override
                    public boolean doesNextTransmissionFail(long l) {
                        return false;
                    }
                };
            }
        };
        TransportLayerGenerator tlg = new TransportLayerGenerator(conf) {
            @Override
            public TransportLayer generate(int i) {
               return new SimpleTcpTransportLayer(i,conf);
            }
        };
        initializer.extend(portGen,ndg,lg,tlg);
        initializer.createInfrastructure(conf);
        initializer.extend(new RemoteRoutingOutputPortGenerator(conf),new RemoteSourceRoutingSwitchGenerator(new DemoIntermediaryGenerator(conf),2, conf),
                lg,new RemoteRoutingTransportLayerGenerator(conf));
    }

    @Test
    public void sendOnePacket(){
        
    }
}
