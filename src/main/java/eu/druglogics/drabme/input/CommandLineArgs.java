package eu.druglogics.drabme.input;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=")
public class CommandLineArgs {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "--project", "-p" },
            description = "Name of the project", order = 0)
    private String projectName;

    @Parameter(names = { "--modelsDir", "-md" }, required = true,
            description = "Models directory", order = 1)
    private String directoryModels;

    @Parameter(names = { "--drugs", "-dr" }, required = true,
            description = "Drugs file", order = 2)
    private String filenameDrugs;

    @Parameter(names = { "--perturbations", "-per" },
            description = "Perturbations file", order = 3)
    private String filenamePerturbations;

    @Parameter(names = { "--config", "-c" }, required = true,
            description = "Configuration file", order = 4)
    private String filenameConfig;

    @Parameter(names = { "--modeloutputs", "-m" }, required = true,
            description = "Model outputs file", order = 5)
    private String filenameModelOutputs;

    public String getProjectName() {
        return projectName;
    }

    public String getDirectoryModels() {
        return directoryModels;
    }

    public String getFilenameDrugs() {
        return filenameDrugs;
    }

    public String getFilenamePerturbations() {
        return filenamePerturbations;
    }

    public String getFilenameConfig() {
        return filenameConfig;
    }

    public String getFilenameModelOutputs() {
        return filenameModelOutputs;
    }
}

