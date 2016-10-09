package drabme;

import java.util.ArrayList;
import java.util.Iterator;

import gitsbe.Logger;

public class Perturbation {

	private Drug[] drugs;

	private int perturbationHash;

	private double observedResponse;

	private ArrayList<Integer> predictedResponses;

	private int predictedSynergies;
	private int predictedNonSynergies;

	private double mean;
	private double sd;
	private double var;

	private boolean isStatisticsCalculated = false;

	public Perturbation(Drug[] perturbation) {

		drugs = perturbation;
		predictedResponses = new ArrayList<Integer>();

		predictedSynergies = 0;
		predictedNonSynergies = 0;

		// Report if any drugs passed are without defined targets
		for (int i = 0; i < perturbation.length; i++) {
			if (drugs[i].getTargets().size() == 0) {
				Logger.output(2, "Added drug " + drugs[i].getName()
						+ " which has no targets to perturbations");
			}
		}

		perturbationHash = DrugPanel.getDrugSetHash(drugs);
	}

	public void addSynergyPrediction() {
		predictedSynergies++;
	}

	public int getSynergyPredictions() {
		return predictedSynergies;
	}

	public void addNonSynergyPrediction() {
		predictedNonSynergies++;
	}

	public int getNonSynergyPredictions() {
		return predictedNonSynergies;
	}

	public void addPrediction(int response) {
		predictedResponses.add(response);
	}

	public int[] getPredictions() {
		return convertIntegers(predictedResponses);
	}

	public void calculateStatistics() {
		isStatisticsCalculated = true;

		String responseString = "";

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

				sd = Math.sqrt(var);
				sd /= (numPredictions - 1);
			}
		}

		this.mean = mean;
		this.var = var;
		this.sd = sd;
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

	public double getObservedResponse() {
		return observedResponse;
	}

	public void addObservation(double response) {
		this.observedResponse = response;
	}

	/**
	 * @return the drugs
	 */
	public Drug[] getDrugs() {
		return drugs;
	}

	public String getName() {
		return PerturbationPanel.getCombinationName(drugs);
	}

	public int getPerturbationHash() {
		return perturbationHash;
	}

	public static int[] convertIntegers(ArrayList<Integer> integers) {
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}

	public static double[] convertDoubles(Double[] doubles) {
		double[] ret = new double[doubles.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = doubles[i].doubleValue();
		}
		return ret;
	}
}
