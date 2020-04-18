package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.drabme.drug.DrugPanel;
import eu.druglogics.drabme.input.Config;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import java.util.ArrayList;

import static java.lang.Math.min;

/**
 * Each {@link ResponseModel} is based on a single {@link BooleanModel} instance
 * and splits the original model to a collection of {@link PerturbationModel}s,
 * where each {@link PerturbationModel} is related to a specific
 * {@link Perturbation} as specified in the {@link PerturbationPanel} Class.
 * 
 * @author asmund
 *
 */

public class ResponseModel {

	private BooleanModel originalModel; // The original (unperturbed) model
	private PerturbationPanel perturbationPanel;
	private Logger logger;
	private String modelName;
	private ArrayList<PerturbationModel> perturbationModels; // Set of boolean models extended with perturbations
	private ModelPredictions modelPredictions;

	public ResponseModel(BooleanModel booleanModel, PerturbationPanel perturbationPanel, Logger logger) {
		this.originalModel = booleanModel;
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
			perturbationModels.add(new PerturbationModel(originalModel,
				perturbationPanel.getPerturbations()[index], logger));
		}
	}

	public void simulateResponses(String directoryTmp) throws Exception {

		for (PerturbationModel perturbationModel : perturbationModels) {
			logger.outputStringMessage(2, ""); // Add blank line for better visualization of results

			Perturbation perturbation = perturbationModel.getPerturbation();

			// Calculate attractors, then determine global output
			perturbationModel.calculateAttractors(directoryTmp);
			perturbationModel.calculateGlobalOutput();

			// Store response for perturbation set
			if (perturbationModel.hasGlobalOutput()) {
				logger.outputStringMessage(2, "Adding predicted response for perturbation "
						+ perturbation.getName() + ": " + perturbationModel.getGlobalOutput());
				perturbation.addPrediction(perturbationModel.getGlobalOutput());
			}

			// Check if perturbation models with 2 or more drugs are synergistic
			if (perturbation.getDrugs().length >= 2) {
				checkCombinationModelForSynergy(perturbation.getDrugs());
			}
		}
	}

	/**
	 * Checks if the {@link PerturbationModel} related to the given drug
	 * <code>combination</code> (must be 2 or more drugs!) is synergistic or not, by
	 * comparing the globaloutput of that perturbed model with the minimum
	 * globaloutput of the models perturbed with each of the drug combination's
	 * subsets (HSA rule) or with the product of the normalized globaloutput
	 * values of these models (Bliss rule). Which method will be used is based on
	 * the {@link eu.druglogics.drabme.input.ConfigParametersDrabme#synergy_method} value.
	 *
	 * @param combination
	 */
	private void checkCombinationModelForSynergy(Drug[] combination) {
		if (combination.length == 2)
			logger.debug("Combination: " + combination[0].getName() + " " + combination[1].getName());

		int drugHash = DrugPanel.getDrugSetHash(combination);
		logger.debug("DrugHash:" + drugHash);

		// get the combined perturbation's drug subsets
		PerturbationModel drugCombPerturbationModel = perturbationModels.get(getIndexOfPerturbationModel(combination));
		Perturbation perturbation = drugCombPerturbationModel.getPerturbation();

		Drug[][] subsets = DrugPanel.getCombinationSubsets(combination);
		String drugCombination = PerturbationPanel.getCombinationName(combination);

		for (Drug[] subset : subsets) {
			logger.debug("Combination subsets:" + PerturbationPanel.getCombinationName(subset));
		}

		boolean computable = true;

		// Check if model for combination and all subsets have attractor(s)
		if (!drugCombPerturbationModel.hasGlobalOutput())
			computable = false;

		for (Drug[] subset : subsets) {
			if (!perturbationModels.get(getIndexOfPerturbationModel(subset)).hasGlobalOutput())
				computable = false;
		}

		if (computable) {
			if (Config.getInstance().getSynergyMethod().equals("hsa")) {
				float minimumGlobalOutput =
					perturbationModels.get(getIndexOfPerturbationModel(subsets[0])).getGlobalOutput();

				// find the subset with the minimum global output
				for (Drug[] subset : subsets) {
					int index = getIndexOfPerturbationModel(subset);
					minimumGlobalOutput =
						min(minimumGlobalOutput, perturbationModels.get(index).getGlobalOutput());
				}

				if (drugCombPerturbationModel.getGlobalOutput() < minimumGlobalOutput) {
					perturbation.addSynergyPrediction();
					modelPredictions.addSynergyPrediction(drugCombination);
					logger.outputStringMessage(2, drugCombination + " is synergistic (HSA)");
				} else {
					perturbation.addNonSynergyPrediction();
					modelPredictions.addNonSynergyPrediction(drugCombination);
					logger.outputStringMessage(2, drugCombination + " is NOT synergistic (HSA)");
				}
			} else { // bliss
				// calculate expected bliss normalized global output from the subsets
				float expectedBlissGlobalOutput = 1;
				for (Drug[] subset : subsets) {
					int index = getIndexOfPerturbationModel(subset);
					expectedBlissGlobalOutput *= perturbationModels.get(index).getNormalizedGlobalOutput();
				}

				if (drugCombPerturbationModel.getNormalizedGlobalOutput() < expectedBlissGlobalOutput) {
					perturbation.addSynergyPrediction();
					modelPredictions.addSynergyPrediction(drugCombination);
					logger.outputStringMessage(2, drugCombination + " is synergistic (Bliss)");
				} else {
					perturbation.addNonSynergyPrediction();
					modelPredictions.addNonSynergyPrediction(drugCombination);
					logger.outputStringMessage(2, drugCombination + " is NOT synergistic (Bliss)");
				}
			}
		} else {
			modelPredictions.addNAPrediction(drugCombination);
			logger.outputStringMessage(2, drugCombination
				+ " cannot be evaluated for synergy (lacking attractors)");
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
