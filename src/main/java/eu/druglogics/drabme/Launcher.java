package eu.druglogics.drabme;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import eu.druglogics.drabme.input.CommandLineArgs;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static eu.druglogics.gitsbe.util.Util.*;

public class Launcher {

	public static void main(String[] args) {
		Launcher drabmeLauncher = new Launcher();
		drabmeLauncher.start(args);
	}

	private void start(String[] args) {

		try {
			CommandLineArgs arguments = new CommandLineArgs();
			JCommander.newBuilder().addObject(arguments).build().parse(args);

			String projectName = arguments.getProjectName();
			String directoryModels = arguments.getDirectoryModels();
			String filenameDrugs = arguments.getFilenameDrugs();
			String filenamePerturbations = arguments.getFilenamePerturbations();
			String filenameConfig = arguments.getFilenameConfig();
			String filenameModelOutputs = arguments.getFilenameModelOutputs();

			// Inferring the input directory from the config file
			String directoryInput = inferInputDir(filenameConfig);

			// projectName is not required, but we set it either way
			if (projectName == null) {
				projectName = getFileName(directoryInput);
			}

			directoryModels = makeDirectoryPathAbsolute(directoryModels);

			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String directoryOutput = new File(directoryInput,
					projectName + "_" + dateFormat.format(Calendar.getInstance().getTime()))
					.getAbsolutePath();
			String directoryTmp = new File(directoryOutput, "drabme_tmp").getAbsolutePath();

			Thread thread = new Thread(new Drabme(
					projectName,
					filenameDrugs,
					filenamePerturbations,
					filenameModelOutputs,
					filenameConfig,
					directoryModels,
					directoryOutput,
					directoryTmp
			));
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				abort();
			}
		} catch (ParameterException parEx) {
			System.out.println("\nOptions preceded by an asterisk are required.");
			parEx.getJCommander().setProgramName("eu.druglogics.drabme.Launcher");
			parEx.usage();
		}
	}
}
