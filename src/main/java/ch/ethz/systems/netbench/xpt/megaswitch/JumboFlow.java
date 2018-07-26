package ch.ethz.systems.netbench.xpt.megaswitch;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;

import java.util.HashMap;

public class JumboFlow {
    long mSize;
    HashMap<Long,Long> mFlowIdToSize;

    public JumboFlow(){
        mSize = 0;
        mFlowIdToSize = new HashMap<>();
    }

    public long getSize(){
        return mSize;
    }

    public void onPacketDispatch(TcpPacket packet) {
        long flowSize = mFlowIdToSize.getOrDefault(packet.getFlowId(),0l);
        if(flowSize>=packet.getSequenceNumber()){
            return;
        }
        long difference = packet.getSequenceNumber() - flowSize;
        mSize = mSize + difference;
        mFlowIdToSize.put(packet.getFlowId(),packet.getSequenceNumber());
    }

    public void onFlowFinished(long flowId){
        mFlowIdToSize.remove(flowId);
    }

    public int getNumFlows() {
        return mFlowIdToSize.size();
    }
}
