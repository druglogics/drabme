package eu.druglogics.drabme;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.drabme.drug.DrugPanel;
import eu.druglogics.drabme.drug.DrugResponseAnalyzer;
import eu.druglogics.drabme.perturbation.ModelPredictions;
import eu.druglogics.drabme.perturbation.Perturbation;
import eu.druglogics.drabme.perturbation.PerturbationPanel;
import eu.druglogics.gitsbe.util.Logger;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.util.Timer;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.FileDeleter;

import static eu.druglogics.gitsbe.util.Util.*;
import static eu.druglogics.gitsbe.util.FileDeleter.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

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

	public static String appName;
	private static String version;

	private int verbosity;

	private String nameProject;

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

        loadDrabmeProperties();

		System.out.print("Welcome to " + appName + " " + version + "\n\n");

		// Initialize logger
		String logDirectory = new File(directoryOutput, "log").getAbsolutePath();
		initializeDrabmeLogger(logDirectory);

		// Start timer
		Timer timer = new Timer();

		// Load boolean models
		ArrayList<BooleanModel> booleanModels = new ArrayList<>();
		loadModels(booleanModels);

		// Load drugs
		DrugPanel drugPanel = loadDrugPanel(booleanModels);

		// Load perturbation panel
		PerturbationPanel perturbationPanel = loadPerturbationPanel(drugPanel);

		// Load output weights
		ModelOutputs outputs = loadModelOutputs(booleanModels);

		// Create temp directory
		if (!createDirectory(directoryTmp, logger))
			return;
		initializeFileDeleter();

		// Run simulations and compute Statistics
		DrugResponseAnalyzer dra = new DrugResponseAnalyzer(perturbationPanel, booleanModels, outputs, directoryTmp,
				logger, logDirectory);
		runDrugResponseAnalyzer(dra, logDirectory);

		// Generate Summary Reports for Drabme
		generateModelWiseResponses(perturbationPanel);
		generateModelWiseSynergies(perturbationPanel, booleanModels);
		generateEnsembleWiseResponses(perturbationPanel);
		generateEnsembleWiseSynergies(perturbationPanel);
		generateModelPredictions(dra.modelPredictionsList, perturbationPanel);

		// Clean tmp directory
		cleanDirectory(logger);

		// Stop timer
		timer.stopTimer();
		logger.outputHeader(1, "\nThe end");

		logger.writeLastLoggingMessage(timer);
	}

    private void loadDrabmeProperties() {
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("drabme.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        version = properties.getProperty("version");
        appName = properties.getProperty("appName");
    }

    /**
	 * Each model's predicted synergies, non-synergies and NA results for each drug
	 * combination tested
	 * 
	 * @param modelPredictionsList
	 * @param perturbationPanel
	 */
	private void generateModelPredictions(ArrayList<ModelPredictions> modelPredictionsList,
			PerturbationPanel perturbationPanel) {
		String filename = new File(directoryOutput, "output_" + nameProject + "_model_predictions.tab")
				.getAbsolutePath();

		ArrayList<String> drugCombinationsList = getDrugCombinationsList(perturbationPanel);

		StringBuilder headerString = new StringBuilder("ModelName");
		for (String drugCombination : drugCombinationsList) {
			String str = "\t" + drugCombination;
			headerString.append(str);
		}

		logger.outputHeader(1, "Model Predictions");
		logger.outputStringMessageToFile(filename, headerString.toString());

		for (ModelPredictions modelPredictions : modelPredictionsList) {
			String modelPredictionsString = modelPredictions.getModelPredictionsVerbose(drugCombinationsList);
			logger.outputStringMessageToFile(filename, modelPredictionsString);
		}
	}

	/**
	 * Returns a list of Strings that represent the drug combinations (not the
	 * single drugs)
	 * 
	 * @param perturbationPanel
	 * @return
	 */
	private ArrayList<String> getDrugCombinationsList(PerturbationPanel perturbationPanel) {
		ArrayList<String> drugCombinationsList = new ArrayList<>();
		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				drugCombinationsList.add(perturbation.getName());
			}
		}
		return drugCombinationsList;
	}

	private void initializeFileDeleter() {
		FileDeleter fileDeleter = new FileDeleter(directoryTmp);
		if (!preserveTmpFiles) {
			fileDeleter.activate();
		}
	}

	private void runDrugResponseAnalyzer(DrugResponseAnalyzer dra, String logDirectory) {
		dra.analyze();
		dra.computeStatistics();
		mergeLogFiles(dra, logDirectory);
	}

	/**
	 * Merges all Drabme simulation logging files into one
	 */
	private void mergeLogFiles(DrugResponseAnalyzer dra, String logDirectory) {
		String mergedLogFilename = new File(logDirectory, appName + "_simulations.log").getAbsolutePath();
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
				logger.error("Synergy and non-synergy count error: "
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

			StringBuilder individualResponses = new StringBuilder();
			float[] predictions = perturbation.getPredictions();
			for(float prediction : predictions) {
				String str = "\t" + prediction;
				individualResponses.append(str);
			}

			logger.outputStringMessageToFile(filename,
					perturbation.getName() + "\t" + perturbation.getAveragePredictedResponse() + "\t"
							+ perturbation.getStandardDeviationPredictedResponse() + individualResponses);
		}

	}

	private PerturbationPanel loadPerturbationPanel(DrugPanel drugPanel) {
		logger.outputHeader(2, "Defining perturbations");

		Drug[][] drugPerturbations = null;

		if (filenameCombinations == null) {
			drugPerturbations = drugPanel.getCombinations(combosize);
		} else {
			try {
				drugPerturbations = drugPanel.loadCombinationsFromFile(filenameCombinations);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new PerturbationPanel(drugPerturbations, logger);
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

		this.loadBooleanModels(directoryModels, booleanModels);
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

	private void loadBooleanModels(String directory, ArrayList<BooleanModel> booleanModels) {
		File[] files = new File(directory).listFiles();

		if(files == null || files.length == 0) {
			logger.error("No models are in the model directory. Drabme analysis stops.");
			System.exit(1);
		}
		else {
			for (File file : files) {
				if (file.getAbsolutePath().toLowerCase().contains("gitsbe")) {
					booleanModels.add(new BooleanModel(file.getPath(), logger));
				}
			}
		}
	}

	private void initializeDrabmeLogger(String directory) {
		try {
			String filenameOutput = appName + "_" + nameProject + ".log";
			logger = new Logger(filenameOutput, directory, verbosity, true);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		logger.writeFirstLoggingMessage(appName, version);
	}
}
