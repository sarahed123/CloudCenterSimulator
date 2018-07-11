package ch.ethz.systems.netbench.xpt.simple.simpleserver;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.ext.basic.IpPacket;

public class SimpleServer extends NetworkDevice {


    private OutputPort onlyPort;

    /**
     * Constructor of a network device.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Transport layer instance (null, if only router and not a server)
     * @param intermediary   Flowlet intermediary instance (takes care of flowlet support)
     * @param configuration
     */
    public SimpleServer(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
    }

    @Override
    public void receive(Packet genericPacket) {
        IpPacket packet = (IpPacket) genericPacket;
        if (packet.getDestinationId() == this.identifier) {

            // Hand to the underlying server
            this.passToIntermediary(packet); // Will throw null-pointer if this network device does not have a server attached to it

        } else {

            // Forward to the next switch (automatically advances path progress)
            sendToToR(packet);


        }
    }

    @Override
    public void addConnection(OutputPort outputPort) {
        super.addConnection(outputPort);
        this.onlyPort = outputPort;
    }

    private void sendToToR(IpPacket packet) {
        this.onlyPort.enqueue(packet);
    }

    @Override
    protected void receiveFromIntermediary(Packet genericPacket) {
        receive(genericPacket);

    }
}