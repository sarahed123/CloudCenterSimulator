package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediary;
import ch.ethz.systems.netbench.testutility.TestTopologyPortsConstruction;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
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
public class RemoteSourceRoutingSwitchTest {

    /*
     * Topology:
     * 0---1---2
     *     |   |
     *     |   |
     *     3---4
     */
    private TestTopologyPortsConstruction topology;
    private RemoteRoutingController remoteRouter;
    @Mock()
    private TcpPacket packet;

    private File tempRunConfig;

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
        topology = new TestTopologyPortsConstruction(
                "0-1,1-2,2-4,4-3,3-1"
        );
    }

    @After
    public void cleanup() {
        Simulator.reset();
        assertTrue(tempRunConfig.delete());
    }

    
    @Test
    public void testSingleForward() {

        // Create device with ports
        RemoteSourceRoutingSwitch device = new RemoteSourceRoutingSwitch(0, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(0, 1));


        // Initialize packet for that destination
        when(packet.getDestinationId()).thenReturn(4);

        ArgumentCaptor<SourceRoutingEncapsulation> captor = ArgumentCaptor.forClass(SourceRoutingEncapsulation.class);
        
        // Give device the packet
        device.receiveFromTransportLayer(packet);
        verify(topology.getPort(0, 1), times(1)).enqueue(captor.capture());

        // Make sure the encapsulation is correct
        SourceRoutingEncapsulation encapsulation = captor.getValue();
        assertTrue(packet == encapsulation.getPacket());
        assertEquals(packet.getDestinationId(), encapsulation.getDestinationId());

    }

    @Test
    public void testPassToTransportLayer() {

        TransportLayer transportLayer = mock(TransportLayer.class);

        // Create device 4 with ports 4->2 and 4->3
        RemoteSourceRoutingSwitch device = new RemoteSourceRoutingSwitch(4, transportLayer, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(4, 2));
        device.addConnection(topology.getPort(4, 3));
        

        // Create encapsulation and hop it two times (such that it "arrives" at 4)
        when(packet.getDestinationId()).thenReturn(4);
        SourceRoutingPath srp = remoteRouter.getRoute(1, 4,device,0);
        SourceRoutingEncapsulation encapsulation = new SourceRoutingEncapsulation(packet, srp);
        encapsulation.nextHop();
        encapsulation.nextHop();

        // Give it to the network device at 4
        device.receive(encapsulation);

        // Assert that it receives the packet itself
        verify(transportLayer, times(1)).receive(packet);
        verify(topology.getPort(4, 2), times(0)).enqueue(encapsulation);
        verify(topology.getPort(4, 3), times(0)).enqueue(encapsulation);

    }

    @Test
    public void switchPath() {
        TransportLayer transportLayer = mock(TransportLayer.class);

        // Create device 4 with ports 4->2 and 4->3
        RemoteSourceRoutingSwitch device = new RemoteSourceRoutingSwitch(1, transportLayer, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(1, 2));
        device.addConnection(topology.getPort(1, 3));
        

        // first checkk simple switch
        SourceRoutingPath srp = remoteRouter.getRoute(1, 4,device,0);
        System.out.println(srp.toString());
        device.addPathToDestination(4, srp);
        SourceRoutingPath old = device.getPathsList().get(4).get(0);
        srp = remoteRouter.getRoute(1, 4,device,0);
        System.out.println(srp.toString());
        device.switchPathToDestination(4, old, srp);
        assert(device.getPathsList().get(4).get(0).equals(srp));
        
        //check that an error is thrown on illegal switch
        boolean thrown = false;
        try {
        	device.switchPathToDestination(4, old, srp);
        }catch (IllegalArgumentException e) {
			thrown = true;
		}
        assert(thrown);
        
        // test an additional switch
        srp = remoteRouter.getRoute(1, 4,device,0);
        System.out.println(srp.toString());
        old = device.getPathsList().get(4).get(0);

        device.switchPathToDestination(4, old, srp);
        assert(device.getPathsList().get(4).get(0).equals(srp));
        
        
    }

    @Test
    public void testToString() {

        // Create device with ports
        SourceRoutingSwitch device = new SourceRoutingSwitch(1, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(1, 0));
        device.addConnection(topology.getPort(1, 2));
        device.addConnection(topology.getPort(1, 3));

        System.out.println(device.toString());

    }


}