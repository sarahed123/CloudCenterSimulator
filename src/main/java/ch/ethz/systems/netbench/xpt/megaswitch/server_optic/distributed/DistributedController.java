package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
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
//        System.out.println("trying to reserve color " + color + " for server " + server);
        if(!serverColorAvailable(server,color,incomming)){
            throw new NoPathException();
        }
        if(incomming){
            if(mRecievingDestinations.getOrDefault(server,0) >= mMaxNumJFlowsOncircuit){
                throw new NoPathException();
            }
            int receiving = mRecievingDestinations.getOrDefault(server,0);
            receiving++;
            mRecievingDestinations.put(server,receiving);
            getReceivingSources(server).add(color);
        }else{
            if(mTransmittingSources.getOrDefault(server,0) >= mMaxNumJFlowsOncircuit){
                throw new NoPathException();
            }
            int sending = mTransmittingSources.getOrDefault(server,0);
            sending++;
            mTransmittingSources.put(server,sending);
            getTransmittingSources(server).add(color);
        }
    }

    public void deallocateServerColor(int server, int color, boolean incomming) {
        if(incomming){
            int receiving = mRecievingDestinations.getOrDefault(server,0);
            receiving--;
            mRecievingDestinations.put(server,receiving);
            getReceivingSources(server).remove(color);
        }else{
            int sending = mTransmittingSources.get(server);
            sending--;
            mTransmittingSources.put(server,sending);
            getTransmittingSources(server).remove(color);
        }
    }

    public void updateRoutingTable( int id, int prevHop, int nextHop,int color) {
        DistributedSourceRoutingSwitch drs = (DistributedSourceRoutingSwitch) mIdToNetworkDevice.get(id);
        drs.updateForwardingTable(prevHop,nextHop,color);
    }

    public int getWaveLengthNum() {
        return mGraphs.length;
    }

    public String getCurrentState() {
        // TODO Auto-generated method stub
        SimulationLogger.printOldestPaths();
        String state = " Allocated " + mAllocateddPathsNum + " Deallocated " + mDeAllocatedPathsNum;
        mAllocateddPathsNum = 0;
        mDeAllocatedPathsNum = 0;
        return state;
    }

    public void onDeallocation() {
        mDeAllocatedPathsNum++;
    }

    public void onAallocation() {
        mAllocateddPathsNum++;
    }
}
