package ch.ethz.systems.netbench.xpt.remotesourcerouting.semi;

import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.remotesourcerouting.RemoteSourceRoutingSwitch;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * this class holds a set of precomputed paths which it
 * will check with a centrelized controller for availability.
 * as opposed to requesting a new path all the time.
 */
public class SemiRemoteRoutingSwitch extends RemoteSourceRoutingSwitch {
    protected HashMap<Integer,List<List<Integer>>>  mPathMap;
    public SemiRemoteRoutingSwitch(int identifier, TransportLayer transportLayer, Intermediary intermediary, NBProperties configuration) {
        super(identifier, transportLayer, intermediary, configuration);
        mPathMap = readMap(configuration.getPropertyOrFail("semi_remote_routing_path_dir"));
    }

    protected HashMap<Integer,List<List<Integer>>>  readMap(String path) {
        HashMap<Integer,List<List<Integer>>> map = new HashMap<>();
        List<Integer>[] paths;
        try {
            paths = readListFromFile(path +"/" + this.identifier +"_obj");

        } catch (IOException e) {
            throw new RuntimeException();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }

        for(List<Integer> p : paths){
            if(p==null) continue;
            if(map.get(p.get(p.size()-1))==null){
                map.put(p.get(p.size()-1),new LinkedList<>());
            }
            map.get(p.get(p.size()-1)).add(p);
        }
        return map;
    }

    protected List<Integer>[] readListFromFile(String path) throws IOException, ClassNotFoundException {
        FileInputStream f = new FileInputStream(new File(path));
        ObjectInputStream inputStream = new ObjectInputStream(f);
        List<Integer>[] paths = (List<Integer>[]) inputStream.readObject();
        return paths;
    }

    public List<List<Integer>> getPathsTo(int dest){
        return mPathMap.get(dest);
    }
}
