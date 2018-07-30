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

    public PerfectSimpleLinkGenerator(long delayNs, long bandwidthBitPerNs) {
        this.delayNs = delayNs;
        this.bandwidthBitPerNs = bandwidthBitPerNs;
        SimulationLogger.logInfo("Link", "PERFECT_SIMPLE_LINK(delayNs=" + delayNs + ", bandwidthBitPerNs=" + bandwidthBitPerNs + ")");
    }

    public PerfectSimpleLinkGenerator(NBProperties configuration) {
        super();
        delayNs = configuration.getLongPropertyOrFail("link_delay_ns");
        bandwidthBitPerNs = configuration.getLongPropertyOrFail("link_bandwidth_bit_per_ns");
    }

    @Override
    public Link generate(NetworkDevice fromNetworkDevice, NetworkDevice toNetworkDevice) {
        return new PerfectSimpleLink(delayNs, bandwidthBitPerNs);
    }

}
