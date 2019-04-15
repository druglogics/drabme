package eu.druglogics.drabme.input;

import eu.druglogics.gitsbe.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static eu.druglogics.gitsbe.util.Util.*;

public class Config extends ConfigParametersDrabme {

    private static Config config = null;

    private Logger logger;
    private HashMap<String, String> parameterMap;

    private Config(String filename, Logger logger) throws IOException {
        this.logger = logger;
        loadConfigFile(filename);
    }

    public static Config getInstance() {
        // To ensure only one instance is created
        if (config == null) {
            throw new AssertionError("You have to call init first");
        }
        return config;
    }

    public synchronized static void init(String filename, Logger logger) throws IOException {
        if (config != null) {
            throw new AssertionError("You already initialized me");
        }
        config = new Config(filename, logger);
    }

    private void loadConfigFile(String filename) throws IOException {

        logger.outputStringMessage(3, "Reading config file: " + new File(filename).getAbsolutePath());
        ArrayList<String> lines = readLinesFromFile(filename, true);
        parameterMap = new LinkedHashMap<>();

        // Process lines
        for (String line: lines) {
            String parameterName = removeLastChar(line.split("\t")[0]);
            String value = line.split("\t")[1];

            if (line.split("\t").length != 2) {
                logger.outputStringMessage(1, "Incorrect line found in config file");
                continue;
            }

            parameterMap.put(parameterName, value);

            switch (parameterName) {
                case "verbosity":
                    verbosity = Integer.parseInt(value);
                    break;

                case "delete_tmp_files":
                    delete_tmp_files = Boolean.parseBoolean(value);
                    break;

                case "compress_log_and_tmp_files":
                    compress_log_and_tmp_files = Boolean.parseBoolean(value);
                    break;

                case "use_parallel_sim":
                    use_parallel_sim = Boolean.parseBoolean(value);
                    break;

                case "parallel_sim_num":
                    parallel_sim_num = Integer.parseInt(value);
                    break;

                case "attractor_tool":
                    attractor_tool = value;
                    checkAttractorTool(logger);
                    break;

                case "max_drug_comb_size":
                    max_drug_comb_size = Integer.parseInt(value);
                    break;
            }
        }
    }

    public String[] getConfig() {

        ArrayList<String> lines = new ArrayList<>();

        for (HashMap.Entry<String, String> entry : parameterMap.entrySet()) {
            lines.add(entry.getKey() + ": " + entry.getValue());
        }

        // adding an empty line for visibility purposes :)
        lines.add(parameterMap.size(), "");
        return lines.toArray(new String[0]);
    }

    public static void writeConfigFileTemplate(String filename) throws IOException {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");

        // Write header with '#'
        writer.println("# Gitsbe config file");
        writer.println("# Each line is a parameter name and value");
        writer.println("# Default parameters are given below");
        writer.println("#");
        writer.println("# Parameter:\tValue");
        writer.println("#");

        // Write parameters
        writer.println("# Output verbosity level");
        writer.println("verbosity:\t3");
        writer.println();
        writer.println("Check the run_example_ags/toy_ags_config.tab for a full configuration file");

        writer.flush();
        writer.close();
    }
}
