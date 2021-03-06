package eu.druglogics.drabme;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.drabme.drug.DrugPanel;
import eu.druglogics.drabme.drug.DrugResponseAnalyzer;
import eu.druglogics.drabme.input.Config;
import eu.druglogics.drabme.perturbation.ModelPredictions;
import eu.druglogics.drabme.perturbation.Perturbation;
import eu.druglogics.drabme.perturbation.PerturbationPanel;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.FileDeleter;
import eu.druglogics.gitsbe.util.Logger;
import eu.druglogics.gitsbe.util.Timer;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import static eu.druglogics.gitsbe.util.FileDeleter.cleanDirectory;
import static eu.druglogics.gitsbe.util.Util.*;

/**
 *  Drabme - Drug Response Analysis of Boolean Models from Evolution
 * 
 * Copyright Asmund Flobak 2014-2015-2016-2017
 * 
 * email: asmund.flobak@ntnu.no
 * 
 * Uses bnet_reduction (BNReduction.sh) by Veliz-Cuba
 * A. Veliz-Cuba, B. Aguilar, F. Hinkelmann and R. Laubenbacher.
 * Steady state analysis of Boolean molecular network models via model
 * reduction and computational algebra. BMC Bioinformatics, 2014.
 *
 */
public class Drabme implements Runnable {

	public static String appName;
	private static String version;

	private String projectName;
	private String filenameDrugs;
	private String filenamePerturbations;
	private String filenameModelOutputs;
	private String filenameConfig;
	private String directoryModels;
	private String directoryOutput;
	private String directoryTmp;

	private Logger logger;

	public Drabme(String projectName, String filenameDrugs,
				  String filenamePerturbations, String filenameModelOutputs,
				  String filenameConfig, String directoryModels,
				  String directoryOutput, String directoryTmp) {

		// Set variables
		this.projectName = projectName;
		this.filenameDrugs = filenameDrugs;
		this.filenamePerturbations = filenamePerturbations;
		this.filenameModelOutputs = filenameModelOutputs;
		this.filenameConfig = filenameConfig;
		this.directoryModels = directoryModels;
		this.directoryOutput = directoryOutput;
		this.directoryTmp = directoryTmp;
	}

	@Override
	public void run() {

        loadDrabmeProperties();

		System.out.print("Welcome to " + appName + " " + version + "\n\n");

		createOutputDirectory();

		String directoryLog = new File(directoryOutput, "log").getAbsolutePath();
		createLogDirectory(directoryLog);

		// Initialize logger
		initializeDrabmeLogger(directoryLog);

		// Start timer
		Timer timer = new Timer();

		// Load config file
		loadConfigFile();

		// Load boolean models
		ArrayList<BooleanModel> booleanModels = new ArrayList<>();
		loadModels(booleanModels);

		// Load drugs
		DrugPanel drugPanel = loadDrugPanel(booleanModels);

		// Load perturbation panel
		PerturbationPanel perturbationPanel = loadPerturbationPanel(drugPanel);

		// Load output weights
		loadModelOutputs(booleanModels);

		createTmpDirectory();
		activateFileDeleter();

		// Run simulations and compute Statistics
		DrugResponseAnalyzer dra = new DrugResponseAnalyzer(perturbationPanel, booleanModels,
			directoryTmp, logger, directoryLog);
		runDrugResponseAnalyzer(dra, directoryLog);

		// Generate Summary Reports for Drabme
		generateModelWiseResponses(perturbationPanel);
		generateModelWiseSynergies(perturbationPanel, booleanModels);
		generateEnsembleWiseResponses(perturbationPanel);
		generateEnsembleWiseSynergies(perturbationPanel);
		generateModelPredictions(dra.modelPredictionsList, perturbationPanel);

		// Clean tmp directory
		cleanDirectory(logger);

		// Compress log and tmp dirs to preserve storage space
		if (Config.getInstance().compressLogAndTmpFiles()) {
			archive(directoryLog, directoryTmp);
		}

		// Stop timer
		timer.stopTimer();
		logger.outputHeader(1, "\nThe end");

		logger.writeLastLoggingMessage(timer);
	}

