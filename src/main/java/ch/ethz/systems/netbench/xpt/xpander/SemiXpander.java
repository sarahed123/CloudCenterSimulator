package ch.ethz.systems.netbench.xpt.xpander;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.semi.SemiRemoteRoutingSwitch;
import ch.ethz.systems.netbench.xpt.sourcerouting.exceptions.NoPathException;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.Vertex;

import java.util.List;
import java.util.Map;

public class SemiXpander extends XpanderRouter {
    public SemiXpander(Map<Integer, NetworkDevice> idToNetworkDevice, NBProperties configuration) {
        super(idToNetworkDevice, configuration);
    }

    @Override
    protected Path generatePathFromGraph(int source, int dest) {
        SemiRemoteRoutingSwitch sourceSwitch = (SemiRemoteRoutingSwitch) mIdToNetworkDevice.get(source);
        List<List<Integer>> paths = sourceSwitch.getPathsTo(dest);

        Path ret;
        for(List<Integer> p : paths){
            try{
                ret = checkPath(p);
            }catch(NoPathException e){
                continue;
            }
            return ret;
        }
        return new Path(0);
    }



    private Path checkPath(List<Integer> p) {
        Path ret;
        for(int i = 0;i<mGraphs.length; i++){
            try{
                ret = checkPathInGraph(p,i);
            }catch(NoPathException e){
                continue;
            }
            return ret;
        }
        throw new NoPathException();
    }

    private Path checkPathInGraph(List<Integer> p, int graphIndex) {
        int curr = p.get(0);
        Path ret = new Path(0,graphIndex);
        ret.add(curr);
        for(int i = 0;i < p.size()-1; i++){
            if(mGraphs[graphIndex].getEdgeCapacity(new Vertex(curr),new Vertex(p.get(i+1)))==0){
                throw new NoPathException();
            }
            curr = p.get(i+1);
            ret.addVertex(new Vertex(curr),1);
        }
        return ret;
    }
}