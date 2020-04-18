package eu.druglogics.drabme.perturbation;

import eu.druglogics.drabme.drug.Drug;
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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class PerturbationTest {

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

	@Test
	void test_double_perturbation() {
		Logger mockLogger = mock(Logger.class);

		// SET: {A,B}
		Drug[] drugSet = new Drug[2];
		drugSet[0] = new Drug("A", mockLogger);
		String[] testTargets = {"A1","A2"};
		drugSet[0].addTargets(testTargets);
		drugSet[0].addEffect(false);

		drugSet[1] = new Drug("B", mockLogger); // no targets added for B!

		Perturbation perturbation = new Perturbation(drugSet, mockLogger);

		doAnswer(invocation -> {
			Integer verbosity = invocation.getArgument(0);
			String message = invocation.getArgument(1);

			if (message.contains("which has no targets to perturbations")) {
				assertEquals(2, verbosity);
				assertEquals("Added drug `B` which has no targets to perturbations", message);
			}
			return null;
		}).when(mockLogger).outputStringMessage(isA(Integer.class), isA(String.class));

		assertEquals(perturbation.getDrugsVerbose(),"A B");
		assertEquals(perturbation.getDrugs(), drugSet);
		assertEquals(perturbation.getName(), "[A]-[B]");
		assertEquals(perturbation.getPerturbationHash(), 4260);

		// No predictions are added
		perturbation.calculateStatistics();

		assertEquals(perturbation.getSynergyPredictions(), 0);
		assertEquals(perturbation.getNonSynergyPredictions(), 0);
		assertEquals(perturbation.getPredictions().length, 0);
		assertEquals(perturbation.getAveragePredictedResponse(), 0);
		assertEquals(perturbation.getStandardDeviationPredictedResponse(), 0);

		// Add some predicted responses and their synergy assessment
		perturbation.addPrediction(0);
		perturbation.addSynergyPrediction();
		perturbation.addPrediction((float) -0.1);
		perturbation.addSynergyPrediction();
		perturbation.addPrediction((float) -1.1);
		perturbation.addSynergyPrediction();

		perturbation.addPrediction((float) 0.3);
		perturbation.addNonSynergyPrediction();
		perturbation.addPrediction((float) 1.5);
		perturbation.addNonSynergyPrediction();

		perturbation.calculateStatistics();

		assertEquals(perturbation.getSynergyPredictions(), 3);
		assertEquals(perturbation.getNonSynergyPredictions(), 2);
		assertArrayEquals(perturbation.getPredictions(),
			new float[]{0, (float) -0.1,(float) -1.1, (float) 0.3, (float) 1.5});

		DecimalFormat df = new DecimalFormat("#.00000");
		assertEquals(Double.valueOf(df.format(perturbation.getAveragePredictedResponse())), 0.12);
		assertEquals(Double.valueOf(df.format(perturbation.getStandardDeviationPredictedResponse())), 0.93381);

		// test_modeloutputs_2: minGl: -2, maxGL: 2
		assertEquals(Double.valueOf(df.format(perturbation.getNormalizedAveragePredictedResponse())), 0.53);
	}
}
