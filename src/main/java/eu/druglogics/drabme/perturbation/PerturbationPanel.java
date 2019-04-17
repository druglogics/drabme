package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.drabme.drug.DrugPanel;
import eu.druglogics.gitsbe.util.Logger;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class PerturbationPanel {

	private Perturbation[] perturbations;
	private Logger logger;

	public PerturbationPanel(Drug[][] perturbations, Logger logger) {
		this.logger = logger;
		this.perturbations = new Perturbation[perturbations.length];

		for (int i = 0; i < perturbations.length; i++) {
			this.perturbations[i] = new Perturbation(perturbations[i], logger);
		}

		logger.outputLines(2, this.getCombinationNames(perturbations));
	}

	/**
	 * Returns the index of the perturbation that has the specific drugs combination
	 * 
	 * @param drugs
	 * @return
	 */
	private int getIndexOfPerturbation(Drug[] drugs) {
		int index = -1;

		int hashA, hashB;

		hashA = DrugPanel.getDrugSetHash(drugs);

		// Go through perturbation list to find condition matching parameter 'drugs'
		for (int i = 0; i < perturbations.length; i++) {
			hashB = perturbations[i].getPerturbationHash();
			if (hashA == hashB) {
				return i;
			}
		}

		return index;
	}

	/**
	 * @return the perturbations
	 */
	public Perturbation[] getPerturbations() {
		return perturbations;
	}

	/**
	 * Returns an array of perturbations that have specific number of drugs
	 * (combinationSize)
	 * 
	 * @param combinationSize
	 * @return
	 */
	private Perturbation[] getPerturbations(int combinationSize) {
		ArrayList<Perturbation> result = new ArrayList<>();

		for (Perturbation perturbation : perturbations) {
			if (perturbation.getDrugs().length == combinationSize) {
				result.add(perturbation);
			}
		}

		return result.toArray(new Perturbation[0]);
	}

	public int getNumberOfPerturbations() {
		return perturbations.length;
	}

	/**
	 * Returns the number of perturbations that have a specific number of drugs
	 * (size)
	 * 
	 * @param size
	 * @return
	 */
	private int getNumberOfPerturbations(int size) {
		int result = 0;

		for (Perturbation perturbation : perturbations) {
			if (perturbation.getDrugs().length == size) {
				result++;
			}
		}

		return result;
	}

	public double getPredictedAverageCombinationResponse(Perturbation perturbation) {
		double response = perturbation.getAveragePredictedResponse();

		Drug[][] subsets = DrugPanel.getCombinationSubsets(perturbation.getDrugs());
		int[] indexSubset = new int[subsets.length];

		for (int i = 0; i < subsets.length; i++) {
			indexSubset[i] = getIndexOfPerturbation(subsets[i]);
		}

		double minimumResponseSubset = perturbations[indexSubset[0]].getAveragePredictedResponse();
		double maximumResponseSubset = perturbations[indexSubset[0]].getAveragePredictedResponse();

		for (int i = 1; i < indexSubset.length; i++) {
			minimumResponseSubset = min(minimumResponseSubset,
					perturbations[indexSubset[i]].getAveragePredictedResponse());
			maximumResponseSubset = max(maximumResponseSubset,
					perturbations[indexSubset[i]].getAveragePredictedResponse());
		}

		logger.debug("min: " + minimumResponseSubset + " max: " + maximumResponseSubset);

		if (response < minimumResponseSubset)
			return response - minimumResponseSubset;
		else if (response > maximumResponseSubset)
			return response - maximumResponseSubset;
		else
			return 0.0;
	}

	public double[] getPredictedAverageCombinationResponses(int combinationSize) {
		double[] responses = new double[getNumberOfPerturbations(combinationSize)];

		Perturbation[] subset = this.getPerturbations(combinationSize);

		for (int i = 0; i < responses.length; i++) {
			responses[i] = getPredictedAverageCombinationResponse(subset[i]);
		}

		return responses;
	}

	/**
	 * Creates an array of names from an array of drug combinations by calling
	 * getCombinationName for each subarray
	 * 
	 * @param combinations
	 * @return name of drug combination
	 */
	private String[] getCombinationNames(Drug[][] combinations) {
		String[] names = new String[combinations.length];

		for (int i = 0; i < combinations.length; i++) {
			names[i] = getCombinationName(combinations[i]);
		}

		return names;
	}

	/**
	 * Creates name for drug combination by iterating drug names and adding them to
	 * String, separating drug names by hyphens (-)
	 * 
	 * @param combination
	 * @return name of drug combination
	 */
	public static String getCombinationName(Drug[] combination) {
		String[] names = new String[combination.length];

		for (int i = 0; i < combination.length; i++) {
			names[i] = combination[i].getName();
		}

		return getCombinationName(names);
	}

	private static String getCombinationName(String[] combination) {
		StringBuilder comboName = new StringBuilder("[");
		for (int i = 0; i < combination.length; i++) {
			if (i > 0)
				comboName.append("]-[");
			comboName.append(combination[i]);
		}
		comboName.append("]");

		return comboName.toString();
	}

}
