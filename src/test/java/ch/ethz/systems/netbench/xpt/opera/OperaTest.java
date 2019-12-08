package ch.ethz.systems.netbench.xpt.opera;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.traffic.TrafficPlanner;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediary;
import ch.ethz.systems.netbench.ext.flowlet.UniformFlowletIntermediary;
import ch.ethz.systems.netbench.ext.poissontraffic.PoissonArrivalPlanner;
import ch.ethz.systems.netbench.ext.poissontraffic.flowsize.PFabricWebSearchUpperBoundFSD;
import ch.ethz.systems.netbench.xpt.dynamic.opera.OperaController;
import ch.ethz.systems.netbench.xpt.dynamic.opera.OperaSwitch;
import ch.ethz.systems.netbench.xpt.simple.simpledctcp.SimpleDctcpTransportLayer;
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
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class OperaTest {
    MockOperaController operaController;
    NBProperties configuration;
    long simulationDuration = 100000;
    @Before
    public void setup() throws IOException {
        configuration = new NBProperties(BaseAllowedProperties.EXPERIMENTAL,BaseAllowedProperties.PROPERTIES_RUN,
                                                        BaseAllowedProperties.EXTENSION, BaseAllowedProperties.LOG);
        configuration.setProperty("opera_rotors_dir_path","xpander_generation/opera/testing/n20/rotors");
        configuration.setProperty("opera_direct_circuit_threshold_byte", "100000");
        configuration.setProperty("opera_reconfiguration_time_ns","1000");
        configuration.setProperty("opera_parallel_rotors_to_config","1");
        configuration.setProperty("opera_routing_tables_dir_path","xpander_generation/opera/testing/n20/routing");
        configuration.setProperty("scenario_topology_file","xpander_generation/opera/testing/n20/n20.topology");
        configuration.setProperty("link_delay_ns", "250");
        configuration.setProperty("FLOWLET_GAP_NS", "5000000");
        configuration.setProperty("run_folder_name", "opera");
        configuration.setProperty("run_folder_base_dir", "temp");
        configuration.setProperty("link_bandwidth_bit_per_ns", "100");
        Simulator.setup(1,configuration);

        HashMap<Integer, TransportLayer> transportLayerHashMap = new HashMap<>();
        HashMap<Integer, NetworkDevice> networkDeviceHashMap = new HashMap<>();
        for(int i =0; i<20; i++){
            SimpleDctcpTransportLayer transportLayer = new SimpleDctcpTransportLayer(i,configuration);
            OperaSwitch os = new OperaSwitch(i,transportLayer,new UniformFlowletIntermediary(configuration),configuration);
            transportLayer.setNetworkDevice(os);
            networkDeviceHashMap.put(i,os);
            transportLayerHashMap.put(i,transportLayer);
        }

        // operaController = new MockOperaController(configuration,networkDeviceHashMap);
        // operaController.populateRoutingTables();
        OperaController.init(configuration,networkDeviceHashMap);
        OperaController.getInstance().populateRoutingTables();
        TrafficPlanner planner = new TrafficPlanner(transportLayerHashMap, configuration) {
            @Override
            public void createPlan(long durationNs) {
                Random random = new Random();
                registerFlow(0, 0, 19, 10000000);

                for(long t = 0; t<durationNs; t+=40){
                    int source = Math.abs(random.nextInt()) % 20;
                    int dest = Math.abs(random.nextInt()) % 20;
                    while(dest==source){
                        dest = Math.abs(random.nextInt()) % 20;
                    }
                    registerFlow(t,source,dest,10000);
                }
            }
        };

        planner.createPlan(simulationDuration);

    }

    @Test
    public void testInitialization(){
        //empty test just to check we loaded withoug exceptions
    }

    @Test
    public void testSomeRoutes(){
        assert(operaController.nextHops(6,0).toString().equals("[(0,8), (1,14), (2,7), (3,12)]"));
        assert(operaController.nextHops(7,5).toString().equals("[(3,19)]"));
    }

    @Test
    public void testConfiguringCorrectRotors(){
        for(int i=0; i<30; i++){
            System.out.println(operaController.getCurrCycle());
            System.out.println(operaController.getNextRotors());
            operaController.reconfigure();
            operaController.onReconfigurationEnd();
        }
        System.out.println();
        operaController.reset();
        operaController.setParellelNum(1);
        for(int i=0; i<30; i++){
            System.out.println(operaController.getCurrCycle());
            System.out.println(operaController.getNextRotors());
            operaController.reconfigure();
            operaController.onReconfigurationEnd();
        }
        System.out.println();
        operaController.reset();

        operaController.setParellelNum(3);
        for(int i=0; i<10; i++){
            System.out.println(operaController.getCurrCycle());
            System.out.println(operaController.getNextRotors());
            operaController.reconfigure();
            operaController.onReconfigurationEnd();
        }


    }

    @Test
    public void testOperaRun(){
        Simulator.runNs(simulationDuration);
    }

    @After
    public void reset(){
        Simulator.reset(false);
    }
}
