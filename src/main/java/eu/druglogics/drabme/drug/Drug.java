package eu.druglogics.drabme.drug;

import eu.druglogics.gitsbe.util.Logger;

import java.util.ArrayList;

public class Drug {

	private String name;
	private ArrayList<String> targets;
	private boolean effect;
	private Logger logger;

	public Drug(String name, Logger logger) {
		this.logger = logger;
		logger.outputStringMessage(1, "Added drug " + name);

		this.name = name;
		targets = new ArrayList<>();
	}

	/**
	 * Adds effect that describes if the drug is activating or inhibiting it's
	 * target (most drugs are inhibitors, but some can be modelled as activators,
	 * i.e. drugs inducing DNA damage that activates TP53)
	 *
	 * @param effect
	 *
	 */
	public void addEffect(boolean effect) {
		String effector;
		if (effect)
			effector = "activator";
		else
			effector = "inhibitor";

		logger.outputStringMessage(2, "Drug " + name + " annotated as " + effector);

		this.effect = effect;
	}

	public boolean getEffect() {
		return effect;
	}

	public void addTargets(String[] targets) {
		for (String target: targets) {
			addTarget(target);
		}
	}

	private void addTarget(String target) {
		logger.outputStringMessage(2, "Added target " + target + " to drug " + name);

		targets.add(target);
	}

	public void removeTarget(String target) {
		logger.outputStringMessage(2, "Removed target " + target + " from drug " + name);

		targets.remove(target);
	}

	public ArrayList<String> getTargets() {
		return targets;
	}

	public String getName() {
		return this.name;
	}
}
