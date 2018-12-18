package ch.ethz.systems.netbench.xpt.megaswitch;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;

import java.util.HashMap;
import java.util.Set;

public class JumboFlow {
    private static long sIdCounter = 0;
    long mId;
    long mSizeByte;
    HashMap<Long,Long> mFlowIdToSize;
    int mSource;
    int mDest;
    boolean onCircuit;
    public JumboFlow(int source,int dest){
        mSizeByte = 0;
        mFlowIdToSize = new HashMap<>();
        mSource = source;
        mDest = dest;
        mId = ++sIdCounter;
        onCircuit = false;
    }

    public long getSizeByte(){
        return mSizeByte;
    }

    public void onPacketDispatch(TcpPacket packet) {
        if(packet.isACK()){
            return;
        }
        long flowSize = mFlowIdToSize.getOrDefault(packet.getFlowId(),0l);
        long seq = packet.getSequenceNumber() + packet.getDataSizeByte();
        if(flowSize > seq){ // should this be >=?
            return;
        }

        long difference = seq - flowSize;
        mSizeByte = mSizeByte + difference;
        mFlowIdToSize.put(packet.getFlowId(),seq);

    }

    public void onFlowFinished(long flowId){
        if(!mFlowIdToSize.containsKey(flowId)){
            return;
        }
        long flowSize = mFlowIdToSize.remove(flowId);
        //this.mSizeByte -= flowSize;
    }

    public int getNumFlows() {
        return mFlowIdToSize.size();
    }

	public boolean isTrivial() {
		// TODO Auto-generated method stub
		return mSource==mDest;
	}

	public long getId(){
        return mId;
    }

    public void onCircuitEntrance() {
	    if(!onCircuit){
	        onCircuit = true;

        }
	    Set<Long> flowIds = mFlowIdToSize.keySet();
	    for(long id : flowIds){
            SimulationLogger.registerFlowOnCircuit(id);
        }
    }

    public boolean isOnCircuit() {
        return onCircuit;
    }

    public boolean hasFlow(long flowId) {
	    return mFlowIdToSize.containsKey(flowId);
    }

    public long getSizeByte(long flowId) {
	    if(!mFlowIdToSize.containsKey(flowId)) return 0;
	    return mFlowIdToSize.get(flowId);
    }

    public void onCircuitEntrance(long flowId) {
	    SimulationLogger.registerFlowOnCircuit(flowId);
    }
}
