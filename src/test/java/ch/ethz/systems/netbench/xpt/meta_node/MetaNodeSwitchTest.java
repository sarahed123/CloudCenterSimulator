package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.infrastructure.*;
import ch.ethz.systems.netbench.ext.bare.BarePacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import ch.ethz.systems.netbench.xpt.simple.simpleserver.SimpleServer;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MetaNodeSwitchTest {
    private File tempRunConfig;
    MockMNController instance;
    @Mock
    MockTcpPacket tcpPacket;

    @Before
    public void setup() throws IOException {
        tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write("scenario_topology_file=./src/test/java/ch/ethz/systems/netbench/xpt/meta_node/S50_O0.5_L5_D6.topology\n");
        runConfigWriter.write("link_bandwidth_bit_per_ns=50\n");
        runConfigWriter.write("link_delay_ns=10\n");
        runConfigWriter.close();
        NBProperties conf = new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );
        Simulator.setup(0,conf);
        BaseInitializer initializer = BaseInitializer.getInstance();
        // 1.1) Generate nodes
        initializer.extend(
                0,
                new OutputPortGenerator(conf) {
                    @Override
                    public OutputPort generate(NetworkDevice networkDevice, NetworkDevice networkDevice1, Link link) {
                        return new OutputPort(networkDevice,networkDevice1 , link, new LinkedList<>()) {
                            @Override
                            public void enqueue(Packet packet) {
                                addPacketToQueue(packet);
                            }
                        };
                    }
                },
                new NetworkDeviceGenerator(conf) {
                    @Override
                    public NetworkDevice generate(int i) {
                        return new MockMetaNodeSwitch(i, null, conf.getGraphDetails().getNumNodes(), new Intermediary() {
                            @Override
                            public Packet adaptOutgoing(Packet packet) {
                                return null;
                            }

                            @Override
                            public Packet adaptIncoming(Packet packet) {
                                return null;
                            }
                        }, conf) {


                            @Override
                            public void receiveFromIntermediary(Packet packet) {

                            }
                        };
                    }

                    @Override
                    public NetworkDevice generate(int i, TransportLayer transportLayer) {
                        return new MockMetaNodeSwitch(i, transportLayer, conf.getGraphDetails().getNumNodes(), new Intermediary() {
                            @Override
                            public Packet adaptOutgoing(Packet packet) {
                                return null;
                            }

                            @Override
                            public Packet adaptIncoming(Packet packet) {
                                return null;
                            }
                        },conf);
                    }
                },
                new LinkGenerator() {
                    @Override
                    public Link generate(NetworkDevice networkDevice, NetworkDevice networkDevice1) {
                        return null;
                    }
                },
                new TransportLayerGenerator(conf) {
                    @Override
                    public TransportLayer generate(int i) {
                        return new TransportLayer(i,conf) {
                            @Override
                            protected Socket createSocket(long l, int i, long l1) {
                                return null;
                            }
                        };
                    }
                }
        );
        initializer.createInfrastructure(conf);
        instance = MockMNController.getInstance(conf,initializer.getIdToNetworkDevice());
        instance.populateRoutingTables();

    }

    @Test
    public void getDestinationToMNTest(){
        MockMetaNodeSwitch mnsw = (MockMetaNodeSwitch) instance.getDevice(0);
        List<Integer> dests =  Arrays.asList(5);

        assert mnsw.getDestinationToMNMock(1).equals(dests);
        dests =  Arrays.asList(10);
        assert mnsw.getDestinationToMNMock(2).equals(dests);
    }

    @Test
    public void forwardFromServerTest(){
        MockMetaNodeSwitch mnsw = (MockMetaNodeSwitch) instance.getDevice(25);

        Mockito.doReturn(50).when(tcpPacket).getDestinationId();
        Mockito.doReturn(1000l).when(tcpPacket).getSizeBit();

        mnsw.receive(tcpPacket);
        assert mnsw.getTargetOuputPort(0).getQueueSize() == 1;
        mnsw.receive(tcpPacket);
        assert mnsw.getTargetOuputPort(1).getQueueSize() == 1;
        mnsw.receive(tcpPacket);
        mnsw.receive(tcpPacket);
        mnsw.receive(tcpPacket);
        mnsw.receive(tcpPacket);
        assert mnsw.getTargetOuputPort(0).getQueueSize() == 2;

    }

    @Test
    public void forwardFromSecondHopTest(){
        MockMetaNodeSwitch mnsw = (MockMetaNodeSwitch) instance.getDevice(0);

        Mockito.doReturn(35).when(tcpPacket).getDestinationId();
        Mockito.doReturn(74).when(tcpPacket).getSourceId();

        Mockito.doReturn(1000l).when(tcpPacket).getSizeBit();
        mnsw.receive(tcpPacket);

        assert mnsw.getTargetOuputPort(5).getQueueSize() == 1;


    }

    @After
    public void clear(){
        Simulator.reset(true);
    }
}
