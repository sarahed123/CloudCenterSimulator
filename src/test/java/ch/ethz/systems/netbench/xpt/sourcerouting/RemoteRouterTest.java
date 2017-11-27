package ch.ethz.systems.netbench.xpt.sourcerouting;


import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.routing.RemoteRoutingController;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediary;
import ch.ethz.systems.netbench.testutility.TestTopologyPortsConstruction;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;

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
        
        RoutingSelector.selectPopulator(null);
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
    public void testAddRemovePaths() {
    	SourceRoutingPath srp1 = remoteRouter.getRoute(1, 4);
    	SourceRoutingPath srp2 = remoteRouter.getRoute(1, 4);
    	System.out.println("path one " + srp1.toString());
    	System.out.println("path two " + srp2.toString());
    	
    	boolean thrown = false;
    	try{
    		System.out.println(remoteRouter.getRoute(1, 4).toString());
    		
    	}catch(NoPathException e){
    		thrown = true;
    	}
    	assert(thrown);
    	System.out.println("recovering path one");
    	remoteRouter.recoverPath(srp1);
    	srp1 = remoteRouter.getRoute(1, 4);
    	System.out.println("path one " + srp1.toString());
    	thrown = false;
    	try{
    		System.out.println(remoteRouter.getRoute(1, 4).toString());
    		
    	}catch(NoPathException e){
    		thrown = true;
    	}
    	assert(thrown);
    	System.out.println("reseting all paths");
    	remoteRouter.reset();
    	srp1 = remoteRouter.getRoute(1, 4);
    	srp2 = remoteRouter.getRoute(1, 4);
    	System.out.println("path one " + srp1.toString());
    	System.out.println("path two " + srp2.toString());
    	
    }


}