package edu.asu.emit.algorithm.graph.algorithms;

import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.NBProperties;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.lp.gurobi.GurobiSolver;
import gurobi.GRBException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)

public class LPTest {
    LPWizard lpWizard;
    NBProperties props;
    File tempRunConfig2;
    HashMap<Integer,HashMap<Integer,LPWizardConstraint> > inEqualsOutConstraints;
    HashMap<ImmutablePair<Integer,Integer>,LPWizardConstraint> edgesConstraints;
    @Before
    public void setup() throws IOException {
        lpWizard = new LPWizard();
        lpWizard.setMinProblem(false);
        tempRunConfig2 = File.createTempFile("temp-run-config2", ".tmp");
        BufferedWriter runConfigWriter2 = new BufferedWriter(new FileWriter(tempRunConfig2));
        //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
        runConfigWriter2.write("scenario_topology_file=example/topologies/simple/simple_n3_v2.topology\n");
        runConfigWriter2.close();
        props = new NBProperties(tempRunConfig2.getAbsolutePath(), BaseAllowedProperties.PROPERTIES_RUN);
        inEqualsOutConstraints = new HashMap<>();
//        lpWizard.setAllVariablesInteger();
        edgesConstraints = new HashMap<>();

    }

    @Test
    public void basicTest(){
        lpWizard.plus("2",1d);
        lpWizard.addConstraint("c2",1d,">=").plus("2");
        lpWizard.addConstraint("c2-0",1d,">=").plus("2-0");
        lpWizard.addConstraint("c0-1",1d,">=").plus("0-1");
        lpWizard.addConstraint("c1-3",1d,">=").plus("1-3");
        lpWizard.addConstraint("2e2-0",0d,"=").plus("2").plus("2-0",-1d);
        lpWizard.addConstraint("2-0e0-1",0d,"=").plus("2-0").plus("0-1",-1d);
        lpWizard.addConstraint("0-1e1-3",0d,"=").plus("0-1").plus("1-3", -1d);
        LPSolution solution = lpWizard.solve();
        assert(solution.getObjectiveValue()==1d);
    }

    @Test
    public void twoSourceTest(){
        lpWizard.plus("2",1d);
        lpWizard.plus("4",1d);
        lpWizard.addConstraint("c2",1d,">=").plus("2");
        lpWizard.addConstraint("c4",1d,">=").plus("4");
        lpWizard.addConstraint("c4-0",1d,">=").plus("4-0");
        lpWizard.addConstraint("c2-0",1d,">=").plus("2-0");
        lpWizard.addConstraint("c0-1",1d,">=").plus("0-1");
        lpWizard.addConstraint("c1-3",1d,">=").plus("1-3");
        lpWizard.addConstraint("2e2-0",0d,"=").plus("2").plus("2-0",-1d);
        lpWizard.addConstraint("2-0e0-1",0d,"=").plus("2-0").plus("0-1",-1d);
        lpWizard.addConstraint("2-0+4-0e0-1",0d,"=").plus("2-0").plus("4-0").plus("0-1",-1d);
        lpWizard.addConstraint("0-1e1-3",0d,"=").plus("0-1").plus("1-3", -1d);
        lpWizard.addConstraint("4e4-0",0d,"=").plus("4").plus("4-0",-1d);

        LPSolution solution = lpWizard.solve();
        assert(solution.getObjectiveValue()==1d);
    }

    private void readGraph(){
//        Graph g = props.getGraph();
//
//        GraphDetails graphDetails = props.getGraphDetails();
//        Set<Integer> torIds = graphDetails.TorIds();
//        List<Vertex> vertices = g.getVertexList();
//        for(Vertex v: vertices){
//            if(!torIds.contains(v.getId())) continue;
//
//            List<Vertex> outNeighbours = g.getAdjacentVertices(v);
//            List<Vertex> inNeighbours = g.getPrecedentVertices(v);
//            LPWizardConstraint inEqualsOut = lpWizard.addConstraint(v.getId() + "IeO",0d,"=");
//            for(Vertex u: inNeighbours){
//                inEqualsOut.plus(u.getId() + "-" + v.getId());
//            }
//            for(Vertex u: outNeighbours){
//                // edges cannot carry more then their capacities
//                lpWizard.addConstraint("c" + v.getId() + "-" + u.getId(),g.getEdgeCapacity(v,u),">=").plus(v.getId() + "-" + u.getId());
//                inEqualsOut.plus(v.getId() + "-" + u.getId(),-1d);
//            }
//            inEqualsOutConstraints.put(v.getId(),inEqualsOut);
//        }
    }

