package eu.druglogics.drabme.input;

import eu.druglogics.gitsbe.input.ConfigParametersGlobal;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ConfigParametersDrabme extends ConfigParametersGlobal {

    public int max_drug_comb_size;

    public int getCombinationSize() {
        return max_drug_comb_size;
    }

    public String[] getParameters() {
        Field[] fields = ConfigParametersDrabme.class.getDeclaredFields();

        return Arrays.stream(fields).map(Field::getName).toArray(String[]::new);
    }
}
