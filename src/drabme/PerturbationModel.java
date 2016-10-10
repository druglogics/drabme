package drabme;

import java.io.IOException;
import java.util.ArrayList;

import gitsbe.BooleanEquation;
import gitsbe.BooleanModel;
import gitsbe.Logger;

/**
 * Similar to the BooleanModel in Gitsbe, but adds output weights, and drugs with their computed effects.  
 * 
 * @author asmund
 *
 */
public class PerturbationModel extends BooleanModel {

	private Perturbation perturbation;
	private ModelOutputs modelOutputs;
	private int globaloutput;
	private boolean hasGlobalOutput = false;

	public PerturbationModel(BooleanModel booleanModel,
			Perturbation perturbation, ModelOutputs modelOutputs) {

		// Copy constructor from parent
		super(booleanModel);

		this.perturbation = perturbation;

		this.modelOutputs = modelOutputs;

		// super.setVerbosity(Drabme.verbosity);

		for (int i = 0; i < perturbation.getDrugs().length; i++) {
			this.modelName = this.modelName + "_"
					+ perturbation.getDrugs()[i].getName();
		}

		Logger.output(3, "Created perturbation model: " + this.modelName);

		// define perturbations in model
		for (int i = 0; i < perturbation.getDrugs().length; i++) {
			this.perturbNodes(perturbation.getDrugs()[i].getTargets(),
					perturbation.getDrugs()[i].getEffect());
			// this.inhibitNodes (perturbation.getDrugs()[i].getTargets ()) ;
		}

		Logger.output(3, "\nModel: " + this.modelName);
		Logger.output(3, this.printBooleanModelBooleannet());

	}

	public void interpretDrugResponse() {

	}

	// public Drug[] getPerturbations()
	// {
	// return perturbations ;
	// }

	public Perturbation getPerturbation() {
		return perturbation;
	}

	public void calculateGlobalOutput() throws IOException {

		if (stableStates.size() == 0) {
			Logger.output(2, "No stable states found");

			Logger.output(
					2,
					PerturbationPanel.getCombinationName(perturbation
							.getDrugs()) + "\tNA");

			this.hasGlobalOutput = false;

		} else {
			globaloutput = 0;
			this.hasGlobalOutput = true;

			for (int i = 0; i < stableStates.size(); i++) {
				for (int j = 0; j < modelOutputs.size(); j++) {
					int indexStableState = this.getIndexOfEquation(modelOutputs
							.get(j).getName());
					if (indexStableState >= 0) {
						int temp = Character.getNumericValue(stableStates
								.get(i).charAt(indexStableState));
						temp *= modelOutputs.get(j).getWeight();
						globaloutput += temp;
					}
				}
			}

			globaloutput /= stableStates.size();

			// Logger.output(2, "Global output: " + globaloutput) ;

			String line = new String();

			Logger.output(
					2,
					PerturbationPanel.getCombinationName(perturbation
							.getDrugs()) + "\t" + globaloutput);

		}
	}

	public boolean hasGlobalOutput() {
		return this.hasGlobalOutput;

	}

	public int getGlobalOutput() {
		return globaloutput;
	}

	public boolean hasStableState() {
		if (stableStates.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public void perturbNode(String nodeName, boolean perturbation) {
		this.fixNode(nodeName, perturbation);
	}

	public void perturbNodes(ArrayList<String> nodeNames, boolean perturbation) {
		for (int i = 0; i < nodeNames.size(); i++) {
			this.fixNode(nodeNames.get(i), perturbation);
		}
	}

	public void inhibitNode(String nodeName) {
		this.fixNode(nodeName, false);
	}

	public void inhibitNodes(ArrayList<String> nodeNames) {
		for (int i = 0; i < nodeNames.size(); i++) {
			this.fixNode(nodeNames.get(i), false);
		}
	}

	public void fixNode(String nodeName, boolean value) {
		int index = super.getIndexOfEquation(nodeName);

		if (index >= 0) {
			Logger.output(2, "Fixing state of node " + nodeName + " value: "
					+ value + " (equation index: " + index + ")");

			if (index >= 0) {
				booleanEquations.set(index, new BooleanEquation("  " + nodeName
						+ " *= " + value + " "));
				// Logger.output(2,
				// booleanEquations.get(index).getBooleanEquation()) ;
			}
		}

	}

	public void fixNodes(String[] nodeNames, boolean[] values) {
		for (int i = 0; i < nodeNames.length; i++) {
			fixNode(nodeNames[i], values[i]);
		}
	}
}
