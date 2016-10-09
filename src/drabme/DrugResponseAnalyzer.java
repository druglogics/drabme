package drabme;

import gitsbe.Logger;
import gitsbe.BooleanModel;

import java.io.IOException;
import java.util.ArrayList;

public class DrugResponseAnalyzer {

	private PerturbationPanel perturbationPanel;
	private final ArrayList<BooleanModel> booleanModels;
	private ModelOutputs modelOutputs;

	public DrugResponseAnalyzer(PerturbationPanel perturbationPanel,
			ArrayList<BooleanModel> booleanModels, ModelOutputs modelOutputs) {
		// TODO Auto-generated constructor stub

		this.perturbationPanel = perturbationPanel;
		this.booleanModels = booleanModels;
		this.modelOutputs = modelOutputs;

	}

	public void analyze() throws IOException {

		ArrayList<ResponseModel> responseModels = new ArrayList<ResponseModel>();

		// Perform all simulations of given models with given drug combinations
		for (int modelIndex = 0; modelIndex < booleanModels.size(); modelIndex++) {
			Logger.output(2, "Adding model "
					+ booleanModels.get(modelIndex).getModelName());

			ResponseModel rm = new ResponseModel(booleanModels.get(modelIndex),
					modelOutputs, perturbationPanel);

			for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {

				String msg = "";

				for (int j = 0; j < perturbationPanel.getPerturbations()[i]
						.getDrugs().length; j++) {

					if (Logger.getVerbosity() >= 2) {
						msg += perturbationPanel.getPerturbations()[i]
								.getDrugs()[j].getName() + " ";
					}

				}
				Logger.output(2, "Added simulation with drug(s): " + msg
						+ "to model: " + rm.getModelName());
			}

			rm.initializeResponseModel();
			rm.simulateResponses();

			responseModels.add(rm);
		}
	}
}
