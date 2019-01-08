package ch.ethz.systems.netbench.xpt.megaswitch.hybrid;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class ConversionUnit {
    NetworkDevice mOptic;
    NBProperties mConf;
    NetworkDevice mOwnDevice;
    long mLinkBandwidth;
    long mEcnThreshold;
    long mMaxQueueSize;
    protected Map<Pair<Integer,Integer>,ConversionPort> mPortMap;
    public ConversionUnit(NBProperties conf, NetworkDevice ownDevice,NetworkDevice opticDevice){
        mOwnDevice = ownDevice;
        mConf = conf;
        mOptic = opticDevice;
        mPortMap = new HashMap<>();
        mLinkBandwidth = opticDevice.getConfiguration().getLongPropertyOrFail("link_bandwidth_bit_per_ns");
        mEcnThreshold = opticDevice.getConfiguration().getLongPropertyOrFail("output_port_ecn_threshold_k_bytes");
        mMaxQueueSize = opticDevice.getConfiguration().getLongPropertyOrFail("output_port_max_queue_size_bytes");

    }

    public void enqueue(int src, int dst, Packet packet){
        ConversionPort port = mPortMap.get(new ImmutablePair<>(src,dst));
        if(port==null){
            port = new ConversionPort(mOwnDevice, mOptic, new Link() {
                @Override
                public long getDelayNs() {
                    return 0;
                }

                @Override
                public long getBandwidthBitPerNs() {
                    return mLinkBandwidth;
                }

                @Override
                public boolean doesNextTransmissionFail(long packetSizeBits) {
                    return false;
                }
            },mMaxQueueSize,mEcnThreshold);
            mPortMap.put(new ImmutablePair<>(src,dst),port);
        }
        port.enqueue(packet);
    }

    public void onFlowFinish(int src, int dst, long flowId){
        if(mPortMap.get(new ImmutablePair<>(src,dst))!=null)
            mPortMap.get(new ImmutablePair<>(src,dst)).onFlowFinished(flowId);
       // mPortMap.remove(new ImmutablePair<>(src,dst));
    }

    public NetworkDevice getOptic() {
        return mOptic;
    }


}
