package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;

public class MockedDispatchEvent extends PacketDispatchedEvent {
	OutputPort dispatchPort;
	MockedDispatchEvent(long timeFromNowNs, Packet packet, OutputPort dispatchPort) {
		super(timeFromNowNs, packet, dispatchPort);
		this.dispatchPort = dispatchPort;
	}
	
	protected NetworkDevice getOwnDevice() {
    	return null;
    }
    
    protected OutputPort getOutputPort(NetworkDevice nd){
    	return dispatchPort;
    }

}
