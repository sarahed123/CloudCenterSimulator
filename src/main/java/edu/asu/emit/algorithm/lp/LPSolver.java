package edu.asu.emit.algorithm.lp;

import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.NBProperties;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.lp.gurobi.GurobiSolver;
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

public class LPSolver {



    public static void main(String[] args){
        GurobiSolver.solve(args);
    }


}
