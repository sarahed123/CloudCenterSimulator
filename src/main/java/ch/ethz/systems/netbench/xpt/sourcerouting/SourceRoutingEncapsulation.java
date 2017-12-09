package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import edu.asu.emit.algorithm.graph.Path;

public class SourceRoutingEncapsulation extends IpPacket {

    private final TcpPacket packet;
    private final SourceRoutingPath path;
    private int currentStep;

    public SourceRoutingEncapsulation(TcpPacket packet, SourceRoutingPath path) {
        super(packet.getFlowId(), packet.getSizeBit() - 480L, packet.getSourceId(), packet.getDestinationId(), packet.getTTL());
        this.packet = packet;
        this.path = path;
        this.currentStep = 0;
    }

    public TcpPacket getPacket() {
        return packet;
    }

    public int nextHop() {
        currentStep++;
        return path.getVertexList().get(currentStep).getId();
    }

    @Override
    public void markCongestionEncountered() {
        this.packet.markCongestionEncountered();
    }

	public Path getPath() {
		// TODO Auto-generated method stub
		return path;
	}

}
