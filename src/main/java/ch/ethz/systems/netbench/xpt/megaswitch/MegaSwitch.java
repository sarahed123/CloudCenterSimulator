package ch.ethz.systems.netbench.xpt.megaswitch;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;

import java.util.ArrayList;

public abstract class MegaSwitch extends NetworkDevice{
    protected ArrayList<NetworkDevice> devices;
    public MegaSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary,configuration);
        devices = new ArrayList<NetworkDevice>();
    }

    @Override
    public void extend(NetworkDevice networkDevice, NBProperties conf) {
        devices.add(networkDevice);
    }
}
