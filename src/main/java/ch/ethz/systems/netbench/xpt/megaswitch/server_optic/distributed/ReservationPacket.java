package ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;

import java.util.Collections;
import java.util.List;

public class ReservationPacket extends TcpPacket {
    private int mServerDest;
    private int mServerSource;
    int ToRdest;
    long flowid;
    List<Integer> mPath;
    private int mSourceToR;
    private boolean mSuccess;
    private boolean mFailure;
    private boolean mDealocation;
    private boolean reversed;
    private long mId = -1;
    private boolean mFinishedDealloc;

    public ReservationPacket(TcpPacket packet, int sourceToR,int dest, List<Integer> path, int color,boolean allocationReques) {
        super(
                packet
        );
        ToRdest = dest;
        this.mSourceToR = sourceToR;
        this.mPath = path;
        this.mColor = color;
        this.mDealocation = !allocationReques;
        this.mServerDest = packet.getDestinationId();
        mServerSource = packet.getSourceId();
        mSuccess = false;
        mFailure = false;
        flowid = packet.getFlowId();
        reversed = false;
        mFinishedDealloc = false;
    }

    @Override
    public Encapsulatable encapsulate(int newSource, int newDestination) {
        return null;
    }

    @Override
    public Encapsulatable deEncapsualte() {
        return null;
    }

    @Override
    public int getDestinationId() {
        return ToRdest;
    }

    public boolean isSuccess() {
        return mSuccess;
    }


    public int getNextHop(int curr) {
        int currIndex = mPath.indexOf(curr);
        if(currIndex + 1 == mPath.size()){
            return getServerDest();
        }
        return mPath.get(currIndex+1);
    }

    public int getServerDest(int ToR) {
        if(ToR==mSourceToR){
            return mServerSource;
        }
        else{
            return mServerDest;
        }
    }

    @Override
    public long getFlowId(){
        return flowid;
    }

    public int getServerDest() {
        return reversed ? mServerSource : mServerDest;
    }

    public int getOriginalServerDest() {
        return mServerDest;
    }

    public void markSuccess() {
        mSuccess = true;
    }

    public void markFailure() {
        mFailure = true;
    }

    public void reverse() {

        Collections.reverse(mPath);

//        mServerDest = mServerSource;
        reversed = !reversed;
    }

    public boolean idDeAllocation() {
        return mFailure || mDealocation;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setDeallocation() {
        this.mDealocation = true;
    }

    @Override
    public long getDataSizeByte() {
        return 10;
    }
    
    @Override
    public long getSizeBit() {
        return 80;
    }

    @Override
    public String toString(){
        return "Reservation packet " + " server dest " + mServerDest + " serverSource " + mServerSource + " isSuccess " + isSuccess() +
                " isFailure " + mFailure + " isDeallocation " + mDealocation +" color " + getColor() + " reversed " + reversed + " path " + mPath.toString();
    }

    public int getSourceToR() {
        return mSourceToR;
    }

    public boolean isFailure() {
        return mFailure;
    }

    public List<Integer> getPath(){
        return mPath;
    }

    public void setId(long id){
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void onFinishDeallocation() {
        mFinishedDealloc = true;
    }

    public boolean finishedDealloc() {
        return mFinishedDealloc;
    }
}