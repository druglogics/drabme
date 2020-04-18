package eu.druglogics.drabme.input;

import eu.druglogics.gitsbe.input.ConfigParametersGlobal;

import javax.naming.ConfigurationException;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ConfigParametersDrabme extends ConfigParametersGlobal {

    public int max_drug_comb_size;
    public String synergy_method;

    public int getCombinationSize() {
        return max_drug_comb_size;
    }

    public String getSynergyMethod() {
        return synergy_method;
    }

    public String[] getParameters() {
        Field[] fields = ConfigParametersDrabme.class.getDeclaredFields();

        return Arrays.stream(fields).map(Field::getName).toArray(String[]::new);
    }

    public void checkSynergyMethod() throws ConfigurationException {
        if (!synergy_method.equals("hsa") && !synergy_method.equals("bliss")) {
            throw new ConfigurationException("Synergy method `" + synergy_method + "` not " +
                "recognised: it must be either `hsa` or `bliss`");
        }
    }
}
