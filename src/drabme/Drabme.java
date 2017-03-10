package drabme;

import gitsbe.Logger;
import gitsbe.BooleanModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/* Drabme - Drug Response Analysis of Boolean Models from Evolution
 * 
 * Copyright Asmund Flobak 2014-2015
 * 
 * email: asmund.flobak@ntnu.no
 * 
 * Uses bnet_reduction (BNReduction.sh) by Veliz-Cuba
 * A. Veliz-Cuba, B. Aguilar, F. Hinkelmann and R. Laubenbacher. Steady state analysis of Boolean molecular network models via model reduction and computational algebra. BMC Bioinformatics, 2014.
 * 
 * 
 * Several levels of drug response can be computed if several outputs (>1) is defined, i.e. 2 outputs prosurvival, 3 outputs antisurvival = 6 levels of survival signaling (2,1,0,-1,-2,-3)
 * Designate effect of a node by sign and number in square brackes. Example: "[+2] Node1 * = Node2 and not Node3" gives output of +2 when Node1 is ON. "[-1] Node4 = Node5" gives output -1 when Node4 is ON. 
 * 
 */
public class Drabme implements Runnable {

	public String appName = "Drabme";
	public String version = "v0.11";

	private int verbosity;

	// inputs
//	private String filenameModelsIndex;

	private String filenameDrugs;
	private String filenameCombinations;
	private String filenameModelOutputs;
	private String filenameOutput;
	private String filenameSummary;
//	private String filenameDrugResponseObservations;
//	private String directoryProject ;
	private String directoryModels ;
	private String directoryTmp ;
	
	private int combosize;

	private Logger logger ;
	
	public Drabme(int verbosity, 
			String directoryModels,
			String filenameDrugs, 
			String filenameCombinations,
			String filenameModelOutputs, 
			String filenameOutput,
			String filenameSummary,
			String directoryTmp,
			int combosize) {

		// Set variables
		this.filenameDrugs = filenameDrugs;
		this.filenameModelOutputs = filenameModelOutputs;
		this.directoryModels = directoryModels ;
		this.filenameOutput = filenameOutput;
		this.filenameSummary = filenameSummary;
		this.directoryTmp = directoryTmp ;
		this.combosize = combosize;
		this.verbosity = verbosity;

		if (filenameCombinations.length() > 0)
			this.filenameCombinations = filenameCombinations;

	}

//	public Drabme(int verbosity, 
//			String filenameModelsIndex,
//			String filenameModels ,
//			String filenameDrugs, 
//			String filenameCombinations,
//			String filenameModelOutputs, 
//			String filenameOutput,
//			String filenameSummary, 
//			int combosize) {
//
//		// Set variables
//		this.filenameModelsIndex = filenameModelsIndex ;
//		this.filenameDrugs = filenameDrugs;
//		this.filenameModelOutputs = filenameModelOutputs;
////		this.modelDirectory = modelDirectory;
//		this.filenameOutput = filenameOutput;
//		this.filenameSummary = filenameSummary;
//		this.combosize = combosize;
//		this.verbosity = verbosity;
////		this.directoryProject = directoryProject ;
////		this.filenameDrugResponseObservations = filenameDrugResponseObservations;
//
//		if (filenameCombinations.length() > 0)
//			this.filenameCombinations = filenameCombinations;
//
//	}

