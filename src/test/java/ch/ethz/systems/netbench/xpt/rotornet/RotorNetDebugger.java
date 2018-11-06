package ch.ethz.systems.netbench.xpt.rotornet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.NullTrasportLayer;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.infrastructure.*;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEvent;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEventTest;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPortGenerator;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitchGenerator;
import ch.ethz.systems.netbench.xpt.mega_switch.MockFullHybrid;
import ch.ethz.systems.netbench.xpt.mega_switch.MockSimpleServer;
import ch.ethz.systems.netbench.xpt.mega_switch.SimpleSocket;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.OpticElectronicHybrid;
import ch.ethz.systems.netbench.xpt.simple.simpledctcp.SimpleDctcpTransportLayerGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpleserver.SimpleServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

@RunWith(MockitoJUnitRunner.class)
public class RotorNetDebugger {
    private MockRotorController controller;

    @Before
    public void setup() throws IOException {
// main network
        File tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter.write("scenario_topology_file=example/topologies/simple/simple_n333_e12.topology\n");
        runConfigWriter.write("hybrid_circuit_threshold_byte=2\n");
        runConfigWriter.write("link_delay_ns=0\n");
        runConfigWriter.write("run_folder_base_dir=/cs/usr/inonkp/rotor_net_testing\n");
        runConfigWriter.write("run_folder_name=results\n");
        runConfigWriter.write("link_bandwidth_bit_per_ns=400\n");
        runConfigWriter.write("output_port_ecn_threshold_k_bytes=50000\n");
        runConfigWriter.write("output_port_max_queue_size_bytes=500000\n");
        runConfigWriter.write("network_device_routing=remote_routing_populator\n");
        runConfigWriter.write("centered_routing_type=rotor_net\n");

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
                return new EcnTailDropOutputPort(own,target,link,1500000,300000) {

                };
            }
        };
        IntermediaryGenerator ig = new DemoIntermediaryGenerator(conf);
        NetworkDeviceGenerator ndg = new NetworkDeviceGenerator(conf) {
            @Override
            public NetworkDevice generate(int i) {
                return new OpticElectronicHybrid(i,null,ig.generate(i),configuration){
                    @Override
                    protected RemoteRoutingController getRemoteRouter(){
                        return controller;
                    }
                };
            }

            @Override
            public NetworkDevice generate(int i, TransportLayer transportLayer) {
                return new MockSimpleServer(i,transportLayer,ig.generate(i),configuration);
            }
        };
        LinkGenerator lg = new PerfectSimpleLinkGenerator(0, 100);
        TransportLayerGenerator tlg = new SimpleDctcpTransportLayerGenerator(conf);
        initializer.extend(portGen,ndg,lg,tlg);
        initializer.createInfrastructure(conf);

        //creating network 2:
        File tempRunConfig2 = File.createTempFile("temp-run-config2", ".tmp");
        BufferedWriter runConfigWriter2 = new BufferedWriter(new FileWriter(tempRunConfig2));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter2.write("scenario_topology_file=example/topologies/simple/simple_n333_e0.topology\n");


        runConfigWriter2.write("network_type=circuit_switch\n");
        runConfigWriter2.write("link_delay_ns=0\n");
        runConfigWriter2.write("output_port_ecn_threshold_k_bytes=50000\n");
        runConfigWriter2.write("output_port_max_queue_size_bytes=500000\n");
        runConfigWriter2.write("max_dynamic_switch_degree=37\n");
        runConfigWriter2.write("link_bandwidth_bit_per_ns=400\n");
        runConfigWriter2.write("rotor_net_reconfiguration_time_ns=0\n");
        runConfigWriter2.write("rotor_net_reconfiguration_interval_ns=1000\n");
        runConfigWriter2.write("max_rotor_buffer_size_byte=50000000\n");
        runConfigWriter2.write("log_port_utilization=false\n");

        runConfigWriter2.close();
        NBProperties conf2 = new NBProperties(
                tempRunConfig2.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );

        BaseInitializer.getInstance().extend(1, new EcnTailDropOutputPortGenerator(10000000, 1500000, conf2), new NetworkDeviceGenerator(conf2) {
                    @Override
                    public NetworkDevice generate(int i) {
                        return new MockRotorSwitch(i, null, new Intermediary() {
                            @Override
                            public Packet adaptOutgoing(Packet packet) {
                                return packet;
                            }

                            @Override
                            public Packet adaptIncoming(Packet packet) {
                                return packet;
                            }
                        }, conf2);
                    }

                    @Override
                    public NetworkDevice generate(int i, TransportLayer transportLayer) {
                        return new SimpleServer(i, transportLayer, new Intermediary() {
                            @Override
                            public Packet adaptOutgoing(Packet packet) {
                                return packet;
                            }

                            @Override
                            public Packet adaptIncoming(Packet packet) {
                                return packet;
                            }
                        }, conf2);
                    }
                },
                new PerfectSimpleLinkGenerator(conf2), new SimpleDctcpTransportLayerGenerator(conf2));
        HashMap<Integer,NetworkDevice> hm = BaseInitializer.getInstance().createInfrastructure(conf2);
        controller = new MockRotorController(hm,conf2);
        MockRotorMap.setRouter(controller);
        MockReconfigurationEvent.setController(controller);
//        MockFullHybrid.setRemoteRouter(controller);
        tempRunConfig2.delete();

        File tempRunConfig3 = File.createTempFile("temp-run-config2", ".tmp");
        BufferedWriter runConfigWriter3 = new BufferedWriter(new FileWriter(tempRunConfig3));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter3.write("scenario_topology_file=example/topologies/xpander/xpander_n333_d36.topology\n");
        runConfigWriter3.write("network_device_routing=ecmp\n");
        runConfigWriter3.write("network_type=packet_switch\n");
        runConfigWriter3.write("link_delay_ns=0\n");
        runConfigWriter3.write("link_bandwidth_bit_per_ns=400\n");
        runConfigWriter3.write("output_port_ecn_threshold_k_bytes=50000\n");
        runConfigWriter3.write("output_port_max_queue_size_bytes=500000\n");
        runConfigWriter3.close();
        NBProperties conf3 = new NBProperties(
                tempRunConfig3.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );


        OutputPortGenerator portGen3 = new EcnTailDropOutputPortGenerator(5000000,500000,conf3);
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

        initializer.extend(2,portGen3,new EcmpSwitchGenerator(new DemoIntermediaryGenerator(conf3),333, conf3),
                lg,tlg3);
        HashMap<Integer,NetworkDevice> hm3 = initializer.createInfrastructure(conf3);
        RoutingSelector.selectPopulator(hm3, conf3).populateRoutingTables();
        BaseInitializer.getInstance().finalize();
        tempRunConfig3.delete();

    }

    @Test
    public void testFlow(){
        FlowStartEvent fse = new FlowStartEvent(0,BaseInitializer.getInstance().getNetworkDeviceById(333).getTransportLayer(),336,10000000);
        FlowStartEvent fse2 = new FlowStartEvent(0,BaseInitializer.getInstance().getNetworkDeviceById(334).getTransportLayer(),337,10000000);
        FlowStartEvent fse3 = new FlowStartEvent(0,BaseInitializer.getInstance().getNetworkDeviceById(335).getTransportLayer(),338,10000000);

        Simulator.registerEvent(fse);
        Simulator.registerEvent(fse2);
        Simulator.registerEvent(fse3);

        Simulator.runNs(20000000);


    }

    @After
    public void clear(){
        Simulator.reset(false);
    }
}
