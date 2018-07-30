package ch.ethz.systems.netbench.xpt.rotornet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingOutputPortGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingTransportLayerGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorNetController;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorOutputPortGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.rotornet.RotorSwitchGenerator;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitchGenerator;
import com.sun.org.apache.regexp.internal.RE;
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
public class RotorNetTest {

    private MockRotorController controller;

    @Before
    public void setup() throws IOException {


        //creating network 2:
        File tempRunConfig2 = File.createTempFile("temp-run-config2", ".tmp");
        BufferedWriter runConfigWriter2 = new BufferedWriter(new FileWriter(tempRunConfig2));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter2.write("scenario_topology_file=example/topologies/simple/simple_n20_e0.topology\n");
        runConfigWriter2.write("centered_routing_type=rotor_net\n");
        runConfigWriter2.write("network_device_routing=remote_routing_populator\n");
        runConfigWriter2.write("network_type=optic\n");
        runConfigWriter2.write("link_delay_ns=0\n");
        runConfigWriter2.write("output_port_ecn_threshold_k_bytes=50000\n");
        runConfigWriter2.write("output_port_max_queue_size_bytes=50000\n");
        runConfigWriter2.write("max_dynamic_switch_degree=4\n");
        runConfigWriter2.write("link_bandwidth_bit_per_ns=10\n");
        runConfigWriter2.write("rotor_net_reconfiguration_time=20\n");
        runConfigWriter2.write("rotor_net_reconfiguration_interval=180\n");
        runConfigWriter2.write("max_rotor_buffer_size=50000\n");
        runConfigWriter2.close();
        NBProperties conf2 = new NBProperties(
                tempRunConfig2.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );

        Simulator.setup(0, conf2);
        BaseInitializer.getInstance().extend(0,null,new RotorSwitchGenerator(conf2),
                null,null);
        HashMap<Integer,NetworkDevice> hm = BaseInitializer.getInstance().createInfrastructure(conf2);
        controller = new MockRotorController(hm,conf2);
        MockReconfigurationEvent.setController(controller);
        BaseInitializer.getInstance().finalize();
        tempRunConfig2.delete();
    }

    @Test
    public void testCycles(){

        String initialConfiguration =
            "0 [1, 6, 11, 16]\n" +
            "1 [2, 7, 12, 17]\n" +
            "2 [3, 8, 13, 18]\n" +
            "3 [4, 9, 14, 19]\n" +
            "4 [5, 10, 15, 0]\n" +
            "5 [6, 11, 16, 1]\n" +
            "6 [7, 12, 17, 2]\n" +
            "7 [8, 13, 18, 3]\n" +
            "8 [9, 14, 19, 4]\n" +
            "9 [10, 15, 0, 5]\n" +
            "10 [11, 16, 1, 6]\n" +
            "11 [12, 17, 2, 7]\n" +
            "12 [13, 18, 3, 8]\n" +
            "13 [14, 19, 4, 9]\n" +
            "14 [15, 0, 5, 10]\n" +
            "15 [16, 1, 6, 11]\n" +
            "16 [17, 2, 7, 12]\n" +
            "17 [18, 3, 8, 13]\n" +
            "18 [19, 4, 9, 14]\n" +
            "19 [0, 5, 10, 15]\n";

        assert(initialConfiguration.equals(controller.toString()));
        controller.initRoute(0,0,0);

        String firstStep =
                "0 [2, 7, 12, 17]\n" +
                "1 [3, 8, 13, 18]\n" +
                "2 [4, 9, 14, 19]\n" +
                "3 [5, 10, 15, 0]\n" +
                "4 [6, 11, 16, 1]\n" +
                "5 [7, 12, 17, 2]\n" +
                "6 [8, 13, 18, 3]\n" +
                "7 [9, 14, 19, 4]\n" +
                "8 [10, 15, 0, 5]\n" +
                "9 [11, 16, 1, 6]\n" +
                "10 [12, 17, 2, 7]\n" +
                "11 [13, 18, 3, 8]\n" +
                "12 [14, 19, 4, 9]\n" +
                "13 [15, 0, 5, 10]\n" +
                "14 [16, 1, 6, 11]\n" +
                "15 [17, 2, 7, 12]\n" +
                "16 [18, 3, 8, 13]\n" +
                "17 [19, 4, 9, 14]\n" +
                "18 [0, 5, 10, 15]\n" +
                "19 [1, 6, 11, 16]\n";
        assert(firstStep.equals(controller.toString()));
        controller.initRoute(0,0,0);
        controller.initRoute(0,0,0);
        controller.initRoute(0,0,0);
        controller.initRoute(0,0,0);
        assert(initialConfiguration.equals(controller.toString()));
    }

    @Test
    public void testSinglePacket(){

    }

    @Test
    public void testReconfigurationEvents(){
        Simulator.runNs(1000);
        assert(MockReconfigurationEvent.reconfigurationEventCount==5);
    }

    @After
    public void clear(){
        Simulator.reset();
    }
}
