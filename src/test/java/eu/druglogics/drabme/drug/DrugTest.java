package eu.druglogics.drabme.drug;

import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DrugTest {

    private Drug testDrug;

    @BeforeEach
    void init() {
        Logger mockLogger = mock(Logger.class);
        this.testDrug = new Drug("PD", mockLogger);
    }

    @Test
    void test_add_effect() {
        testDrug.addEffect(false);
        assertFalse(testDrug.getEffect());

        testDrug.addEffect(true);
        assertTrue(testDrug.getEffect());
    }

    @Test
    void test_add_empty_targets() {
        String[] testTargets = {};
        testDrug.addTargets(testTargets);

        assertTrue(testDrug.getTargets().isEmpty());
    }

    @Test
    void test_add_non_empty_targets() {
        String [] testTargets = {"MAP2K1", "MAP2K2"};
        testDrug.addTargets(testTargets);

        ArrayList<String> targets = testDrug.getTargets();

        assertFalse(targets.isEmpty());
        assertEquals(targets.size(), 2);
        assertEquals(targets.get(0), "MAP2K1");
        assertEquals(targets.get(1), "MAP2K2");
    }

    @Test
    void test_remove_target() {
        String [] testTargets = {"MAP2K1", "MAP2K2"};
        testDrug.addTargets(testTargets);
        testDrug.removeTarget("MAP"); // non-existent target

        ArrayList<String> targets = testDrug.getTargets();

        assertEquals(targets.size(), 2);
        assertEquals(targets.get(0), "MAP2K1");
        assertEquals(targets.get(1), "MAP2K2");

        testDrug.removeTarget("MAP2K1");
        assertEquals(targets.size(), 1);
        assertEquals(targets.get(0), "MAP2K2");
    }
}
