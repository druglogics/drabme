package drabme;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
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
		String filenameResponseData = "toy_ags_response_data.tab";
		String filenameDrugs = "toy_ags_drugs.tab";
		String filenameModelOutputs = "toy_ags_modeloutputs.tab";
		String filenameCombinations = "toy_ags_perturbations.tab";

		String[] dirProjectsArray = { "toy_ags_network_toy_ags_steadystate" };

		String[] filenameBooleanModelsArray = { "toy_ags_network_toy_ags_steadystate_models.txt" };

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

		for (int i = 0; i < dirProjectsArray.length; i++) {
			dirProject = dirProjectsArray[i] + "/";
			filenameBooleanModels = dirProject + filenameBooleanModelsArray[i];
			filenameOutput = filenameBooleanModels + "_output.txt";
			filenameSummary = filenameBooleanModels + "_summary.txt";

			t = new Thread(new Drabme(verbosity, filenameBooleanModels,
					filenameDrugs, filenameCombinations, filenameResponseData,
					filenameModelOutputs, filenameOutput, filenameSummary,
					combosize));
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Thread t = new Thread(new Drabme(verbosity, filenameBooleanModels,
		// filenameDrugs, filenameCombinations, filenameResponseData,
		// filenameModelOutputs, filenameOutput, filenameSummary, combosize)) ;

		// t.start();

	}

}
