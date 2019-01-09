package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.JumboFlow;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.OpticServer;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.semi.SemiRemoteRoutingSwitch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DistributedOpticServer extends OpticServer {
    enum State{
        NO_CIRCUIT,
        IN_PROCESS,
        HAS_CIRCUIT
    }
    HashMap<Long,State> mFlowState;
    static Random rand =  Simulator.selectIndependentRandom("semit_remote_paths_randomizer");
    final int NUM_PATH_TO_RANDOMIZE;
    final int NUM_COLORS_TO_RANDOMIZE;
    /**
     * Constructor of a network device.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Transport layer instance (null, if only router and not a server)
     * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
     * @param configuration
     */
    protected DistributedOpticServer(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
        mFlowState = new HashMap<>();
        NUM_PATH_TO_RANDOMIZE = configuration.getIntegerPropertyOrFail("num_paths_to_randomize");
        NUM_COLORS_TO_RANDOMIZE = configuration.getIntegerPropertyOrFail("num_colors_to_randomize");

    }

    @Override
    protected void routeThroughCircuit(IpPacket packet, long flowId){
        switch(mFlowState.get(flowId)){

            case NO_CIRCUIT:
                initRoute(packet, flowId);
            case IN_PROCESS:
                routeThroughtPacketSwitch((TcpPacket)packet);
                break;
            case HAS_CIRCUIT:
                JumboFlow jumbo = getJumboFlow(packet.getSourceId(),packet.getDestinationId());
                this.conversionUnit.enqueue(this.identifier,packet.getDestinationId(),packet);
                jumbo.onCircuitEntrance();
                SimulationLogger.increaseStatisticCounter("PACKET_ROUTED_THROUGH_CIRCUIT");
                break;
        }
    }

    protected void initRoute(IpPacket packet, long jumboFlowiId) {
        SemiRemoteRoutingSwitch srrs = (SemiRemoteRoutingSwitch)this.optic;
        int destToRId = getTransportLayer().getNetworkDevice().getConfiguration().getGraphDetails().getTorIdOfServer(packet.getDestinationId());
        int sourceToRId = getTransportLayer().getNetworkDevice().getConfiguration().getGraphDetails().getTorIdOfServer(getIdentifier());
        List<List<Integer>> paths = srrs.getPathsTo(destToRId);
        List<Integer> path;
        for(int i = 0; i < NUM_PATH_TO_RANDOMIZE; i++){
            if(destToRId == sourceToRId){
                path = new LinkedList<>();
                path.add(sourceToRId);
            }else{
                int p = rand.nextInt() % paths.size();
                path = paths.get(p);

            }
            int c = rand.nextInt() % optic.getConfiguration().getIntegerPropertyOrFail("circuit_wave_length_num");
            sendReservationPackets(path,c,(TcpPacket)packet);
        }

    }

    private void sendReservationPackets(List<Integer> path, int color, TcpPacket packet) {
        ReservationPacket rp = new  ReservationPacket(packet,path.get(0),path,color,true);
        routeThroughtPacketSwitch(rp);
    }
}
