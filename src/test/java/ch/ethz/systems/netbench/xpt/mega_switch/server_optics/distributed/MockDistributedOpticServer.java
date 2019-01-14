package ch.ethz.systems.netbench.xpt.mega_switch.server_optics.distributed;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.DistributedController;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.DistributedOpticServer;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.ReservationPacket;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.semi.SemiRemoteRoutingSwitch;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.LinkedList;
import java.util.List;

public class MockDistributedOpticServer extends DistributedOpticServer {
    static MockDistributedServerOpticsRouter remoteRouter;
    /**
     * Constructor of a network device.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Transport layer instance (null, if only router and not a server)
     * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
     * @param configuration
     */
    protected MockDistributedOpticServer(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }


    public static void setRemoteRouter(MockDistributedServerOpticsRouter remoteRouter) {
        MockDistributedOpticServer.remoteRouter = remoteRouter;
    }

    @Override
    protected void onCircuitEntrance(long flowId) {
        remoteRouter.markFlowRouted(flowId);
    }

    protected void sendReservationPackets(List<Integer> path, int color, TcpPacket packet) {
        super.sendReservationPackets(path,color,packet);
    }

    @Override
    protected void routeThroughtPacketSwitch(TcpPacket packet) {
        super.routeThroughtPacketSwitch(packet);
    }

    @Override
    public void receive(Packet genericPacket) {
        super.receive(genericPacket);
    }

    @Override
    protected List<List<Integer>>  getAvailablePaths(int destToRId) {
        List<Integer> p = new LinkedList<>();
        p.add(0);
        p.add(1);
        List<List<Integer>> ll = new LinkedList<>();
        ll.add(p);
        return ll;
    }

    @Override
    protected RemoteRoutingController getRemoteRouter() {
        return remoteRouter;
    }

    public int allocatedColor(int c) {
        return ((MockDistributedServerOpticsRouter)getRemoteRouter()).allocatedColor(identifier,c);
    }

    public int deallocatedColor(int c) {
        return ((MockDistributedServerOpticsRouter)getRemoteRouter()).deallocatedColor(identifier,c);
    }

    public ReservationPacket getResrvationPacket(int serverDest){
        return mFlowReservation.get(serverDest);
    }
}
