package drabme;

import java.util.ArrayList;
import java.util.Iterator;

import gitsbe.Logger;

public class Perturbation {

	private Drug[] drugs;
	private int perturbationHash;
	private ArrayList<Integer> predictedResponses;

	private int predictedSynergies;
	private int predictedNonSynergies;

	public double mean;
	public double sd;
	public double var;

	private boolean isStatisticsCalculated = false;

	protected Logger logger;

	public Perturbation(Drug[] perturbation, Logger logger) {

		this.logger = logger;
		drugs = perturbation;
		predictedResponses = new ArrayList<Integer>();

		predictedSynergies = 0;
		predictedNonSynergies = 0;

		// Report if any drugs passed are without defined targets
		for (int i = 0; i < drugs.length; i++) {
			if (drugs[i].getTargets().size() == 0) {
				logger.outputStringMessage(2,
						"Added drug " + drugs[i].getName() + " which has no targets to perturbations");
			}
		}

		perturbationHash = DrugPanel.getDrugSetHash(drugs);
	}

	synchronized public void addSynergyPrediction() {
		predictedSynergies++;
	}

	synchronized public void addNonSynergyPrediction() {
		predictedNonSynergies++;
	}

	public int getSynergyPredictions() {
		return predictedSynergies;
	}

	public int getNonSynergyPredictions() {
		return predictedNonSynergies;
	}

	synchronized public void addPrediction(int response) {
		predictedResponses.add(response);
	}

	public int[] getPredictions() {
		return convertIntegers(predictedResponses);
	}

	public void calculateStatistics() {
		int responseSum = 0;
		int numPredictions = predictedResponses.size();

		for (int i = 0; i < numPredictions; i++) {
			responseSum += predictedResponses.get(i);
		}

		double mean = 0;
		double var = 0;
		double sd = 0;

		if (numPredictions > 0) {
			mean = (double) responseSum / (double) numPredictions;

			if (numPredictions > 1) {
				for (int i = 0; i < numPredictions; i++) {
					int prediction = predictedResponses.get(i);
					var += (prediction - mean) * (prediction - mean);
				}
				sd = Math.sqrt(var / (numPredictions - 1));
			}
		}

		this.mean = mean;
		this.var = var;
		this.sd = sd;

		isStatisticsCalculated = true;
		logger.outputStringMessage(0,
				"Statistics calculated for perturbation: " + PerturbationPanel.getCombinationName(drugs));
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

	public String getDrugsVerbose() {
		String result = "";
		for (int i = 0; i < drugs.length; i++) {
			result += drugs[i].getName() + " ";
		}
		return result;
	}

	public String getName() {
		return PerturbationPanel.getCombinationName(drugs);
	}

	public int getPerturbationHash() {
		return perturbationHash;
	}

	/**
	 * Converts an ArrayList of Integers to an Array of int
	 * 
	 * @param integers
	 * @return
	 */
	public static int[] convertIntegers(ArrayList<Integer> integers) {
		int[] result = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < result.length; i++) {
			result[i] = iterator.next().intValue();
		}
		return result;
	}

	public static double[] convertDoubles(Double[] doubles) {
		double[] result = new double[doubles.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = doubles[i].doubleValue();
		}
		return result;
	}
}
