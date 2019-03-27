package edu.asu.emit.algorithm.lp.scpsolver;

import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.NBProperties;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import java.util.Random;

public class ScpSolver {
    static LPWizard lpWizard;
    static NBProperties props;
    static File tempRunConfig2;
    static HashMap<Integer,HashMap<Integer,LPWizardConstraint> > inEqualsOutConstraints;
    static HashMap<ImmutablePair<Integer,Integer>,LPWizardConstraint> edgesConstraints;
    private static void solve(String[] args){
        String topologyPath = args[0];
        lpWizard = new LPWizard();
        lpWizard.setMinProblem(false);

        BufferedWriter runConfigWriter2 = null;
        try {
            tempRunConfig2 = File.createTempFile("temp-run-config2", ".tmp");
            runConfigWriter2 = new BufferedWriter(new FileWriter(tempRunConfig2));
            //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
            runConfigWriter2.write("scenario_topology_file=" + topologyPath + "\n");
            runConfigWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Random rand = new Random(212);
        props = new NBProperties(tempRunConfig2.getAbsolutePath(), BaseAllowedProperties.PROPERTIES_RUN);
        inEqualsOutConstraints = new HashMap<>();
//        lpWizard.setAllVariablesInteger();
        edgesConstraints = new HashMap<>();
        List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
        Graph g = props.getGraph();
        for(int i = 0; i<3; i++){
            for (Vertex v: g.getVertexList()){
                Vertex u;
                while(true){
                    u = g.getVertex(rand.nextInt(g.getVertexList().size()));
                    if(u.getId() == v.getId()){
                        continue;
                    }
                    break;
                }
                sourceDestPairs.add(new ImmutablePair<>(v.getId(),u.getId()));
                System.out.println("adding source dest pair " + new ImmutablePair<>(v.getId(),u.getId()));
            }
        }

        System.out.println("finished loading commodities");
        readGraph(sourceDestPairs);
        System.out.println("finished defining lp problem");
        lpWizard.setAllVariablesInteger();
        LPSolution lpSolution = lpWizard.solve();
        System.out.println(lpSolution.toString());
        tempRunConfig2.delete();
    }

    private static void readGraph(List<ImmutablePair<Integer, Integer>> sourceDestPairs) {
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

    private static void addSourceDestConstraints(ImmutablePair<Integer, Integer> pair, int id) {
//        System.out.println("source-dest constraint id " + id +" source " + pair.getLeft());
        int source = pair.getLeft();
        int dest = pair.getRight();
        lpWizard.addConstraint("c" + id  + "_" + source,1,">=").plus(id + "_" + source);
        lpWizard.addConstraint("pos" + id  + "_" + source,0,"<=").plus(id + "_" + source);

        inEqualsOutConstraints.get(id).get(source).plus(id + "_" + source);
        inEqualsOutConstraints.get(id).get(dest).plus(id + "_" + dest,-1d);
    }

    private static void readGraph(ImmutablePair pair, int id) {
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

}
