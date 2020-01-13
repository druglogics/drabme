package eu.druglogics.drabme.drug;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class DrugPanelTest {

	@Test
	void test_drugs_and_targets_with_correct_input() throws IOException, ConfigurationException {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel drugPanel = new DrugPanel(drugPanelFile, mockLogger);

		ArrayList<Drug> drugs = drugPanel.getDrugs();

		assertEquals(drugPanel.getDrugPanelSize(), 3);
		assertEquals(drugPanel.getDrugNames(), newArrayList("AA", "BB", "CC"));

		Drug drug1 = drugs.get(0);
		assertEquals(drug1.getName(), "AA");
		assertFalse(drug1.getEffect());
		assertEquals(drug1.getTargets(), newArrayList("A"));

		Drug drug2 = drugs.get(1);
		assertEquals(drug2.getName(), "BB");
		assertFalse(drug2.getEffect());
		assertEquals(drug2.getTargets(), newArrayList("B", "E"));

		Drug drug3 = drugs.get(2);
		assertEquals(drug3.getName(), "CC");
		assertFalse(drug3.getEffect());
		assertEquals(drug3.getTargets(), newArrayList("C", "F", "G"));
	}

	@Test
	void test_throw_expection_with_incorrect_effect() {
		ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
			ClassLoader classLoader = getClass().getClassLoader();
			String drugPanelFile = new File(classLoader.getResource("test_drugpanel_wrong_format").getFile()).getPath();
			Logger mockLogger = mock(Logger.class);

			new DrugPanel(drugPanelFile, mockLogger);
		});

		assertEquals(exception.getMessage(), "Drug effect: `perturbs` is neither `activates` or `inhibits`");
	}

	@Test
	void test_check_drug_targets() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel drugPanel = new DrugPanel(drugPanelFile, mockLogger);

		// Boolean Model
		ArrayList<SingleInteraction> testInteractions = new ArrayList<>();
		testInteractions.add(new SingleInteraction("A\t->\tB"));
		testInteractions.add(new SingleInteraction("C\t-|\tB"));
		testInteractions.add(new SingleInteraction("C\t->\tA"));
		testInteractions.add(new SingleInteraction("B\t-|\tD"));
		testInteractions.add(new SingleInteraction("D\t->\tC"));

		GeneralModel generalModel = new GeneralModel(testInteractions, mockLogger);
		generalModel.buildMultipleInteractions();

		BooleanModel booleanModel = new BooleanModel(generalModel, "biolqm_stable_states", mockLogger);

		doAnswer(invocation -> {
			Integer verbosity = invocation.getArgument(0);
			String message = invocation.getArgument(1);

			assertEquals(3, verbosity);
			if (message.contains("E"))
				assertEquals("Warning: Target `E` of Drug `BB` is not in the network file/model", message);
			else if (message.contains("F"))
				assertEquals("Warning: Target `F` of Drug `CC` is not in the network file/model", message);
			else if (message.contains("G"))
				assertEquals("Warning: Target `G` of Drug `CC` is not in the network file/model", message);
			return null;
		}).when(mockLogger).outputStringMessage(isA(Integer.class), isA(String.class));

		// `E`,`F` and `G` targets are not in the network model
		drugPanel.checkDrugTargets(newArrayList(booleanModel));
	}

	@Test
	void test_are_drugs_in_panel() throws IOException, ConfigurationException {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel drugPanel = new DrugPanel(drugPanelFile, mockLogger);

		assertTrue(drugPanel.areDrugsInPanel(newArrayList("AA")));
		assertTrue(drugPanel.areDrugsInPanel(newArrayList("BB")));
		assertTrue(drugPanel.areDrugsInPanel(newArrayList("CC")));
		assertTrue(drugPanel.areDrugsInPanel(newArrayList("AA", "BB")));
		assertTrue(drugPanel.areDrugsInPanel(newArrayList("AA", "CC")));
		assertTrue(drugPanel.areDrugsInPanel(newArrayList("BB", "CC")));
		assertTrue(drugPanel.areDrugsInPanel(newArrayList("AA", "BB", "CC")));

		assertTrue(drugPanel.isDrugInPanel("AA"));
		assertTrue(drugPanel.isDrugInPanel("BB"));
		assertTrue(drugPanel.isDrugInPanel("CC"));

		// at least one is not in the panel
		assertFalse(drugPanel.areDrugsInPanel(newArrayList("")));
		assertFalse(drugPanel.areDrugsInPanel(newArrayList("A", "C")));
		assertFalse(drugPanel.areDrugsInPanel(newArrayList("AA", "CB")));
		assertFalse(drugPanel.areDrugsInPanel(newArrayList("AA", "BB", "WER")));

		assertFalse(drugPanel.isDrugInPanel("A"));
		assertFalse(drugPanel.isDrugInPanel(""));
		assertFalse(drugPanel.isDrugInPanel("LSD"));
		assertFalse(drugPanel.isDrugInPanel("ARGAIV"));
	}

	@Test
	void test_get_drug() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel drugPanel = new DrugPanel(drugPanelFile, mockLogger);

		Drug a = drugPanel.getDrug("AA");
		assertEquals(a.getName(), "AA");
		assertFalse(a.getEffect());
		assertEquals(a.getTargets(), newArrayList("A"));

		Drug b = drugPanel.getDrug("BB");
		assertEquals(b.getName(), "BB");
		assertFalse(b.getEffect());
		assertEquals(b.getTargets(), newArrayList("B", "E"));

		Drug c = drugPanel.getDrug("CC");
		assertEquals(c.getName(), "CC");
		assertFalse(c.getEffect());
		assertEquals(c.getTargets(), newArrayList("C", "F", "G"));

		Exception exception = assertThrows(Exception.class, () -> drugPanel.getDrug("DD"));
		assertEquals(exception.getMessage(), "Drug `DD` was not found in drug panel");
	}

	@Test
	void test_load_combos_from_file_and_data_consistency() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel drugPanel = new DrugPanel(drugPanelFile, mockLogger);

		String perturbationsFile = new File(classLoader.getResource("test_perturbations").getFile()).getPath();
		Drug[][] perturbations = drugPanel.loadCombinationsFromFile(perturbationsFile);

		assertEquals(perturbations.length, 7);

		// single perturbations
		Drug[] a = perturbations[0];
		assertEquals(a.length, 1);
		Drug aa = a[0];
		assertEquals(aa.getName(), "AA");
		assertFalse(aa.getEffect());
		assertEquals(aa.getTargets(), newArrayList("A"));

		Drug[] b = perturbations[1];
		assertEquals(b.length, 1);
		Drug bb = b[0];
		assertEquals(bb.getName(), "BB");
		assertFalse(bb.getEffect());
		assertEquals(bb.getTargets(), newArrayList("B", "E"));

		Drug[] c = perturbations[2];
		assertEquals(c.length, 1);
		Drug cc = c[0];
		assertEquals(cc.getName(), "CC");
		assertFalse(cc.getEffect());
		assertEquals(cc.getTargets(), newArrayList("C", "F", "G"));

		// double perturbations
		Drug[] ab = perturbations[3];
		assertEquals(ab.length, 2);
		assertEquals(ab[0].getName(), "AA");
		assertEquals(ab[1].getName(), "BB");

		Drug[] ac = perturbations[4];
		assertEquals(ac.length, 2);
		assertEquals(ac[0].getName(), "AA");
		assertEquals(ac[1].getName(), "CC");

		Drug[] bc = perturbations[5];
		assertEquals(bc.length, 2);
		assertEquals(bc[0].getName(), "BB");
		assertEquals(bc[1].getName(), "CC");

		// triple perturbations
		Drug[] abc = perturbations[6];
		assertEquals(abc.length, 3);
		assertEquals(abc[0].getName(), "AA");
		assertEquals(abc[1].getName(), "BB");
		assertEquals(abc[2].getName(), "CC");

		Exception exception1 = assertThrows(Exception.class, () -> {
			String perturbationsFile1 =
				new File(classLoader.getResource("test_perturbations_check1").getFile()).getPath();
			drugPanel.loadCombinationsFromFile(perturbationsFile1);
		});

		assertEquals(exception1.getMessage(), "Drug `C` was not found in drug panel");

		Exception exception2 = assertThrows(Exception.class, () -> {
			String perturbationsFile2 =
				new File(classLoader.getResource("test_perturbations_check2").getFile()).getPath();
			drugPanel.loadCombinationsFromFile(perturbationsFile2);
		});

		assertEquals(exception2.getMessage(), "Drug `E` was not found in drug panel");

		Exception exception3 = assertThrows(Exception.class, () -> {
			String perturbationsFile3 =
				new File(classLoader.getResource("test_perturbations_check3").getFile()).getPath();
			drugPanel.loadCombinationsFromFile(perturbationsFile3);
		});

		assertEquals(exception3.getMessage(), "The drug combination `[AA]-[CC]-[BB]` does not have "
			+ "the subset `[AA]-[CC]` defined in the perturbations file");
	}

	@Test
	void test_get_drug_combinations() throws IOException, ConfigurationException {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		// Test DrugPanel has 3 Drugs
		DrugPanel drugPanel = new DrugPanel(drugPanelFile, mockLogger);

		assertEquals(drugPanel.getDrugCombinations(-1).length, 0);
		assertEquals(drugPanel.getDrugCombinations(0).length, 0);
		assertEquals(drugPanel.getDrugCombinations(1).length, 3); // single
		assertEquals(drugPanel.getDrugCombinations(2).length, 6); // pair-wise (+3)
		assertEquals(drugPanel.getDrugCombinations(3).length, 7); // + 1 triple combo
		assertEquals(drugPanel.getDrugCombinations(4).length, 7); // no 4-element combinations
		assertEquals(drugPanel.getDrugCombinations(5).length, 7);

		Drug[] singleDrug = drugPanel.getDrugCombinations(2)[1];
		assertEquals(singleDrug.length, 1);
		assertEquals(singleDrug[0].getName(), "BB");

		Drug[] doubleCombo = drugPanel.getDrugCombinations(2)[4];
		assertEquals(doubleCombo.length, 2);
		assertEquals(doubleCombo[0].getName(), "AA");
		assertEquals(doubleCombo[1].getName(), "CC");

		Drug[] tripleCombo = drugPanel.getDrugCombinations(3)[6];
		assertEquals(tripleCombo.length, 3);
		assertEquals(tripleCombo[0].getName(), "AA");
		assertEquals(tripleCombo[1].getName(), "BB");
		assertEquals(tripleCombo[2].getName(), "CC");
	}

	@Test
	void test_get_combination_subsets() {
		Logger mockLogger = mock(Logger.class);

		// SET: {A,B}
		Drug[] drugSet = new Drug[2];
		drugSet[0] = new Drug("A", mockLogger);
		drugSet[1] = new Drug("B", mockLogger);

		Drug[][] subsets = DrugPanel.getCombinationSubsets(drugSet);
		assertEquals(subsets.length, 2);
		assertEquals(subsets[0][0].getName(), "A");
		assertEquals(subsets[1][0].getName(), "B");

		// SET: {A,B,C}
		Drug[] drugSet2 = new Drug[3];
		drugSet2[0] = new Drug("A", mockLogger);
		drugSet2[1] = new Drug("B", mockLogger);
		drugSet2[2] = new Drug("C", mockLogger);

		Drug[][] subsets2 = DrugPanel.getCombinationSubsets(drugSet2);
		assertEquals(subsets2.length, 3);

		Drug[] firstSubset = subsets2[0]; // A-B
		assertEquals(firstSubset[0].getName(), "A");
		assertEquals(firstSubset[1].getName(), "B");

		Drug[] secondSubset = subsets2[1]; // A-C
		assertEquals(secondSubset[0].getName(), "A");
		assertEquals(secondSubset[1].getName(), "C");

		Drug[] thirdSubset = subsets2[2]; // B-C
		assertEquals(thirdSubset[0].getName(), "B");
		assertEquals(thirdSubset[1].getName(), "C");

		// SET: {A,B,C,D,E}
		Drug[] drugSet3 = new Drug[5];
		drugSet3[0] = new Drug("A", mockLogger);
		drugSet3[1] = new Drug("B", mockLogger);
		drugSet3[2] = new Drug("C", mockLogger);
		drugSet3[3] = new Drug("D", mockLogger);
		drugSet3[4] = new Drug("E", mockLogger);

		Drug[][] subsets3 = DrugPanel.getCombinationSubsets(drugSet3);
		assertEquals(subsets3.length, 5);
	}

	@Test
	void test_get_drug_set_hash() {
		Logger mockLogger = mock(Logger.class);

		// EMPTY SET
		assertEquals(DrugPanel.getDrugSetHash(new Drug[0]), 0);

		// SINGLE SET
		Drug[] singleDrugSet = new Drug[1];
		singleDrugSet[0] = new Drug("A", mockLogger);
		assertEquals(DrugPanel.getDrugSetHash(singleDrugSet), 65);

		// SET: {A,B}
		Drug[] doubleDrugSet = new Drug[2];
		doubleDrugSet[0] = new Drug("A", mockLogger);
		doubleDrugSet[1] = new Drug("B", mockLogger);
		assertEquals(DrugPanel.getDrugSetHash(doubleDrugSet), 131);

		// SET: {A,B,C,D}
		Drug[] fourDrugSet = new Drug[4];
		fourDrugSet[0] = new Drug("A", mockLogger);
		fourDrugSet[1] = new Drug("B", mockLogger);
		fourDrugSet[2] = new Drug("C", mockLogger);
		fourDrugSet[3] = new Drug("D", mockLogger);
		assertEquals(DrugPanel.getDrugSetHash(fourDrugSet), 266);
	}
}
