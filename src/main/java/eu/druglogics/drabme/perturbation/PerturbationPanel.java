package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.drabme.drug.DrugPanel;
import eu.druglogics.drabme.input.Config;
import eu.druglogics.gitsbe.util.Logger;

import java.util.ArrayList;

import static eu.druglogics.gitsbe.util.Util.abort;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class PerturbationPanel {

	private Perturbation[] perturbations;
	private Logger logger;

	public PerturbationPanel(Drug[][] perturbations, Logger logger) throws Exception {
		this.logger = logger;
		this.perturbations = new Perturbation[perturbations.length];

		for (int i = 0; i < perturbations.length; i++) {
			this.perturbations[i] = new Perturbation(perturbations[i], logger);
		}

		this.checkPerturbationHashes();

		logger.outputLines(2, this.getCombinationNames(perturbations));
	}

	/**
	 * Returns the index of the perturbation that has the specific drugs combination
	 * 
	 * @param drugs
	 * @return
	 */
	int getIndexOfPerturbation(Drug[] drugs) {
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
	 * @return the {@link #perturbations}
	 */
	public Perturbation[] getPerturbations() {
		return perturbations;
	}

	/**
	 * Returns an array of perturbations that have specific number of drugs
	 * (<i>size</i>)
	 * 
	 * @param size
	 */
	public Perturbation[] getPerturbations(int size) {
		ArrayList<Perturbation> result = new ArrayList<>();

		for (Perturbation perturbation : perturbations) {
			if (perturbation.getDrugs().length == size) {
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
	 */
	public int getNumberOfPerturbations(int size) {
		int result = 0;

		for (Perturbation perturbation : perturbations) {
			if (perturbation.getDrugs().length == size) {
				result++;
			}
		}

		return result;
	}

	/**
	 * This function returns the average excess response over all the
	 * perturbation subsets of the given one, using either the <b>HSA (Highest
	 * Simple Agent)</b> or the <b>Bliss</b> method (which is used is based on
	 * the {@link eu.druglogics.drabme.input.ConfigParametersDrabme#synergy_method} value).
	 *
	 * <br/><br/>
	 * For example, using the HSA method for a given double perturbation <i>{A,B}</i>
	 * and its respective perturbation subsets {A} and {B} (A and B are drugs), we get
	 * the 3 average predicted responses using the {@link Perturbation#getAveragePredictedResponse()}.
	 * Then, if <code>avgPredRes(A+B) < min(avgPredRes(A),avgPredRes(B))</code> we return the
	 * average negative excess (<b>synergy score</b>), if <code>avgPredRes(A+B)</code> is between the
	 * value of the subsets we return <i>0</i> (<b>non-interaction score</b>), and if larger than both,
	 * we return the average positive excess (<b>antagonistic score</b>).
	 * <br/><br/>
	 *
	 * Same logic applies to the Bliss method, with the only difference that we use the
	 * {@link Perturbation#getNormalizedAveragePredictedResponse()} to get the normalized
	 * average responses, compute their product (<code>normAvgPredRes(A) *
	 * normAvgPredRes(B)</code>) and compare it with the normalized average combination response
	 * <code>normAvgPredRes(A+B)</code>.
	 *
	 * @param perturbation
	 */
	public double getAverageResponseExcessOverSubsets(Perturbation perturbation) {
		double excess;

		Drug[][] subsets = DrugPanel.getCombinationSubsets(perturbation.getDrugs());
		int[] indexSubset = new int[subsets.length];

		for (int i = 0; i < subsets.length; i++) {
			indexSubset[i] = getIndexOfPerturbation(subsets[i]);
		}

		if (Config.getInstance().getSynergyMethod().equals("hsa")) {
			double minimumResponseSubset = perturbations[indexSubset[0]].getAveragePredictedResponse();
			double maximumResponseSubset = perturbations[indexSubset[0]].getAveragePredictedResponse();

			for (int i = 1; i < indexSubset.length; i++) {
				minimumResponseSubset = min(minimumResponseSubset,
					perturbations[indexSubset[i]].getAveragePredictedResponse());
				maximumResponseSubset = max(maximumResponseSubset,
					perturbations[indexSubset[i]].getAveragePredictedResponse());
			}

			logger.debug("min: " + minimumResponseSubset + " max: " + maximumResponseSubset);

			double response = perturbation.getAveragePredictedResponse();
			if (response < minimumResponseSubset)
				excess = response - minimumResponseSubset;
			else if (response > maximumResponseSubset)
				excess = response - maximumResponseSubset;
			else
				excess = 0.0;
		} else { // bliss
			double expectedBlissCombinationResponse = 1;
			for (int index : indexSubset) {
				expectedBlissCombinationResponse *= perturbations[index].getNormalizedAveragePredictedResponse();
			}

			double response = perturbation.getNormalizedAveragePredictedResponse();
			excess = response - expectedBlissCombinationResponse;
		}

		return excess;
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
	 * the returned string, separating drug names by hyphens (-)
	 * 
	 * @param combination an array of drugs (don't have to be part of the {@link PerturbationPanel})
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

	public void checkPerturbationHashes() throws Exception {
		ArrayList<Integer> hashes = new ArrayList<>();
		ArrayList<String> names = new ArrayList<>();
		for (Perturbation perturbation: this.getPerturbations()) {
			hashes.add(perturbation.getPerturbationHash());
			names.add(perturbation.getName());
		}

		// search for same hashes in all perturbation pairs
		for (int i = 0; i < hashes.size(); i++) {
			int hash = hashes.get(i);
			for (int j = i+1; j < hashes.size(); j++) {
				if (hash == hashes.get(j)) {
					String message = "Perturbations `" + names.get(i) + "` and `" + names.get(j) +
						"` have the same hash: " + hash;
					throw new Exception(message);
				}
			}
		}
	}

}
