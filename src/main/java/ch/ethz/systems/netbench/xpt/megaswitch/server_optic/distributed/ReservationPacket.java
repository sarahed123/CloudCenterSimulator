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
    List<Integer> mPath;
    int mColor;
    private boolean mSuccess;
    private boolean mFailure;
    private boolean mDealocation;
    private boolean reversed;

    public ReservationPacket(TcpPacket packet, int dest, List<Integer> path, int color,boolean allocationReques) {
        super(
                packet
        );
        ToRdest = dest;
        this.mPath = path;
        this.mColor = color;
        this.mDealocation = !allocationReques;
        this.mServerDest = packet.getDestinationId();
        mServerSource = packet.getSourceId();
        mSuccess = false;
        mFailure = false;
        reversed = false;
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

    public int getColor() {
        return mColor;
    }

    public int getNextHop(int curr) {
        int currIndex = mPath.indexOf(curr);
        if(currIndex + 1 == mPath.size()){
            return getServerDest();
        }
        return mPath.get(currIndex+1);
    }

    public int getServerDest() {
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
        mServerDest = mServerSource;
        reversed = true;
    }

    public boolean idDeAllocation() {
        return mFailure || mDealocation;
    }

    public boolean isReversed() {
        return reversed;
    }
}
