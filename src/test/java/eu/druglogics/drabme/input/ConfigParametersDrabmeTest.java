package eu.druglogics.drabme.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigParametersDrabmeTest {

    @Test
    void test_get_parameter_names() {
        ConfigParametersDrabme parameters = new ConfigParametersDrabme();
        int expectedParNum = 1;

        String[] pars = parameters.getParameters();

        assertEquals(expectedParNum, pars.length);
        assertEquals(pars[0], "max_drug_comb_size");
    }
}
