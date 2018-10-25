package ch.ethz.systems.netbench.xpt.mega_switch;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.xpt.megaswitch.hybrid.ConversionUnit;

public class MockConversionUnit extends ConversionUnit {
    public MockConversionUnit(NBProperties conf, NetworkDevice ownDevice, NetworkDevice opticDevice) {
        super(conf, ownDevice, opticDevice);
    }

    public int getNumOfPorts(){
        return mPortMap.size();
    }
}
