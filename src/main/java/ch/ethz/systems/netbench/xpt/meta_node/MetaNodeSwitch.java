package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class MetaNodeSwitch extends EcmpSwitch {
    Map<Integer, MetaNodeToken> tokenMap;
    private Random rand;
    private int MNId;
    private int currToRDest;
    /**
     * Constructor for MetaNodeSwitch
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
        currToRDest = 0;
        if(!isServer()) currToRDest = -1;

    }

    protected void  forwardToNextSwitch(Packet genericPacket){
        // Convert to TCP packet
        TcpHeader tcpHeader = (TcpHeader) genericPacket;
        boolean isSecondHop = !targetIdToOutputPort.containsKey(tcpHeader.getSourceId());

        if(this.isServer()){
            forwardFromserver(genericPacket);
            return;
        }
        if(isSecondHop){
            forwardFromSecondHop(genericPacket);
            return;
        }

        MetaNodeToken token = getToken(tcpHeader.getDestinationId());
        List<Integer> possibilities = getDestinationToMN(token.getMNDest());
        int randomNext = possibilities.get(rand.nextInt(possibilities.size()));
        this.targetIdToOutputPort.get(randomNext).enqueue(genericPacket);
        token.nextBytes(tcpHeader.getSizeBit()/(8));

    }

    protected void forwardFromserver(Packet genericPacket){
        TcpHeader tcpHeader = (TcpHeader) genericPacket;
        assert currToRDest >= 0;
        List<Integer> possibilities = destinationToNextSwitch.get(tcpHeader.getDestinationId());
        int next = possibilities.get(currToRDest);
        this.targetIdToOutputPort.get(next).enqueue(genericPacket);
        currToRDest++;
        currToRDest%=possibilities.size();
        return;
    }

    protected void forwardFromSecondHop(Packet genericPacket){
        TcpHeader tcpHeader = (TcpHeader) genericPacket;
        List<Integer> possibilities = destinationToNextSwitch.get(tcpHeader.getDestinationId());
        int randomNext = possibilities.get(rand.nextInt(possibilities.size()));
        this.targetIdToOutputPort.get(randomNext).enqueue(genericPacket);
        return;

    }
    protected List<Integer> getDestinationToMN(int mnDest) {
        List<Integer> possiblilities = new LinkedList<>();
        for(int i : this.targetIdToOutputPort.keySet()){
            NetworkDevice nd = targetIdToOutputPort.get(i).getTargetDevice();
            if(nd.isServer()) continue;
            MetaNodeSwitch mnsw = (MetaNodeSwitch) nd;
            if(mnsw.MNId == mnDest) possiblilities.add(mnsw.identifier);

        }
        if(possiblilities.size() == 0) throw new IllegalStateException("there are no routes to the MNDest " + mnDest);
        return possiblilities;
    }

    private MetaNodeToken getToken(int destinationId) {
        MNController controller = MNController.getInstance();
        int MNSource = this.MNId;
        int MNDest = controller.getMetaNodeId(destinationToNextSwitch.get(destinationId).get(0));
        MetaNodeToken currToken = tokenMap.get(MNDest);
        if(currToken==null){
            currToken = MNController.getInstance().getToken(MNSource, MNDest);
        }
        if(currToken.expired()){
            currToken = MNController.getInstance().getToken(MNSource, MNDest, currToken.bytes);
        }

        return currToken;
    }

    public void setMetaNodeId(int mn) {
        if(MNId!= -1) throw new IllegalStateException("Meta Node Id cannot be set twice");
        MNId = mn;
    }

    public void setRandomizer(Random rand) {
        this.rand = rand;
    }
}
