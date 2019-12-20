package ch.ethz.systems.netbench.xpt.dynamic.opera;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.InputPort;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.infrastructure.LinkGenerator;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLinkGenerator;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class OperaController  extends RoutingPopulator {
    protected Map<Integer,NetworkDevice> idToNetworkDevice;
    protected int ToRNum;
    protected int rotorMatchings;
    protected static OperaController sInstance;
    protected ArrayList<OperaRotorSwitch> rotors;
    protected ArrayList<ArrayList<ArrayList<ArrayList<ImmutablePair<Integer,Integer>>>>> routingTables;
    protected ArrayList<ArrayList<ArrayList<ImmutablePair<Integer,Integer>>>> currTable;
    protected int currCycle;
    protected int rotorParallelConfiguration;
    protected long reconfigurationInterval;
    protected long rotorGuardTime;
    protected LinkGenerator linkGenerator;
    protected long reconfigurationExecutionTime;
    protected Map<ImmutablePair<Integer, Integer>, OperaOutputPort> portMap;
    protected int nextRotorToConfigure;
    private long nextReconfigurationTime;

    protected OperaController(NBProperties configuration, Map<Integer, NetworkDevice> idToNetworkDevice) {
        super(configuration);
        this.idToNetworkDevice =idToNetworkDevice;
        rotorParallelConfiguration = configuration.getIntegerPropertyWithDefault("opera_parallel_rotors_to_config",1);
        reconfigurationInterval = configuration.getLongPropertyOrFail("opera_reconfiguration_time_ns");
        reconfigurationExecutionTime = configuration.getLongPropertyWithDefault("opera_reconfiguration_execution_time",0L);
        //rotorGuardTime = configuration.getLongPropertyOrFail("opera_rotor_guard_time");
        routingTables = new ArrayList<>();
        rotors = new ArrayList<>();
        portMap = new HashMap<>();
        ToRNum = configuration.getGraphDetails().getNumTors();
        String rotorsDir = configuration.getPropertyOrFail("opera_rotors_dir_path");
        loadRotorsFromDir(rotorsDir);
        assert(ToRNum % rotors.size() == 0);
        rotorMatchings = ToRNum/rotors.size();
        currCycle = 0;
        nextRotorToConfigure = 0;
        linkGenerator = new PerfectSimpleLinkGenerator(configuration);
        registerReconfigurationEvent();
    }

    private void registerReconfigurationEvent() {
        OperaReconfigurationEvent ore =  new OperaReconfigurationEvent(reconfigurationInterval);
        nextReconfigurationTime = ore.getTime();
        Simulator.registerEvent(ore);
    }

    public static void init(NBProperties conf, Map<Integer,NetworkDevice> idToNetworkDevice){
        sInstance = new OperaController(conf,idToNetworkDevice);
    }
    public static OperaController getInstance(){
        return sInstance;
    }

    public OperaRotorSwitch getRotor(int id){
        return rotors.get(id);
    }


    private void loadRotorsFromDir(String rotorsDir) {
        int rotorsId = 0;
        File dir = new File(rotorsDir);
        while(true){
            try {
                File rotorFile = new File(rotorsDir + "/rotor_" + rotorsId);
                loadRotorFromFile(rotorFile, rotorsId);
            } catch (FileNotFoundException e) {
                break;
            }
            rotorsId++;
        }


    }

    private void loadRotorFromFile(File matchingFile, int rotorNum) throws FileNotFoundException {
        OperaRotorSwitch oprs = new OperaRotorSwitch(rotorNum);
        oprs.loadRotorFromFile(matchingFile);
        rotors.add(oprs);

    }

    @Override
    public void populateRoutingTables() {
        String routinTablesDirPath = configuration.getPropertyOrFail("opera_routing_tables_dir_path");
        int N = configuration.getGraphDetails().getNumTors();
        for(int i = 0; i < N-1; i++){
            File cycleDir = new File(routinTablesDirPath + "/cycle_" + i);
            try {
                routingTables.add(i,loadRoutingTablesFromCycleDir(cycleDir,N));
            } catch (FileNotFoundException e) {
                if(i!=N-2) throw new RuntimeException();

                throw new RuntimeException(e);
            }
        }
        currTable = routingTables.get(0);


    }

    private ArrayList<ArrayList<ArrayList<ImmutablePair<Integer,Integer>>>> loadRoutingTablesFromCycleDir(File cycleDir, int N) throws FileNotFoundException {
        ArrayList<ArrayList<ArrayList<ImmutablePair<Integer,Integer>>>> cycleRoutingTable = new ArrayList<>();
        for (int j = 0; j < ToRNum; j++) {
            File nodeFile = new File(cycleDir.getAbsolutePath() + "/n" + j);
            Scanner reader = new Scanner(nodeFile);
            int currNode = Integer.parseInt(nodeFile.getName().substring(1));
            ArrayList<ArrayList<ImmutablePair<Integer,Integer>>> nodeTable = new ArrayList<>();
            for(int i = 0; i<N; i++){
                if(i==currNode){
                    nodeTable.add(currNode, new ArrayList<>());
                    continue;
                }
                String[] possiblitesLine = reader.nextLine().split(":");
                int destNode = Integer.parseInt(possiblitesLine[0]);
                ArrayList<ImmutablePair<Integer,Integer>> table = parseNextHopPossibilities(possiblitesLine);
                nodeTable.add(destNode,table);
            }
            cycleRoutingTable.add(currNode,nodeTable);
            reader.close();
        }
        return cycleRoutingTable;
    }

    private ArrayList<ImmutablePair<Integer,Integer>> parseNextHopPossibilities(String[] possiblitesLine ) {
        ArrayList<ImmutablePair<Integer,Integer>> nodeRoutingTable = new ArrayList<>();
        String[] possiblitesArray = possiblitesLine[1].split(",");
        for(String possiblity: possiblitesArray){
            String[] rotorDestPair = possiblity.split("-");
            //divide to pair, first is rotor second is nextHop
            nodeRoutingTable.add(new ImmutablePair<>(Integer.parseInt(rotorDestPair[0]),Integer.parseInt(rotorDestPair[1])));
        }

        return nodeRoutingTable;
    }

    public ArrayList<ImmutablePair<Integer,Integer>> nextHops(int source, int dest){
        return routingTables.get(currCycle).get(source).get(dest);
    }


    public void reconfigure() {
        for(OperaRotorSwitch ors: getNextRotors()){
            ors.reconfigure();
        }
        registerReconfigurationEndEvent();
    }

    private void registerReconfigurationEndEvent() {
        OperaReconfigurationEndEvent oree = new OperaReconfigurationEndEvent(reconfigurationExecutionTime);
        Simulator.registerEvent(oree);
    }

    public void onReconfigurationEnd(){
        portMap.clear();
        ArrayList<OperaRotorSwitch> nextReconfiguringRotors = getNextRotors();

        for(OperaRotorSwitch ors: nextReconfiguringRotors){
            ors.start();
        }
        currCycle = (currCycle + nextReconfiguringRotors.size()) % (ToRNum - 1);
        currTable = routingTables.get(currCycle);

        registerReconfigurationEvent();
        for(int id: configuration.getGraphDetails().getTorNodeIds()){
            OperaSwitch sw = (OperaSwitch) idToNetworkDevice.get(id);
            sw.sendPending();
        }
    }

    public boolean hasDirectConnection(int source, int dest) {
        return currTable.get(source).get(dest).get(0).getRight() == dest;
    }

    public OperaOutputPort getOutpuPort(int source, int dest, long hash) throws OperaNoPathException {
        if(!hasDirectConnection(source,dest)){
            throw new OperaNoPathException();
        }

        ImmutablePair<Integer, Integer> pairKey = new ImmutablePair<Integer, Integer>(source,dest);
        OperaOutputPort port = portMap.get(pairKey);
        if(port == null){
            ArrayList<ImmutablePair<Integer,Integer>> possibilities = currTable.get(source).get(dest);
            OperaRotorSwitch rotor  = rotors.get(possibilities.get((int) hash % possibilities.size()).getLeft());
            NetworkDevice sourceDevice = idToNetworkDevice.get(source);
            NetworkDevice destDevice = idToNetworkDevice.get(dest);
            port = new OperaOutputPort(sourceDevice,destDevice,
                                        linkGenerator.generate(sourceDevice,destDevice),
                                        new LinkedList<>(), rotor);

            portMap.put(pairKey,port);
        }
        return port;
    }

    public ArrayList<ImmutablePair<Integer, Integer>> getPossiblities(int source, int dest) {

        return currTable.get(source).get(dest);
    }

    public int getCurrCycle() {
        return currCycle;
    }

    public long getReconfigurationInterval() {
        return reconfigurationInterval;
    }
    
    public ArrayList<OperaRotorSwitch> getNextRotors(){
        ArrayList<OperaRotorSwitch> nextReconfiguringRotors = new ArrayList<>();
        int firstRotor = nextRotorToConfigure;
        int rotorsInParallel = rotorParallelConfiguration;
        for(int i=0; i < rotorsInParallel; i++){
            if(currCycle + i == ToRNum-1){
                return nextReconfiguringRotors;
            }
            int rotorIndex = (currCycle + i) % rotors.size();
            nextReconfiguringRotors.add(rotors.get(rotorIndex));

        }

        return nextReconfiguringRotors;
    }

    public boolean hasPacketPath(int source, int dest, long hash) {
        ArrayList<OperaRotorSwitch> nextReconfiguringRotors = getNextRotors();
        while(true){
            if(source==dest){
                break;
            }
            ArrayList<ImmutablePair<Integer,Integer>> possibilities = getPossiblities(source,dest);
            ImmutablePair<Integer,Integer> nextHopPair = possibilities.get((int) hash % possibilities.size());
            OperaRotorSwitch rotor = rotors.get(nextHopPair.getLeft());
            assert rotor.contains(nextHopPair.getRight());
            if(rotor.isReconfiguring()) return false;
            // if(nextReconfiguringRotors.contains(rotors.get(rotor.getId()))) return false;
            source = nextHopPair.getRight();
        }

        return true;
    }

    public long getRotorGuardTime() {
        return rotorGuardTime;
    }

    public InputPort getInputPort(int identifier, int sourceNetworkDeviceId) {
        NetworkDevice sourceDevice = idToNetworkDevice.get(sourceNetworkDeviceId);
        NetworkDevice destDevice = idToNetworkDevice.get(identifier);
        return new InputPort(destDevice, sourceDevice, linkGenerator.generate(sourceDevice,destDevice));
    }

    public long getRemainingTimeToReconfigure() {
        return nextReconfigurationTime - Simulator.getCurrentTime();
    }

    public long getNextReconfigurationTIme(){
        return nextReconfigurationTime;
    }
}
