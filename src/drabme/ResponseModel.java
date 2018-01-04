package drabme;

import gitsbe.Logger;
import gitsbe.ModelOutputs;

import static java.lang.Math.*;
import java.io.IOException;
import java.util.ArrayList;

import gitsbe.BooleanModel;

/**
 * Each ResponseModel is based on a single BooleanModel instance, and splits the
 * original model to a collection of PerturbationModel, where each
 * PerturbationModel is the specific instance of models with perturbations
 * specified in the PerturbationPanel instance
 * 
 * @author asmund
 *
 */

public class ResponseModel {

	private BooleanModel originalModel; // The original (unperturbed) model
	private ModelOutputs modelOutputs;
	private PerturbationPanel perturbationPanel;
	private Logger logger;
	private String modelName;
	private ArrayList<PerturbationModel> perturbationModels; // Set of boolean models extended with perturbations

	public ResponseModel(BooleanModel booleanModel, ModelOutputs modelOutputs, PerturbationPanel perturbationPanel,
			Logger logger) {
		this.originalModel = booleanModel;
		this.modelOutputs = modelOutputs;
		this.perturbationPanel = perturbationPanel;
		this.logger = logger;
		this.modelName = booleanModel.getModelName() + "_responsemodel";
	}

	public String getModelName() {
		return this.modelName;
	}

	public void initializeResponseModel() {
		logger.outputStringMessage(2, "Initializing response model: " + this.getModelName() + "\n");
		perturbationModels = new ArrayList<PerturbationModel>();

		// Define model for each perturbation set
		for (int index = 0; index < perturbationPanel.getNumberOfPerturbations(); index++) {
			perturbationModels.add(new PerturbationModel(originalModel, perturbationPanel.getPerturbations()[index],
					modelOutputs, logger));
		}
	}

	public void simulateResponses(String directoryTmp) throws IOException {
		for (int i = 0; i < perturbationModels.size(); i++) {
			logger.outputStringMessage(2, ""); // Add blank line for better visualization of results

			// Calculate stable state(s), then determine global output
			perturbationModels.get(i).calculateStableStatesVC(directoryTmp);
			perturbationModels.get(i).calculateGlobalOutput();

			// Store response for perturbation set
			if (perturbationModels.get(i).hasGlobalOutput()) {
				logger.outputStringMessage(2,
						"Adding predicted response for perturbation "
								+ perturbationModels.get(i).getPerturbation().getName() + ": "
								+ perturbationModels.get(i).getGlobalOutput());
				perturbationModels.get(i).getPerturbation().addPrediction(perturbationModels.get(i).getGlobalOutput());
			}

			// Check for synergies among drugs in combination (more than two drugs)
			if (perturbationModels.get(i).getPerturbation().getDrugs().length >= 2) {
				this.isCombinationSynergistic(perturbationModels.get(i).getPerturbation().getDrugs());
			}
		}
	}

	public boolean isCombinationSynergistic(Drug[] combination) {
		boolean value = false;

		if (combination.length == 2)
			logger.debug("Combination: " + combination[0].getName() + " " + combination[1].getName());

		logger.debug("DrugHash:" + DrugPanel.getDrugSetHash(combination));

		PerturbationModel combinationResponse = perturbationModels.get(getIndexOfPerturbationModel(combination));
		Perturbation perturbation = combinationResponse.getPerturbation();

		Drug[][] subsets = DrugPanel.getCombinationSubsets(combination);

		for (int i = 0; i < subsets.length; i++) {
			logger.debug("Combination subsets:" + PerturbationPanel.getCombinationName(subsets[i]));
		}

		boolean computable = true;

		// Check if model for combination and all subsets have stable state(s)
		if (!combinationResponse.hasGlobalOutput())
			computable = false;

		for (int i = 0; i < subsets.length; i++) {
			if (!perturbationModels.get(getIndexOfPerturbationModel(subsets[i])).hasGlobalOutput())
				computable = false;
		}

		if (computable) {
			int minimumGlobalOutput = perturbationModels.get(getIndexOfPerturbationModel(subsets[0])).getGlobalOutput();

			// find the subset with the minimum global output
			for (int i = 0; i < subsets.length; i++) {
				int index = getIndexOfPerturbationModel(subsets[i]);
				minimumGlobalOutput = min(minimumGlobalOutput, perturbationModels.get(index).getGlobalOutput());
			}

			if (combinationResponse.getGlobalOutput() < minimumGlobalOutput) {
				perturbation.addSynergyPrediction();
				value = true;
				logger.outputStringMessage(2, PerturbationPanel.getCombinationName(combination) + " is synergistic");
			} else {
				perturbation.addNonSynergyPrediction();
				value = false;
				logger.outputStringMessage(2,
						PerturbationPanel.getCombinationName(combination) + " is NOT synergistic");
			}
		} else {
			logger.outputStringMessage(2, PerturbationPanel.getCombinationName(combination)
					+ " cannot be evaluated for synergy (lacking stable state(s))");
		}

		return value;
	}

	private int getIndexOfPerturbationModel(Drug[] drugs) {

		// Compute hash for drugs in parameter
		int hashA = DrugPanel.getDrugSetHash(drugs);

		// Compare with drugs in each perturbationModel
		for (int i = 0; i < perturbationModels.size(); i++) {
			int hashB = perturbationModels.get(i).getPerturbation().getPerturbationHash();
			if (hashB == hashA) {
				return i;
			}
		}

		return -1;
	}

}
