package eu.druglogics.drabme;

import java.io.IOException;
import java.util.ArrayList;

import eu.druglogics.gitsbe.BooleanEquation;
import eu.druglogics.gitsbe.BooleanModel;
import eu.druglogics.gitsbe.Logger;
import eu.druglogics.gitsbe.ModelOutputs;

/**
 * Similar to the BooleanModel in Gitsbe, but adds output weights, and drugs
 * with their computed effects.
 * 
 * @author asmund
 *
 */
public class PerturbationModel extends BooleanModel {

	private Perturbation perturbation;
	private ModelOutputs modelOutputs;
	private float globaloutput;
	private boolean hasGlobalOutput = false;
	private Logger logger;

	public PerturbationModel(BooleanModel booleanModel, Perturbation perturbation, ModelOutputs modelOutputs,
			Logger logger) {
		// Copy constructor from parent
		super(booleanModel, logger);

		this.perturbation = perturbation;
		this.modelOutputs = modelOutputs;
		this.logger = logger;

		for (int i = 0; i < perturbation.getDrugs().length; i++) {
			this.modelName = this.modelName + "_" + perturbation.getDrugs()[i].getName();
		}

		logger.outputStringMessage(3,
				"Added new perturbation model: " + this.modelName + " with drug(s): " + perturbation.getDrugsVerbose());

		// define perturbations in model
		for (int i = 0; i < perturbation.getDrugs().length; i++) {
			this.perturbNodes(perturbation.getDrugs()[i].getTargets(), perturbation.getDrugs()[i].getEffect());
		}
	}

	public void calculateGlobalOutput() throws IOException {

		if (stableStates.size() == 0) {
			logger.outputStringMessage(2, "No stable states found");
			logger.outputStringMessage(2, PerturbationPanel.getCombinationName(perturbation.getDrugs()) + "\tNA");
			this.hasGlobalOutput = false;
		} else {
			globaloutput = 0;
			this.hasGlobalOutput = true;

			for (int i = 0; i < stableStates.size(); i++) {
				for (int j = 0; j < modelOutputs.size(); j++) {
					int indexStableState = this.getIndexOfEquation(modelOutputs.get(j).getName());
					if (indexStableState >= 0) {
						int temp = Character.getNumericValue(stableStates.get(i).charAt(indexStableState));
						temp *= modelOutputs.get(j).getWeight();
						globaloutput += temp;
					}
				}
			}

			globaloutput /= stableStates.size();

			logger.debug(PerturbationPanel.getCombinationName(perturbation.getDrugs()) + "\t" + globaloutput);

		}
	}

	public Perturbation getPerturbation() {
		return perturbation;
	}

	public boolean hasGlobalOutput() {
		return this.hasGlobalOutput;
	}

	public float getGlobalOutput() {
		return globaloutput;
	}

	public boolean hasStableState() {
		if (stableStates.size() > 0) {
			return true;
		}
		return false;
	}

	public void perturbNode(String nodeName, boolean perturbation) {
		this.fixNode(nodeName, perturbation);
	}

	public void perturbNodes(ArrayList<String> nodeNames, boolean perturbation) {
		for (int i = 0; i < nodeNames.size(); i++) {
			this.fixNode(nodeNames.get(i), perturbation);
		}
	}

	public void fixNode(String nodeName, boolean value) {
		int index = super.getIndexOfEquation(nodeName);

		if (index >= 0) {
			logger.outputStringMessage(2,
					"Fixing state of node " + nodeName + " value: " + value + " (equation index: " + index + ")");
			booleanEquations.set(index, new BooleanEquation("  " + nodeName + " *= " + value + " "));
		}
	}

	public void fixNodes(String[] nodeNames, boolean[] values) {
		for (int i = 0; i < nodeNames.length; i++) {
			fixNode(nodeNames[i], values[i]);
		}
	}
}