	@Override
	public void run() {
		// Initialize logger
		String directory = System.getProperty("user.dir") + File.separator;
		try {
			logger = new Logger (filenameOutput, filenameSummary, this.appName
					+ "_debug.txt", directory, verbosity, false, true);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		// Date/time
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		long starttime = System.nanoTime();

		// Output header
		logger.outputHeader(1, this.appName + " " + this.version);
		logger.output(1, "Analysis start: " + dateFormat.format(cal.getTime()));


		// ---------------
		// Load all models
		// ---------------
		
		
				
		logger.outputHeader(2, "Loading Boolean models");

		ArrayList<BooleanModel> booleanModels = new ArrayList<BooleanModel>();

		try {
			//this.loadBooleanModels(filenameBooleanModels, "", booleanModels);
			this.loadBooleanModels(directoryModels, booleanModels);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// ----------
		// Load drugs
		// ----------

		logger.outputHeader(2, "Loading drug panel");

		DrugPanel drugPanel;

		try {
			drugPanel = new DrugPanel(filenameDrugs, logger);

			// Create drug combinations based on combosize
			// drugPanel.createCombinations(combosize);

		} catch (IOException e) {

			logger.output(1, "ERROR: Couldn't load drugs file: "
					+ filenameDrugs);
			logger.output(1, "Writing template drugs file: " + filenameDrugs);

			try {
				DrugPanel.saveDrugPanelFileTemplate(filenameDrugs);
			} catch (IOException e2) {
				logger.output(1, "ERROR: Couldn't write template drugs file.");
				e2.printStackTrace();
			}

			e.printStackTrace();
			return;
		}

		// ---------------------------------------------------------------------
		// Define perturbations based on drugs and drug combinations (drugpanel)
		// ---------------------------------------------------------------------

		logger.outputHeader(2, "Defining perturbations");

		Drug[][] drugperturbations = null;

		if (filenameCombinations == null) {
			drugperturbations = drugPanel.getCombinations(combosize);
		} else {
			try {
				drugperturbations = drugPanel.loadCombinationsFromFile(filenameCombinations);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// PerturbationPanel perturbationPanel = new PerturbationPanel
		// (drugPanel.getCombinations(combosize), drugPanel) ;
		PerturbationPanel perturbationPanel = new PerturbationPanel(
				drugperturbations, drugPanel, logger);

		// --------------------------------------------
		// Load drug perturbation response observations
		// --------------------------------------------

//		logger.outputHeader(2, "Loading experimentally observed data");
//		try {
//			perturbationPanel.loadObservationData(filenameDrugResponseObservations);
//		} catch (IOException e1) {
//
//			logger.output(1,
//					"ERROR: Couldn't load drug response observations: "
//							+ filenameDrugResponseObservations);
//
//			logger.output(1, "Writing template drug response data file: "
//					+ filenameDrugResponseObservations);
//
//			try {
//				PerturbationPanel.writeObservationsTemplateFile(
//						perturbationPanel.getPerturbations(),
//						filenameDrugResponseObservations);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				logger.output(1,
//						"ERROR: Couldn't write drug response observation data");
//				e.printStackTrace();
//			}
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//
//			return;
//		}

		// -------------------
		// Load output weights
		// -------------------
		logger.outputHeader(2, "Loading model outputs");
		ModelOutputs outputs;

		try {
			outputs = new ModelOutputs(filenameModelOutputs, logger);
		} catch (IOException e) {

			logger.output(1, "ERROR: Couldn't load model outputs file: "
					+ filenameModelOutputs);
			logger.output(1, "Writing template model outputs file: "
					+ filenameModelOutputs);

			try {
				ModelOutputs.saveModelOutputsFileTemplate(filenameModelOutputs);
			} catch (IOException e2) {
				logger.output(1,
						"ERROR: Couldn't write template model outputs file.");
				e2.printStackTrace();
			}

			e.printStackTrace();
			return;
		}

		// ----------------------------
		// Run simulations and Analyzer
		// ----------------------------

		DrugResponseAnalyzer dra = new DrugResponseAnalyzer(perturbationPanel,
				booleanModels, outputs, directoryTmp, logger);

		try {
			dra.analyze();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// -------
		// Summary
		// -------

		logger.outputHeader(1, "Drug perturbation responses");
		logger.output(1, "Perturbation" + "\t" + "Average" + "\t" + "SD" + "\t"
				+ "Data");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			String individualresponses = "";

			for (int j = 0; j < perturbation.getPredictions().length; j++) {
				individualresponses += "\t" + perturbation.getPredictions()[j];
			}

			logger.output(
					1,
					perturbation.getName()
							+ "\t"
							+ perturbation.getAveragePredictedResponse()
							+ "\t"
							+ perturbation
									.getStandardDeviationPredictedResponse()
							+ individualresponses);
		}

		// --------------------------
		// Synergies vs non-synergies
		// --------------------------

		logger.outputHeader(1, "Synergy observations, model-wise");
		logger.output(1, "Perturbation" + "\t" + "Synergies" + "\t"
				+ "Non-synergies");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				logger.output(
						1,
						perturbation.getName() + "\t"
								+ perturbation.getSynergyPredictions() + "\t"
								+ perturbation.getNonSynergyPredictions());
			}
		}

		

		// ----------
		// Statistics
		// ----------

		logger.outputHeader(1, "Statistics");

		StatisticalAnalysis sa = new StatisticalAnalysis(perturbationPanel);

		logger.outputHeader(1, "Average responses");
		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			logger.output(
					1,
					perturbation.getName() + "\t"
							+ perturbation.getAveragePredictedResponse());
		}

		// Get the 'extra' effect of combinations vs drug subset that has the strongest response
		// i.e. pairwise combination: PD-PI vs min(PD, PI)
		// i.e. threeway combination: PD-PI-5Z vs min(PD-PI, PD-5Z, PI-5Z)
		
		logger.outputHeader(1, "Combinatorial response over subsets");
		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				logger.output(
						1,
						perturbation.getName()
								+ "\t"
								+ perturbationPanel
										.getPredictedAverageCombinationResponse(perturbation));
			}
		}

