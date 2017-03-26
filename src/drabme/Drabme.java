package drabme;

import gitsbe.Logger;
import gitsbe.BooleanModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/* Drabme - Drug Response Analysis of Boolean Models from Evolution
 * 
 * Copyright Asmund Flobak 2014-2015-2016-2017
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
	
	String nameProject ;

	private String filenameDrugs;
	private String filenameCombinations;
	private String filenameModelOutputs;
	private String directoryModels ;
	private String directoryTmp ;
	
	private String directoryOutput ;
	
	private int combosize;

	private Logger logger ;
	
	public Drabme(int verbosity, 
			String nameProject,
			String directoryModels,
			String filenameDrugs, 
			String filenameCombinations,
			String filenameModelOutputs, 
			String directoryOutput,
			String directoryTmp,
			int combosize) {

		// Set variables
		this.nameProject = nameProject ;
		this.filenameDrugs = filenameDrugs;
		this.filenameModelOutputs = filenameModelOutputs;
		this.directoryModels = directoryModels ;
		this.directoryOutput = directoryOutput ;
		this.directoryTmp = directoryTmp ;
		this.combosize = combosize;
		this.verbosity = verbosity;

		if (filenameCombinations.length() > 0)
			this.filenameCombinations = filenameCombinations;

	}



	@Override
	public void run() {
		// Initialize logger

		try {
			logger = new Logger (appName + "_" + nameProject + "_log.txt", nameProject + "_summary.txt", directoryOutput, verbosity, true);
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
		
		logger.outputHeader(1, "Project: " + nameProject);


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

		// ----------------
		// Generate summary
		// ----------------

		String filename ;
		
		// File to store perturbation responses in, separately
		filename = new File(directoryOutput, "output_" + nameProject + "_modelwise_responses.tab").getAbsolutePath() ; 
		
		logger.outputHeader(1, "Drug perturbation responses");
		logger.output(
				filename,
				"Perturbation" + "\t" + "Average" + "\t" + "SD" + "\t"
				+ "Data");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			String individualresponses = "";

			for (int j = 0; j < perturbation.getPredictions().length; j++) {
				individualresponses += "\t" + perturbation.getPredictions()[j];
			}

			logger.output(
					filename,
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

		// File to store drug synergies in
		filename = new File(directoryOutput, "output_" + nameProject + "_modelwise_synergies.tab").getAbsolutePath() ;
		
		logger.outputHeader(1, "Combinatorial response in excess over subsets, model-wise");
		logger.output(filename, "Perturbation" + "\t" + "Synergies" + "\t"
				+ "Non-synergies");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				logger.output(
						filename,
						perturbation.getName() + "\t"
								+ perturbation.getSynergyPredictions() + "\t"
								+ perturbation.getNonSynergyPredictions());
			}
		}

		

		// ----------
		// Statistics
		// ----------

		// File to store average responses in
		filename = new File (directoryOutput, "output_" + nameProject + "_ensemblewise_responses.tab").getAbsolutePath() ;
		
		logger.outputHeader(1, "Statistics");

		StatisticalAnalysis sa = new StatisticalAnalysis(perturbationPanel);

		logger.outputHeader(1, "Ensemble average responses");
		
		logger.output(filename, "Perturbation" + "\t" + "Ensemble average response");
			
		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			logger.output(
					filename,
					perturbation.getName() + "\t"
							+ perturbation.getAveragePredictedResponse());
		}

		// Get the 'extra' effect of combinations vs drug subset that has the strongest response
		// i.e. pairwise combination: PD-PI vs min(PD, PI)
		// i.e. threeway combination: PD-PI-5Z vs min(PD-PI, PD-5Z, PI-5Z)
		
		
		// File to store excess effects over agerage responses in
		filename = new File (directoryOutput, "output_" + nameProject + "_ensemblewise_synergies.tab").getAbsolutePath() ;
		
		logger.outputHeader(1, "Combinatorial response in excess over subsets (ensemble-wise synergy)");
		
		logger.output(filename, "Perturbation" + "\t" + "Response excess over subset");
		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				logger.output(
						filename,
						perturbation.getName()
								+ "\t"
								+ perturbationPanel
										.getPredictedAverageCombinationResponse(perturbation));
			}
		}
		
	
		
		
		// -------------------
		// Clean tmp directory
		// -------------------
		
		String filenameArchive = new File(directoryOutput, nameProject + ".drabme.tmp.tar.gz").getAbsolutePath() ;
		
		logger.output(2, "\nCreating archive with all temporary files: " + filenameArchive);
		compressDirectory (filenameArchive, directoryTmp) ;
		
		logger.output(2, "Cleaning tmp directory...");
		cleanTmpDirectory(new File(directoryTmp));


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

	private void cleanTmpDirectory (File dir)
	{
	    for (File file: dir.listFiles()) {
	        if (file.isDirectory()) cleanTmpDirectory(file);
	        file.delete();
	    }
	}


	private void loadBooleanModels(String directory, ArrayList<BooleanModel> booleanModels) throws IOException {

	    File[] files = new File(directory).listFiles();
		
	    for (int i = 0; i < files.length; i++) {
	    File f = files[i];
	      
		booleanModels.add(new BooleanModel(f.getPath(), logger));
		}
	}
	private void compressDirectory (String filenameArchive, String directory)
	{
		//tar cvfz tmp.tar.gz tmp

		try {
			
			// "BNReduction_timeout.sh" calls BNReduction.sh, but with the 'timeout' commanding, ensuring that the process has to
			// complete within specified amount of time (in case BNReduction should hang).
			
			
			ProcessBuilder pb = new ProcessBuilder("tar", "cvfz", filenameArchive, directory);
			
			if (logger.getVerbosity() >= 3)
			{
				pb.redirectErrorStream(true);
				pb.redirectOutput() ;
			}
			
			logger.output(3, "Compressing temporary models: " + filenameArchive) ;
			
			
			Process p ;
			p = pb.start ();
			
			try {
				p.waitFor() ;
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while(r.ready()) {
	        	logger.output(3, r.readLine());
            }
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
