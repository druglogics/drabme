package drabme;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import gitsbe.Logger;

public class DrugPanel {

	// Panel of single drugs to be used
	protected ArrayList<Drug> drugs;

	private Logger logger ;
	// Panel of perturbations, given combination sizes
	// protected ArrayList <Drug[]> perturbations ;

	public DrugPanel(String filename, Logger logger) throws IOException {
		
		this.logger = logger ;
		
		drugs = new ArrayList<Drug>();

		loadDrugsFromFile(filename);
	}

	/**
	 * 
	 * @param name
	 *            array of drugs to be probed
	 * @return boolean of whether all drugs are in panel or not
	 */
	public boolean areDrugsInPanel(String[] name) {
		for (int i = 0; i < name.length; i++) {
			if (!isDrugInPanel(name[i]))
				return false;
		}

		return true;
	}

	/**
	 * 
	 * @param name
	 *            of drug probed
	 * @return boolean of whether drug is in panel or not
	 */
	public boolean isDrugInPanel(String name) {
		if (getIndexOfDrug(name) >= 0)
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @param drugName
	 * @return index of drug in ArrayList<Drug> drugs
	 */
	private int getIndexOfDrug(String drugName) {
		int index = -1;

		for (int i = 0; i < drugs.size(); i++) {
			if (drugs.get(i).getName().equals(drugName)) {
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns Drug based on name
	 * 
	 * @param name
	 *            of drug
	 * @return Drug
	 * @throws Exception
	 *             if drug is not in panel
	 */
	public Drug getDrug(String name) throws Exception {
		int index = getIndexOfDrug(name);

		if (index >= 0) {
			return drugs.get(index);
		} else {
			throw new Exception(
					"Exception: Drug not found in drug panel. Use function isDrugInPanel (String name)");
		}
	}

	/**
	 * 
	 * @param filename
	 *            storing drugs to be loaded
	 * @throws IOException
	 */
	public void loadDrugsFromFile(String filename) throws IOException {
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
			// Add drug name
			drugs.add(new Drug(lines.get(i).split("\t")[0], logger));

			// Add perturbation effect

			String effect = lines.get(i).split("\t")[1];
			if (effect.equals("inhibits")) {
				drugs.get(i).addEffect(false);
			} else if (effect.equals("activates")) {
				drugs.get(i).addEffect(true);
			} else {
				logger.output(1,
						"ERROR: Drug effect not annotated as either \"activates\" or \"inhibits\" "
								+ (effect));
			}

			// Add drug targets
			drugs.get(i).addTargets(
					Arrays.copyOfRange(lines.get(i).split("\t"), 2, lines
							.get(i).split("\t").length));

		}
	}

	public Drug[][] loadCombinationsFromFile(String filename) throws Exception {
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

		ArrayList<Drug[]> perturbations = new ArrayList<Drug[]>();

		for (int i = 0; i < lines.size(); i++) {

			int combosize = lines.get(i).split("\t").length;
			Drug[] combination = new Drug[combosize];

			for (int j = 0; j < combosize; j++) {
				if (isDrugInPanel(lines.get(i).split("\t")[j])) {
					String drugname = lines.get(i).split("\t")[j];
					// System.out.println(drugname) ;
					combination[j] = getDrug(drugname);
					// System.out.println(combination[j].getName()) ;
				} else {
					logger.output(1,
							"ERROR: Combination refers to drug not in drugpanel: "
									+ lines.get(i).split("\t")[j]);
				}

			}
			// if (combination != null)
			perturbations.add(combination);
		}

		// logger.outputHeader(2, "Loaded combinations from file");
		//
		// for (int i = 0; i < perturbations.size(); i++)
		// {
		// String output = "" ;
		// for (int j = 0; j< perturbations.get(i).length; j++)
		// {
		// output += perturbations.get(i)[j].getName() ;
		// if (j > 0)
		// output += "-" ;
		//
		// }
		// logger.output(2, output);
		// }
		//

		return perturbations.toArray(new Drug[0][]);
	}

	public static void saveDrugPanelFileTemplate(String filename)
			throws IOException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		// Write header with '#'
		writer.println("# Drugs and drug targets");
		writer.println("#");
		writer.println("# First column is name of drug, subsequent tab-separated ");
		writer.println("# columns indicate drug target(s).");
		writer.println("#");
		writer.println("# Name of drug targets must match name of targeted node in model.");
		writer.println("#");
		writer.println("# Name\tTarget");
		writer.println("#");

		writer.close();
	}

	public Drug[][] getCombinations(int size) {
		return getCombinations(size, 0);
	}

	/**
	 * 
	 * @param combosize
	 * @return array of drug arrays, where each drug array is a perturbation
	 *         condition (set of drugs)
	 */
	public Drug[][] getCombinations(int upperlimit, int lowerlimit) {

		ArrayList<Drug[]> perturbations = new ArrayList<Drug[]>();

		// Add all possible combinations to perturbation set from binomial
		// distribution (n,k)

		for (int k = lowerlimit; k < (upperlimit + 1); k++) {
			// --------------------------------------------------
			// Next add each drug combination to perturbation set
			// --------------------------------------------------

			ICombinatoricsVector<Drug> initialVector = Factory
					.createVector(drugs.toArray(new Drug[0]));

			// Create a simple combination generator to generate n-combinations
			// of the initial vector
			Generator<Drug> gen = Factory.createSimpleCombinationGenerator(
					initialVector, k);

			ArrayList<ICombinatoricsVector<Drug>> drugs = new ArrayList<ICombinatoricsVector<Drug>>();

			for (ICombinatoricsVector<Drug> combination : gen) {
				drugs.add(combination);
			}

			for (int i = 0; i < gen.getNumberOfGeneratedObjects(); i++) {
				Drug[] temp = new Drug[k];

				for (int j = 0; j < k; j++) {
					temp[j] = drugs.get(i).getValue(j);
				}

				perturbations.add(temp);
			}
		}

		// if (Drabme.verbosity >= 2)
		// {
		// logger.output(2, "\n" + (perturbations.size()-1) +
		// " conditions added to perturbation set:");
		//
		// for (int i = 0; i < perturbations.size (); i++)
		// {
		// for (int j = 0; j < perturbations.get(i).length; j++)
		// {
		// logger.output(2, perturbations.get(i)[j].getName() + " ") ;
		// }
		// logger.output(2, "") ;
		// }
		// }
		return perturbations.toArray(new Drug[0][]);

	}

	/**
	 * 
	 * @param combination
	 *            of size k that will be split to subsets of size k-1
	 * @return
	 */
	public static Drug[][] getCombinationSubsets(Drug[] combination) {
		ArrayList<Drug[]> subsets = new ArrayList<Drug[]>();
		
		int drugsInCombination = combination.length;
		int drugsInSubset = drugsInCombination - 1;

		ICombinatoricsVector<Drug> initialVector = Factory
				.createVector(combination);

		// Create a simple combination generator to generate n-combinations of
		// the initial vector
		Generator<Drug> gen = Factory.createSimpleCombinationGenerator(
				initialVector, drugsInSubset);

		ArrayList<ICombinatoricsVector<Drug>> drugs = new ArrayList<ICombinatoricsVector<Drug>>();

		for (ICombinatoricsVector<Drug> subset : gen) {
			drugs.add(subset);
		}

		for (int i = 0; i < gen.getNumberOfGeneratedObjects(); i++) {
			Drug[] temp = new Drug[drugsInSubset];

			for (int j = 0; j < drugsInSubset; j++) {
				temp[j] = drugs.get(i).getValue(j);
				// System.out.print(drugs.get(i).getValue(j).getName() + " ");

			}

			subsets.add(temp);
		}

		return subsets.toArray(new Drug[0][]);

	}

	/**
	 * 
	 * @param drugs
	 *            in drugset for which unique id will be generated. Hash will be
	 *            name-based, so each drug must have unique name
	 * @return hash
	 */
	public static int getDrugSetHash(Drug[] drugs) {
		int hash = 0;

		for (int i = 0; i < drugs.length; i++) {
			hash += drugs[i].getName().hashCode();
		}

		return hash;
	}
}
