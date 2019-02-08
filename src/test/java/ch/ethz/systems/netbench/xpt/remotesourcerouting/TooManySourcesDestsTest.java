package ch.ethz.systems.netbench.xpt.remotesourcerouting;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.core.run.routing.remote.LightOutputPortGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingTransportLayerGenerator;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TooManySourcesDestsTest {
    NBProperties conf;
    private File tempRunConfig;
    private RemoteRoutingController remoteRouter;
    private Map<Integer, NetworkDevice> idToNetworkDevice;
    @Before
    public void setup() throws IOException {

        // Create temporary run configuration file
        tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write("scenario_topology_file=example/topologies/simple/simple_n6_v1.topology\n");
        runConfigWriter.write("centered_routing_type=Xpander\n");
        runConfigWriter.write("dijkstra_vertex_shuffle=false\n");
        runConfigWriter.write("circuit_wave_length_num=14\n");
        runConfigWriter.write("max_num_flows_on_circuit=2\n");
        runConfigWriter.write("network_device_routing=remote_routing_populator\n");
        runConfigWriter.write("output_port_max_queue_size_bytes=150000\n");
        runConfigWriter.write("output_port_ecn_threshold_k_bytes=30000\n");

        runConfigWriter.close();
        conf = new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXPERIMENTAL);
        Simulator.setup(0, conf);
        BaseInitializer.getInstance().extend(new LightOutputPortGenerator(conf), new RemoteSourceRoutingSwitchGenerator( new DemoIntermediaryGenerator(conf), 5, conf),
                new PerfectSimpleLinkGenerator(0,10), new RemoteRoutingTransportLayerGenerator(conf));
        BaseInitializer b = BaseInitializer.getInstance() ;
        b.createInfrastructure(conf);
        b.finalize();
        idToNetworkDevice = b.getIdToNetworkDevice();
        RoutingSelector.selectPopulator(idToNetworkDevice,conf);
        this.remoteRouter = RemoteRoutingController.getInstance();
    }


    @Test
    public void testMultiWaveLength() {
        boolean thrown = false;
        remoteRouter.initRoute(5,0,0);
        remoteRouter.initRoute(5,1,1);
        try{
            remoteRouter.initRoute(5,2,2);
        }catch (NoPathException e){
            thrown = true;
        }

        assert(thrown);
        remoteRouter.recoverPath(5,1,1);
        remoteRouter.initRoute(5,2,2);

    }

    @After
    public void clear(){
        Simulator.reset();
        assertTrue(tempRunConfig.delete());
    }
}
