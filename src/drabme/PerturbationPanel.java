package drabme;

import gitsbe.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class PerturbationPanel {

	Perturbation[] perturbations;
	DrugPanel drugPanel;

	public PerturbationPanel(Drug[][] perturbations, DrugPanel drugPanel) {

		this.drugPanel = drugPanel;
		this.perturbations = new Perturbation[perturbations.length];

		for (int i = 0; i < perturbations.length; i++) {
			this.perturbations[i] = new Perturbation(perturbations[i]);
		}

		Logger.output(2, this.getCombinationNames(perturbations));
	}

	/**
	 * Load experimentally observed data, used for statistical analysis of
	 * predictions
	 * 
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void loadObservationData(String filename) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new FileReader(filename));

		try {
			while (true) {
				String line = reader.readLine();

				// no more lines to read
				if (line == null) {
					reader.close();
					break;
				}

				if (!line.startsWith("#")) {
					lines.add(line);
				}
			}
		}

		finally {
			reader.close();
		}

		for (int i = 0; i < lines.size(); i++) {
			String[] splitline = lines.get(i).split("\t");
			double response = Double.parseDouble(splitline[0]);

			Drug[] drugs = new Drug[splitline.length - 1];

			if (drugPanel.areDrugsInPanel(Arrays.copyOfRange(splitline, 1,
					splitline.length))) {
				for (int j = 1; j < splitline.length; j++) {
					try {
						drugs[j - 1] = drugPanel.getDrug(splitline[j]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				int index = this.getIndexOfPerturbation(drugs);

				if (index >= 0) {
					Logger.output(2, "Observation for drug combination "
							+ PerturbationPanel.getCombinationName(drugs)
							+ ", observed data: " + response);
					perturbations[index].addObservation(response);
				} else {
					Logger.output(
							2,
							"Observation file contains observations on drug combination "
									+ PerturbationPanel
											.getCombinationName(drugs)
									+ ", but this combination is not simulated, even though single drugs are part of drug panel used for simulations.");
				}

			} else {
				Logger.output(
						2,
						"Observation file contains observations on drug combination not in drugpanel: "
								+ PerturbationPanel.getCombinationName(Arrays
										.copyOfRange(splitline, 1,
												splitline.length)));
			}
		}
	}

	private int getIndexOfPerturbation(Drug[] drugs) {
		int index = -1;

		int hashA = 0;
		int hashB = 0;

		hashA = DrugPanel.getDrugSetHash(drugs);

		// Go through perturbation list to find condition matching parameter
		// 'drugs'

		for (int i = 0; i < perturbations.length; i++) {
			hashB = DrugPanel.getDrugSetHash(perturbations[i].getDrugs());
			// h ashB = perturbations[i].getPerturbationHash() ;
			// getDrugSetHash(perturbations.get(i)) ;

			if (hashA == hashB) {
				return i;
			}
		}

		return index;
	}

	private int getIndexOfPerturbation(Perturbation perturbation) {
		return getIndexOfPerturbation(perturbation.getDrugs());
	}

	/**
	 * @return the perturbations
	 */
	public Perturbation[] getPerturbations() {
		return perturbations;
	}

	public Perturbation[] getPerturbations(int combinationsize) {
		ArrayList<Perturbation> result = new ArrayList<Perturbation>();

		for (int i = 0; i < perturbations.length; i++) {
			if (perturbations[i].getDrugs().length == combinationsize) {
				result.add(perturbations[i]);
			}
		}

		return result.toArray(new Perturbation[0]);
	}

	public int getNumberOfPerturbations() {
		return perturbations.length;
	}

	public int getNumberOfPerturbations(int size) {
		int result = 0;

		for (int i = 0; i < perturbations.length; i++) {
			if (perturbations[i].getDrugs().length == size) {
				result++;
			}
		}

		return result;
	}

	public static void writeObservationsTemplateFile(
			Perturbation[] perturbations, String filename)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		// Write header with '#'
		writer.println("# Experimentally observed CI values");
		writer.println("# Use tab-separated columns");
		writer.println("#");
		writer.println("# In template file all drug combinations are given a CI of 1.0, these must be changed to observed CIs");
		writer.println("#");
		writer.println("# CI\tDrug1\tDrug2");

		for (int i = 0; i < perturbations.length; i++) {
			if (perturbations[i].getDrugs().length == 2) {
				String line = "1.0";

				for (int j = 0; j < perturbations[i].getDrugs().length; j++) {
					line += "\t" + perturbations[i].getDrugs()[j].getName();
				}
				writer.println(line);
			}
		}

		writer.close();
	}

	public static String getCombinationName(String[] combination) {
		String namecombo = "[";
		for (int i = 0; i < combination.length; i++) {
			if (i > 0)
				namecombo += "]-[";

			namecombo += combination[i];
		}
		namecombo += "]";

		return namecombo;
	}

	public double getPredictedAverageCombinationResponse(
			Perturbation perturbation) {
		int index = getIndexOfPerturbation(perturbation);
		double response = perturbation.getAveragePredictedResponse();

		Drug[][] subsets = DrugPanel.getCombinationSubsets(perturbation
				.getDrugs());
		int indexSubset[] = new int[subsets.length];

		// Logger.output(1, perturbations[index].getName());

		for (int i = 0; i < subsets.length; i++) {
			indexSubset[i] = getIndexOfPerturbation(subsets[i]);
			// Logger.output(1, perturbations[indexSubset[i]].getName());
		}

		double minimumResponseSubset = perturbations[indexSubset[0]]
				.getAveragePredictedResponse();
		double maximumResponseSubset = perturbations[indexSubset[0]]
				.getAveragePredictedResponse();

		for (int i = 1; i < indexSubset.length; i++) {
			minimumResponseSubset = min(minimumResponseSubset,
					perturbations[indexSubset[i]].getAveragePredictedResponse());
			maximumResponseSubset = max(maximumResponseSubset,
					perturbations[indexSubset[i]].getAveragePredictedResponse());
		}

		// Logger.output(1, "min: " + minimumResponseSubset + " max: " +
		// maximumResponseSubset);
		if (response < minimumResponseSubset)
			return response - minimumResponseSubset;
		else if (response > maximumResponseSubset)
			return response - maximumResponseSubset;
		else
			return 0.0;
	}

	public double[] getPredictedAverageCombinationResponses(int combinationsize) {
		double[] responses = new double[getNumberOfPerturbations(combinationsize)];

		Perturbation[] subset = this.getPerturbations(combinationsize);

		for (int i = 0; i < responses.length; i++) {
			responses[i] = getPredictedAverageCombinationResponse(subset[i]);
		}

		return responses;
	}

	// How define SD for ratio towards minimum

	// public double getPredictedCombinationResponseStandardDeviation
	// (Perturbation perturbation)
	// {
	// int index = getIndexOfPerturbation(perturbation) ;
	//
	// double sd = 0 ;
	//
	// }

	public double[] getObservedCombinationResponses(int combinationsize) {
		double[] responses = new double[this
				.getNumberOfPerturbations(combinationsize)];

		Perturbation[] subset = this.getPerturbations(combinationsize);

		for (int i = 0; i < responses.length; i++) {
			responses[i] = subset[i].getObservedResponse();
		}

		return responses;
	}

	/**
	 * Creates name for drug combination by iterating drug names and adding them
	 * to String, separating drug names by hyphens (-)
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

	/**
	 * Creates an array of names for an array of drug combinations by calling
	 * getCombinationName for each subarray
	 * 
	 * @param combinations
	 * @return name of drug combination
	 */
	public static String[] getCombinationNames(Drug[][] combinations) {
		String[] names = new String[combinations.length];

		for (int i = 0; i < combinations.length; i++) {
			names[i] = getCombinationName(combinations[i]);
		}

		return names;
	}

	private static double min(double a, double b) {
		if (a > b)
			return b;
		else
			return a;

	}

	private static double max(double a, double b) {
		if (a < b)
			return b;
		else
			return a;

	}
}
