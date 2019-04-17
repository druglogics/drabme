package eu.druglogics.drabme.drug;

import eu.druglogics.drabme.perturbation.PerturbationPanel;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static eu.druglogics.gitsbe.util.Util.abort;
import static eu.druglogics.gitsbe.util.Util.readLinesFromFile;

public class DrugPanel {

	// Panel of single drugs to be used
	protected ArrayList<Drug> drugs;
	private Logger logger;

	public DrugPanel(String filename, Logger logger) throws IOException {
		this.logger = logger;
		this.drugs = new ArrayList<>();
		loadDrugsFromFile(filename);
	}

	/**
	 * 
	 * @param drugNames
	 *            array of drugs to be probed
	 * @return boolean of whether all drugs are in panel or not
	 */
	public boolean areDrugsInPanel(String[] drugNames) {
		for (String drugName : drugNames) {
			if (!isDrugInPanel(drugName))
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
	private boolean isDrugInPanel(String name) {
		return (getIndexOfDrug(name) >= 0);
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
	private Drug getDrug(String name) throws Exception {
		int index = getIndexOfDrug(name);

		if (index >= 0) {
			return drugs.get(index);
		}
		throw new Exception("Exception: Drug not found in drug panel. "
				+ "Use function isDrugInPanel (String name)");
	}

	/**
	 * 
	 * @param filename
	 *            storing drugs to be loaded
	 * @throws IOException
	 */
	private void loadDrugsFromFile(String filename) throws IOException {

		logger.outputStringMessage(3, "Reading drugpanel file: "
				+ new File(filename).getAbsolutePath());
		ArrayList<String> lines = readLinesFromFile(filename, true);

		for (int i = 0; i < lines.size(); i++) {
			// Add drug name
			String drugName = lines.get(i).split("\t")[0];
			drugs.add(new Drug(drugName, logger));

			// Add perturbation effect
			String effect = lines.get(i).split("\t")[1];
			if (effect.equals("inhibits")) {
				drugs.get(i).addEffect(false);
			} else if (effect.equals("activates")) {
				drugs.get(i).addEffect(true);
			} else {
				logger.outputStringMessage(1,
						"Drug effect not annotated as either \"activates\" or \"inhibits\" " + (effect));
			}

			// Add drug targets
			drugs.get(i).addTargets(
				Arrays.copyOfRange(lines.get(i).split("\t"), 2, lines.get(i).split("\t").length)
			);
		}
	}

	/**
	 * Adds warnings to the log if there are drug targets not defined in the
	 * model/network topology
	 *
	 * @param booleanModels
	 * @throws Exception
	 */
	public void checkDrugTargets(ArrayList<BooleanModel> booleanModels) {
		logger.outputHeader(3, "Checking drug targets");

		BooleanModel booleanModel = booleanModels.get(0);
		ArrayList<String> nodes = booleanModel.getNodeNames();

		for (Drug drug : this.drugs) {
			for (String target : drug.getTargets()) {
				if (!nodes.contains(target)) {
					logger.outputStringMessage(3, "Warning: Target " + target
							+ " not in network file.");
				}
			}
		}
	}

	public Drug[][] loadCombinationsFromFile(String filename) throws Exception {

		ArrayList<String> lines = readLinesFromFile(filename, true);
		ArrayList<Drug[]> perturbations = new ArrayList<>();

		for (String line : lines) {

			int combosize = line.split("\t").length;
			Drug[] combination = new Drug[combosize];

			for (int j = 0; j < combosize; j++) {
				String drugName = line.split("\t")[j];
				if (isDrugInPanel(drugName)) {
					combination[j] = getDrug(drugName);
				} else {
					logger.error("Combination refers to drug not in drugpanel: " + drugName);
					abort();
				}
			}
			perturbations.add(combination);
		}

		checkDrugCombinationConsistency(perturbations);

		return perturbations.toArray(new Drug[0][]);
	}

	/**
	 * Checks if the drug combinations from the perturbations file are consistent:
	 * If I have drugs AK and PI as perturbations and not PD, I can have AK-PI
	 * combination, but not AK-PD for example. This means that for every combination
	 * (no matter the size) I should have its subsets in the perturbations
	 * 
	 * @param perturbations
	 */
	private void checkDrugCombinationConsistency(ArrayList<Drug[]> perturbations) {
		boolean foundSubset;

		for (int index = 0; index < perturbations.size(); index++) {
			Drug[] combination = perturbations.get(index);
			logger.debug("Combination: " + PerturbationPanel.getCombinationName(combination));
			if (combination.length > 1) { // pairs of drugs, triplets, etc.
				// generate the subsets
				Drug[][] subsets = getCombinationSubsets(combination);

				// for every subset check that there is a defined perturbation in the file
				for (Drug[] subset : subsets) {
					foundSubset = false;
					for (int k = 0; k < index; k++) {
						logger.debug("Checking subset: " + PerturbationPanel.getCombinationName(subset)
								+ " with perturbation: " + PerturbationPanel.getCombinationName(perturbations.get(k)));
						if (Arrays.equals(perturbations.get(k), subset)) {
							logger.debug("FOUND!");
							foundSubset = true;
							break;
						}
					}
					if (!foundSubset) {
						logger.error("The drug combination: " + PerturbationPanel.getCombinationName(combination)
									+ " does not have the subset: " + PerturbationPanel.getCombinationName(subset)
									+ " defined in the perturbations file");
						abort();
					}
				}
			}
		}
	}

	public static void writeDrugPanelFileTemplate(String filename) throws IOException {
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		writer.println("#Name\tEffect\tTarget");
		writer.println("PI\tinhibits\tPIK3CA");
		writer.println("PD\tinhibits\tMAP2K1\tMAP2K2");
		writer.println("CT\tinhibits\tGSK3A\tGSK3B");
		writer.println("BI\tinhibits\tMAPK14");
		writer.println("PK\tinhibits\tCTNNB1");
		writer.println("AK\tinhibits\tAKT\tAKT1\tAKT2\tAKT3");
		writer.println("5Z\tinhibits\tMAP3K7");

		writer.flush();
		writer.close();
	}

	public Drug[][] getCombinations(int size) {
		int lowerLimit = 0;
		return getCombinations(lowerLimit, size);
	}

	/**
	 * 
	 * @return array of drug arrays, where each drug array is a perturbation
	 *         condition (set of drugs)
	 */
	private Drug[][] getCombinations(int lowerLimit, int upperLimit) {

		ArrayList<Drug[]> perturbations = new ArrayList<>();

		// Add all possible combinations to perturbation set from binomial
		// distribution (n,k)

		for (int k = lowerLimit; k < (upperLimit + 1); k++) {
			// --------------------------------------------------
			// Next add each drug combination to perturbation set
			// --------------------------------------------------

			ICombinatoricsVector<Drug> initialVector = Factory.createVector(drugs.toArray(new Drug[0]));

			// Create a simple combination generator to generate n-combinations
			// of the initial vector
			Generator<Drug> gen = Factory.createSimpleCombinationGenerator(initialVector, k);

			ArrayList<ICombinatoricsVector<Drug>> drugs = new ArrayList<>();

			for (ICombinatoricsVector<Drug> combination : gen) {
				drugs.add(combination);
			}

			for (int i = 0; i < gen.getNumberOfGeneratedObjects(); i++) {
				Drug[] temp = new Drug[k];

				for (int j = 0; j < k; j++) {
					temp[j] = drugs.get(i).getValue(j);
				}
				// We don't want an empty perturbation
				if (!(temp.length == 0)) {
					perturbations.add(temp);
				}
			}
		}

		return perturbations.toArray(new Drug[0][]);
	}

	/**
	 * 
	 * @param combination
	 *            of size k that will be split to subsets of size k-1
	 * @return
	 */
	public static Drug[][] getCombinationSubsets(Drug[] combination) {
		ArrayList<Drug[]> subsets = new ArrayList<>();

		int drugsInCombination = combination.length;
		int drugsInSubset = drugsInCombination - 1;

		ICombinatoricsVector<Drug> initialVector = Factory.createVector(combination);

		// Create a simple combination generator to generate n-combinations of
		// the initial vector
		Generator<Drug> gen = Factory.createSimpleCombinationGenerator(initialVector, drugsInSubset);

		ArrayList<ICombinatoricsVector<Drug>> drugs = new ArrayList<>();

		for (ICombinatoricsVector<Drug> subset : gen) {
			drugs.add(subset);
		}

		for (int i = 0; i < gen.getNumberOfGeneratedObjects(); i++) {
			Drug[] temp = new Drug[drugsInSubset];

			for (int j = 0; j < drugsInSubset; j++) {
				temp[j] = drugs.get(i).getValue(j);
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

		for (Drug drug : drugs) {
			hash += drug.getName().hashCode();

			for (int j = 0; j < drug.getTargets().size(); j++) {
				hash += drug.getTargets().get(j).hashCode();
			}
		}

		return hash;
	}
}
