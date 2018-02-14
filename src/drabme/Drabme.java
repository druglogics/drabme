package drabme;

import gitsbe.Logger;
import gitsbe.ModelOutputs;
import gitsbe.Timer;
import gitsbe.BooleanModel;

import static gitsbe.Util.*;

import java.io.File;
import java.io.IOException;
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
 */
public class Drabme implements Runnable {

	public static String appName = "Drabme";
	public String version = "v0.13";

	private int verbosity;

	String nameProject;

	private String filenameDrugs;
	private String filenameCombinations;
	private String filenameModelOutputs;
	private String directoryModels;
	private String directoryTmp;
	private String directoryOutput;
	private boolean preserveTmpFiles;

	private int combosize;

	private Logger logger;

	public Drabme(int verbosity, String nameProject, String directoryModels, String filenameDrugs,
			String filenameCombinations, String filenameModelOutputs, String directoryOutput, String directoryTmp,
			boolean preserveTmpFiles, int combosize) {

		// Set variables
		this.nameProject = nameProject;
		this.filenameDrugs = filenameDrugs;
		this.filenameModelOutputs = filenameModelOutputs;
		this.directoryModels = directoryModels;
		this.directoryOutput = directoryOutput;
		this.directoryTmp = directoryTmp;
		this.preserveTmpFiles = preserveTmpFiles;
		this.combosize = combosize;
		this.verbosity = verbosity;

		if (filenameCombinations.length() > 0)
			this.filenameCombinations = filenameCombinations;
	}

	@Override
	public void run() {

		System.out.print("Welcome to " + appName + " " + version + "\n\n");

		// Initialize logger
		String logDirectory = new File(directoryOutput, "log").getAbsolutePath();
		initializeDrabmeLogger(logDirectory);

		// Start timer
		Timer timer = new Timer();

		// Load boolean models
		ArrayList<BooleanModel> booleanModels = new ArrayList<BooleanModel>();
		loadModels(booleanModels);

		// Load drugs
		DrugPanel drugPanel = loadDrugPanel(booleanModels);

		// Load perturbation panel
		PerturbationPanel perturbationPanel = loadPerturbationPanel(drugPanel);

		// Load output weights
		ModelOutputs outputs = loadModelOutputs(booleanModels);

		// Create temp directory
		File tempDir = new File(directoryTmp);
		if (!createDirectory(directoryTmp, logger))
			return;

		// Run simulations and compute Statistics
		runDrugResponseAnalyzer(perturbationPanel, booleanModels, outputs, logDirectory);

		// Generate Summary Reports for Drabme
		generateModelWiseResponses(perturbationPanel);
		generateModelWiseSynergies(perturbationPanel, booleanModels);
		generateEnsembleWiseResponses(perturbationPanel);
		generateEnsembleWiseSynergies(perturbationPanel);

		// Clean tmp directory
		cleanTmpDirectory(tempDir);

		// Stop timer
		timer.stopTimer();
		logger.outputHeader(1, "\nThe end");

		closeLogger(timer);
	}

	private void runDrugResponseAnalyzer(PerturbationPanel perturbationPanel, ArrayList<BooleanModel> booleanModels,
			ModelOutputs outputs, String logDirectory) {

		DrugResponseAnalyzer dra = new DrugResponseAnalyzer(perturbationPanel, booleanModels, outputs, directoryTmp,
				logger, logDirectory);
		dra.analyze();
		mergeLogFiles(dra, logDirectory);
		dra.computeStatistics();
	}

