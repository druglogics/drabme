package eu.druglogics.drabme.perturbation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores for each (response) model its predictions for each drug
 * combination. The prediction can be either a synergy (1), a non-synergy (0),
 * or NA (the case where the respective perturbation drugs or any of the subset
 * drugs were applied to a model which resulted in no stable states)
 * 
 * @author john
 *
 */
public class ModelPredictions {
	private String modelName;
	private Map<String, String> drugPredictions;

	ModelPredictions(String modelName) {
		this.modelName = modelName;
		drugPredictions = new HashMap<>();
	}

	void addSynergyPrediction(String drugCombination) {
		drugPredictions.put(drugCombination, "1");
	}

	void addNonSynergyPrediction(String drugCombination) {
		drugPredictions.put(drugCombination, "0");
	}

	void addNAPrediction(String drugCombination) {
		drugPredictions.put(drugCombination, "NA");
	}

	public String getPredictions() {
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, String> entry : drugPredictions.entrySet()) {
			String str = "Drugs: " + entry.getKey() + "  Prediction: " + entry.getValue() + "\n";
			result.append(str);
		}

		return result.toString();
	}

	/**
	 * Return a tab-seperated String starting with the <i>modelName</i> and then the
	 * prediction value for each drug combination as in the order specified in the
	 * DrugCombinationsList
	 * 
	 * @param drugCombinationsList
	 */
	public String getModelPredictionsVerbose(ArrayList<String> drugCombinationsList) {
		StringBuilder result = new StringBuilder(getModelName());

		for (String drugCombination : drugCombinationsList) {
			String str = "\t" + drugPredictions.get(drugCombination);
			result.append(str);
		}

		return result.toString();
	}

	String getModelName() {
		return modelName;
	}

}