	private void loadDrabmeProperties() {
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader()
								.getResourceAsStream("drabme.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        version = properties.getProperty("version");
        appName = properties.getProperty("appName");
    }

	private void createOutputDirectory() {
		try {
			createDirectory(directoryOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createLogDirectory(String logDirectory) {
		try {
			createDirectory(logDirectory);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createTmpDirectory() {
		try {
			createDirectory(directoryTmp, logger);

			// Hack: if full BNReduction is used, copy FPGB.m2 file in the drabme_tmp
			if (Config.getInstance().getAttractorTool().equals("bnet_reduction")) {
				String directoryBNET = System.getenv("BNET_HOME");
				Files.copy(Paths.get(directoryBNET + "/FPGB.m2"),
						Paths.get(directoryTmp + "/FPGB.m2"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String filename = new File(directoryOutput, projectName + "_model_predictions.tab")
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
			String modelPredictionsString =
					modelPredictions.getModelPredictionsVerbose(drugCombinationsList);
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

	private void activateFileDeleter() {
		FileDeleter fileDeleter = new FileDeleter(directoryTmp);
		if (Config.getInstance().deleteTmpDir()) {
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
		String mergedLogFilename =
				new File(logDirectory, appName + "_simulations.log").getAbsolutePath();
		try {
			mergeFiles(dra.simulationFileList, mergedLogFilename, true);
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
		// File to store excess effects over average responses in
		String filename = new File(directoryOutput, projectName + "_ensemblewise_synergies.tab")
				.getAbsolutePath();

		logger.outputHeader(1, "Combinatorial response in excess over subsets (ensemble-wise synergy)");
		logger.outputStringMessageToFile(filename, "Perturbation" + "\t" + "Response excess over subset");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				logger.outputStringMessageToFile(filename, perturbation.getName() + "\t"
					+ perturbationPanel.getAverageResponseExcessOverSubsets(perturbation));
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
		String filename = new File(directoryOutput, projectName + "_ensemblewise_responses.tab")
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
		String filename = new File(directoryOutput, projectName + "_modelwise_synergies.tab")
			.getAbsolutePath();

		logger.outputHeader(1, "Synergies vs Non-synergies per perturbation");
		logger.outputStringMessageToFile(filename, "Perturbation" + "\t" + "Synergies" + "\t" + "Non-synergies");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			if (perturbation.getDrugs().length >= 2) {
				logger.outputStringMessageToFile(filename, perturbation.getName() + "\t"
					+ perturbation.getSynergyPredictions() + "\t"
					+ perturbation.getNonSynergyPredictions());
			}

			// if synergies + non-synergies for one combination exceeds the number of models
			// then there is an error in storing the synergies, previously happened due to a
			// bug in hash generation for drugs
			if ((perturbation.getSynergyPredictions() + perturbation.getNonSynergyPredictions())
					> booleanModels.size()) {
				logger.error("Synergy and non-synergy count error: "
						+ perturbation.getSynergyPredictions() + perturbation.getNonSynergyPredictions());
				abort();
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
		String filename = new File(directoryOutput, projectName + "_modelwise_responses.tab")
			.getAbsolutePath();

		logger.outputHeader(1, "Drug perturbation responses");
		logger.outputStringMessageToFile(filename, "Perturbation" + "\t" + "Average"
													     + "\t" + "SD" + "\t" + "Data");

		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {
			Perturbation perturbation = perturbationPanel.getPerturbations()[i];

			StringBuilder individualResponses = new StringBuilder();
			float[] predictions = perturbation.getPredictions();
			for(float prediction : predictions) {
				String str = "\t" + prediction;
				individualResponses.append(str);
			}

			logger.outputStringMessageToFile(filename,perturbation.getName() + "\t"
				+ perturbation.getAveragePredictedResponse() + "\t"
				+ perturbation.getStandardDeviationPredictedResponse()
				+ individualResponses);
		}
	}

	private PerturbationPanel loadPerturbationPanel(DrugPanel drugPanel) {
		logger.outputHeader(2, "Defining perturbations");

		Drug[][] drugPerturbations = null;

		if (filenamePerturbations == null || filenamePerturbations.trim().isEmpty()) {
			drugPerturbations = drugPanel.getDrugCombinations(Config.getInstance().getCombinationSize());
		} else {
			try {
				drugPerturbations = drugPanel.loadCombinationsFromFile(filenamePerturbations);
			} catch (Exception e) {
				e.printStackTrace();
				abort();
			}
		}

		PerturbationPanel perturbationPanel = null;

		try {
			perturbationPanel = new PerturbationPanel(drugPerturbations, logger);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			abort();
		}

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
			logger.outputStringMessage(1, "Cannot find drugpanel file, generating template file: "
					+ filenameDrugs);
			try {
				DrugPanel.writeDrugPanelFileTemplate(filenameDrugs);
				drugPanel = new DrugPanel(filenameDrugs, logger);
			} catch (Exception e1) {
				e1.printStackTrace();
				abort();
			}
		} catch (ConfigurationException e) {
			abort();
		}

		drugPanel.checkDrugTargets(booleanModels);

		return drugPanel;
	}

	private void loadModels(ArrayList<BooleanModel> booleanModels) {
		logger.outputHeader(2, "Loading Boolean models");

		this.loadBooleanModels(directoryModels, booleanModels);
	}

	private void loadModelOutputs(ArrayList<BooleanModel> booleanModels) {
		try {
			ModelOutputs.reset(); // in case Gitsbe is executed before Drabme, Singleton needs to be reset
			ModelOutputs.init(filenameModelOutputs, logger);
		} catch (Exception e) {
			e.printStackTrace();
			abort();
		}

		logger.outputHeader(1, "Model Outputs");
		logger.outputLines(1, ModelOutputs.getInstance().getModelOutputsVerbose());

		ModelOutputs.getInstance().checkModelOutputNodeNames(booleanModels.get(0));
	}

	private void loadConfigFile() {
		try {
			Config.init(filenameConfig, logger);
		} catch (Exception e) {
			e.printStackTrace();
			abort();
		}

		// Now that we have the verbosity from the config, we can re-set it in the logger
		logger.setVerbosity(Config.getInstance().getVerbosity());
		logger.outputHeader(1, "Config options");
		logger.outputLines(1, Config.getInstance().getConfig());
	}

	private void loadBooleanModels(String directory, ArrayList<BooleanModel> booleanModels) {
		File[] files = new File(directory).listFiles();

		if(files == null || files.length == 0) {
			logger.error("No models are in the model directory. Drabme analysis stops.");
			abort();
		}
		else {
			for (File file : files) {
				String filename = file.getAbsolutePath();
				if (filename.toLowerCase().endsWith("gitsbe")) {
					booleanModels.add(new BooleanModel(
						filename, Config.getInstance().getAttractorTool(), logger));
				}
			}
		}
	}

	private void initializeDrabmeLogger(String directory) {
		try {
			String filenameOutput = appName + "_" + projectName + ".log";
			logger = new Logger(filenameOutput, directory, 3, true);
		} catch (IOException e) {
			e.printStackTrace();
			abort();
		}

		logger.writeFirstLoggingMessage(appName, version);
	}
}
