package ch.ethz.systems.netbench.xpt.sourcerouting;


import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.MainFromProperties;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingOutputPortGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingTransportLayerGenerator;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediary;
import ch.ethz.systems.netbench.testutility.TestTopologyPortsConstruction;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.demo.DemoIntermediaryGenerator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemoteRouterTest {

    /*
     * Topology:
     * 0---1---2
     *     |   |
     *     |   |
     *     3---4
     */
    private TestTopologyPortsConstruction topology;

    @Mock
    private TcpPacket packet;

    private File tempRunConfig;
    private RemoteRoutingController remoteRouter;
    private Map<Integer, NetworkDevice> idToNetworkDevice;
    @Before
    public void setup() throws IOException {

        // Create temporary run configuration file
        tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write("scenario_topology_file=example/topologies/simple/simple_n5.topology\n");
        runConfigWriter.write("centered_routing_type=Xpander\n");
        runConfigWriter.write("network_device_routing=remote_routing_populator\n");
        
        runConfigWriter.close();

        Simulator.setup(0, new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXPERIMENTAL
        ));
        BaseInitializer b = new BaseInitializer(new RemoteRoutingOutputPortGenerator(), new RemoteSourceRoutingSwitchGenerator( new DemoIntermediaryGenerator(), 5),
        		new PerfectSimpleLinkGenerator(0,10), new RemoteRoutingTransportLayerGenerator()) ;
        b.createInfrastructure();
        idToNetworkDevice = b.getIdToNetworkDevice();
        RoutingSelector.selectPopulator(b.getIdToNetworkDevice());
        this.remoteRouter = RemoteRoutingController.getInstance();
    }

    @After
    public void cleanup() {
        Simulator.reset();
        assertTrue(tempRunConfig.delete());
    }

    @Test
    public void testRemoteRoutingInitilization() {
    	
    	assertTrue(RemoteRoutingController.getInstance() != null);
    }
    
    @Test
    public void switchPath() {
        // to test this we need first to know under which conditions will a switch happen
        
        
    }
    

    @Test
    public void testAddRemovePaths() {
    	RemoteSourceRoutingSwitch device =(RemoteSourceRoutingSwitch) idToNetworkDevice.get(1);
    	remoteRouter.initRoute(1, 4,0);
    	remoteRouter.initRoute(1, 4,1);
    	assert(device.getNextHop(0) != null);
    	assert(device.getNextHop(1) != null);
    	
    	boolean thrown = false;
    	try{
    		remoteRouter.initRoute(1, 4,2);
    		
    	}catch(NoPathException e){
    		thrown = true;
    	}
    	assert(thrown);
    	System.out.println("recovering path one");
    	remoteRouter.recoverPath(0);
    	remoteRouter.initRoute(1, 4,3);
    	thrown = false;
    	try{
    		remoteRouter.initRoute(1, 4,4);
    		
    	}catch(NoPathException e){
    		thrown = true;
    	}
    	assert(thrown);
    	thrown = false;
    	try{
    		remoteRouter.initRoute(1, 4,3);
    		
    	}catch(IllegalArgumentException e){
    		thrown = true;
    	}
    	assert(thrown);
    	System.out.println("reseting all paths");
    	remoteRouter.reset();
    	remoteRouter.initRoute(1, 4,0);
    	remoteRouter.initRoute(1, 4,1);
    	assert(device.getNextHop(0) != null);
    	assert(device.getNextHop(1) != null);
    	
    }


}