package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.drabme.drug.DrugPanel;
import eu.druglogics.gitsbe.util.Logger;

import java.util.ArrayList;

import static eu.druglogics.gitsbe.util.Util.convertFloats;

public class Perturbation {

	private Drug[] drugs;
	private int perturbationHash;
	private ArrayList<Float> predictedResponses;

	private int predictedSynergies;
	private int predictedNonSynergies;

	private double mean;
	private double sd;

	private boolean isStatisticsCalculated = false;

	protected Logger logger;

	public Perturbation(Drug[] perturbation, Logger logger) {

		this.logger = logger;
		drugs = perturbation;
		predictedResponses = new ArrayList<>();

		predictedSynergies = 0;
		predictedNonSynergies = 0;

		// Report if any drugs passed are without defined targets
		for (Drug drug : drugs) {
			if (drug.getTargets().size() == 0) {
				logger.outputStringMessage(2,
					"Added drug " + drug.getName() + " which has no targets to perturbations");
			}
		}

		perturbationHash = DrugPanel.getDrugSetHash(drugs);
	}

	synchronized void addSynergyPrediction() {
		predictedSynergies++;
	}

	synchronized void addNonSynergyPrediction() {
		predictedNonSynergies++;
	}

	public int getSynergyPredictions() {
		return predictedSynergies;
	}

	public int getNonSynergyPredictions() {
		return predictedNonSynergies;
	}

	synchronized void addPrediction(float response) {
		predictedResponses.add(response);
	}

	public float[] getPredictions() {
		return convertFloats(predictedResponses);
	}

	public void calculateStatistics() {
		float responseSum = 0;
		int numPredictions = predictedResponses.size();

		for (Float predictedResponse : predictedResponses) {
			responseSum += predictedResponse;
		}

		double mean = 0;
		double var = 0;
		double sd = 0;

		if (numPredictions > 0) {
			mean = (double) responseSum / (double) numPredictions;

			if (numPredictions > 1) {
				for (float prediction : predictedResponses) {
					var += (prediction - mean) * (prediction - mean);
				}
				sd = Math.sqrt(var / (numPredictions - 1));
			}
		}

		this.mean = mean;
		this.sd = sd;

		isStatisticsCalculated = true;
		logger.outputStringMessage(1, "Statistics calculated for perturbation: "
				+ PerturbationPanel.getCombinationName(drugs));
	}

	public double getAveragePredictedResponse() {
		if (!isStatisticsCalculated)
			calculateStatistics();
		return mean;
	}

	public double getStandardDeviationPredictedResponse() {
		if (!isStatisticsCalculated)
			calculateStatistics();
		return sd;
	}

	/**
	 * @return the drugs
	 */
	public Drug[] getDrugs() {
		return drugs;
	}

	String getDrugsVerbose() {
		StringBuilder result = new StringBuilder();

		for (Drug drug : drugs) {
			String str = drug.getName() + " ";
			result.append(str);
		}

		return result.toString();
	}

	public String getName() {
		return PerturbationPanel.getCombinationName(drugs);
	}

	int getPerturbationHash() {
		return perturbationHash;
	}

}
