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
        RoutingSelector.selectPopulator(null);
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


        // Add path to a certain destination - not necessary because the remote
        // controller will hold the path.
        /*SourceRoutingPath path = new SourceRoutingPath();
        path.add(1);
        path.add(3);
        path.add(4);
        device.addPathToDestination(4, path);*/

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
        
        
        // Add path to a certain destination - not necessary because the remote
        // controller will hold the path.
        // Add path from 1 to 4
        /*SourceRoutingPath path = new SourceRoutingPath();
        path.add(1);
        path.add(3);
        path.add(4);*/

        // Create encapsulation and hop it two times (such that it "arrives" at 4)
        when(packet.getDestinationId()).thenReturn(4);
        SourceRoutingPath srp = remoteRouter.getRoute(1, 4);
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
    public void testPacketArrival() {

        // Create device with ports
        RemoteSourceRoutingSwitch source = new RemoteSourceRoutingSwitch(0, null, 5, new IdentityFlowletIntermediary());
        source.addConnection(topology.getPort(0, 1));
        
        RemoteSourceRoutingSwitch target = Mockito.spy(new RemoteSourceRoutingSwitch(1, null, 5, new IdentityFlowletIntermediary()));
        target.addConnection(topology.getPort(1, 0));

        when(packet.getDestinationId()).thenReturn(1);
        
        source.receiveFromTransportLayer(packet);
        verify(target,times(1)).receive(packet);

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