	/**
	 * Merges all Drabme simulation logging files into one
	 */
	public void mergeLogFiles(DrugResponseAnalyzer dra, String logDirectory) {
		String mergedLogFilename = new File(logDirectory, "Drabme_simulations_log.txt").getAbsolutePath();
		try {
			mergeFiles(dra.simulationFileList, mergedLogFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the 'extra' effect of combinations vs drug subset that has the strongest
	 * response i.e. pairwise combination: PD-PI vs min(PD, PI) i.e. threeway
	 * combination: PD-PI-5Z vs min(PD-PI, PD-5Z, PI-5Z)
	 * 
	 * @param perturbationPanel
	 */
	private void generateEnsembleWiseSynergies(PerturbationPanel perturbationPanel) {
		// File to store excess effects over agerage responses in
		String filename = new File(directoryOutput, "output_" + nameProject + "_ensemblewise_synergies.tab")
				.getAbsolutePath();

		logger.outputHeader(1, "Combinatorial response in excess over subsets (ensemble-wise synergy)");
		logger.outputStringMessageToFile(filename, "Perturbation" + "\t" + "Response excess over subset");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				logger.outputStringMessageToFile(filename, perturbation.getName() + "\t"
						+ perturbationPanel.getPredictedAverageCombinationResponse(perturbation));
			}
		}
	}

	/**
	 * The average predicted responses for each perturbation
	 * 
	 * @param perturbationPanel
	 */
	private void generateEnsembleWiseResponses(PerturbationPanel perturbationPanel) {
		// File to store average responses in
		String filename = new File(directoryOutput, "output_" + nameProject + "_ensemblewise_responses.tab")
				.getAbsolutePath();

		logger.outputHeader(1, "Ensemble average responses");
		logger.outputStringMessageToFile(filename, "Perturbation" + "\t" + "Ensemble average response");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			logger.outputStringMessageToFile(filename,
					perturbation.getName() + "\t" + perturbation.getAveragePredictedResponse());
		}
	}

	/**
	 * Synergies vs non-synergies for each perturbation
	 * 
	 * @param perturbationPanel
	 * @param booleanModels
	 */
	private void generateModelWiseSynergies(PerturbationPanel perturbationPanel,
			ArrayList<BooleanModel> booleanModels) {
		// File to store drug synergies in
		String filename = new File(directoryOutput, "output_" + nameProject + "_modelwise_synergies.tab")
				.getAbsolutePath();

		logger.outputHeader(1, "Synergies vs Non-synergies per perturbation");
		logger.outputStringMessageToFile(filename, "Perturbation" + "\t" + "Synergies" + "\t" + "Non-synergies");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				logger.outputStringMessageToFile(filename, perturbation.getName() + "\t"
						+ perturbation.getSynergyPredictions() + "\t" + perturbation.getNonSynergyPredictions());
			}

