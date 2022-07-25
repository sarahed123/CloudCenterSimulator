package ch.ethz.systems.netbench.core.run.traffic;

public class Flow {

    public long startTime;
    public int sourceId;
    public int destId;
    public long syzeByte;
    public long flowId;
    public Flow(long flowId, long startTime, long syzeByte, int sourceId,int destId){
        this.sourceId = sourceId;
        this.destId = destId;
        this.startTime = startTime;
        this.syzeByte = syzeByte;
        this.flowId = flowId;

    }
}
