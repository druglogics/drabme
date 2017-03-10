package drabme;

import gitsbe.Logger;

import java.util.ArrayList;

public class Drug {

	private String name ;
	private ArrayList <String> targets ;
	private boolean effect ;
	private Logger logger ;
	
	/**
	 * 
	 * @param name
	 *            of drug
	 */
	public Drug(String name, Logger logger) {

		this.logger = logger ;
		logger.output(1, "Added drug " + name);

		this.name = name;
		targets = new ArrayList<String>();
		
		
	}

	/**
	 * 
	 * @param effect
	 *            to describe if drug is activating or inhibiting it's target
	 *            (most drugs are inhibitors, but some can be modelled as
	 *            activators, i.e. drugs inducing DNA damage that activates
	 *            TP53)
	 */
	public void addEffect(boolean effect) {
		String effector = "";
		if (effect == true)
			effector = "activator";
		else
			effector = "inhibitor";

		logger.output(2, "Drug " + name + " annotated as " + effector);

		this.effect = effect;
	}

	public boolean getEffect() {
		return effect;
	}

	/**
	 * 
	 * @param targets
	 *            to be added
	 */
	public void addTargets(String[] targets) {
		for (int i = 0; i < targets.length; i++) {
			this.addTarget(targets[i]);
		}

	}

	/**
	 * 
	 * @param target
	 *            to be added
	 */
	public void addTarget(String target) {
		logger.output(2, "Added target " + target + " to drug " + name);

		targets.add(target);
	}

	/**
	 * 
	 * @param target
	 *            to be removed
	 */
	public void removeTarget(String target) {
		logger.output(2, "Removed target " + target + " from drug " + name);

		targets.remove(targets.indexOf(target));
	}

	/**
	 * 
	 * @return ArrayList of all drug targets
	 */
	public ArrayList<String> getTargets() {
		return targets;
	}

	/**
	 * 
	 * @return name of drug
	 */
	public String getName() {
		return this.name;
	}
}
