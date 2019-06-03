package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;
import ch.ethz.systems.netbench.xpt.simple.simpleserver.SimpleServer;

public class ElectronicOpticHybridGenerator extends NetworkDeviceGenerator{
    IntermediaryGenerator intermediaryGenerator;
    public ElectronicOpticHybridGenerator(IntermediaryGenerator intermediaryGenerator, NBProperties configuration) {
        super(configuration);
        this.intermediaryGenerator = intermediaryGenerator;
        if(configuration.getBooleanPropertyWithDefault("enable_jumbo_flows",false)){
        	System.out.println("Jumbo flows enabled");
        }
        
    }

    @Override
    public NetworkDevice generate(int identifier) {
        if(configuration.getBooleanPropertyWithDefault("enable_jumbo_flows",false)){
            return new JumboOpticElectronicHybrid(identifier,null,intermediaryGenerator.generate(identifier),configuration);
        }

        return new OpticElectronicHybrid(identifier,null,intermediaryGenerator.generate(identifier),configuration);
    }

    @Override
    public NetworkDevice generate(int identifier, TransportLayer tl) {
        if(configuration.getBooleanPropertyWithDefault("use_dummy_servers",false)){
            return new DummyServer(identifier,tl,intermediaryGenerator.generate(identifier),configuration);
        }
        return new SimpleServer(identifier,tl,intermediaryGenerator.generate(identifier),configuration);
    }
}
