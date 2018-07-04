package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.infrastructure.*;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingOutputPortGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingTransportLayerGenerator;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediary;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.ElectronicOpticHybridGenerator;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.OpticElectronicHybrid;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitchGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpleserver.SimpleServer;
import ch.ethz.systems.netbench.xpt.simple.simpletcp.SimpleTcpTransportLayer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HybridTest {


    @Before
    public void setup() throws IOException {
        // main network
        File tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter.write("scenario_topology_file=example/topologies/simple/simple_n2x2_v1.topology\n");
        runConfigWriter.write("centered_routing_type=Xpander\n");
        runConfigWriter.write("network_device_routing=remote_routing_populator\n");
        runConfigWriter.close();
        NBProperties conf = new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );
        RoutingSelector.selectPopulator(null, conf);

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
        IntermediaryGenerator ig = new DemoIntermediaryGenerator(conf);
        NetworkDeviceGenerator ndg = new NetworkDeviceGenerator(conf) {
            @Override
            public NetworkDevice generate(int i) {
                return new MockOpticalHybrid(i,null,null,configuration);
            }

            @Override
            public NetworkDevice generate(int i, TransportLayer transportLayer) {
                return new MockSimpleServer(i,transportLayer,ig.generate(i),configuration);
            }
        };
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


        //creating network 2:
        File tempRunConfig2 = File.createTempFile("temp-run-config2", ".tmp");
        BufferedWriter runConfigWriter2 = new BufferedWriter(new FileWriter(tempRunConfig2));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter2.write("scenario_topology_file=example/topologies/simple/simple_n2_v2.topology\n");
        runConfigWriter2.close();
        NBProperties conf2 = new NBProperties(
                tempRunConfig2.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );
        initializer.extend(1,new RemoteRoutingOutputPortGenerator(conf2),new RemoteSourceRoutingSwitchGenerator(new DemoIntermediaryGenerator(conf2),2, conf2),
                lg,new RemoteRoutingTransportLayerGenerator(conf2));
        initializer.createInfrastructure(conf2);

        tempRunConfig.delete();
        tempRunConfig2.delete();
    }

    @Test
    public void sendOnePacket(){
        NetworkDevice source = BaseInitializer.getInstance().getNetworkDeviceById(2);
        NetworkDevice dest = spy(BaseInitializer.getInstance().getNetworkDeviceById(3));
        IpPacket p = mock(IpPacket.class);
        when(p.getDestinationId()).thenReturn(3);
        when(p.getFlowId()).thenReturn((long) 0);
        source.receive(p);
    }
}
