package ch.ethz.systems.netbench.xpt.meta_node.v2;

import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;

public class MNEpochPacket extends FullExtTcpPacket {
    RoutingAlg.RoutingRule routingRule;
    public final int sourceMN;
    public final int destMN;
    public MNEpochPacket(long flowId, long dataSizeByte, int sourceId, int destinationId, long sequenceNumber, int sourceMN, int destMN) {
        super(flowId, dataSizeByte, sourceId, destinationId, 100, 80, 80, // TTL, source port, destination port
                sequenceNumber, -1, // Seq number, Ack number
                false, false, false, // NS, CWR, ECE
                false, false, false, // URG, ACK, PSH
                false, false, false, // RST, SYN, FIN
                0, 0 // Window size, Priority
        );

        this.destMN = destMN;
        this.sourceMN = sourceMN;
        routingRule = null;
    }

    public void setRoutingRule(RoutingAlg.RoutingRule routingRule) {
        if(this.routingRule!=null){
            throw new IllegalStateException("routing rull must be set once");
        }
        this.routingRule = routingRule;
    }

    public RoutingAlg.RoutingRule getRoutingRule() {
        return routingRule;
    }

    @Override
    public String toString(){
        return super.toString() + " " + " MNSource " + sourceMN + " MNDest " + destMN;
    }
}
