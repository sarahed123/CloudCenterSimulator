package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.Simulator;

public class MetaNodeToken {
    int dest;
    long bytes;
    private long timeoutNs;
    private boolean expired;
    private long lastUsed;
    private MetaNodeTokeTimeoutEvent timeoutEvent;
    public MetaNodeToken(long bytes, int dest, long timeoutNs) {
        this.dest = dest;
        this.bytes = bytes;
        this.timeoutNs = timeoutNs;
        timeoutEvent = new MetaNodeTokeTimeoutEvent(timeoutNs,this);
        Simulator.registerEvent(timeoutEvent);
        lastUsed = Simulator.getCurrentTime();

    }

    public boolean expired() {
        return bytes <=0 || this.expired;
    }

    public void setExpired(){
        this.expired = true;
    }

    public void nextBytes(long nextBytes){
        bytes -=nextBytes;
        lastUsed = Simulator.getCurrentTime();

    }

    public long getTimeout(){
        return timeoutNs;
    }

    public long lastUsed() {
        return lastUsed;
    }

    public boolean active() {
        return (Simulator.getCurrentTime() - lastUsed()) <= getTimeout();
    }

    public int getMNDest(){
        return dest;
    }

}
