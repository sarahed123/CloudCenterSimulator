package ch.ethz.systems.netbench.xpt.dynamic.rotornet;

import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import java.lang.Integer;
import java.util.*;

public class RotorMap extends LinkedList<Integer> {
    LinkGenerator mLinkGeneraor;
    RotorSwitch  mCurrentDevice;
    static int sNumOfNodes;
    protected RotorOutputPortGenerator mOutputPortGenerator;
    HashMap<Integer, RotorOutputPort> mOutputPortMap;
    RotorSwitch mOriginalDevice;
    static Random mRnd;
    public RotorMap(RotorOutputPortGenerator rotorOutputPortGenerator, LinkGenerator linkGenerator, RotorSwitch rotorSwitch){
        mOutputPortGenerator = rotorOutputPortGenerator;
        mLinkGeneraor = linkGenerator;
        mOutputPortMap = new HashMap<>();
        mOriginalDevice = rotorSwitch;
    }

    static void setRandom(Random rnd){
        mRnd = rnd;
    }

    public void setCurrentDevice(RotorSwitch rs){
        mCurrentDevice = rs;

    }

    public RotorOutputPort getOutpurPort(int dest){
        if(!this.contains(dest)){
            throw new NoPathException();
        }
        RotorOutputPort port = mOutputPortMap.get(dest);
        if(port == null) {
            RotorNetController controller = (RotorNetController) getController();
            RotorSwitch rs = controller.getDevice(dest);
            port = (RotorOutputPort) mOutputPortGenerator.generate(mCurrentDevice, rs, mLinkGeneraor.generate(mCurrentDevice, rs));
            port.setRotorMap(this);
            mOutputPortMap.put(dest,port);
        }
        return port;
    }

    @Override
    public boolean contains(Object var1) {
        int dest = (Integer) var1;
        if(super.contains(mCurrentDevice.getIdentifier())){

            return dest == ((mCurrentDevice.getIdentifier()+1) % sNumOfNodes);
        }
        return super.contains(var1);
    }

    public RotorOutputPort getRandomOutputPort(){
        return (RotorOutputPort) this.toArray()[mRnd.nextInt(this.size())];
    }


    void put(int dest){
        this.add(dest);
    }


    public RotorSwitch getOriginalDevice() {
        return mOriginalDevice;
    }

    public void clearOutputPorts() {
        mOutputPortMap.clear();
    }

    protected RemoteRoutingController getController(){
        return RemoteRoutingController.getInstance();
    }
}
