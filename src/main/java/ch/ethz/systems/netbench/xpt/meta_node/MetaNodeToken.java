package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.Simulator;

public class MetaNodeToken {
    int dest;
    long KBytes;
    private long currKbytes;
    private long timeoutNs;
    private boolean expired;
    private long lastUsed;
    private MetaNodeTokeTimeoutEvent timeoutEvent;
    public MetaNodeToken(long KBytes, int dest, long timeoutNs) {
        this.dest = dest;
        this.KBytes = KBytes;
        this.currKbytes = 0;
        this.timeoutNs = timeoutNs;
        timeoutEvent = new MetaNodeTokeTimeoutEvent(timeoutNs,this);
        Simulator.registerEvent(timeoutEvent);
        lastUsed = Simulator.getCurrentTime();

    }

    public boolean expired() {
        return KBytes<=0 || this.expired;
    }

    public void setExpired(){
        this.expired = true;
    }

    public void update(long nextKBytes){
        KBytes-=nextKBytes;
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
}
