package drabme;

import gitsbe.Logger;

import java.util.ArrayList;

public class Drug {

	private String name ;
	private ArrayList <String> targets ;
	private boolean effect ;
	
	/**
	 * 
	 * @param name
	 *            of drug
	 */
	public Drug(String name) {

		Logger.output(1, "Added drug " + name);

		this.name = name;
		targets = new ArrayList<String>();
	}

	public void addEffect(boolean effect) {
		String effector = "";
		if (effect == true)
			effector = "activator";
		else
			effector = "inhibitor";

		Logger.output(2, "Drug " + name + " annotated as " + effector);

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
		Logger.output(2, "Added target " + target + " to drug " + name);

		targets.add(target);
	}

	/**
	 * 
	 * @param target
	 *            to be removed
	 */
	public void removeTarget(String target) {
		Logger.output(2, "Removed target " + target + " from drug " + name);

		targets.remove(targets.indexOf(target));
	}

	/**
	 * 
	 * @return arraylist of all drug targets
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
