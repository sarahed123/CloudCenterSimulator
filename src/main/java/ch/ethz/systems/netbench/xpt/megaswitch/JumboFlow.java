package ch.ethz.systems.netbench.xpt.megaswitch;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;

import java.util.HashMap;

public class JumboFlow {
    long mSizeByte;
    HashMap<Long,Long> mFlowIdToSize;
    int mSource;
    int mDest;
    public JumboFlow(int source,int dest){
        mSizeByte = 0;
        mFlowIdToSize = new HashMap<>();
        mSource = source;
        mDest = dest;
    }

    public long getSize(){
        return mSizeByte;
    }

    public void onPacketDispatch(TcpPacket packet) {
        long flowSize = mFlowIdToSize.getOrDefault(packet.getFlowId(),0l);

        if(flowSize>=packet.getSequenceNumber()){
            return;
        }
        long difference = packet.getSequenceNumber() - flowSize;
        mSizeByte = mSizeByte + difference;
        mFlowIdToSize.put(packet.getFlowId(),packet.getSequenceNumber());
        ;
    }

    public void onFlowFinished(long flowId){
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
}
