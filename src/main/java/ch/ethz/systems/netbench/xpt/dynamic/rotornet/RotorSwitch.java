package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.device.DynamicSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;

import java.util.Collections;
import java.util.LinkedList;

public class RotorSwitch extends DynamicSwitch {
    protected RotorMap mRotorMap;
    protected long mCurrentBufferSize;
    private static long sMaxBufferSizeBit;
    private LinkedList<Packet> mBuffer;
    /**
     * Constructor of a network device.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Transport layer instance (null, if only router and not a server)
     * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
     * @param configuration
     */
    protected RotorSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
        mRotorMap = new RotorMap(new RotorOutputPortGenerator(configuration) , new PerfectSimpleLinkGenerator(configuration),this);
        mRotorMap.setCurrentDevice(this);
        mBuffer = new LinkedList<>();
        mCurrentBufferSize = 0;

    }

    @Override
    public void receive(Packet genericPacket) {
        IpPacket ipPacket = (IpPacket) genericPacket;
        boolean deadline = false;
        boolean nopath = false;
        if(ipPacket.getSourceId()==this.getIdentifier()){
            try{
                forwardToNextSwitch(ipPacket,ipPacket.getDestinationId());
                SimulationLogger.increaseStatisticCounter("ROTOR_PACKET_DIRECT_FORWARD");
                return;
            }catch (NoPathException e){
                nopath = true;
                sendToRandomDestination(ipPacket);
                return;
            }catch (ReconfigurationDeadlineException e){
                deadline = true;
                sendToRandomDestination(ipPacket);
                return;
            }
        }else {
            try {
                forwardToNextSwitch(ipPacket, ipPacket.getDestinationId());
                return;
            } catch (ReconfigurationDeadlineException e) {
                deadline = true;
            } catch (NoPathException e) {
                nopath = true;
            }
        }
        if(hasResources(genericPacket)){
            String counter_name = "ROTOR_PACKET_BUFFERED";
            if(nopath) counter_name = "ROTOR_PACKET_BUFFERED_NO_PATH";
            if(deadline) counter_name = "ROTOR_PACKET_BUFFERED_DEADLINE";
            SimulationLogger.increaseStatisticCounter(counter_name);
            addToBuffer(genericPacket);

        }



    }

    private void addToBuffer(Packet genericPacket) {
        mBuffer.addLast(genericPacket);
        mCurrentBufferSize+= genericPacket.getSizeBit();
    }

    private Packet popFromBuffer(){
        Packet p = mBuffer.pop();
        mCurrentBufferSize -= p.getSizeBit();
        return p;
    }

    void sendPendingData(){
        int size = mBuffer.size();
        for(int i = 0; i<size; i++){
            IpPacket p = null;
            try{
                p = (IpPacket) popFromBuffer();

                forwardToNextSwitch(p,p.getDestinationId());
            }catch (ReconfigurationDeadlineException | NoPathException e){
                addToBuffer(p);
            }
        }
    }

    private void sendToRandomDestination(IpPacket ipPacket) {
        Collections.shuffle(mRotorMap,mRotorMap.mRnd);
        for(int i = 0;i<mRotorMap.size();i++){
            RotorSwitch target = (RotorSwitch) mRotorMap.getOutpurPort(mRotorMap.get(i)).getTargetDevice();
            if(target.hasResources(ipPacket)){
                try{
                    forwardToNextSwitch(ipPacket,target.getIdentifier());
                    SimulationLogger.increaseStatisticCounter("ROTOR_PACKET_RANDOM_FORWARD");

                    return;
                }catch (ReconfigurationDeadlineException e){

                }
            }
        }
        throw new NoPathException();
    }

    private boolean hasResources(Packet genericPacket) {
        return mCurrentBufferSize + genericPacket.getSizeBit() <= sMaxBufferSizeBit;
    }

    static void setMaxBufferSizeByte(long sizeByte){
        sMaxBufferSizeBit = sizeByte*8L;
    }

    private void forwardToNextSwitch(IpPacket ipPacket,int destination) {
        if(mRotorMap.contains(destination)){
            RotorOutputPort port = mRotorMap.getOutpurPort(destination);
            port.enqueue(ipPacket);
            return;
        }
        throw new NoPathException();
    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {

    }



    protected void addConnection(int dest) {
        mRotorMap.put(dest);
    }

    public RotorMap getRotorMap() {
        return mRotorMap;
    }

    public void setRotortMap(RotorMap rotortMap) {
        rotortMap.clearOutputPorts();
        this.mRotorMap = rotortMap;
        mRotorMap.setCurrentDevice(this);
    }
}
