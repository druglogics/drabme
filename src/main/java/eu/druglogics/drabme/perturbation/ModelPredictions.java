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

	public ModelPredictions(String modelName) {
		this.modelName = modelName;
		drugPredictions = new HashMap<String, String>();
	}

	public void addSynergyPrediction(String drugCombination) {
		drugPredictions.put(drugCombination, "1");
	}

	public void addNonSynergyPrediction(String drugCombination) {
		drugPredictions.put(drugCombination, "0");
	}

	public void addNAPrediction(String drugCombination) {
		drugPredictions.put(drugCombination, "NA");
	}

	public String getPredictions() {
		String result = "";

		for (Map.Entry<String, String> entry : drugPredictions.entrySet()) {
			result += "Drugs: " + entry.getKey() + "  Prediction: " + entry.getValue() + "\n";
		}

		return result;
	}

	/**
	 * Return a tab-seperated String starting with the modelName and then the
	 * prediction value for each drug combination as in the order specified in the
	 * DrugCombinationsList
	 * 
	 * @param drugCombinationsList
	 * @return
	 */
	public String getModelPredictionsVerbose(ArrayList<String> drugCombinationsList) {
		String result = getModelName();

		for (String drugCombination : drugCombinationsList) {
			result += "\t" + drugPredictions.get(drugCombination);
		}

		return result;
	}

	private String getModelName() {
		return modelName;
	}

}
