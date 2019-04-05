package eu.druglogics.drabme.drug;

import eu.druglogics.gitsbe.util.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class DrugTest {

    private Drug testDrug;

    @Before
    public void init() {
        Logger mockLogger = mock(Logger.class);
        this.testDrug = new Drug("PD", mockLogger);
    }

    @Test
    public void test_add_effect() {
        testDrug.addEffect(false);
        Assert.assertFalse(testDrug.getEffect());

        testDrug.addEffect(true);
        Assert.assertTrue(testDrug.getEffect());
    }

    @Test
    public void test_add_empty_targets() {
        String[] testTargets = {};
        testDrug.addTargets(testTargets);

        Assert.assertTrue(testDrug.getTargets().isEmpty());
    }

    @Test
    public void test_add_non_empty_targets() {
        String [] testTargets = {"MAP2K1", "MAP2K2"};
        testDrug.addTargets(testTargets);

        ArrayList<String> targets = testDrug.getTargets();

        Assert.assertFalse(targets.isEmpty());
        Assert.assertEquals(targets.size(), 2);
        Assert.assertEquals(targets.get(0), "MAP2K1");
        Assert.assertEquals(targets.get(1), "MAP2K2");
    }

    @Test
    public void test_remove_target() {
        String [] testTargets = {"MAP2K1", "MAP2K2"};
        testDrug.addTargets(testTargets);
        testDrug.removeTarget("MAP"); // non-existent target

        ArrayList<String> targets = testDrug.getTargets();

        Assert.assertEquals(targets.size(), 2);
        Assert.assertEquals(targets.get(0), "MAP2K1");
        Assert.assertEquals(targets.get(1), "MAP2K2");

        testDrug.removeTarget("MAP2K1");
        Assert.assertEquals(targets.size(), 1);
        Assert.assertEquals(targets.get(0), "MAP2K2");
    }
}
