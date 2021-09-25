package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.InputPort;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;

public class PerfectSimpleLinkGenerator extends LinkGenerator {

    private final long delayNs;
    private final long bandwidthBitPerNs;
    private int serverLanes = 1;
    private NBProperties configuration;
    public PerfectSimpleLinkGenerator(long delayNs, long bandwidthBitPerNs) {
        super();
        this.delayNs = delayNs;
        this.bandwidthBitPerNs = bandwidthBitPerNs;
        SimulationLogger.logInfo("Link", "PERFECT_SIMPLE_LINK(delayNs=" + delayNs + ", bandwidthBitPerNs=" + bandwidthBitPerNs + ")");
    }
    
    public PerfectSimpleLinkGenerator(long delayNs, long bandwidthBitPerNs, int serverLanes) {
	    this(delayNs, bandwidthBitPerNs);
	    this.serverLanes = serverLanes;
	    SimulationLogger.logInfo("Link", "PERFECT_SIMPLE_LINK(serverLanes=" + serverLanes + ")");

    }

    public PerfectSimpleLinkGenerator(NBProperties configuration) {
	    this(configuration.getLongPropertyOrFail("link_delay_ns"), configuration.getLongPropertyOrFail("link_bandwidth_bit_per_ns"),configuration.getGraphDetails().getServerLanesNum());
        this.configuration = configuration;
    }

    @Override
    public Link generate(NetworkDevice fromNetworkDevice, NetworkDevice toNetworkDevice) {
	    long deviceBW = bandwidthBitPerNs;
	    if(fromNetworkDevice.isServer() || toNetworkDevice.isServer()) {
            deviceBW = this.configuration.getLongPropertyWithDefault("server_link_bandwidth_bit_per_ns", deviceBW);
            deviceBW /= serverLanes;
        }
        return new PerfectSimpleLink(delayNs, deviceBW);
    }

}
