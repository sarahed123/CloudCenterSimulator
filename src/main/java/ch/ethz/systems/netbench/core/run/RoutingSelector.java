package ch.ethz.systems.netbench.core.run;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitchRouting;
import ch.ethz.systems.netbench.ext.ecmp.ForwarderSwitchRouting;
import ch.ethz.systems.netbench.xpt.sourcerouting.EcmpThenKspNoShortestRouting;
import ch.ethz.systems.netbench.xpt.sourcerouting.EcmpThenKspRouting;
import ch.ethz.systems.netbench.xpt.sourcerouting.KShortestPathsSwitchRouting;

import java.util.Map;

public class RoutingSelector {

    /**
     * Select the populator which populates the routing state in all network devices.
     *
     * Selected using following property:
     * network_device_routing=...
     *
     * @param idToNetworkDevice     Identifier to instantiated network device
     */
    public static RoutingPopulator selectPopulator(Map<Integer, NetworkDevice> idToNetworkDevice) {

        switch (Simulator.getConfiguration().getPropertyOrFail("network_device_routing")) {

            case "single_forward": {
                return new ForwarderSwitchRouting(
                        idToNetworkDevice
                );
            }

            case "ecmp": {
                return new EcmpSwitchRouting(
                        idToNetworkDevice
                );
            }

            /*case "k_paths": {
                return new KPathsSwitchRouting(
                        idToNetworkDevice
                );
            }*/

            case "k_shortest_paths": {
                return new KShortestPathsSwitchRouting(
                        idToNetworkDevice
                );
            }

            case "ecmp_then_k_shortest_paths": {
                return new EcmpThenKspRouting(
                        idToNetworkDevice
                );
            }

            case "ecmp_then_k_shortest_paths_without_shortest": {
                return new EcmpThenKspNoShortestRouting(
                        idToNetworkDevice
                );
            }
            
            case "remote_routing_populator": {
                RemoteRoutingController.initRemoteRouting(Simulator.getConfiguration().getPropertyOrFail("centered_routing_type"),"centered_routing_type",idToNetworkDevice);
                
                return RemoteRoutingController.getInstance();
            }

            default:
                throw new PropertyValueInvalidException(
                        Simulator.getConfiguration(),
                        "network_device_routing"
                );

        }

    }

}
