package eu.druglogics.drabme.drug;

import eu.druglogics.drabme.Drabme;
import eu.druglogics.drabme.perturbation.ModelPredictions;
import eu.druglogics.drabme.perturbation.PerturbationPanel;
import eu.druglogics.drabme.perturbation.ResponseModel;
import eu.druglogics.gitsbe.util.Logger;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.model.BooleanModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class DrugResponseAnalyzer {

	private PerturbationPanel perturbationPanel;
	private final ArrayList<BooleanModel> booleanModels;
	private ModelOutputs modelOutputs;
	private String directoryTmp;
	private Logger logger;
	private String logDirectory;
	public ArrayList<String> simulationFileList;
	public ArrayList<ModelPredictions> modelPredictionsList;

	public DrugResponseAnalyzer(PerturbationPanel perturbationPanel, ArrayList<BooleanModel> booleanModels,
			ModelOutputs modelOutputs, String directoryTmp, Logger logger, String logDirectory) {
		this.perturbationPanel = perturbationPanel;
		this.booleanModels = booleanModels;
		this.modelOutputs = modelOutputs;
		this.directoryTmp = directoryTmp;
		this.logger = logger;
		this.logDirectory = logDirectory;
		this.simulationFileList = new ArrayList<String>();
		this.modelPredictionsList = new ArrayList<ModelPredictions>();
	}

	public void analyze() {
		IntStream.range(0, booleanModels.size()).parallel().forEach(modelIndex -> {
			try {
				runSimulation(modelIndex);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void runSimulation(int modelIndex) throws IOException {
		String modelName = booleanModels.get(modelIndex).getModelName();
		String filenameOutput = Drabme.appName + modelName.substring(modelName.lastIndexOf("_run_")) + "_log.txt";

		addFileToSimulationFileList(new File(logDirectory, filenameOutput).getAbsolutePath());

		// create new logger for each (parallel) simulation
		Logger simulation_logger = new Logger(filenameOutput, logDirectory, logger.getVerbosity(), true);
		simulation_logger.outputHeader(2, "Adding model " + modelName);

		ResponseModel responseModel = new ResponseModel(booleanModels.get(modelIndex), modelOutputs, perturbationPanel,
				simulation_logger);
		responseModel.initializeResponseModel();
		responseModel.simulateResponses(directoryTmp);

		addModelPredictionsToList(responseModel.getModelPredictions());

		simulation_logger.finish();
	}

	public void computeStatistics() {
		logger.outputHeader(1, "Calculating Statistics");
		for (int i = 0; i < perturbationPanel.getPerturbations().length; i++) {
			perturbationPanel.getPerturbations()[i].calculateStatistics();
		}
	}

	synchronized private void addFileToSimulationFileList(String filename) {
		simulationFileList.add(filename);
	}

	synchronized private void addModelPredictionsToList(ModelPredictions modelPredictions) {
		modelPredictionsList.add(modelPredictions);
	}
}
