package eu.druglogics.drabme.input;

import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParametersDrabmeTest {

    @Test
    void test_get_parameter_names() {
        ConfigParametersDrabme parameters = new ConfigParametersDrabme();
        int expectedParNum = 2;

        String[] pars = parameters.getParameters();

        assertEquals(expectedParNum, pars.length);
        assertEquals(pars[0], "max_drug_comb_size");
        assertEquals(pars[1], "synergy_method");
    }

    @Test
    void test_synergy_method() {
        ConfigParametersDrabme parameters = new ConfigParametersDrabme();

        parameters.synergy_method = "something";
        assertThrows(ConfigurationException.class, parameters::checkSynergyMethod);

        parameters.synergy_method = "bliss";
        assertDoesNotThrow(parameters::checkSynergyMethod);

        parameters.synergy_method = "hsaaa";
        assertThrows(ConfigurationException.class, parameters::checkSynergyMethod);

        parameters.synergy_method = "hsa";
        assertDoesNotThrow(parameters::checkSynergyMethod);
    }
}
