package edu.asu.emit.algorithm.lp.gurobi;

import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.NBProperties;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import gurobi.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
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
    private static int currColor;

    public static void  solve(String[] args){
        String resultsPath = args[0];
        int colors = Integer.parseInt(args[1]);
        List<ImmutablePair<Integer,Integer>> sourceDestPairs;
        try {
            BufferedWriter runConfigWriter2 = null;

            tempRunConfig2 = File.createTempFile("temp-run-config2", ".tmp");
            runConfigWriter2 = new BufferedWriter(new FileWriter(tempRunConfig2));
            //runConfigWriter.write("network_device=hybrid_optic_electronic\n");
            runConfigWriter2.write("scenario_topology_file=" + resultsPath + "/extended_topology.txt" + "\n");
            runConfigWriter2.close();
            sourceDestPairs = initSourceDestList(resultsPath);
            System.out.println("finished reading commodities. found " + sourceDestPairs.size());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        props = new NBProperties(tempRunConfig2.getAbsolutePath(), BaseAllowedProperties.PROPERTIES_RUN);





        solve(props,sourceDestPairs,colors);
        tempRunConfig2.delete();
        try {
            close();
        } catch (GRBException e) {
            e.printStackTrace();
        }

    }

    private static List<ImmutablePair<Integer, Integer>> initSourceDestList(String resultsPath) throws IOException {
        List<ImmutablePair<Integer,Integer>> sourceDestPairs = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(resultsPath + "/commodities/commodities_0"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] commodities = line.split(" ");
                ImmutablePair<Integer,Integer> pair = new ImmutablePair(Integer.parseInt(commodities[0]),Integer.parseInt(commodities[1]));
                sourceDestPairs.add(pair);
            }
        }
        return sourceDestPairs;
    }

    public static Double solve(NBProperties properties, List<ImmutablePair<Integer,Integer>> sourceDestPairs,int colors){
//        System.setProperty("java.library.path","/cs/usr/inonkp/gurobi/gurobi811/linux64/lib");

        modelVars = new HashMap<>();
        props = properties;
        try {
            env = new GRBEnv();
            model = new GRBModel(env);


            objectiveExpr = new GRBLinExpr();
            for(currColor = 0; currColor<colors; currColor++){
                System.out.println("starting with color " + currColor);
                edgesSumExpresssions = new HashMap<>();
                inOutExpresssions = new HashMap<>();
                readGraph(sourceDestPairs);
                System.out.println("finished reading color " + currColor);
                for(Pair<Integer,Integer> pair: edgesSumExpresssions.keySet()){
                    model.addConstr(edgesSumExpresssions.get(pair),GRB.EQUAL,0,"c" + currColor + "Edge" + pair.getLeft() + "-" + pair.getRight());
                }
                for(String key: inOutExpresssions.keySet()){
                    model.addConstr(inOutExpresssions.get(key),GRB.EQUAL,0,"c" + currColor + "InOut" + key);
                }
            }
            int id = 0;
            for(ImmutablePair pair: sourceDestPairs){
                GRBLinExpr colorConstraint = new GRBLinExpr();
                for(currColor = 0; currColor<colors; currColor++){
                    GRBVar colorSourceVar = getOrCreateVarWithColor("s" + id +"_" + pair.getLeft());
                    colorConstraint.addTerm(1,colorSourceVar);

                }
                GRBVar sourceVar = getOrCreateVar("s" + id +"_" + pair.getLeft());
                colorConstraint.addTerm(-1,sourceVar);
                model.addConstr(colorConstraint,GRB.EQUAL,0,"cS_" + pair.getLeft() + "_" + id);
                objectiveExpr.addTerm(1,sourceVar);
                id++;
            }

//            for(int i = 1; i<=objectiveExpr.size(); i++){
//                System.out.println("var: \n");
//                System.out.println(objectiveExpr.getVar(i).get(GRB.StringAttr.VarName));
//            }

            model.setObjective(objectiveExpr,GRB.MAXIMIZE);
            model.set(GRB.IntParam.Method,2);
            model.set(GRB.IntParam.Crossover,0);


            model.optimize();

//            model.write("/cs/usr/inonkp/netbench_results/logs/gurobi/model.lp");


//            for(GRBVar var: model.getVars()){
//                System.out.println(var.get(GRB.StringAttr.VarName) + " = " + var.get(GRB.DoubleAttr.X));
//            }

            Double objVal = model.get(GRB.DoubleAttr.ObjVal);

            return objVal;
        } catch (GRBException e) {
            e.printStackTrace();
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
        return -1d;
    }

    public static void close() throws GRBException {
        model.dispose();
        env.dispose();
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

    public static void writeModel(String filename) throws GRBException {
        model.write(filename);

    }

    private static void addSourceDestConstraints(ImmutablePair<Integer,Integer> pair, int id) throws GRBException {
        GRBVar sourceVar = getOrCreateVarWithColor("s" + id +"_" + pair.getLeft());
        GRBLinExpr inExpression = inOutExpresssions.get(pair.getLeft() + "_" + id);
        inExpression.addTerm(1,sourceVar);
//        inOutExpresssions.put(pair.getLeft() + "_" + id,inExpression);

        GRBVar destVar = getOrCreateVarWithColor( "t" + id +"_" + pair.getRight());
        GRBLinExpr outExpression = inOutExpresssions.get(pair.getRight() + "_" +id);
        outExpression.addTerm(-1,destVar);
//        inOutExpresssions.put(pair.getRight() + "_" +id,outExpression);
//        objectiveExpr.addTerm(1,destVar);

    }

    private static GRBVar getOrCreateVarWithColor(String varName) throws GRBException {
        return getOrCreateVar(currColor + "_" + varName);
    }

    private static GRBVar getOrCreateVar(String varName) throws GRBException {
        GRBVar var = modelVars.get(varName);
        if(var==null){
            var = model.addVar(0,1,1,GRB.INTEGER,varName);
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

                GRBVar inVar = getOrCreateVarWithColor(id + "_" + u.getId() + "-" + v.getId());
                inOutExpression.addTerm(1,inVar);
            }
            for(Vertex u: outNeighbours){
                if(g.getEdgeCapacity(v,u) == 1) {
                    GRBLinExpr edgeSumExpression = edgesSumExpresssions.get(new ImmutablePair<>(v.getId(),u.getId()));
                    GRBVar outVar = getOrCreateVarWithColor(id + "_" + v.getId() + "-" + u.getId());
                    if(edgeSumExpression == null){
                        GRBVar edgeVar = getOrCreateVarWithColor("e_" + v.getId() + "-" + u.getId());
                        GRBLinExpr edgeExpression = new GRBLinExpr();
                        edgeExpression.addTerm(1, edgeVar);

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
