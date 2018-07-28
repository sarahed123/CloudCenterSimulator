package ch.ethz.systems.netbench.xpt.dynamic.device;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.InputPort;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicDevice;

public class DynamicSwitch extends NetworkDevice implements DynamicDevice {


	LinkGenerator mLinkGenerator;
	OutputPortGenerator mOutputPortGenerator;
	protected DynamicSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary,
			NBProperties configuration) {
		super(identifier, transportLayer, intermediary, configuration);
		mLinkGenerator = new DynamicLinkGenerator(configuration);
		mOutputPortGenerator = new DynamicOutputPortGenerator(configuration);
	}

	@Override
	public void receive(Packet genericPacket) {
		targetIdToOutputPort.get(((IpPacket)genericPacket).getDestinationId()).enqueue(genericPacket);

	}

	@Override
	public void addConnection(NetworkDevice source,NetworkDevice dest) {
		Link link = mLinkGenerator.generate(this, dest);
		targetIdToOutputPort.put(dest.getIdentifier(), mOutputPortGenerator.generate(this, dest, link));
		((DynamicSwitch)dest).setInputPort(new InputPort(dest, this,link));
	}

	@Override
	public void removeConnection(NetworkDevice source,NetworkDevice dest) {
		targetIdToOutputPort.remove(dest.getIdentifier());
		((DynamicSwitch)dest).removeInputPort(this.identifier);

	}


	private void removeInputPort(int identifier) {
		this.sourceIdToInputPort.remove(identifier);

	}

	@Override
	protected void receiveFromIntermediary(Packet genericPacket) {
		// TODO Auto-generated method stub

	}

	private void setInputPort(InputPort inputPort) {
		this.sourceIdToInputPort.put(inputPort.getSourceNetworkDevice().getIdentifier(), inputPort);
	}





}