			// if synergies + non-synergies for one combination exceeds number of models
			// then there is an error
			// in storing synergies, previously happened due to bug in hash generation for
			// drugs
			if ((perturbation.getSynergyPredictions() + perturbation.getNonSynergyPredictions()) > booleanModels
					.size()) {
				logger.outputStringMessage(1, "ERROR: Synergy and non-synergy count error: "
						+ perturbation.getSynergyPredictions() + perturbation.getNonSynergyPredictions());
				System.exit(1);
			}
		}
	}

	/**
	 * Predicted responses for every model for each perturbation
	 * 
	 * @param perturbationPanel
	 */
	private void generateModelWiseResponses(PerturbationPanel perturbationPanel) {
		// File to store perturbation responses
		String filename = new File(directoryOutput, "output_" + nameProject + "_modelwise_responses.tab")
				.getAbsolutePath();

		logger.outputHeader(1, "Drug perturbation responses");
		logger.outputStringMessageToFile(filename, "Perturbation" + "\t" + "Average" + "\t" + "SD" + "\t" + "Data");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			String individualresponses = "";
			float[] predictions = perturbation.getPredictions();
			for (int j = 0; j < predictions.length; j++) {
				individualresponses += "\t" + predictions[j];
			}

			logger.outputStringMessageToFile(filename,
					perturbation.getName() + "\t" + perturbation.getAveragePredictedResponse() + "\t"
							+ perturbation.getStandardDeviationPredictedResponse() + individualresponses);
		}

	}

	private PerturbationPanel loadPerturbationPanel(DrugPanel drugPanel) {
		logger.outputHeader(2, "Defining perturbations");

		Drug[][] drugperturbations = null;

		if (filenameCombinations == null) {
			drugperturbations = drugPanel.getCombinations(combosize);
		} else {
			try {
				drugperturbations = drugPanel.loadCombinationsFromFile(filenameCombinations);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		PerturbationPanel perturbationPanel = new PerturbationPanel(drugperturbations, logger);
		return perturbationPanel;
	}

	private DrugPanel loadDrugPanel(ArrayList<BooleanModel> booleanModels) {
		logger.outputHeader(2, "Loading drug panel");
		DrugPanel drugPanel = null;
		try {
			drugPanel = new DrugPanel(filenameDrugs, logger);
		} catch (IOException e) {
			e.printStackTrace();
			File file = new File(directoryOutput);
			filenameDrugs = file.getParent() + "/" + "drugpanel.tab";
			logger.outputStringMessage(1, "Cannot find drugpanel file, generating template file: " + filenameDrugs);
			try {
				DrugPanel.writeDrugPanelFileTemplate(filenameDrugs);
				drugPanel = new DrugPanel(filenameDrugs, logger);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}

		drugPanel.checkDrugTargets(booleanModels);

		return drugPanel;
	}

	private void loadModels(ArrayList<BooleanModel> booleanModels) {
		logger.outputHeader(2, "Loading Boolean models");

		try {
			this.loadBooleanModels(directoryModels, booleanModels);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private ModelOutputs loadModelOutputs(ArrayList<BooleanModel> booleanModels) {
		ModelOutputs outputs = null;
		try {
			outputs = new ModelOutputs(filenameModelOutputs, logger);
		} catch (IOException e) {
			e.printStackTrace();
			File file = new File(directoryOutput);
			filenameModelOutputs = file.getParent() + "/" + "modeloutputs.tab";
			logger.outputStringMessage(1,
					"Couldn't load model outputs file, generating template file: " + filenameModelOutputs);
			try {
				ModelOutputs.saveModelOutputsFileTemplate(filenameModelOutputs);
				outputs = new ModelOutputs(filenameModelOutputs, logger);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}

		logger.outputHeader(1, "Model Outputs");
		logger.outputLines(1, outputs.getModelOutputs());

		outputs.checkModelOutputNodeNames(booleanModels.get(0));

		return outputs;
	}

	private void cleanTmpDirectory(File tempDir) {
		if (!preserveTmpFiles) {
			logger.outputStringMessage(2, "\n" + "Deleting temporary directory: " + tempDir.getAbsolutePath());
			deleteFilesFromDirectory(tempDir);
			tempDir.delete();
		}
	}

	private void closeLogger(Timer timer) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar calendarData = Calendar.getInstance();
		logger.outputStringMessage(1, "End: " + dateFormat.format(calendarData.getTime()));
		logger.outputStringMessage(1, "Analysis completed in " + timer.getHoursOfDuration() + " hours, "
				+ timer.getMinutesOfDuration() + " minutes, and " + timer.getSecondsOfDuration() + " seconds ");
		logger.outputStringMessage(1, "\nWith that we say thank you and good bye!");
		logger.finish();
	}

	private void loadBooleanModels(String directory, ArrayList<BooleanModel> booleanModels) throws IOException {

		File[] files = new File(directory).listFiles();
		File file;

		for (int i = 0; i < files.length; i++) {
			if (files[i].getAbsolutePath().toLowerCase().contains("gitsbe")) {
				file = files[i];
				booleanModels.add(new BooleanModel(file.getPath(), logger));
			}
		}
	}

	private void initializeDrabmeLogger(String directory) {
		try {
			String filenameOutput = appName + "_" + nameProject + "_log.txt";
			logger = new Logger(filenameOutput, directory, verbosity, true);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar calendarData = Calendar.getInstance();
		logger.outputHeader(1, appName + " " + version);
		logger.outputStringMessage(1, "Start: " + dateFormat.format(calendarData.getTime()));
	}
}
