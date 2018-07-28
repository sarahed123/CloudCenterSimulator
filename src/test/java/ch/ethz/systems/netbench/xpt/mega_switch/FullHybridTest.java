package ch.ethz.systems.netbench.xpt.mega_switch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingOutputPortGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingTransportLayerGenerator;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEvent;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitchGenerator;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.MockRemoteRouter;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitchGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpledctcp.SimpleDctcpTransportLayerGenerator;

@RunWith(MockitoJUnitRunner.class)
public class FullHybridTest {
	MockRemoteRouter router;
	@Before
    public void setup() throws IOException {
        // main network
        File tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter.write("scenario_topology_file=example/topologies/simple/simple_n2x2_v1.topology\n");
        runConfigWriter.write("hybrid_circuit_threshold=1000");
        runConfigWriter.close();
        NBProperties conf = new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );
        Simulator.setup(0, conf);

        BaseInitializer initializer = BaseInitializer.getInstance();
        OutputPortGenerator portGen = new OutputPortGenerator(conf) {
            @Override
            public OutputPort generate(NetworkDevice own, NetworkDevice target, Link link) {
                return new OutputPort(own,target,link,new LinkedList<Packet>()) {
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
                return new MockFullHybrid(i,null,ig.generate(i),configuration);
            }

            @Override
            public NetworkDevice generate(int i, TransportLayer transportLayer) {
                return new MockSimpleServer(i,transportLayer,ig.generate(i),configuration);
            }
        };
        LinkGenerator lg = new PerfectSimpleLinkGenerator(0, 10);
        TransportLayerGenerator tlg = new SimpleDctcpTransportLayerGenerator(conf);
        initializer.extend(portGen,ndg,lg,tlg);
        initializer.createInfrastructure(conf);


        //creating network 2, circuit switching:
        File tempRunConfig2 = File.createTempFile("temp-run-config2", ".tmp");
        BufferedWriter runConfigWriter2 = new BufferedWriter(new FileWriter(tempRunConfig2));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter2.write("scenario_topology_file=example/topologies/simple/simple_n2_v2.topology\n");
        runConfigWriter2.write("centered_routing_type=Xpander\n");
        runConfigWriter2.write("network_device_routing=remote_routing_populator\n");
        runConfigWriter2.write("network_type=optic");
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
        HashMap<Integer,NetworkDevice> hm = initializer.createInfrastructure(conf2);
        //RoutingSelector.selectPopulator(hm, conf2);
        router = new MockRemoteRouter(hm, conf2);
        MockFullHybrid.setRemoteRouter(router);
      //creating network 3, packet switching:
        File tempRunConfig3 = File.createTempFile("temp-run-config2", ".tmp");
        BufferedWriter runConfigWriter3 = new BufferedWriter(new FileWriter(tempRunConfig3));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter3.write("scenario_topology_file=example/topologies/simple/simple_n2_v2.topology\n");
        runConfigWriter3.write("network_device_routing=ecmp\n");
        runConfigWriter3.write("network_type=electronic");
        runConfigWriter3.close();
        NBProperties conf3 = new NBProperties(
        		tempRunConfig3.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );


        OutputPortGenerator portGen3 = new OutputPortGenerator(conf3) {
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
        IntermediaryGenerator ig3 = new DemoIntermediaryGenerator(conf3);
        TransportLayerGenerator tlg3 = new TransportLayerGenerator(conf3) {
            @Override
            public TransportLayer generate(int i) {
                return new TransportLayer(i,conf3) {

                    @Override
                    protected Socket createSocket(long l, int i, long l1) {
                        return new SimpleSocket(this,l,this.identifier,i,l1);
                    }
                };
            }
        };

        initializer.extend(2,portGen3,new EcmpSwitchGenerator(new DemoIntermediaryGenerator(conf3),2, conf3),
                lg,tlg3);
        HashMap<Integer,NetworkDevice> hm3 = initializer.createInfrastructure(conf3);
        RoutingSelector.selectPopulator(hm3, conf3).populateRoutingTables();
        
        initializer.finalize();
        tempRunConfig.delete();
        tempRunConfig2.delete();
        tempRunConfig3.delete();
    }
	
	@Test
    public void testSingleFlow(){
        MockSimpleServer source = (MockSimpleServer) BaseInitializer.getInstance().getNetworkDeviceById(2);
        MockSimpleServer dest =(MockSimpleServer) (BaseInitializer.getInstance().getNetworkDeviceById(3));
        FlowStartEvent fse = new FlowStartEvent(0, source.getTransportLayer(), dest.getIdentifier(), 2000);
        MockFullHybrid tor1 = (MockFullHybrid) (BaseInitializer.getInstance().getNetworkDeviceById(0));
        Simulator.registerEvent(fse);
        Simulator.runNs(1000000000);
        assert(tor1.routedThroughCircuit);
        assert(tor1.routedThroughPacketSwitch);
        assert(tor1.recoveredPath);
        assert(router.routed(0,1));
        assert(router.recovered(0,1));
        tor1.reset();
    }
	
	@Test
    public void testSingleToRFlow(){
        MockSimpleServer source = (MockSimpleServer) BaseInitializer.getInstance().getNetworkDeviceById(2);
        MockSimpleServer dest =(MockSimpleServer) (BaseInitializer.getInstance().getNetworkDeviceById(4));
        FlowStartEvent fse = new FlowStartEvent(0, source.getTransportLayer(), dest.getIdentifier(), 2000);
        MockFullHybrid tor1 = (MockFullHybrid) (BaseInitializer.getInstance().getNetworkDeviceById(0));
        Simulator.registerEvent(fse);
        Simulator.runNs(1000000000);
        assert(tor1.routedThroughPacketSwitch);

        tor1.reset();
    }
    
    @After
    public void clear() {
    	Simulator.reset();
    }
}