package eu.druglogics.drabme.perturbation;

import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.input.OutputWeight;
import eu.druglogics.gitsbe.model.BooleanEquation;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.util.Logger;

import java.util.ArrayList;

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

	/**
	 * Constructor for building a {@link PerturbationModel} out of a
	 * given {@link BooleanModel} and {@link Perturbation}.
	 *
	 * @param booleanModel
	 * @param perturbation
	 * @param logger
	 */
	PerturbationModel(BooleanModel booleanModel, Perturbation perturbation, Logger logger) {
		// Copy constructor from parent
		super(booleanModel, logger);

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
	 * Use this function after you have calculated the {@link #calculateAttractors(String)
	 * attractors} in order to find the <b>non-normalized</b> <i>globaloutput</i> of the
	 * perturbed model, based on the weights of the nodes as defined in the {@link ModelOutputs}
	 * Class. Also, a boolean value indicating whether the model has a globaloutput or
	 * not (the latter in case no attractors were found) is stored.
	 *
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

			ModelOutputs modelOutputs = ModelOutputs.getInstance();
			globalOutput = 0;

			for (String attractor : this.getAttractors()) {
				for (OutputWeight outputWeight : modelOutputs.getModelOutputs()) {
					int nodeIndexInAttractor = this.getIndexOfEquation(outputWeight.getNodeName());
					if (nodeIndexInAttractor >= 0) {
						// can only be '1','0' or '-'
						char nodeState = attractor.charAt(nodeIndexInAttractor);
						float stateValue = (nodeState == '-')
							? (float) 0.5
							: Character.getNumericValue(attractor.charAt(nodeIndexInAttractor));

						globalOutput += stateValue * outputWeight.getWeight();
					}
				}
			}

			globalOutput /= getAttractors().size();

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

	/**
	 * Use this function after {@link #calculateGlobalOutput()} in order to get the
	 * <b>normalized</b> <i>globaloutput</i> of the perturbed model.
	 *
	 * @return the {@link #globalOutput} value in the [0,1] range
	 */
	float getNormalizedGlobalOutput() {
		ModelOutputs modelOutputs = ModelOutputs.getInstance();
		return ((globalOutput - modelOutputs.getMinOutput()) / (modelOutputs.getMaxOutput() - modelOutputs.getMinOutput()));
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
}
