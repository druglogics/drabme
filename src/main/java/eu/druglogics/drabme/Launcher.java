package eu.druglogics.drabme;

import static eu.druglogics.gitsbe.Util.*;

public class Launcher {

	public static void main(String[] args) {
		Launcher drabmeLauncher = new Launcher();
		drabmeLauncher.start(args);
	}

	public void start(String[] args) {
		if (environmentalVariableBNETisNULL())
			return;
		setupInputAndRun(args);
	}

	private void setupInputAndRun(String[] args) {
		if (args.length == 0) {
			System.out.println("No user arguments supplied");
			System.out.println(
					"Usage: drabme <name project> <directory models> <filename drugs> <filename model outputs> "
							+ "<filename combinations> <directory output> <preserveTmpFiles> <directory tmp> <verbosity>");

			System.out.println("\nExample Testrun (command line usage): ");

			args = new String[] { "example_run_ags", "directoryOutput/models", "toy_ags_drugpanel.tab",
					"toy_ags_modeloutputs.tab", "toy_ags_perturbations.tab", "directoryOutput", "false",
					"directoryOutput/drabme_tmp", "verbosity" };
			System.out.println(
					"java -cp /pathToBinFolderOfDrabmeProject:/pathToBinFolderOfGitsbeProject:lib/combinatoricslib-2.1.jar:lib/commons-math3-3.4.1.jar drabme.Launcher "
							+ args[0] + " " + args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5]
							+ " " + args[6] + " " + args[7] + " " + args[8] + "\n\n");
		} else {
			String projectName = args[0];
			String directoryModels = args[1];
			String filenameDrugs = args[2];
			String filenameModelOutputs = args[3];
			String filenameCombinations = args[4];
			String directoryOutput = args[5];
			boolean preserveTmpFiles = Boolean.parseBoolean(args[6]);
			String directoryTmp = args[7];
			int verbosity = Integer.parseInt(args[8]);
			
			directoryTmp = makeDirectoryPathAbsolute(directoryTmp);
			directoryOutput = makeDirectoryPathAbsolute(directoryOutput);
			
			int combosize = 2;
			Thread t;

			t = new Thread(new Drabme(verbosity, projectName, directoryModels, filenameDrugs, filenameCombinations,
					filenameModelOutputs, directoryOutput, directoryTmp, preserveTmpFiles, combosize));
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
