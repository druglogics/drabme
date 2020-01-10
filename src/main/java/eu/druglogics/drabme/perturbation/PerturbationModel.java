package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.input.Config;
import eu.druglogics.gitsbe.model.BooleanEquation;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Similar to the BooleanModel in Gitsbe, but adds drugs
 * with their computed effects.
 * 
 * @author asmund
 *
 */
public class PerturbationModel extends BooleanModel {

	private Perturbation perturbation;
	private float globalOutput;
	private boolean hasGlobalOutput = false;
	private Logger logger;

	PerturbationModel(BooleanModel booleanModel, Perturbation perturbation, Logger logger) {
		// Copy constructor from parent
		super(booleanModel, Config.getInstance().getAttractorTool(), logger);

		this.perturbation = perturbation;
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

	/**
	 * Overriden version of the {@link eu.druglogics.gitsbe.model.BooleanModel#calculateGlobalOutput}
	 * which also stores a boolean value indicating whether the <i>globalOutput</i> was calculated
	 * or not (in case there were no attractors found).
	 *
	 * @return
	 */
	@Override
	public float calculateGlobalOutput() {
		if (!hasAttractors()) {
			logger.outputStringMessage(2, "No attractors found");
			logger.outputStringMessage(2,
				PerturbationPanel.getCombinationName(perturbation.getDrugs()) + "\tNA");
			this.hasGlobalOutput = false;
		} else {
			this.hasGlobalOutput = true;
			this.globalOutput = super.calculateGlobalOutput();

			logger.debug(
				PerturbationPanel.getCombinationName(perturbation.getDrugs()) + "\t" + globalOutput
			);
		}

		return globalOutput;
	}

	public Perturbation getPerturbation() {
		return perturbation;
	}

	boolean hasGlobalOutput() {
		return this.hasGlobalOutput;
	}

	float getGlobalOutput() {
		return globalOutput;
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
