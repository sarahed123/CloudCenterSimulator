package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.random.RandomManager;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.xpt.dynamic.device.DynamicSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

public class RotorSwitch extends DynamicSwitch {
    RotorMap mRotorMap;
    private long mCurrentBufferSize;
    private static long sMaxBufferSize;
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
        if(ipPacket.getSourceId()==this.getIdentifier()){
            try{
                forwardToNextSwitch(ipPacket,ipPacket.getDestinationId());
                return;
            }catch (BufferOverflowException e){

            }catch(NoPathException e){
                sendToRandomDestination(ipPacket);
            }
        }else {
            try {
                forwardToNextSwitch(ipPacket, ipPacket.getDestinationId());
                return;
            } catch (BufferOverflowException e) {

            } catch (NoPathException e) {


            }
        }
        if(hasResources(genericPacket)){
            mBuffer.add(genericPacket);
            mCurrentBufferSize+= genericPacket.getSizeBit();
        }



    }

    void sendPendingData(){
        for(int i = 0; i<mBuffer.size(); i++){
            try{
                IpPacket p = (IpPacket) mBuffer.get(i);
                forwardToNextSwitch(p,p.getDestinationId());
            }catch (BufferOverflowException e){

            }catch(NoPathException e){

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
                    return;
                }catch (BufferOverflowException e){

                }
            }
        }
    }

    private boolean hasResources(Packet genericPacket) {
        return mCurrentBufferSize + genericPacket.getSizeBit() <= sMaxBufferSize;
    }

    static void setMaxBufferSize(long size){
        sMaxBufferSize = size;
    }

    private void forwardToNextSwitch(IpPacket ipPacket,int destination) {
        if(mRotorMap.contains(ipPacket.getDestinationId())){
            RotorOutputPort port = mRotorMap.getOutpurPort(destination);
            port.enqueue(ipPacket);
            return;
        }
        throw new NoPathException();
    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {

    }



    void addConnection(int dest) {
        mRotorMap.put(dest);
    }

    public RotorMap getRotorMap() {
        return mRotorMap;
    }

    public void setRotortMap(RotorMap rotortMap) {
        rotortMap.clearOutputPorts();
        this.mRotorMap = rotortMap;
    }
}
