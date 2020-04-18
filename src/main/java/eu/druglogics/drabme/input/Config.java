package eu.druglogics.drabme.input;

import eu.druglogics.gitsbe.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static eu.druglogics.gitsbe.util.Util.readLinesFromFile;
import static eu.druglogics.gitsbe.util.Util.removeLastChar;

public class Config extends ConfigParametersDrabme {

    private static Config config = null;

    private Logger logger;
    private LinkedHashMap<String, String> parameterMap;

    private Config(String filename, Logger logger) throws Exception {
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

    public synchronized static void init(String filename, Logger logger) throws Exception {
        if (config != null) {
            throw new AssertionError("You already initialized me");
        }
        config = new Config(filename, logger);
    }

    private void loadConfigFile(String filename) throws Exception {

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
                    checkVerbosity();
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
                    checkParallelSimulationsNumber();
                    break;

                case "attractor_tool":
                    attractor_tool = value;
                    checkAttractorTool();
                    break;

                case "max_drug_comb_size":
                    max_drug_comb_size = Integer.parseInt(value);
                    break;

                case "synergy_method":
                    synergy_method = value;
                    checkSynergyMethod();
                    break;
            }
        }
    }



    public String[] getConfig() {

        ArrayList<String> lines = new ArrayList<>();

        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            lines.add(entry.getKey() + ": " + entry.getValue());
        }

        // adding an empty line for visibility purposes :)
        lines.add(parameterMap.size(), "");
        return lines.toArray(new String[0]);
    }
}
