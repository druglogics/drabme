package drabme;

import gitsbe.Logger;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class StatisticalAnalysis {

	PerturbationPanel perturbationPanel ;
	
	public StatisticalAnalysis(PerturbationPanel perturbationPanel) {
		this.perturbationPanel = perturbationPanel ;
		
		for (int i = 0; i < perturbationPanel.getPerturbations().length; i++)
		{
			perturbationPanel.getPerturbations()[i].calculateStatistics(); 
		}
	}
	
	public double getSpearmansCorrelation (int combinationsize)
	{
		return new SpearmansCorrelation().correlation(perturbationPanel.getObservedCombinationResponses(combinationsize), perturbationPanel.getPredictedAverageCombinationResponses(combinationsize)) ;
	}
	
//	public double getSpearmansCorrelationSignificance (int combinationsize)
//	{
//		
//	}

	public double getPearsonCorrelation (int combinationsize)
	{
		return new PearsonsCorrelation().correlation(perturbationPanel.getObservedCombinationResponses(combinationsize), perturbationPanel.getPredictedAverageCombinationResponses(combinationsize)) ;
	}
	
	public double getKendallsCorrelation (int combinationsize)
	{
		return new KendallsCorrelation().correlation(perturbationPanel.getObservedCombinationResponses(combinationsize), perturbationPanel.getPredictedAverageCombinationResponses(combinationsize)) ;
	}
	
//	public double getSensitivity (int combinationsize)
//	{
//		
//	}
//	
//	public double getSpecificity (int combinationsize)
//	{
//		
//	}
//	
//	public double getPositivePredictiveValue (int combinationsize)
//	{
//		
//	}
//	
//	public double getNegativePredictiveValue (int combinationsize)
//	{
//		
//	}
	
	public double getSynergyClassificationAccuracy (int combinationsize)
	{
		int TP = 0 ;
		int FP = 0 ;
		int TN = 0 ;
		int FN = 0 ;
		
		for (int i = 0; i < perturbationPanel.getNumberOfPerturbations(combinationsize); i++)
		{
			double predicted = perturbationPanel.getPerturbations(combinationsize)[i].getAveragePredictedResponse() ;
			double observed = perturbationPanel.getPerturbations(combinationsize)[i].getObservedResponse() ;
			
			if (predicted < 0 && observed < 1)
				TP++ ;
			else if (predicted >= 0 && observed >= 1)
				TN++ ;
			else if (predicted < 0 && observed >= 1)
				FP++ ;
			else if (predicted >= 0 && observed < 1)
				FN++ ;
		}
		
		int observations = TP + FP + TN + FN ;
		
		double result = ((double) TP + (double) TN) / ((double) observations) ;
		
		return result ;
	}
	

	/**
	 * http://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing-integers-to-primitive-int-array
	 * 
	 * @param integers
	 * @return
	 */
	public static int[] convertIntegers(ArrayList<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    Iterator<Integer> iterator = integers.iterator();
	    for (int i = 0; i < ret.length; i++)
	    {
	        ret[i] = iterator.next().intValue();
	    }
	    return ret;
	}
	
	public static double[] convertDoubles(Double[] doubles)
	{
	    double[] ret = new double[doubles.length];
	    for (int i = 0; i < ret.length; i++)
	    {
	        ret[i] = doubles[i].doubleValue() ; 
	    }
	    return ret;
	}
}
