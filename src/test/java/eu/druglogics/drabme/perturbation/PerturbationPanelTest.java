package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.drabme.input.Config;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassLoaderUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PerturbationPanelTest {

	private static Logger mockLogger;
	private static Drug drugA;
	private static Drug drugB;
	private static Drug drugC;

	@BeforeAll
	static void init_model_outputs() throws Exception {
		Logger mockLogger = mock(Logger.class);
		ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
		String filename = new File(classLoader.getResource("test_modeloutputs_2").getFile()).getPath();
		ModelOutputs.init(filename, mockLogger);
	}

	@AfterAll
	static void reset_model_outputs() throws IllegalAccessException, NoSuchFieldException {
		Field instance = ModelOutputs.class.getDeclaredField("modeloutputs");
		instance.setAccessible(true);
		instance.set(null, null);
	}

	@BeforeAll
	static void init_config() throws Exception {
		Logger mockLogger = mock(Logger.class);

		ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
		String filename = new File(classLoader.getResource("test_config").getFile()).getPath();

		Config.init(filename, mockLogger);
	}

	@AfterAll
	static void reset_config() throws IllegalAccessException, NoSuchFieldException {
		Field instance = Config.class.getDeclaredField("config");
		instance.setAccessible(true);
		instance.set(null, null);
	}

	@BeforeAll
	static void init_drugs_and_logger() {
		mockLogger = mock(Logger.class);

		// A drug, a double inhibitor
		drugA = new Drug("A", mockLogger);
		drugA.addTargets(new String[]{"AA1", "AA2"});
		drugA.addEffect(false);

		// B drug, a double inhibitor
		drugB = new Drug("B", mockLogger);
		drugB.addTargets(new String[]{"BB1", "BB2"});
		drugB.addEffect(false);

		// C Drug, a single activator
		drugC = new Drug("C", mockLogger);
		drugC.addTargets(new String[]{"CC1"});
		drugC.addEffect(true);
	}

	@Test
	void test_panel_with_three_perturbations() {
		// 1st perturbation: A drug alone
		Drug[] perturbationA = new Drug[]{ drugA };

		// 2nd perturbation: B drug alone
		Drug[] perturbationB = new Drug[]{ drugB };

		// 3rd perturbation: A + B together!
		Drug[] perturbationAB = new Drug[]{ drugA, drugB };

		Drug[][] perturbations = new Drug[][]{perturbationA, perturbationB, perturbationAB};
		PerturbationPanel panel = new PerturbationPanel(perturbations, mockLogger);

		assertEquals(panel.getNumberOfPerturbations(), 3);
		assertEquals(panel.getNumberOfPerturbations(-1), 0);
		assertEquals(panel.getNumberOfPerturbations(0), 0);
		assertEquals(panel.getNumberOfPerturbations(1), 2);
		assertEquals(panel.getNumberOfPerturbations(2), 1);
		assertEquals(panel.getNumberOfPerturbations(34), 0);
		assertArrayEquals(panel.getPerturbations(2), new Perturbation[]{panel.getPerturbations()[2]});
		assertArrayEquals(panel.getPerturbations(0), new Perturbation[]{});
		assertEquals(panel.getPerturbations()[0].getName(), "[A]");
		assertEquals(panel.getPerturbations()[1].getName(), "[B]");
		assertEquals(panel.getPerturbations()[2].getName(), "[A]-[B]");
		assertEquals(panel.getIndexOfPerturbation(perturbationA), 0);
		assertEquals(panel.getIndexOfPerturbation(perturbationB), 1);
		assertEquals(panel.getIndexOfPerturbation(perturbationAB), 2);
		// perturbation not in panel!
		assertEquals(panel.getIndexOfPerturbation(new Drug[]{drugB, drugC}), -1);

		// norm => normalized with test_modeloutputs_2: minGl: -2, maxGL: 2
		panel.getPerturbations()[0].addPrediction((float) 0.7); // A, norm => 0.675
		panel.getPerturbations()[1].addPrediction((float) 0.8); // B, norm => 0.7
		panel.getPerturbations()[2].addPrediction((float) 1.0); // A + B or (A,B), norm => 0.75

		DecimalFormat df = new DecimalFormat("#.000");

		// HSA: (A,B) < min(A,B) => 0.7
		// Bliss: (A,B) - A*B => (norm) 0.4725

		// HSA Antagonistic
		Config.getInstance().synergy_method = "hsa";
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.2);
		// Bliss Antagonistic
		Config.getInstance().synergy_method = "bliss";
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.278);

		// HSA non-interaction
		Config.getInstance().synergy_method = "hsa";
		panel.getPerturbations()[2].addPrediction((float) 0.5); // A + B => 0.75, norm => 0.6875
		panel.getPerturbations()[2].calculateStatistics();
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.0);
		// Bliss Antagonistic
		Config.getInstance().synergy_method = "bliss";
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.215);

		// HSA still non-interaction
		Config.getInstance().synergy_method = "hsa";
		panel.getPerturbations()[2].addPrediction((float) 0.65); // A + B => 0.716, norm => 0.679
		panel.getPerturbations()[2].calculateStatistics();
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.0);
		// Bliss Antagonistic
		Config.getInstance().synergy_method = "bliss";
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.207);

		// HSA Synergy
		Config.getInstance().synergy_method = "hsa";
		panel.getPerturbations()[2].addPrediction((float) 0.2); // A + B => 0.588, norm => 0.647
		panel.getPerturbations()[2].calculateStatistics();
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), -0.112);
		// Bliss still Antagonistic!
		Config.getInstance().synergy_method = "bliss";
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.174);

		// HSA Synergy
		Config.getInstance().synergy_method = "hsa";
		panel.getPerturbations()[2].addPrediction((float) -5); // A + B => -0.53, norm => 0.3675
		panel.getPerturbations()[2].calculateStatistics();
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), -1.23);
		// Bliss Synergy as well!
		Config.getInstance().synergy_method = "bliss";
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), -0.105);
	}

	@Test
	void test_get_combination_name() {
		assertEquals(PerturbationPanel.getCombinationName(new Drug[]{drugA}), "[A]");
		assertEquals(PerturbationPanel.getCombinationName(new Drug[]{drugB}), "[B]");
		assertEquals(PerturbationPanel.getCombinationName(new Drug[]{drugC}), "[C]");
		assertEquals(PerturbationPanel.getCombinationName(new Drug[]{drugA, drugB}), "[A]-[B]");
		assertEquals(PerturbationPanel.getCombinationName(new Drug[]{drugA, drugC}), "[A]-[C]");
		assertEquals(PerturbationPanel.getCombinationName(new Drug[]{drugB, drugC}), "[B]-[C]");
		assertEquals(PerturbationPanel.getCombinationName(new Drug[]{drugA, drugB, drugC}), "[A]-[B]-[C]");
		assertEquals(PerturbationPanel.getCombinationName(new Drug[]{drugC, drugA, drugB}), "[C]-[A]-[B]");
	}
}
