package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaNodeSwitch extends EcmpSwitch {
    Map<Pair<Integer,Integer>, MetaNodeToken> tokenMap;
    private int MNId;
    /**
     * Constructor for ECMP switch.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Underlying server transport layer instance (set null, if none)
     * @param n              Number of network devices in the entire network (for routing table size)
     * @param intermediary   Flowlet intermediary instance (takes care of hash adaptation for flowlet support)
     * @param configuration
     */
    public MetaNodeSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, n, intermediary, configuration);
        MNId = -1;
        tokenMap = new HashMap<>();
    }

    protected void  forwardToNextSwitch(Packet genericPacket){
        // Convert to TCP packet
        TcpHeader tcpHeader = (TcpHeader) genericPacket;
        boolean isSecondHop = !targetIdToOutputPort.containsKey(tcpHeader.getSourceId());
        if(this.isServer() || isSecondHop){
            List<Integer> possibilities = destinationToNextSwitch.get(tcpHeader.getDestinationId());
            int randomNext = possibilities.get(rand.nextInt(possibilities.size()));
            this.targetIdToOutputPort.get(randomNext).enqueue(genericPacket);
            return;
        }

        MetaNodeToken token = getToken(tcpHeader.getDestinationId());



    }

    private MetaNodeToken getToken(int destinationId) {
        MNController controller = MNController.getInstance();
        int MNSource = controller.getMetaNodeId(this.identifier);
        int MNDest = controller.getMetaNodeId(destinationToNextSwitch.get(destinationId).get(0));
        MetaNodeToken currToken = tokenMap.get(new ImmutablePair<>(MNSource,MNDest));
        if(currToken==null){
            currToken = MNController.getInstance().getToken(MNSource, MNDest);
        }
        if(currToken.expired()){
            currToken = MNController.getInstance().getToken(MNSource, MNDest, Math.min(currToken.KBytes*2, 1000));
        }

        return currToken;
    }

    public void setMetaNodeId(int mn) {
        if(MNId!= -1) throw new IllegalStateException("Meta Node Id cannot be set twice");
        MNId = mn;
    }
}
