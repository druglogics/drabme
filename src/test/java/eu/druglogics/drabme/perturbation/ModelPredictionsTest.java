package eu.druglogics.drabme.perturbation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelPredictionsTest {

	@Test
	void test_methods() {
		ModelPredictions modelPredictions = new ModelPredictions("aRandomModel");
		assertEquals(modelPredictions.getModelName(), "aRandomModel");
		assertEquals(modelPredictions.getPredictions(), "");

		modelPredictions.addNAPrediction("S-D");
		modelPredictions.addNAPrediction("A-B");
		modelPredictions.addSynergyPrediction("E-R");
		modelPredictions.addSynergyPrediction("R-T");
		modelPredictions.addNonSynergyPrediction("T-S");

		assertEquals(modelPredictions.getPredictions(),
			"Drugs: A-B  Prediction: NA\n" + "Drugs: R-T  Prediction: 1\n"
			+ "Drugs: S-D  Prediction: NA\n" + "Drugs: E-R  Prediction: 1\n"
			+ "Drugs: T-S  Prediction: 0\n");

		assertEquals(modelPredictions.getModelPredictionsVerbose(newArrayList("A-B")), "aRandomModel\tNA");
		assertEquals(modelPredictions.getModelPredictionsVerbose(newArrayList("A-B","T-S","S-D","R-T","E-R")),
			"aRandomModel\tNA\t0\tNA\t1\t1");
	}
}
