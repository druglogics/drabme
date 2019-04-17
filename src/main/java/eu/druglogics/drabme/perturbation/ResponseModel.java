package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.drabme.drug.DrugPanel;
import eu.druglogics.drabme.input.Config;
import eu.druglogics.gitsbe.util.Logger;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.model.BooleanModel;

import static java.lang.Math.*;
import java.io.IOException;
import java.util.ArrayList;

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
	private ModelPredictions modelPredictions;

	public ResponseModel(BooleanModel booleanModel, ModelOutputs modelOutputs,
						 PerturbationPanel perturbationPanel, Logger logger) {
		this.originalModel = booleanModel;
		this.modelOutputs = modelOutputs;
		this.perturbationPanel = perturbationPanel;
		this.logger = logger;
		this.modelName = booleanModel.getModelName() + "_responsemodel";
		this.modelPredictions = new ModelPredictions(originalModel.getModelName());
	}

	private String getModelName() {
		return this.modelName;
	}

	public void initializeResponseModel() {
		logger.outputStringMessage(2, "Initializing response model: " + this.getModelName() + "\n");
		perturbationModels = new ArrayList<>();

		// Define model for each perturbation set
		for (int index = 0; index < perturbationPanel.getNumberOfPerturbations(); index++) {
			perturbationModels.add(new PerturbationModel(
					originalModel, perturbationPanel.getPerturbations()[index], modelOutputs, logger
			));
		}
	}

	public void simulateResponses(String directoryTmp) throws IOException {

		for (PerturbationModel perturbationModel : perturbationModels) {
			logger.outputStringMessage(2, ""); // Add blank line for better visualization of results

			Perturbation perturbation = perturbationModel.getPerturbation();

			// Calculate stable state(s), then determine global output
			perturbationModel.calculateStableStatesVC(directoryTmp, Config.getInstance().getAttractorTool());
			perturbationModel.calculateGlobalOutput();

			// Store response for perturbation set
			if (perturbationModel.hasGlobalOutput()) {
				logger.outputStringMessage(2, "Adding predicted response for perturbation "
						+ perturbation.getName() + ": " + perturbationModel.getGlobalOutput());
				perturbation.addPrediction(perturbationModel.getGlobalOutput());
			}

			// Check for synergies among drugs in combination (more than two drugs)
			if (perturbation.getDrugs().length >= 2) {
				checkCombinationSynergy(perturbation.getDrugs());
			}
		}
	}

	private void checkCombinationSynergy(Drug[] combination) {

		if (combination.length == 2)
			logger.debug("Combination: " + combination[0].getName() + " " + combination[1].getName());

		int drugHash = DrugPanel.getDrugSetHash(combination);
		logger.debug("DrugHash:" + drugHash);

		PerturbationModel combinationResponse = perturbationModels.get(getIndexOfPerturbationModel(combination));
		Perturbation perturbation = combinationResponse.getPerturbation();

		Drug[][] subsets = DrugPanel.getCombinationSubsets(combination);
		String drugCombination = PerturbationPanel.getCombinationName(combination);

		for (Drug[] subset : subsets) {
			logger.debug("Combination subsets:" + PerturbationPanel.getCombinationName(subset));
		}

		boolean computable = true;

		// Check if model for combination and all subsets have stable state(s)
		if (!combinationResponse.hasGlobalOutput())
			computable = false;

		for (Drug[] subset : subsets) {
			if (!perturbationModels.get(getIndexOfPerturbationModel(subset)).hasGlobalOutput())
				computable = false;
		}

		if (computable) {
			float minimumGlobalOutput =
					perturbationModels.get(getIndexOfPerturbationModel(subsets[0])).getGlobalOutput();

			// find the subset with the minimum global output
			for (Drug[] subset : subsets) {
				int index = getIndexOfPerturbationModel(subset);
				minimumGlobalOutput =
						min(minimumGlobalOutput, perturbationModels.get(index).getGlobalOutput());
			}

			if (combinationResponse.getGlobalOutput() < minimumGlobalOutput) {
				perturbation.addSynergyPrediction();
				modelPredictions.addSynergyPrediction(drugCombination);
				logger.outputStringMessage(2, drugCombination + " is synergistic");
			} else {
				perturbation.addNonSynergyPrediction();
				modelPredictions.addNonSynergyPrediction(drugCombination);
				logger.outputStringMessage(2, drugCombination + " is NOT synergistic");
			}
		} else {
			modelPredictions.addNAPrediction(drugCombination);
			logger.outputStringMessage(2,
					drugCombination + " cannot be evaluated for synergy (lacking stable state(s))");
		}
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

	public ModelPredictions getModelPredictions() {
		return modelPredictions;
	}

}
