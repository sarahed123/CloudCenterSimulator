package ch.ethz.systems.netbench.xpt.meta_node.v2;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MetaNodeServer extends MetaNodeSwitch {
    int currToRDest = 0;
    /**
     * Constructor of a network device.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Transport layer instance (null, if only router and not a server)
     * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
     * @param configuration
     */
    protected MetaNodeServer(int identifier, TransportLayer transportLayer,  int N, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer,N, intermediary, configuration);
        MNId = -1;
    }

    @Override
    public void receive(Packet genericPacket) {
        TcpPacket tcpPacket = (TcpPacket) genericPacket;
        if(tcpPacket.getDestinationId() == this.identifier) {
            getTransportLayer().receive(tcpPacket);
        } else {
            forwardFromserver(genericPacket);
        }
        return;
    }

    protected void forwardFromserver(Packet genericPacket){
        MNEpochPacket packet = (MNEpochPacket) genericPacket;
        initPacketMetaRouting(packet);

        assert currToRDest >= 0;
        List<Integer> possibilities = destinationToNextSwitch.get(packet.getDestinationId());
        int next = possibilities.get(currToRDest);
        this.targetIdToOutputPort.get(next).enqueue(genericPacket);
        currToRDest++;
        currToRDest%=possibilities.size();
//        Simulator.registerEvent(new Event(480l) {
//            @Override
//            public void trigger() {
//                MetaNodeTransport transport = (MetaNodeTransport) getTransportLayer();
//                transport.pullPacket(genericPacket.getFlowId());
//            }
//        });

        return;
    }

    private void initPacketMetaRouting(MNEpochPacket packet) {
        List<RoutingAlg.RoutingRule> routingRules = rules.get(MNEpochController.getInstance().getMetaNodeId(packet.getDestinationId()));
        RoutingAlg.RoutingRule routingRule = routingRules.get(rand.nextInt(routingRules.size()));
        packet.setRoutingRule(routingRule);

    }

    @Override
    public void receiveFromIntermediary(Packet genericPacket) {
        this.receive(genericPacket);
    }

    public void sendPackets(long flowId, long bytesToSend){
        MetaNodeTransport transport = (MetaNodeTransport) this.getTransportLayer();
        transport.pullPackets(flowId,bytesToSend);
    }

    @Override
    public void setMetaNodeId(int mn) {
        MNId = mn;
    }


    public void invalidateRules(){
//        if(identifier==0){
//            System.out.println(rules);
//        }
        this.rules.clear();
    }

    public void addRule(RoutingAlg.RoutingRule rule){
        List<RoutingAlg.RoutingRule> routingRules = rules.getOrDefault(rule.MNDest, new ArrayList<>());
        routingRules.add(rule);
        rules.put(rule.MNDest,routingRules);

    }



}
