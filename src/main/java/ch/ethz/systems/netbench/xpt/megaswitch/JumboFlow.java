package ch.ethz.systems.netbench.xpt.megaswitch;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;

import java.util.HashMap;

public class JumboFlow {
    private static long sIdCounter = 0;
    long mId;
    long mSizeByte;
    HashMap<Long,Long> mFlowIdToSize;
    int mSource;
    int mDest;
    public JumboFlow(int source,int dest){
        mSizeByte = 0;
        mFlowIdToSize = new HashMap<>();
        mSource = source;
        mDest = dest;
        mId = ++sIdCounter;
    }

    public long getSizeByte(){
        return mSizeByte;
    }

    public void onPacketDispatch(TcpPacket packet) {
        long flowSize = mFlowIdToSize.getOrDefault(packet.getFlowId(),0l);
        long seq = packet.getSequenceNumber() + packet.getDataSizeByte();
        if(flowSize >= seq){
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
        this.mSizeByte -= flowSize;
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
}
