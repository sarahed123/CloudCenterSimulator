package ch.ethz.systems.netbench.core.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.PriorityQueue;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.config.exceptions.PropertyValueInvalidException;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.core.run.routing.remote.RemoteRoutingController;
import ch.ethz.systems.netbench.xpt.xpander.XpanderRouter;

public class SimulatorStateSaver {

	public static void save() {
		RemoteRoutingController rrc;
		rrc = RemoteRoutingController.getInstance();
		if(rrc==null) {
			System.out.println("Only central routing is currently supported");
			return;
		}

		long time = Simulator.getCurrentTime();
		long totalRunTIme = Simulator.getTotalRunTimeNs();
		DecimalFormat df = new DecimalFormat("#.##"); 
		double percent = ((double) time) / ((double)totalRunTIme) * 100;
		String dumpFolderName = "dumps" + "/" + "test_dump";
		//String dumpFolderName = SimulationLogger.getRunFolderFull() + "/" + df.format(percent) + "%";
		new File(dumpFolderName).mkdirs();
		String confFileName = Simulator.getConfiguration().getFileName();
		File confFile = new File(confFileName);


		try {
			System.out.println("Starting to write state..");
			NBProperties p = new NBProperties(Simulator.getConfiguration());
			Simulator.dumpState(dumpFolderName);
			SimulationLogger.dumpState(dumpFolderName);
			rrc.dumpState(dumpFolderName);
			p.put("from_state", dumpFolderName);
			p.saveToFile(dumpFolderName + "/" + confFile.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static JSONObject loadJson(String fileName) {
		File f = new File(fileName);
        if (f.exists()){
        	try {
        		InputStream is = new FileInputStream(fileName);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonTxt);
                return json;
        	}catch(IOException | ParseException e) {
        		e.printStackTrace();
        	}
            
        }
        throw new RuntimeException("Failure loading json from file " + fileName);
	}
	
	public static Object readObjectFromFile(String filename) {

		Object obj = null;

		FileInputStream fin = null;
		ObjectInputStream ois = null;

		try {

			fin = new FileInputStream(filename);
			ois = new ObjectInputStream(fin);
			obj =  ois.readObject();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
		if(obj==null) {
			throw new RuntimeException("Error reading object from file " + filename);
		}
		return obj;

	}

}
