package ch.ethz.systems.netbench.xpt.mega_switch.server_optics;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.OpticServer;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.MockRemoteRouter;

public class MockOpticServer extends OpticServer {
    public boolean routedThroughCircuit = false;
    public boolean routedThroughPacketSwitch = false;
    public boolean recoveredPath = false;
    static RemoteRoutingController remoteRouter;
    protected MockOpticServer(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }

    public static void setRemoteRouter(RemoteRoutingController remoteRouter) {
        MockOpticServer.remoteRouter = remoteRouter;
    }

    public void reset() {
        routedThroughCircuit = false;
        routedThroughPacketSwitch = false;
        recoveredPath = false;
    }

    @Override
    protected void routeThroughCircuit(IpPacket packet, long jumboFlowiId) {
        routedThroughCircuit = true;
        super.routeThroughCircuit(packet, jumboFlowiId);
    }

    @Override
    protected void routeThroughtPacketSwitch(TcpPacket packet) {
        routedThroughPacketSwitch = true;
        super.routeThroughtPacketSwitch(packet);
    }

    @Override
    protected void recoverPath(int source, int dest,int serverSource,int serverDest, long jumboFlowId) {
        recoveredPath = true;
        super.recoverPath(source,dest,serverSource,serverDest,jumboFlowId);
    }

    @Override
    protected RemoteRoutingController getRemoteRouter(){
        return remoteRouter;
    }
}