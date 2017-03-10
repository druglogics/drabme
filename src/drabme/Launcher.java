package drabme;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Launcher {

	public Launcher() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		// Check if location of BNReduction.sh is defined in environment variable BNET_HOME 
		if (System.getenv("BNET_HOME") == null)
		{
			System.out.println("Set environment variable BNET_HOME to point to location of BNReduction.sh");
			System.out.println("BNReduction can be obtained from https://github.com/alanavc/BNReduction") ;
			return ;
		}
		
		if (args.length == 0)
		{
			System.out.println("No user argumetns supplied") ;
			System.out.println("Usage: drabme <directory models> <filename drugs> <filename model outputs> <directory tmp> [filename combinations]") ;
		
			System.out.println("\nTestrun: setting up run with example files:");

			args = new String[] {"toy_ags_network_toy_ags_steadystate/models", "toy_ags_drugs.tab", "toy_ags_modeloutputs.tab", "toy_ags_perturbations.tab", "toy_ags_network_toy_ags_steadystate/tmp"} ;
			System.out.println("drabme " + args[0] + " " + args[1] + " " + args[2] + args[3] + "\n\n")  ;
			
		}
		
//		String filenameResponseData = args[0];
		String directoryModels = args[0] ;
		String filenameDrugs = args[1];
		String filenameModelOutputs = args[2];
		String filenameCombinations = args[3];
		String directoryTmp = args[4] ;

		// make sure path to tmp directory is absolute, since BNreduction will be run from another working directory
		if (!new File (directoryTmp).isAbsolute())
			directoryTmp = System.getProperty("user.dir") + File.separator + directoryTmp ;
		
	
//		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
//		Calendar cal = Calendar.getInstance();
//		String filesuffix = dateFormat.format(cal.getTime());

		int verbosity = 3;
		int combosize = 2;


		// // 20160211 Final round submission
		//String filenameResponseData = "toy_ags_response_data.tab";
		//String filenameDrugs = "toy_ags_drugs.tab";
		//String filenameModelOutputs = "toy_ags_modeloutputs.tab";
		//String filenameCombinations = "toy_ags_perturbations.tab";

		//String[] dirProjectsArray = { "toy_ags_network_toy_ags_steadystate" };

		//String[] filenameBooleanModelsArray = { "toy_ags_network_toy_ags_steadystate_models.txt" };

		
		
		

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

    t = new Thread(new Drabme(verbosity, 
    							directoryModels,
    							filenameDrugs, 
    							filenameCombinations, 
    							filenameModelOutputs, 
    							"output.txt", 
    							"summary.txt", 
    							directoryTmp,
    							combosize));
    t.start();
    try {
      t.join();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

	}

}
