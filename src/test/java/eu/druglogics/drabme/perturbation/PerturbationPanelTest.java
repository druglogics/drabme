package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

		panel.getPerturbations()[0].addPrediction((float) 0.7); // A
		panel.getPerturbations()[1].addPrediction((float) 0.8); // B
		panel.getPerturbations()[2].addPrediction((float) 1.0); // A + B

		DecimalFormat df = new DecimalFormat("#.000");
		// Antagonistic
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.2);

		// non-interaction
		panel.getPerturbations()[2].addPrediction((float) 0.5); // A + B => 0.75
		panel.getPerturbations()[2].calculateStatistics();
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.0);

		// still non-interaction
		panel.getPerturbations()[2].addPrediction((float) 0.65); // A + B => 0.716
		panel.getPerturbations()[2].calculateStatistics();
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), 0.0);

		// Synergy
		panel.getPerturbations()[2].addPrediction((float) 0.2); // A + B => 0.588
		panel.getPerturbations()[2].calculateStatistics();
		assertEquals(Double.valueOf(df.format(
			panel.getAverageResponseExcessOverSubsets(panel.getPerturbations()[2]))), -0.112);
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
