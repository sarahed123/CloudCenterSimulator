package ch.ethz.systems.netbench.xpt.mega_switch.server_optics.distributed;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.xpt.megaswitch.server_optic.distributed.DistributedController;

import java.util.Map;

public class MockDistributedServerOpticsRouter extends DistributedController{
    public MockDistributedServerOpticsRouter(Map<Integer, NetworkDevice> idToNetworkDevice, NBProperties configuration) {
        super(idToNetworkDevice, configuration);
    }
}
