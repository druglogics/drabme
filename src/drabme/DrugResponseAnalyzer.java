package drabme;

import gitsbe.Logger;
import gitsbe.BooleanModel;

import java.io.IOException;
import java.util.ArrayList;

public class DrugResponseAnalyzer {

	private PerturbationPanel perturbationPanel;
	private final ArrayList<BooleanModel> booleanModels;
	private ModelOutputs modelOutputs;

	private Logger logger ;
	
	private String directoryTmp ;
	
	public DrugResponseAnalyzer(PerturbationPanel perturbationPanel,
								ArrayList<BooleanModel> booleanModels, 
								ModelOutputs modelOutputs, 
								String directoryTmp, 
								Logger logger) {
		this.logger = logger ;
		this.perturbationPanel = perturbationPanel;
		this.booleanModels = booleanModels;
		this.modelOutputs = modelOutputs;
		this.directoryTmp = directoryTmp ;

	}

	public void analyze() throws IOException {

		ArrayList<ResponseModel> responseModels = new ArrayList<ResponseModel>();

		// Perform all simulations of given models with given drug combinations
		for (int modelIndex = 0; modelIndex < booleanModels.size(); modelIndex++) {
			logger.output(2, "Adding model "
					+ booleanModels.get(modelIndex).getModelName());

			ResponseModel rm = new ResponseModel(booleanModels.get(modelIndex),
					modelOutputs, perturbationPanel, logger);

			for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++) {

				String msg = "";

				for (int j = 0; j < perturbationPanel.getPerturbations()[i]
						.getDrugs().length; j++) {

					if (logger.getVerbosity() >= 2) {
						msg += perturbationPanel.getPerturbations()[i]
								.getDrugs()[j].getName() + " ";
					}

				}
				logger.output(2, "Added simulation with drug(s): " + msg
						+ "to model: " + rm.getModelName());
			}

			rm.initializeResponseModel();
			rm.simulateResponses(directoryTmp);

			responseModels.add(rm);
		}
	}
}
