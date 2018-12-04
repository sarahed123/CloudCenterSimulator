package ch.ethz.systems.netbench.ext.poissontraffic.flowsize;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class FromCSV extends FlowSizeDistribution {

    LinkedList<Double> mSizeDist;
    LinkedList<Double> mProbs;
    public FromCSV(String csvFile){
        mSizeDist = new LinkedList<>();
        mProbs = new LinkedList<>();
        processCSVFile(csvFile);
    }

    private void processCSVFile(String csvFile) {
        try(BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            int currProb = 0;
            for(String line; (line = br.readLine()) != null; ) {
                if(line.startsWith("#")){
                    continue;
                }
                Double[] values = (Double[]) ((Stream<String>) Arrays.asList(line.split(",")).stream()).map(val -> Double.parseDouble(val)).toArray(Double[]::new);
                mSizeDist.addLast(values[0]);
                mProbs.add(values[1]);
                
            }
            // line is not visible here.
        } catch (IOException e) {
            throw new RuntimeException("cannot access csv file " + csvFile);
        }
    }

    @Override
    public long generateFlowSizeByte() {
        double outcome = independentRng.nextDouble();
        long newSize = getFlowSize(outcome);
        return newSize;
    }

    protected long getFlowSize(double outcome) {
        double currProb = 0;
        for(int i = 0; i<mProbs.size()-1; i++){
            if(outcome >= currProb && outcome<mProbs.get(i)){
                return new Double(mSizeDist.get(i)).longValue();
            }
            currProb = mProbs.get(i);
        }
        return new Double(mSizeDist.get(mSizeDist.size()-1)).longValue();
    }

//    private long toKB(double bytes){
//        return (long)(bytes*1000);
//    }
}
