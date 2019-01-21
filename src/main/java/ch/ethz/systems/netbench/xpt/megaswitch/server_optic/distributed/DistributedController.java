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
	long doubleSuccesses = 0;
	long successes = 0;
	long failures = 0;
	long torNoPath = 0;
	long destNoPath = 0;
	long sourceNoPath = 0;
	int concurrentPaths = 0;
	int  maxConcurrentPaths = 0;
	
    public DistributedController(Map<Integer, NetworkDevice> idToNetworkDevice, NBProperties configuration) {
        super(idToNetworkDevice, configuration);
        System.out.println("running distributed controller with max flows on circuit " + mMaxNumJFlowsOncircuit);

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

    private Set<Integer> getTransmittingSources(int server) {
        Set<Integer> transimtting = mServerTransmitColorsUsed.get(server);
        if(transimtting==null){
            transimtting = new HashSet<>();
            mServerTransmitColorsUsed.put(server,transimtting);
        }
        return transimtting;
    }

    private Set<Integer> getReceivingSources(int server) {
        Set<Integer> receving = mServerReceiveColorsUsed.get(server);
        if(receving==null){
            receving = new HashSet<>();
            mServerReceiveColorsUsed.put(server,receving);
        }
        return receving;
    }
    
//    private void addFlowOnCircuit(int server, boolean incomming) {
//    	if(incomming){
//            if(mRecievingDestinations.getOrDefault(server,0) >= mMaxNumJFlowsOncircuit){
//                throw new NoPathException();
//            }
//            int receiving = mRecievingDestinations.getOrDefault(server,0);
//            receiving++;
//            mRecievingDestinations.put(server,receiving);
//            
//        }else{
//            if(mTransmittingSources.getOrDefault(server,0) >= mMaxNumJFlowsOncircuit){
//                throw new NoPathException();
//            }
//            int sending = mTransmittingSources.getOrDefault(server,0);
//            sending++;
//            mTransmittingSources.put(server,sending);
//            
//        }
//    }
    
//    public void removeFlowOnCircuit(int server, boolean incomming) {
//        if(incomming){
//            int receiving = mRecievingDestinations.get(server);
//            receiving--;
//            assert(receiving>=0);
//            mRecievingDestinations.put(server,receiving);
//
//        }else{
//            int sending = mTransmittingSources.get(server);
//            sending--;
//            assert(sending>=0);
//            mTransmittingSources.put(server,sending);
//
//        }
//    }

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
            assert(getReceivingSources(server).size() <= mGraphs.length);
        }else{
            if(mTransmittingSources.getOrDefault(server,0) >= mMaxNumJFlowsOncircuit){
                throw new NoPathException();
            }
            int sending = mTransmittingSources.getOrDefault(server,0);
            sending++;
            mTransmittingSources.put(server,sending);
            getTransmittingSources(server).add(color);
            assert(getTransmittingSources(server).size() <= mGraphs.length);
        }
        assert(!serverColorAvailable(server,color,incomming));
    }

    public void deallocateServerColor(int server, int color, boolean incomming) {
        if(incomming){
            int receiving = mRecievingDestinations.getOrDefault(server,0);
            receiving--;
            assert(receiving>=0);
            mRecievingDestinations.put(server,receiving);
            getReceivingSources(server).remove(color);
        }else{
            int sending = mTransmittingSources.get(server);
            sending--;
            assert(sending>=0);
            mTransmittingSources.put(server,sending);
            getTransmittingSources(server).remove(color);
        }
        assert(serverColorAvailable(server,color,incomming));

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
    	System.out.println("oldes paths");
        SimulationLogger.printOldestPaths();
        System.out.println("oldest states");
        SimulationLogger.printOldestDistProtocolStates();
        String state = " Allocated " + mAllocateddPathsNum + " Deallocated " + mDeAllocatedPathsNum + "\n";
        state += " max concurrent paths " + maxConcurrentPaths + "\n";
        state += "double success count " + (SimulationLogger.getStatistic("DISTRIBUTED_PATH_DOUBLE_SUCCESS_COUNT") - doubleSuccesses) + "\n";
        state += "success count " + (SimulationLogger.getStatistic("DISTRIBUTED_PATH_SUCCESS_COUNT") - successes) + "\n";
        state += "failure count " + (SimulationLogger.getStatistic("DISTRIBUTED_PATH_FAILURE_COUNT") -failures) + "\n";
        
        state += "tor no path count " + (SimulationLogger.getStatistic("DISTRIBUTED_TOR_NO_PATH") - torNoPath) + "\n";
        state += "dest no path count " + (SimulationLogger.getStatistic("DISTRIBUTED_DEST_ENDPOINT_NO_PATH") - destNoPath) + "\n";
        state += "source no path count " + (SimulationLogger.getStatistic("DISTRIBUTED_SOURCE_ENDPOINT_NO_PATH") -sourceNoPath) + "\n";
        
        state += "ack loss " + SimulationLogger.getStatistic("ACK_PACKETS_DROPPED") + "\n";
        state += "packet loss " + SimulationLogger.getStatistic("PACKETS_DROPPED") + "\n";
        state += "packet loss at source " + SimulationLogger.getStatistic("PACKETS_DROPPED_AT_SOURCE")+ "\n";
        state += "packet loss at conversion " + SimulationLogger.getStatistic("PACKETS_DROPPED_ON_CONVERSION")+ "\n";
        state += "auto teardowns " + SimulationLogger.getStatistic("AUTO_CIRCUIT_TEARDOWN_COUNT")+ "\n";
        state += "packets on circuit " + SimulationLogger.getStatistic("PACKET_ROUTED_THROUGH_CIRCUIT")+ "\n";
        
        doubleSuccesses = SimulationLogger.getStatistic("DISTRIBUTED_PATH_DOUBLE_SUCCESS_COUNT");
        successes = SimulationLogger.getStatistic("DISTRIBUTED_PATH_SUCCESS_COUNT");
        failures = SimulationLogger.getStatistic("DISTRIBUTED_PATH_FAILURE_COUNT");
        
        torNoPath = SimulationLogger.getStatistic("DISTRIBUTED_TOR_NO_PATH");
        destNoPath = SimulationLogger.getStatistic("DISTRIBUTED_DEST_ENDPOINT_NO_PATH");
        sourceNoPath = SimulationLogger.getStatistic("DISTRIBUTED_SOURCE_ENDPOINT_NO_PATH");
        
        mAllocateddPathsNum = 0;
        mDeAllocatedPathsNum = 0;
        return state;
    }

    public void onDeallocation() {
        mDeAllocatedPathsNum++;
        concurrentPaths--;
        assert(concurrentPaths>=0);
    }

    public void onAallocation() {
        mAllocateddPathsNum++;
        concurrentPaths++;
        if(concurrentPaths > maxConcurrentPaths) {
        	maxConcurrentPaths = concurrentPaths;
        }
    }

	public boolean serverHasColor(int server, int color,boolean incomming) {
		// TODO Auto-generated method stub
		 if(incomming){
//           int receiving = mRecievingDestinations.getOrDefault(server,0);
//           receiving--;
//           mRecievingDestinations.put(server,receiving);
           return getReceivingSources(server).contains(color) && mRecievingDestinations.getOrDefault(server,0)<=mMaxNumJFlowsOncircuit;
       }else{
//           int sending = mTransmittingSources.get(server);
//           sending--;
//           mTransmittingSources.put(server,sending);
    	   return getTransmittingSources(server).contains(color)&& mTransmittingSources.getOrDefault(server,0)<=mMaxNumJFlowsOncircuit;
       }
	}
}
