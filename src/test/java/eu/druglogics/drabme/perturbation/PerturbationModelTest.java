package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.util.ClassLoaderUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PerturbationModelTest {

	private BooleanModel booleanModel;
	private Logger mockLogger = mock(Logger.class);

	@BeforeAll
	static void init_model_outputs() throws Exception {
		Logger mockLogger = mock(Logger.class);
		ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
		String filename = new File(classLoader.getResource("test_modeloutputs").getFile()).getPath();
		ModelOutputs.init(filename, mockLogger);
	}

	@AfterAll
	static void reset_model_outputs() throws IllegalAccessException, NoSuchFieldException {
		Field instance = ModelOutputs.class.getDeclaredField("modeloutputs");
		instance.setAccessible(true);
		instance.set(null, null);
	}

	@BeforeEach
	void init() {
		// I,J are input nodes, F,U,K are output nodes
		ArrayList<SingleInteraction> testInteractions = new ArrayList<>();
		testInteractions.add(new SingleInteraction("A\t->\tB"));
		testInteractions.add(new SingleInteraction("C\t-|\tB"));
		testInteractions.add(new SingleInteraction("C\t->\tA"));
		testInteractions.add(new SingleInteraction("B\t-|\tD"));
		testInteractions.add(new SingleInteraction("D\t->\tC"));
		testInteractions.add(new SingleInteraction("D\t-|\tW"));
		testInteractions.add(new SingleInteraction("W\t->\tF"));
		testInteractions.add(new SingleInteraction("W\t->\tU"));
		testInteractions.add(new SingleInteraction("W\t->\tK"));
		testInteractions.add(new SingleInteraction("I\t->\tW"));
		testInteractions.add(new SingleInteraction("E\t->\tC"));
		testInteractions.add(new SingleInteraction("J\t->\tE"));

		GeneralModel generalModel = new GeneralModel(testInteractions, mockLogger);
		generalModel.buildMultipleInteractions();
		generalModel.setModelName("test_model");
		this.booleanModel = new BooleanModel(generalModel, "biolqm_trapspaces", mockLogger);
	}

	@TempDir
	File tempDir;

	@Test
	void test_double_perturbed_model() throws Exception {
		// SET: {A,B}
		Drug[] drugSet = new Drug[2];
		drugSet[0] = new Drug("FirstDrug", mockLogger);
		String[] testTargets1 = {"E", "I"};
		drugSet[0].addTargets(testTargets1);
		drugSet[0].addEffect(true);

		drugSet[1] = new Drug("SecondDrug", mockLogger);
		String[] testTargets2 = {"C", "A"};
		drugSet[1].addTargets(testTargets2);
		drugSet[1].addEffect(false);

		Perturbation perturbation = new Perturbation(drugSet, mockLogger);
		PerturbationModel perturbationModel = new PerturbationModel(booleanModel, perturbation, mockLogger);

		assertEquals(perturbationModel.getModelName(), "test_model_FirstDrug_SecondDrug");
		assertEquals(perturbationModel.getPerturbation(), perturbation);
		assertEquals(perturbationModel.getAttractorTool(), "biolqm_trapspaces");

		// check that the equations of the perturbed nodes (drug targets) were changed
		assertThat(perturbationModel.getModelBoolNet())
			.hasSize(11)
			.contains("B, ( A ) & ! ( C )",
				"A, ( 0 )",
				"D, ! ( B )",
				"C, ( 0 )",
				"W, ( I ) & ! ( D )",
				"F, ( W )",
				"K, ( W )",
				"E, ( 1 )",
				"I, ( 1 )",
				"J, ( J )");

		assertThat(perturbationModel.getBooleanEquations())
			.hasSize(11)
			.extracting("target", "activatingRegulators", "inhibitoryRegulators", "link")
			.contains(tuple("B", newArrayList("A"), newArrayList("C"), "and"))
			.contains(tuple("A", newArrayList("false"), newArrayList(), ""))
			.contains(tuple("D", newArrayList(), newArrayList("B"), ""))
			.contains(tuple("C", newArrayList("false"), newArrayList(), ""))
			.contains(tuple("W", newArrayList("I"), newArrayList("D"), "and"))
			.contains(tuple("F", newArrayList("W"), newArrayList(), ""))
			.contains(tuple("K", newArrayList("W"), newArrayList(), ""))
			.contains(tuple("E", newArrayList("true"), newArrayList(), ""))
			.contains(tuple("I", newArrayList("true"), newArrayList(), ""))
			.contains(tuple("J", newArrayList("J"), newArrayList(), ""));

		// no attractors calculated yet
		assertFalse(perturbationModel.hasAttractors());
		assertFalse(perturbationModel.hasGlobalOutput());

		// now we will get something!
		perturbationModel.calculateAttractors(tempDir.getAbsolutePath());

		// modeloutput nodes are the 3 last: E (weight:-1), I (weight:-1), J (weight:1)
		assertEquals(perturbationModel.getAttractors(), newArrayList("00100000110", "00100000111"));
		assertEquals(perturbationModel.calculateGlobalOutput(), -1.5); // non-normalized globalOutput
		assertTrue(perturbationModel.hasAttractors());
		assertTrue(perturbationModel.hasGlobalOutput());
	}
}
