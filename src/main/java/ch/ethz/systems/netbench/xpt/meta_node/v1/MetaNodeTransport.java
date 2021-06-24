package ch.ethz.systems.netbench.xpt.meta_node.v1;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;

public class MetaNodeTransport extends TransportLayer {
    public MetaNodeTransport(int identifier, NBProperties configuration) {
        super(identifier, configuration);
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new MetaNodeSocket(this,flowId,identifier,destinationId,flowSizeByte);
    }

    public void createReceiverSocket(long flowId, int identifier, int destinationId, long flowSizeByte) {

//            flowIdToReceiver.put(packet.getFlowId(), this);
        Socket socket = new MetaNodeSocket(this,flowId,identifier,destinationId,flowSizeByte);
        socket.markAsReceiver();
        flowIdToSocket.put(flowId, socket);
    }

    public class MetaNodeSocket extends Socket{
        ServerToken serverToken;
        long tokenBytes = 0;
        public final long backoffTime = 5000l;
        /**
         * Create a socket. By default, it should be the receiver.
         * Use the {@link #start() start} method to make the socket a
         * sender and initiate the communication handshake.
         *
         * @param transportLayer Transport layer
         * @param flowId
         * @param sourceId       Source network device identifier
         * @param destinationId  Target network device identifier
         * @param flowSizeByte   Size of the flow in bytes
         */
        public MetaNodeSocket(TransportLayer transportLayer, long flowId, int sourceId, int destinationId, long flowSizeByte) {
            super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
            serverToken = ServerToken.dummyToken();
        }


        @Override
        public void start() {
            MNController.getInstance().createReceiverSocket(flowId,identifier,destinationId,flowSizeByte);
            sendData();
        }

        protected void initLogger() {
            // Initialize logger
            if(sourceId!=this.transportLayer.getNetworkDevice().getIdentifier()){
                super.initLogger();
            }
        }

        private void sendData() {
            serverToken.invalidate();
            try {
                serverToken = MNController.getInstance().getServerToken(sourceId, destinationId, flowId);
                tokenBytes = serverToken.bytes;
                long tokenSize = tokenBytes;
                while(tokenSize > 0){
                    long tcpPacketSize = 1380l;
                    long headers = 120l;
                    long packetSize = Math.min(tcpPacketSize, tokenSize);
                    packetSize = Math.min(packetSize, remainderToConfirmFlowSizeByte);
                    remainderToConfirmFlowSizeByte-=packetSize;

                    MetaNodePacket mnPacket = new MetaNodePacket(flowId,packetSize,
                            sourceId,destinationId,100,-1,-1,
                            remainderToConfirmFlowSizeByte,-1,
                            false,false,false,false,false,false,false,false, false, 0l, 0l);
                    mnPacket.setMetaNodeToken(serverToken.getMetaNodeToken());
                    transportLayer.send(mnPacket);
//                    System.out.println("sending");
//                    System.out.println(tcpPacket);
                    tokenSize -= (packetSize + headers);
                    if(remainderToConfirmFlowSizeByte==0){
                        MNController.getInstance().releaseServerTokenOutgoing(serverToken);
                        transportLayer.cleanupSockets(flowId);
                        return;
                    }
                }
            } catch (ServerOverloadedException e) {
//                System.out.println("server overloaded");
            }finally {

            }

            Simulator.registerEvent(new Event(!serverToken.isExpired() ? serverToken.expiryTime : backoffTime) {
                @Override
                public void trigger() {
                    if(!serverToken.isExpired())
                        MNController.getInstance().releaseServerTokenOutgoing(serverToken);
                    sendData();
                }
            });

        }

        @Override
        public void handle(Packet genericPacket) {
            TcpPacket tcpPacket = (TcpPacket) genericPacket;

            logSender(tcpPacket.getDataSizeByte());
            remainderToConfirmFlowSizeByte-=tcpPacket.getDataSizeByte();
            if(isAllFlowConfirmed()){
                onAllFlowConfirmed();

            }
        }

        @Override
        public void logSender(long bytes){
            if(!isReceiver) return;
            super.logSender(bytes);
        }



    }


}
