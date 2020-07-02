package ch.ethz.systems.netbench.xpt.meta_node;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.run.infrastructure.BaseInitializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class MetaNodeTrafficPlanTest {
    private File tempRunConfig;
    MockMNTrafficPlanner planner;
    @Before
    public void setup() throws IOException {
        tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write("scenario_topology_file=./src/test/java/ch/ethz/systems/netbench/xpt/meta_node/S50_O0.5_L5_D6.topology\n");
        runConfigWriter.write("link_bandwidth_bit_per_ns=50\n");
        runConfigWriter.write("link_delay_ns=10\n");
        runConfigWriter.close();
        NBProperties conf = new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION,
                BaseAllowedProperties.EXPERIMENTAL,
                BaseAllowedProperties.BASE_DIR_VARIANTS
        );
        Simulator.setup(777,conf);
        planner = new MockMNTrafficPlanner(null,0,null,conf);
    }

    @Test
    public void planTest(){

        assert planner.pairs.size() == 400;
        assert planner.pairs.toString().equals("[(25,35), (35,25), (25,36), " +
                "(36,25), (25,37), (37,25), (25,38), (38,25), (25,39), (39,25), (25,40), (40,25), (25,41), (41,25), (25,42), (42,25), (25,43), (43,25), " +
                "(25,44), (44,25), (26,35), (35,26), (26,36), (36,26), (26,37), (37,26), (26,38), (38,26), (26,39), (39,26), (26,40), (40,26), (26,41)," +
                " (41,26), (26,42), (42,26), (26,43), (43,26), (26,44), (44,26), (27,35), (35,27), (27,36), (36,27), (27,37), (37,27), (27,38), (38,27)," +
                " (27,39), (39,27), (27,40), (40,27), (27,41), (41,27), (27,42), (42,27), (27,43), (43,27), (27,44), (44,27), (28,35), (35,28), (28,36), " +
                "(36,28), (28,37), (37,28), (28,38), (38,28), (28,39), (39,28), (28,40), (40,28), (28,41), (41,28), (28,42), (42,28), (28,43), (43,28)," +
                " (28,44), (44,28), (29,35), (35,29), (29,36), (36,29), (29,37), (37,29), (29,38), (38,29), (29,39), (39,29), (29,40), (40,29), (29,41)," +
                " (41,29), (29,42), (42,29), (29,43), (43,29), (29,44), (44,29), (30,35), (35,30), (30,36), (36,30), (30,37), (37,30), (30,38), (38,30), " +
                "(30,39), (39,30), (30,40), (40,30), (30,41), (41,30), (30,42), (42,30), (30,43), (43,30), (30,44), (44,30), (31,35), (35,31), (31,36), " +
                "(36,31), (31,37), (37,31), (31,38), (38,31), (31,39), (39,31), (31,40), (40,31), (31,41), (41,31), (31,42), (42,31), (31,43), (43,31), " +
                "(31,44), (44,31), (32,35), (35,32), (32,36), (36,32), (32,37), (37,32), (32,38), (38,32), (32,39), (39,32), (32,40), (40,32), (32,41), " +
                "(41,32), (32,42), (42,32), (32,43), (43,32), (32,44), (44,32), (33,35), (35,33), (33,36), (36,33), (33,37), (37,33), (33,38), (38,33), " +
                "(33,39), (39,33), (33,40), (40,33), (33,41), (41,33), (33,42), (42,33), (33,43), (43,33), (33,44), (44,33), (34,35), (35,34), (34,36), " +
                "(36,34), (34,37), (37,34), (34,38), (38,34), (34,39), (39,34), (34,40), (40,34), (34,41), (41,34), (34,42), (42,34), (34,43), (43,34), " +
                "(34,44), (44,34), (45,55), (55,45), (45,56), (56,45), (45,57), (57,45), (45,58), (58,45), (45,59), (59,45), (45,60), (60,45), (45,61), " +
                "(61,45), (45,62), (62,45), (45,63), (63,45), (45,64), (64,45), (46,55), (55,46), (46,56), (56,46), (46,57), (57,46), (46,58), (58,46), " +
                "(46,59), (59,46), (46,60), (60,46), (46,61), (61,46), (46,62), (62,46), (46,63), (63,46), (46,64), (64,46), (47,55), (55,47), (47,56), " +
                "(56,47), (47,57), (57,47), (47,58), (58,47), (47,59), (59,47), (47,60), (60,47), (47,61), (61,47), (47,62), (62,47), (47,63), (63,47), " +
                "(47,64), (64,47), (48,55), (55,48), (48,56), (56,48), (48,57), (57,48), (48,58), (58,48), (48,59), (59,48), (48,60), (60,48), (48,61), " +
                "(61,48), (48,62), (62,48), (48,63), (63,48), (48,64), (64,48), (49,55), (55,49), (49,56), (56,49), (49,57), (57,49), (49,58), (58,49), " +
                "(49,59), (59,49), (49,60), (60,49), (49,61), (61,49), (49,62), (62,49), (49,63), (63,49), (49,64), (64,49), (50,55), (55,50), (50,56), " +
                "(56,50), (50,57), (57,50), (50,58), (58,50), (50,59), (59,50), (50,60), (60,50), (50,61), (61,50), (50,62), (62,50), (50,63), (63,50), " +
                "(50,64), (64,50), (51,55), (55,51), (51,56), (56,51), (51,57), (57,51), (51,58), (58,51), (51,59), (59,51), (51,60), (60,51), (51,61)," +
                " (61,51), (51,62), (62,51), (51,63), (63,51), (51,64), (64,51), (52,55), (55,52), (52,56), (56,52), (52,57), (57,52), (52,58), (58,52), " +
                "(52,59), (59,52), (52,60), (60,52), (52,61), (61,52), (52,62), (62,52), (52,63), (63,52), (52,64), (64,52), (53,55), (55,53), (53,56), " +
                "(56,53), (53,57), (57,53), (53,58), (58,53), (53,59), (59,53), (53,60), (60,53), (53,61), (61,53), (53,62), (62,53), (53,63), (63,53), " +
                "(53,64), (64,53), (54,55), (55,54), (54,56), (56,54), (54,57), (57,54), (54,58), (58,54), (54,59), (59,54), (54,60), (60,54), (54,61), " +
                "(61,54), (54,62), (62,54), (54,63), (63,54), (54,64), (64,54)]");
    }


    @After
    public void clear(){
        Simulator.reset(true);

    }
}
