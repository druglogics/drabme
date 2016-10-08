package drabme;

import gitsbe.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import gitsbe.BooleanModel;

/*
 * Similar to the BooleanModel in Gitsbe, but adds output weights, and drugs with their computed effects.  
 * 
 */

public class ResponseModel { 
	
	private PerturbationPanel perturbationPanel ;
	
	// The original (unperturbed) model
	private BooleanModel originalModel ;
	
	private String modelName ;
	
	// Set of boolean models extended with perturbations
	private ArrayList<PerturbationModel> perturbationModels ;
	
	private ModelOutputs modelOutputs ;
	
//	public ResponseModel() {
//		
//	}
	
	public ResponseModel(BooleanModel booleanModel, ModelOutputs modelOutputs, PerturbationPanel perturbationPanel) 
	{
		this.originalModel = booleanModel  ;
		
		this.perturbationPanel = perturbationPanel ;
		
		this.modelOutputs = modelOutputs ;
		
		this.modelName = booleanModel.getModelName() + "_responsemodel" ;
		
	}
	
	public String getModelName ()
	{
		return this.modelName ;
	}
	
	public void initializeResponseModel ()
	{
		perturbationModels = new ArrayList<PerturbationModel> () ;
		
		// Define model for each perturbation set
		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(); i++)
		{
			perturbationModels.add (new PerturbationModel (originalModel, perturbationPanel.getPerturbations()[i], modelOutputs)) ;
		}
		
	}
	
	public void simulateResponses() throws IOException
	{
		for (int i = 0; i < perturbationModels.size(); i++)
		{
			Logger.output (2, "") ; // Just to add blank line per output in results file to structure results
			
			// Calculate stable state(s), then determine global output
			perturbationModels.get(i).calculateStableStatesVC("/home/asmund/Dokumenter/Cycret/Drabme/bnet/");
			perturbationModels.get(i).calculateGlobalOutput(); 
			
			// Store response for perturbation set
			if (perturbationModels.get(i).hasGlobalOutput())
			{
				Logger.output(2, "Adding predicted response for perturbation " + perturbationModels.get(i).getPerturbation().getName() + ": " + perturbationModels.get(i).getGlobalOutput()) ;
				perturbationModels.get(i).getPerturbation().addPrediction(perturbationModels.get(i).getGlobalOutput());
			}
//					perturbations.addPerturbationResponse(perturbationModels.get(i).getPerturbations(), perturbationModels.get(i).getGlobalOutput());
			
			
			// Check for synergies among drugs in combination (more than two drugs)
			if (perturbationModels.get(i).getPerturbation().getDrugs().length >= 2)
			{
				this.isCombinationSynergistic(perturbationModels.get(i).getPerturbation().getDrugs()) ;
//				this.isPairSynergistic(perturbationModels.get(i).getPerturbations()) ;
			}
		}
	}
	

	public boolean isCombinationSynergistic (Drug[] combination)
	{
		boolean value = false ;
		
		PerturbationModel combinationresponse = perturbationModels.get(getIndexOfPerturbationModel(combination)) ;
		Perturbation perturbation = combinationresponse.getPerturbation() ;
		
		Drug[][] subsets = DrugPanel.getCombinationSubsets(combination) ;
		
		boolean computable = true ;
		
		// Check if model for combination and all subsets have stable state(s)
		if (!combinationresponse.hasGlobalOutput())
			computable = false ;
		
		
		for (int i = 0; i < subsets.length; i++)
		{
			if (!perturbationModels.get(getIndexOfPerturbationModel(subsets[i])).hasGlobalOutput())
				computable = false ;
		}
		
		if (computable)
		{
			int minimumGlobalOutput = perturbationModels.get(getIndexOfPerturbationModel(subsets[0])).getGlobalOutput() ;
			
			for (int i = 0; i < subsets.length; i++)
				minimumGlobalOutput = min(minimumGlobalOutput, perturbationModels.get(getIndexOfPerturbationModel(subsets[i])).getGlobalOutput()) ;
			
			if (combinationresponse.getGlobalOutput() < minimumGlobalOutput)
			{
				perturbation.addSynergyPrediction();
				value = true ;
				String namecombo = "" ;
				for (int i = 0; i < combination.length; i++)
				{
					if (i > 0)
						namecombo += "-" ;
					
					namecombo += combination[i].getName() ;
				}
				
				Logger.output(2, namecombo + " is synergistic");
			}
			else
			{
				perturbation.addNonSynergyPrediction();
				value = false ;
				String namecombo = "" ;
				for (int i = 0; i < combination.length; i++)
				{
					if (i > 0)
						namecombo += "-" ;
					
					namecombo += combination[i].getName() ;
				}
				
				Logger.output(2, namecombo + " is NOT synergistic");
			}
		}
		else
		{
			String namecombo = "" ;
			for (int i = 0; i < combination.length; i++)
			{
				if (i > 0)
					namecombo += "-" ;
				
				namecombo += combination[i].getName() ;
			}
			
			Logger.output(2, namecombo + " cannot be evaluated for synergy (lacking stable state(s))");
		}
		
		
		return value ;
	}
	