    @Test
    public void testGurobi_v0() {
        props.setProperty("scenario_topology_file","example/topologies/simple/simple_n2.topology");
        try {
            List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
            sourceDestPairs.add(new ImmutablePair<>(0,1));
            //readGraph();
            Double solution = GurobiSolver.solve(props,sourceDestPairs,1);
            assert(solution==1);

            GurobiSolver.writeModel("/cs/usr/inonkp/test_gurobi/simple_n2_v0_1c_1.lp");
            GurobiSolver.close();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGurobi_v1() {
        props.setProperty("scenario_topology_file","example/topologies/simple/simple_n3_v2.topology");
        try {
            List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
            sourceDestPairs.add(new ImmutablePair<>(3,7));
            sourceDestPairs.add(new ImmutablePair<>(5,4));
            //readGraph();
            Double solution = GurobiSolver.solve(props,sourceDestPairs,1);
            assert(solution==2);

            GurobiSolver.writeModel("/cs/usr/inonkp/test_gurobi/simple_n3_v2_1c_1.lp");
            GurobiSolver.close();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGurobi_v2() {
        props.setProperty("scenario_topology_file","example/topologies/simple/simple_n3_v2.topology");
        try {
            List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
            sourceDestPairs.add(new ImmutablePair<>(3,4));
            sourceDestPairs.add(new ImmutablePair<>(5,4));
            //readGraph();
            Double solution = GurobiSolver.solve(props,sourceDestPairs,1);
            assert(solution==1);

            GurobiSolver.writeModel("/cs/usr/inonkp/test_gurobi/simple_n3_v2_1c_2.lp");
            GurobiSolver.close();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGurobi_v3() {
        props.setProperty("scenario_topology_file","example/topologies/simple/simple_n2.topology");
        try {
            List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
            sourceDestPairs.add(new ImmutablePair<>(0,1));
            sourceDestPairs.add(new ImmutablePair<>(0,1));
            //readGraph();
            Double solution = GurobiSolver.solve(props,sourceDestPairs,2);
            assert(solution==2);

            GurobiSolver.writeModel("/cs/usr/inonkp/test_gurobi/simple_n2_v0_1c_1.lp");
            GurobiSolver.close();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGurobi_v4() {
        props.setProperty("scenario_topology_file","example/topologies/simple/simple_n3_v2.topology");
        try {
            List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
            sourceDestPairs.add(new ImmutablePair<>(3,4));
            sourceDestPairs.add(new ImmutablePair<>(5,4));
            //readGraph();
            Double solution = GurobiSolver.solve(props,sourceDestPairs,2);
            assert(solution==2);

            GurobiSolver.writeModel("/cs/usr/inonkp/test_gurobi/simple_n3_v2_1c_2.lp");
            GurobiSolver.close();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGurobi_v5() {
        props.setProperty("scenario_topology_file","example/topologies/simple/simple_n3_v2.topology");
        try {
            List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
            sourceDestPairs.add(new ImmutablePair<>(3,4));
            sourceDestPairs.add(new ImmutablePair<>(5,4));
            sourceDestPairs.add(new ImmutablePair<>(5,4));
            //readGraph();
            Double solution = GurobiSolver.solve(props,sourceDestPairs,2);
            assert(solution==2);

            GurobiSolver.writeModel("/cs/usr/inonkp/test_gurobi/simple_n3_v2_1c_2.lp");
            GurobiSolver.close();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGurobi_v6() {
        props.setProperty("scenario_topology_file","example/topologies/simple/simple_n3_v2.topology");
        try {
            List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
            sourceDestPairs.add(new ImmutablePair<>(3,4));
            sourceDestPairs.add(new ImmutablePair<>(5,4));
            sourceDestPairs.add(new ImmutablePair<>(5,4));
            //readGraph();
            Double solution = GurobiSolver.solve(props,sourceDestPairs,3);
            assert(solution==3);

            GurobiSolver.writeModel("/cs/usr/inonkp/test_gurobi/simple_n3_v2_1c_2.lp");
            GurobiSolver.close();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    private void readGraph(List<ImmutablePair<Integer, Integer>> sourceDestPairs) {
        GraphDetails graphDetails = props.getGraphDetails();
        HashMap<Integer,Integer> sourceCounts = new HashMap<>();
        HashMap<Integer,Integer> destCounts = new HashMap<>();
        int i = 0;
        for(ImmutablePair pair: sourceDestPairs){
            readGraph(pair, i);

            addSourceDestConstraints(pair,i);
            lpWizard.plus(i + "_" + pair.getRight());
            i++;
//            int source = (int) pair.getLeft();
//            int dest = (int)  pair.getRight();
//
//            int sourceCount = sourceCounts.getOrDefault(source,0);
//            if(sourceCount==0){
//                int sourceToR = graphDetails.getTorIdOfServer(source);
//                inEqualsOutConstraints.get(sourceToR).plus(source + "-" + sourceToR);
//                lpWizard.addConstraint("c" + source + "-" + sourceToR,1d,">=").plus(source + "-" + sourceToR);
//
//                inEqualsOutConstraints.put(source,lpWizard.addConstraint(source + "IeO",0d,"=").plus(source + "-" + sourceToR,-1d));
//            }
//            lpWizard.addConstraint("c" + "s" + source + "_" + sourceCount,1d,">=").plus("s" + source + "_" + sourceCount);
//            inEqualsOutConstraints.get(source).plus("s" + source + "_" + sourceCount);
//            lpWizard.plus("s" + source + "_" + sourceCount);
//
//
//            int destCount = sourceCounts.getOrDefault(dest,0);
//            if(destCount==0){
//                int destToR = graphDetails.getTorIdOfServer(dest);
//                inEqualsOutConstraints.get(destToR).plus(destToR + "-" + dest,-1d);
//                lpWizard.addConstraint("c" + destToR + "-" + dest,1d,">=").plus(destToR + "-" + dest);
//
//                inEqualsOutConstraints.put(dest,lpWizard.addConstraint(dest + "IeO",0d,"=").plus(destToR + "-" + dest));
//            }
//            lpWizard.addConstraint("c" + "d" + dest + "_" + destCount,1d,">=").plus("d" + dest + "_" + destCount);
//            inEqualsOutConstraints.get(dest).plus("d" + dest + "_" + destCount,-1d);
//
//            lpWizard.addConstraint("s" + source + "_" + sourceCount + "d" + dest + "_" + destCount,0d, "=").plus("s" + source + "_" + sourceCount).plus("d" + dest + "_" + destCount,-1d);
//
//            sourceCount++;
//            sourceCounts.put(source,sourceCount);
//            destCount++;
//            sourceCounts.put(dest,destCount);

        }
    }

    private void addSourceDestConstraints(ImmutablePair<Integer, Integer> pair, int id) {
//        System.out.println("source-dest constraint id " + id +" source " + pair.getLeft());
        int source = pair.getLeft();
        int dest = pair.getRight();
        lpWizard.addConstraint("c" + id  + "_" + source,1,">=").plus(id + "_" + source);
        lpWizard.addConstraint("pos" + id  + "_" + source,0,"<=").plus(id + "_" + source);

        inEqualsOutConstraints.get(id).get(source).plus(id + "_" + source);
        inEqualsOutConstraints.get(id).get(dest).plus(id + "_" + dest,-1d);
    }

    private void readGraph(ImmutablePair pair, int id) {
        Graph g = props.getGraph();

        GraphDetails graphDetails = props.getGraphDetails();
//        Set<Integer> torIds = graphDetails.TorIds();
        List<Vertex> vertices = g.getVertexList();
        for(Vertex v: vertices){
//            if(!torIds.contains(v.getId())) continue;

            List<Vertex> outNeighbours = g.getAdjacentVertices(v);
            List<Vertex> inNeighbours = g.getPrecedentVertices(v);
            LPWizardConstraint inEqualsOut = lpWizard.addConstraint("c" + id + "_" + v.getId() + "IeO",0d,"=");
            for(Vertex u: inNeighbours){
                inEqualsOut.plus(id + "_" + u.getId() + "-" + v.getId());
            }
            for(Vertex u: outNeighbours){
                // edges cannot carry more then their capacities
                if(g.getEdgeCapacity(v,u) == 1){
                    LPWizardConstraint constraint = edgesConstraints.getOrDefault(new ImmutablePair<>(v.getId(),u.getId()),null);
                    if(constraint == null){
                        constraint = lpWizard.addConstraint("c" + v.getId() + "-" + u.getId(),1,">=");
                        edgesConstraints.put(new ImmutablePair<>(v.getId(),u.getId()),constraint);
                    }
                    constraint.plus(id + "_" + v.getId() + "-" + u.getId());
                    // traffic must be positive
                    lpWizard.addConstraint("pos" + id + "_" +  v.getId() + "-" + u.getId(),  0, "<=").plus(id + "_" +  v.getId() + "-" + u.getId());
                }

                inEqualsOut.plus(id + "_" + v.getId() + "-" + u.getId(),-1d);
            }
            HashMap constraintMap = inEqualsOutConstraints.getOrDefault(id,new HashMap<>());
            constraintMap.put(v.getId(),inEqualsOut);
//            System.out.println("read graph id " + id +" source " + pair.getLeft());
            inEqualsOutConstraints.put(id,constraintMap);
        }
    }

    @After
    public void finish(){
        tempRunConfig2.delete();
    }
}
