package ch.ethz.systems.netbench.xpt.rotornet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.NullTrasportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEvent;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEventTest;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPortGenerator;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
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

@RunWith(MockitoJUnitRunner.class)
public class RotorNetDebugger {
    private MockRotorController controller;

    @Before
    public void setup() throws IOException {


        //creating network 2:
        File tempRunConfig2 = File.createTempFile("temp-run-config2", ".tmp");
        BufferedWriter runConfigWriter2 = new BufferedWriter(new FileWriter(tempRunConfig2));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter2.write("scenario_topology_file=example/topologies/simple/simple_n20_e0.topology\n");
        runConfigWriter2.write("centered_routing_type=rotor_net\n");
        runConfigWriter2.write("run_folder_base_dir=/cs/usr/inonkp/rotor_net_testing\n");
        runConfigWriter2.write("run_folder_name=results\n");
        runConfigWriter2.write("network_device_routing=remote_routing_populator\n");
        runConfigWriter2.write("network_type=optic\n");
        runConfigWriter2.write("link_delay_ns=0\n");
        runConfigWriter2.write("output_port_ecn_threshold_k_bytes=50000\n");
        runConfigWriter2.write("output_port_max_queue_size_bytes=500000\n");
        runConfigWriter2.write("max_dynamic_switch_degree=4\n");
        runConfigWriter2.write("link_bandwidth_bit_per_ns=100\n");
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

        Simulator.setup(1, conf2);
        BaseInitializer.getInstance().extend(0, new EcnTailDropOutputPortGenerator(10000000, 10000000, conf2), new NetworkDeviceGenerator(conf2) {
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
                        return new MockRotorSwitch(i, transportLayer, new Intermediary() {
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
        BaseInitializer.getInstance().finalize();
        tempRunConfig2.delete();
    }

    @Test
    public void testFlow(){
        FlowStartEvent fse = new FlowStartEvent(0,BaseInitializer.getInstance().getNetworkDeviceById(0).getTransportLayer(),19,2000000);
        Simulator.registerEvent(fse);
        Simulator.runNs(2000000);


    }

    @After
    public void clear(){
        Simulator.reset(false);
    }
}