//	public boolean isPairSynergistic (Drug[] combination) throws IOException
//	{
//		boolean value = false ;
//	
//		if (combination.length != 2)
//		{
//			Logger.output(1, "ERROR: ResponseModel.isPairSynergistic invoked without a proper pair referenced!") ;
//			return false ;
//		}
//		// BUG AAF
//		// THIS ALGORITHM ONLY (CURRENTLY) WORKS FOR PAIRS - NOT TRIPLETS ETC
//		PerturbationModel combinationresponse = perturbationModels.get(getIndexOfPerturbationModel(combination)) ;
//		PerturbationModel single1response = perturbationModels.get(getIndexOfPerturbationModel(new Drug[]{combination[0]})) ;
//		PerturbationModel single2response = perturbationModels.get(getIndexOfPerturbationModel(new Drug[]{combination[1]})) ;
//		
//		if (combinationresponse.hasGlobalOutput() && single1response.hasGlobalOutput() && single2response.hasGlobalOutput())
//		{
//			
//			if (combinationresponse.getGlobalOutput() < min(single1response.getGlobalOutput(), single2response.getGlobalOutput()))
//			{
//				Logger.output (2, combination[0].getName() + " + " + combination[1].getName() + " is synergistic");
//				perturbations.addSynergyObservation(combination);
//				value = true ;
//			}
//			else
//			{
//				Logger.output (2, combination[0].getName() + " + " + combination[1].getName() + " is NOT synergistic");
//				
//				perturbations.addNonSynergyObservation(combination);
//				value = false ;
//			}
//		}
//		
//		return value ;
//	}
	
	private int getIndexOfPerturbationModel(Drug[] drugs)
	{
		int hashA = DrugPanel.getDrugSetHash(drugs) ;
		
		// Compute has for drugs in parameter
		
		
		// Compare with drugs in each perturbationModel
		for (int i = 0; i < perturbationModels.size(); i++)
		{
			int hashB = DrugPanel.getDrugSetHash(perturbationModels.get(i).getPerturbation().getDrugs()) ;
			
			if (hashB == hashA)
			{
				return i ;
			}
		}
		
		return -1 ;
	}
	
//	public static boolean compareDrugSets(Drug[] arr1, Drug[] arr2) {
//	    HashSet<Drug> set1 = new HashSet<Drug>(Arrays.asList(arr1));
//	    HashSet<Drug> set2 = new HashSet<Drug>(Arrays.asList(arr2));
//	    return set1.equals(set2);
//	}
//	
//	public void identifySynergies ()
//	{
//		for (int i = 0; i < this.get; i++)
//		{
//			ArrayList <Drug[]> combination ;
//			ArrayList <Drug[]> fractions ;
//			
//			
//
//		}
//	}
	
	private int min(int a, int b)
	{
		if (a > b) return b;
		else return a ;
		
	}
	
//	public void predictSynergies ()
//	{
//		
//	}
//	
//	public void predictSensitivities ()
//	{
//		// Find response of unperturbed model
//		
//		
//		// Find response of perturbed models
//		for (int i = 0; i < perturbations.getPerturbations().size(); i++)
//		{
//			
//		}
//		
//	}
	
//	public void fixNode (String nodeName, boolean value)
//	{
//		booleanEquations.set(super.getIndexOfEquation(nodeName), " " + nodeName + " *= " + value + " ") ;
//	}
//	
//	public void fixNodes(String[] nodeNames, boolean[] values)
//	{
//		for (int i = 0; i < nodeNames.length; i++)
//		{
//			fixNode(nodeNames[i],values[i]) ;
//		}
//	}
	
//	public void testConditions (DrugPanel drugPanel) 
//	{
//		
//	}
	

}
