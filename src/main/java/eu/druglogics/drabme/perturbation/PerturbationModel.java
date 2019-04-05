package eu.druglogics.drabme.perturbation;

import java.util.ArrayList;
import java.util.stream.IntStream;

import eu.druglogics.gitsbe.model.BooleanEquation;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;
import eu.druglogics.gitsbe.input.ModelOutputs;

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

	PerturbationModel(BooleanModel booleanModel, Perturbation perturbation, ModelOutputs modelOutputs,
			Logger logger) {
		// Copy constructor from parent
		super(booleanModel, logger);

		this.perturbation = perturbation;
		this.modelOutputs = modelOutputs;
		this.logger = logger;

		StringBuilder modelNameWithDrugs = new StringBuilder(modelName);
		for (int i = 0; i < perturbation.getDrugs().length; i++) {
			modelNameWithDrugs.append("_").append(perturbation.getDrugs()[i].getName());
		}

		this.modelName = modelNameWithDrugs.toString();

		logger.outputStringMessage(3, "Added new perturbation model: " + this.modelName
				+ " with drug(s): " + perturbation.getDrugsVerbose());

		// define perturbations in model
		for (int i = 0; i < perturbation.getDrugs().length; i++) {
			this.perturbNodes(perturbation.getDrugs()[i].getTargets(),
					 		  perturbation.getDrugs()[i].getEffect());
		}
	}

	void calculateGlobalOutput() {

		if (stableStates.size() == 0) {
			logger.outputStringMessage(2, "No stable states found");
			logger.outputStringMessage(2,
				PerturbationPanel.getCombinationName(perturbation.getDrugs()) + "\tNA");
			this.hasGlobalOutput = false;
		} else {
			globaloutput = 0;
			this.hasGlobalOutput = true;

			for (String stableState : stableStates) {
				for (int j = 0; j < modelOutputs.size(); j++) {
					int indexStableState = this.getIndexOfEquation(modelOutputs.get(j).getName());
					if (indexStableState >= 0) {
						int temp = Character.getNumericValue(stableState.charAt(indexStableState));
						temp *= modelOutputs.get(j).getWeight();
						globaloutput += temp;
					}
				}
			}

			globaloutput /= stableStates.size();

			logger.debug(
				PerturbationPanel.getCombinationName(perturbation.getDrugs()) + "\t" + globaloutput
			);

		}
	}

	public Perturbation getPerturbation() {
		return perturbation;
	}

	boolean hasGlobalOutput() {
		return this.hasGlobalOutput;
	}

	float getGlobalOutput() {
		return globaloutput;
	}

	public boolean hasStableState() {
		return (stableStates.size() > 0);
	}

	public void perturbNode(String nodeName, boolean perturbation) {
		this.fixNode(nodeName, perturbation);
	}

	private void perturbNodes(ArrayList<String> nodeNames, boolean perturbation) {
		for (String nodeName : nodeNames) {
			this.fixNode(nodeName, perturbation);
		}
	}

	private void fixNode(String nodeName, boolean value) {
		int index = super.getIndexOfEquation(nodeName);

		if (index >= 0) {
			logger.outputStringMessage(2, "Fixing state of node " + nodeName + " value: " + value
					+ " (equation index: " + index + ")");
			booleanEquations.set(index, new BooleanEquation("  " + nodeName + " *= " + value + " "));
		}
	}

	public void fixNodes(String[] nodeNames, boolean[] values) {
		IntStream.range(0, nodeNames.length)
				.forEach(i -> this.fixNode(nodeNames[i], values[i]));
	}
}
