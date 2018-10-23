package ch.ethz.systems.netbench.xpt.dynamic.device;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;
import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.xpt.dynamic.controller.DynamicDevice;
import ch.ethz.systems.netbench.xpt.megaswitch.Encapsulatable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class DynamicSwitch extends NetworkDevice implements DynamicDevice {


	LinkGenerator mLinkGenerator;
	OutputPortGenerator mOutputPortGenerator;
	private InputPort inputPort;
	private Map<Pair<Integer,Integer>,OutputPort> forwardingTable;

	protected DynamicSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary,
			NBProperties configuration) {
		super(identifier, transportLayer, intermediary, configuration);
		mLinkGenerator = new DynamicLinkGenerator(configuration);
		mOutputPortGenerator = new DynamicOutputPortGenerator(configuration);
		inputPort = new InputPort(this,null,mLinkGenerator.generate(null,null));
		this.forwardingTable = new HashMap<Pair<Integer,Integer>,OutputPort>();

	}

	@Override
	public void receive(Packet genericPacket) {

		ImmutablePair pair = super.getSourceDestinationEncapsulated((IpPacket) genericPacket);
		forwardingTable.get(pair).enqueue(genericPacket);

	}

	@Override
	public void addConnection(NetworkDevice source,NetworkDevice dest, int serverSource, int serverDest) {
		Link link = mLinkGenerator.generate(this, dest);
		forwardingTable.put(new ImmutablePair<>(serverSource,serverDest), mOutputPortGenerator.generate(this, dest, link));
//		((DynamicSwitch)dest).setInputPort(new InputPort(dest,this,link));
	}

	@Override
	public void removeConnection(int serverSource, int serverDest) {
		forwardingTable.remove(new ImmutablePair<>(serverSource,serverDest));
//		targetIdToOutputPort.remove(dest.getIdentifier());
//		((DynamicSwitch)dest).removeInputPort(this.identifier);

	}


	private void removeInputPort(int identifier) {
		// the dynamic switch has only one input port to receive all traffic.
		this.sourceIdToInputPort.remove(identifier);

	}

	@Override
	protected void receiveFromIntermediary(Packet genericPacket) {
		// TODO Auto-generated method stub

	}

	public InputPort getSourceInputPort(int sourceNetworkDeviceId) {
		return inputPort;
	}

	private void setInputPort(InputPort inputPort) {
		this.sourceIdToInputPort.put(inputPort.getSourceNetworkDevice().getIdentifier(), inputPort);
	}


}
