package ch.ethz.systems.netbench.xpt.xpander;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.FlowPathExists;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Vertex;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class SemiXpanderServerOptics extends SemiXpander {
    protected HashMap<Integer,Set<Integer>> mServerTransmitColorsUsed; // which colors are going into some destination
    protected HashMap<Integer,Set<Integer>> mServerReceiveColorsUsed; // which colors are going out of some source
    int mCurrentServerSource; // helper field - should probably not be global
    int mCurrentServerDest; // --- """" ----
    public SemiXpanderServerOptics(Map<Integer, NetworkDevice> idToNetworkDevice, NBProperties configuration) {
        super(idToNetworkDevice, configuration);
        mServerTransmitColorsUsed = new HashMap<>();
        mServerReceiveColorsUsed = new HashMap<>();
        mCurrentServerSource = -1;
        mCurrentServerDest = -1;
    }


    /**
     * specific implementation for server optics setup
     * @param sourceToR
     * @param destToR
     * @param sourceServer
     * @param destServer
     * @param flowId
     */
    @Override
    public void initRoute(int sourceToR,int destToR, int sourceServer, int destServer, long flowId){
        mCurrentServerDest = destServer;
        mCurrentServerSource = sourceServer;
        Set transimtting = mServerTransmitColorsUsed.get(sourceServer);
        if(transimtting==null){
            mServerTransmitColorsUsed.put(sourceServer,new HashSet<>());
        }

        Set receving = mServerReceiveColorsUsed.get(destServer);
        if(receving==null){
            mServerReceiveColorsUsed.put(destServer,new HashSet<>());
        }

        ImmutablePair pair = new ImmutablePair<>(sourceServer,destServer);
        if(mPaths.containsKey(pair)) {
            mFlowIdsOnCircuit.get(pair).add(flowId);
            throw new FlowPathExists(flowId);
        }
//        int sourceToCheck = mIsServerOptics ? sourceServer : sourceToR;
//        int destToCheck = mIsServerOptics ? destServer : destToR;
        if(mTransmittingSources.getOrDefault(sourceServer,0) >= mMaxNumJFlowsOncircuit || mRecievingDestinations.getOrDefault(destServer,0)>=mMaxNumJFlowsOncircuit){
            SimulationLogger.increaseStatisticCounter("TOO_MANY_DESTS_OR_SOURCES_ON_XPANDER_SERVER");
            throw new NoPathException(sourceToR,destToR);
        }
        Path p;
        if(destToR==sourceToR){
            p = allocateTrivialPath(sourceServer,destServer);
//            List<Vertex> trivalPath = new LinkedList<>();
            p.add(destToR);
        }else{
            p = generatePathFromGraph(sourceToR, destToR);
        }




        updateForwardingTables(sourceServer,destServer,p,flowId);
        removePathFromGraph(p);
        onPathAllocation(sourceServer,destServer);
        mAllocateddPathsNum++;
        mPaths.put(new ImmutablePair<>(sourceServer,destServer), p);
        HashSet hs = (HashSet) mFlowIdsOnCircuit.getOrDefault(pair,new HashSet<>());
        hs.add(flowId);
        mFlowIdsOnCircuit.put(pair,hs);
        flowCounter++;
        logRoute(p,sourceToR,destToR,flowId, Simulator.getCurrentTime(),true);

    }

    /**
     * pecific implementation for server optics setup
     * @param sourceToR
     * @param destToR
     * @param serverSource
     * @param serverDest
     * @param flowId
     */
    public void recoverPath(int sourceToR, int destToR, int serverSource, int serverDest,long flowId){

        Pair<Integer, Integer> pair = new ImmutablePair<>(serverSource,serverDest);
        Path p = mPaths.get(pair);

        if(p==null) {
            throw new NoPathException();
        }
        mFlowIdsOnCircuit.get(pair).remove(flowId);
        if(!mFlowIdsOnCircuit.get(pair).isEmpty()) return;

        for(int i=0; i< p.getVertexList().size() - 1;i++){
            Vertex v = p.getVertexList().get(i);
            Vertex u = p.getVertexList().get(i+1);
            mGraphs[p.fromGraph()].increaseCapacity(new ImmutablePair<Integer,Integer>(v.getId(),u.getId()));
            // recover the opisite edge
            //mMainGraph.increaseCapacity(new ImmutablePair<Integer,Integer>(u.getId(),v.getId()));


        }
        mServerTransmitColorsUsed.get(serverSource).remove(p.fromGraph());
        mServerReceiveColorsUsed.get(serverDest).remove(p.fromGraph());
        logRoute(p,p.getVertexList().get(0).getId(),p.getVertexList().get(p.getVertexList().size()-1).getId()
                ,flowId,Simulator.getCurrentTime(),false);
        mPaths.remove(pair);

//		int sourceToCheck = mIsServerOptics ? serverSource : sourceToR;
//		int destToCheck = mIsServerOptics ? serverDest : destToR;
        onPathDeAllocation(serverSource,serverDest);
        mDeAllocatedPathsNum++;
    }


    private Path allocateTrivialPath(int sourceServer, int destServer) {
        for(int i = 0;i<mGraphs.length; i++){
            if(mServerTransmitColorsUsed.get(mCurrentServerSource).contains(i)){
                continue;
            }
            if(mServerReceiveColorsUsed.get(mCurrentServerDest).contains(i)){
                continue;
            }
            Path p = new Path(2,i);
            return p;
        }
        throw new NoPathException();
    }

    protected Path checkPathInGraph(List<Integer> p, int graphIndex) {

        if(mServerTransmitColorsUsed.get(mCurrentServerSource).contains(graphIndex)){

            throw new NoPathException();
        }
        if(mServerReceiveColorsUsed.get(mCurrentServerDest).contains(graphIndex)){
            throw new NoPathException();
        }
        return super.checkPathInGraph(p,graphIndex);
    }

    @Override
    protected void removePathFromGraph(Path p) {
        mServerReceiveColorsUsed.get(mCurrentServerDest).add(p.fromGraph());
        mServerTransmitColorsUsed.get(mCurrentServerSource).add(p.fromGraph());
        super.removePathFromGraph(p);
    }

    /**
     * configures the switches forwarding table according to some path
     * @param source
     * @param dest
     * @param p
     * @param flowId
     */
    @Override
    protected void updateForwardingTables(int source, int dest, Path p, long flowId) {
        super.updateForwardingTables(source,dest,p,flowId);
        RemoteSourceRoutingSwitch rsrs = (RemoteSourceRoutingSwitch) mIdToNetworkDevice.get(p.getLastVertex().getId());
        rsrs.updateForwardingTable(source,dest,dest);
    }
}