		// --------------------------
		// Predicted vs observed data
		// --------------------------

//		int combinationsize = 2;
//
//		String obs = "Observed";
//		for (int i = 0; i < perturbationPanel
//				.getObservedCombinationResponses(combinationsize).length; i++) {
//			obs += "\t"
//					+ perturbationPanel
//							.getObservedCombinationResponses(combinationsize)[i];
//		}
//
//		String pred = "Predicted";
//		for (int i = 0; i < perturbationPanel
//				.getPredictedAverageCombinationResponses(combinationsize).length; i++) {
//			pred += "\t"
//					+ perturbationPanel
//							.getPredictedAverageCombinationResponses(combinationsize)[i];
//		}
//
//		String combinationNames = "";
//
//		for (int i = 0; i < perturbationPanel.getPerturbations(combinationsize).length; i++) {
//			combinationNames += "\t"
//					+ perturbationPanel.getPerturbations(combinationsize)[i]
//							.getName();
//		}
//		logger.output(2, combinationNames);
//		logger.output(2, obs);
//		logger.output(2, pred);
//
//		logger.output(1, "");
//
//		if ((perturbationPanel.getObservedCombinationResponses(combinationsize).length > 0)
//				& perturbationPanel
//						.getPredictedAverageCombinationResponses(combinationsize).length > 0) {
//			logger.output(
//					1,
//					"Spearman's correlation: "
//							+ sa.getSpearmansCorrelation(combinationsize));
//			logger.output(
//					1,
//					"Pearson's correlation: "
//							+ sa.getPearsonCorrelation(combinationsize));
//			logger.output(
//					1,
//					"Kendall's correlation: "
//							+ sa.getKendallsCorrelation(combinationsize));
//			logger.output(
//					1,
//					"Classification accuracy: "
//							+ sa.getSynergyClassificationAccuracy(combinationsize));
//		}
		
		
		// -------------------
				// Clean tmp directory
				// -------------------
//				cleanTmpDirectory(new File(
//						directoryTmp));
//				logger.output(2, "Cleaning tmp directory...");

		// -------
		// The end
		// -------
		long endtime = System.nanoTime();

		long duration = (endtime - starttime) / 1000000000;

		int seconds = (int) (duration) % 60;
		int minutes = (int) ((duration / 60) % 60);
		int hours = (int) ((duration / (60 * 60)));

		logger.outputHeader(1, "\nThe end");
		logger.output(1, "End: " + dateFormat.format(cal.getTime()));
		logger.output(1, "Analysis completed in " + hours + " hours, "
				+ minutes + " minutes, and " + seconds + " seconds ");

		logger.output(1, "\nWith that we say thank you and good bye!");
	}

	private void cleanTmpDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				cleanTmpDirectory(file);
			file.delete();
		}
	}

	//private void loadBooleanModels(String filename, String directory,
	//		ArrayList<BooleanModel> booleanModels) throws IOException {

	private void loadBooleanModels(String directory, ArrayList<BooleanModel> booleanModels) throws IOException {

//	    System.out.println("HERE: " + directory);
			// Each line is the filename of a model
//	    
//		if (! new File(directory).isAbsolute())
//		{
//			directory = System.getProperty("user.dir") + File.separator + "models" ;
//		}
	    		File[] files = new File(directory).listFiles();
		
	    for (int i = 0; i < files.length; i++) {
	    File f = files[i];
	      
//    	System.out.println("HERE: " + f.getPath());
		booleanModels.add(new BooleanModel(f.getPath(), logger));
		}
	}

}
