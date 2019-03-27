package edu.asu.emit.algorithm.lp.gurobi;

import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.NBProperties;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import gurobi.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GurobiSolver {
    static GRBEnv env;
    static NBProperties props;
    static File tempRunConfig2;
    static GRBModel  model;
    static GRBLinExpr objectiveExpr;
    static HashMap<Pair<Integer,Integer>, GRBLinExpr> edgesSumExpresssions;
    static HashMap<String, GRBLinExpr> inOutExpresssions;
    static HashMap<String,GRBVar> modelVars;
    public static void  solve(String[] args){
        String topologyPath = args[0];
        try {
            BufferedWriter runConfigWriter2 = null;

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

        List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
        Graph g = props.getGraph();

        for (Vertex v: g.getVertexList()){
            Vertex u = g.getVertexList().get(rand.nextInt(g.getVertexList().size()));
            if(u.getId() == v.getId()){
                continue;
            }
            sourceDestPairs.add(new ImmutablePair<>(v.getId(),u.getId()));


        }

        solve(props,sourceDestPairs);

    }

    public static void solve(NBProperties properties, List<ImmutablePair<Integer,Integer>> sourceDestPairs){
//        System.setProperty("java.library.path","/cs/usr/inonkp/gurobi/gurobi811/linux64/lib");
        edgesSumExpresssions = new HashMap<>();
        inOutExpresssions = new HashMap<>();
        modelVars = new HashMap<>();
        props = properties;
        try {
            env = new GRBEnv("/cs/usr/inonkp/netbench_results/logs/gurobi/mip1.log");
            model = new GRBModel(env);
            model.set(GRB.IntParam.Crossover,0);
            objectiveExpr = new GRBLinExpr();

            readGraph(sourceDestPairs);
            for(Pair<Integer,Integer> pair: edgesSumExpresssions.keySet()){
                model.addConstr(edgesSumExpresssions.get(pair),GRB.EQUAL,0,"cEdge"+pair.getLeft() + "-" + pair.getRight());
            }
            for(String key: inOutExpresssions.keySet()){
                model.addConstr(inOutExpresssions.get(key),GRB.EQUAL,0,"cInOut" + key);
            }
//            for(int i = 1; i<=objectiveExpr.size(); i++){
//                System.out.println("var: \n");
//                System.out.println(objectiveExpr.getVar(i).get(GRB.StringAttr.VarName));
//            }

            model.setObjective(objectiveExpr,GRB.MAXIMIZE);

            model.optimize();

//            model.write("/cs/usr/inonkp/netbench_results/logs/gurobi/model.lp");
            System.out.println(model.get(GRB.DoubleAttr.ObjVal));

//            for(GRBVar var: model.getVars()){
//                System.out.println(var.get(GRB.StringAttr.VarName) + " = " + var.get(GRB.DoubleAttr.X));
//            }


            model.dispose();
            env.dispose();
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    private static void readGraph(List<ImmutablePair<Integer, Integer>> sourceDestPairs) throws GRBException {
        GraphDetails graphDetails = props.getGraphDetails();
        HashMap<Integer,Integer> sourceCounts = new HashMap<>();
        HashMap<Integer,Integer> destCounts = new HashMap<>();
        int i = 0;
        for(ImmutablePair pair: sourceDestPairs){
            readGraph(pair, i);
            addSourceDestConstraints(pair,i);
            i++;


        }

    }

    private static void addSourceDestConstraints(ImmutablePair<Integer,Integer> pair, int id) throws GRBException {
        GRBVar sourceVar = getOrCreateVar("s" + id +"_" + pair.getLeft());
        GRBLinExpr inExpression = inOutExpresssions.get(pair.getLeft() + "_" + id);
        inExpression.addTerm(1,sourceVar);
//        inOutExpresssions.put(pair.getLeft() + "_" + id,inExpression);

        GRBVar destVar = getOrCreateVar( "t" + id +"_" + pair.getRight());
        GRBLinExpr outExpression = inOutExpresssions.get(pair.getRight() + "_" +id);
        outExpression.addTerm(-1,destVar);
//        inOutExpresssions.put(pair.getRight() + "_" +id,outExpression);
        objectiveExpr.addTerm(1,destVar);
    }

    private static GRBVar getOrCreateVar(String varName) throws GRBException {
        GRBVar var = modelVars.get(varName);
        if(var==null){
            var = model.addVar(0,1,1,GRB.CONTINUOUS,varName);
            modelVars.put(varName,var);
        }
        return var;
    }

    private static void readGraph(ImmutablePair pair, int id) throws GRBException {
        Graph g = props.getGraph();
        List<Vertex> vertices = g.getVertexList();
        for(Vertex v: vertices){
            List<Vertex> outNeighbours = g.getAdjacentVertices(v);
            List<Vertex> inNeighbours = g.getPrecedentVertices(v);
            GRBLinExpr inOutExpression = new GRBLinExpr();
            for(Vertex u: inNeighbours){

                GRBVar inVar = getOrCreateVar(id + "_" + u.getId() + "-" + v.getId());
                inOutExpression.addTerm(1,inVar);
            }
            for(Vertex u: outNeighbours){
                if(g.getEdgeCapacity(v,u) == 1) {
                    GRBLinExpr edgeSumExpression = edgesSumExpresssions.get(new ImmutablePair<>(v.getId(),u.getId()));
                    GRBVar outVar = getOrCreateVar(id + "_" + v.getId() + "-" + u.getId());
                    if(edgeSumExpression == null){
                        GRBVar edgeVar = getOrCreateVar("e_" + v.getId() + "-" + u.getId());
                        GRBLinExpr edgeExpression = new GRBLinExpr();
                        edgeExpression.addTerm(1, edgeVar);
                        // edges have capacity 1
                        model.addConstr(edgeExpression, GRB.LESS_EQUAL, 1, "c" + v.getId() + "-" + u.getId());

                        edgeSumExpression = new GRBLinExpr();
                        edgeSumExpression.addTerm(-1,edgeVar);
                    }

                    edgeSumExpression.addTerm(1,outVar);
                    edgesSumExpresssions.put(new ImmutablePair<>(v.getId(),u.getId()),edgeSumExpression);
                    inOutExpression.addTerm(-1,outVar);
                }
            }
            inOutExpresssions.put(v.getId() + "_" + id,inOutExpression);
        }

    }
}
