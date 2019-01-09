package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.xpander.SemiXpanderServerOptics;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;
import edu.asu.emit.algorithm.graph.Vertex;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DistributedController extends SemiXpanderServerOptics
{

    public DistributedController(Map<Integer, NetworkDevice> idToNetworkDevice, NBProperties configuration) {
        super(idToNetworkDevice, configuration);
    }

    public void initRoute(int sourceToR,int destToR, int sourceServer, int destServer, long flowId){

    }

    public void recoverPath(int sourceToR, int destToR, int serverSource, int serverDest,long flowId){

    }

    public long checkEdgeCapacity(int source, int nextHop, int color) {
        return mGraphs[color].getEdgeCapacity(new Vertex(source),new Vertex(nextHop));
    }

    public void decreaseEdgeCapacity(int source, int nextHop, int color) {
        mGraphs[color].decreaseCapacity(new ImmutablePair<>(source,nextHop));
    }

    public void increaseEdgeCapacity(int source, int nextHop, int color) {
        mGraphs[color].increaseCapacity(new ImmutablePair<>(source,nextHop));
    }

    public boolean serverColorAvailable(int server, int color, boolean incomming) {

        if(incomming){
            return !getReceivingSources(server).contains(color);
        }else{
            return !getTransmittingSources(server).contains(color);
        }
    }

    private Set getTransmittingSources(int server) {
        Set transimtting = mServerTransmitColorsUsed.get(server);
        if(transimtting==null){
            transimtting = new HashSet<>();
            mServerTransmitColorsUsed.put(server,transimtting);
        }
        return transimtting;
    }

    private Set getReceivingSources(int server) {
        Set receving = mServerReceiveColorsUsed.get(server);
        if(receving==null){
            receving = new HashSet<>();
            mServerReceiveColorsUsed.put(server,receving);
        }
        return receving;
    }

    public void reserveServerColor(int server, int color, boolean incomming) {
        if(incomming){
            getReceivingSources(server).add(color);
        }else{
            getTransmittingSources(server).add(color);
        }
    }

    public void deallocateServerColor(int server, int color, boolean incomming) {
        if(incomming){
            getReceivingSources(server).remove(color);
        }else{
            getTransmittingSources(server).remove(color);
        }
    }

    public void updateRoutingTable(int identifier, int serverSourceId, int serverDestId, int nextHop) {
        RemoteSourceRoutingSwitch rsrs = (RemoteSourceRoutingSwitch) mIdToNetworkDevice.get(identifier);
        rsrs.updateForwardingTable(serverSourceId,serverDestId,nextHop);
    }
}
