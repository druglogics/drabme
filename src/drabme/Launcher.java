package drabme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Launcher {

	public Launcher() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
		Calendar cal = Calendar.getInstance();
		String filesuffix = dateFormat.format(cal.getTime());

		int verbosity = 2;
		int combosize = 2;

		String filenameBooleanModels;
		String dirProject;
		String filenameOutput;
		String filenameSummary;

		// // 20160211 Final round submission
		//String filenameResponseData = "toy_ags_response_data.tab";
		//String filenameDrugs = "toy_ags_drugs.tab";
		//String filenameModelOutputs = "toy_ags_modeloutputs.tab";
		//String filenameCombinations = "toy_ags_perturbations.tab";

		//String[] dirProjectsArray = { "toy_ags_network_toy_ags_steadystate" };

		//String[] filenameBooleanModelsArray = { "toy_ags_network_toy_ags_steadystate_models.txt" };

		String filenameResponseData = args[0];
		String filenameDrugs = args[1];
		String filenameModelOutputs = args[2];
		String filenameCombinations = args[3];

		String modelDirectory = args[4];

		// TEMPLATE
		// String filenameResponseData = "" ;
		// String filenameDrugs = "" ;
		// String filenameModelOutputs = "" ;
		// String filenameCombinations = "" ;
		//
		// String[] dirProjectsArray = {
		//
		// } ;
		//
		// String[] filenameBooleanModelsArray = {
		//
		// } ;

		Thread t;

    t = new Thread(new Drabme(verbosity, modelDirectory,
          filenameDrugs, filenameCombinations, filenameResponseData,
          filenameModelOutputs, "output.txt", "summary.txt", combosize));
    t.start();
    try {
      t.join();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

	}

}
