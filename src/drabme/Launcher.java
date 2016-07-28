package drabme;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Launcher {

	public Launcher() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
		Calendar cal = Calendar.getInstance();
		String filesuffix = dateFormat.format(cal.getTime());
		
		
		int verbosity = 2 ;
		int combosize = 2 ;

		String filenameBooleanModels ;
		String dirProject ;
		String filenameOutput ;
		String filenameSummary ;
		
		
//		// 20160211 Final round submission
		String filenameResponseData = "20151013_response.tab" ;
		String filenameDrugs = "20151013_drugpanel.tab" ;
		String filenameModelOutputs = "20151002 Effector_nodes.tab" ;
		String filenameCombinations = "20160212_perturbations.tab" ;
		
		String[] dirProjectsArray = {
				"20151007_123001_20150918_DREAM10_model2_22RV1",
				"20151007_133333_20150918_DREAM10_model2_647-V",
				"20151007_144415_20150918_DREAM10_model2_A549",
				"20151007_154830_20150918_DREAM10_model2_BFTC-905",
				"20151007_165933_20150918_DREAM10_model2_BT-20",
				"20151007_181054_20150918_DREAM10_model2_BT-474",
				"20151007_192533_20150918_DREAM10_model2_BT-549",
				"20151007_204428_20150918_DREAM10_model2_C32",
				"20151007_220313_20150918_DREAM10_model2_CAL-51",
				"20151007_232046_20150918_DREAM10_model2_CAL-120",
				"20151008_003732_20150918_DREAM10_model2_CAL-148",
				"20151008_015335_20150918_DREAM10_model2_Calu-3",
				"20151008_030841_20150918_DREAM10_model2_Calu-6",
				"20151008_042334_20150918_DREAM10_model2_CAMA-1",
				"20151008_053854_20150918_DREAM10_model2_COLO-205",
				"20151008_065358_20150918_DREAM10_model2_DMS-114",
				"20151008_081038_20150918_DREAM10_model2_DU-4475",
				"20151008_092500_20150918_DREAM10_model2_EVSA-T",
				"20151008_115640_20150918_DREAM10_model2_HCC38",
				"20151008_131135_20150918_DREAM10_model2_HCC70",
				"20151008_142730_20150918_DREAM10_model2_HCC1143",
				"20151008_154435_20150918_DREAM10_model2_HCC1187",
				"20151008_165834_20150918_DREAM10_model2_HCC1395",
				"20151008_181620_20150918_DREAM10_model2_HCC1419",
				"20151008_193153_20150918_DREAM10_model2_HCC1428",
				"20151008_204833_20150918_DREAM10_model2_HCC1500",
				"20151008_220502_20150918_DREAM10_model2_HCC1569",
				"20151008_232110_20150918_DREAM10_model2_HCC1806",
				"20151009_003737_20150918_DREAM10_model2_HCC1937",
				"20151009_015403_20150918_DREAM10_model2_HCC1954",
				"20151009_031103_20150918_DREAM10_model2_HCT-116",
				"20151009_042736_20150918_DREAM10_model2_Hs-578-T",
				"20151009_054433_20150918_DREAM10_model2_HT-29",
				"20151009_072701_20150918_DREAM10_model2_HT-1197",
				"20151009_092821_20150918_DREAM10_model2_HT-1376",
				"20151009_115239_20150918_DREAM10_model2_J82",
				"20151009_131227_20150918_DREAM10_model2_KATOIII",
				"20151009_142650_20150918_DREAM10_model2_KMS-11",
				"20151009_154114_20150918_DREAM10_model2_KU-19-19",
				"20151009_165832_20150918_DREAM10_model2_LS-513",
				"20151009_181211_20150918_DREAM10_model2_M14",
				"20151009_202513_20150918_DREAM10_model2_MCF7",
				"20151009_214559_20150918_DREAM10_model2_MDA-MB-157",
				"20151010_004739_20150918_DREAM10_model2_MDA-MB-231",
				"20151010_025846_20150918_DREAM10_model2_MDA-MB-415",
				"20151010_080022_20150918_DREAM10_model2_MDA-MB-436",
				"20151010_091502_20150918_DREAM10_model2_MDA-MB-453",
				"20151010_102321_20150918_DREAM10_model2_MDA-MB-468",
				"20151010_113340_20150918_DREAM10_model2_MFM-223",
				"20151010_124350_20150918_DREAM10_model2_NCI-H23",
				"20151010_135635_20150918_DREAM10_model2_NCI-H226",
				"20151010_150744_20150918_DREAM10_model2_NCI-H358",
				"20151010_161841_20150918_DREAM10_model2_NCI-H520",
				"20151010_173153_20150918_DREAM10_model2_NCI-H522",
				"20151010_184733_20150918_DREAM10_model2_NCI-H747",
				"20151010_200231_20150918_DREAM10_model2_NCI-H838",
				"20151010_211547_20150918_DREAM10_model2_NCI-H1299",
				"20151010_222918_20150918_DREAM10_model2_NCI-H1563",
				"20151010_234544_20150918_DREAM10_model2_NCI-H1703",
				"20151011_010309_20150918_DREAM10_model2_NCI-H1793",
				"20151011_025221_20150918_DREAM10_model2_NCI-H1975",
				"20151011_074943_20150918_DREAM10_model2_NCI-H2085",
				"20151011_090604_20150918_DREAM10_model2_NCI-H2170",
				"20151011_101949_20150918_DREAM10_model2_NCI-H2228",
				"20151011_113552_20150918_DREAM10_model2_NCI-H2291",
				"20151011_125355_20150918_DREAM10_model2_NCI-H3122",
				"20151011_140516_20150918_DREAM10_model2_NCI-SNU-16",
				"20151011_152039_20150918_DREAM10_model2_RKO",
				"20151011_163712_20150918_DREAM10_model2_RT4",
				"20151011_175247_20150918_DREAM10_model2_SW48",
				"20151011_190634_20150918_DREAM10_model2_SW620",
				"20151011_202129_20150918_DREAM10_model2_SW780",
				"20151011_213333_20150918_DREAM10_model2_SW837",
				"20151011_224624_20150918_DREAM10_model2_SW900",
				"20151012_000242_20150918_DREAM10_model2_SW948",
				"20151012_012025_20150918_DREAM10_model2_T47D",
				"20151012_035427_20150918_DREAM10_model2_T-24",
				"20151012_065816_20150918_DREAM10_model2_TCCSUP",
				"20151012_081324_20150918_DREAM10_model2_UACC-812",
				"20151012_093006_20150918_DREAM10_model2_UM-UC-3",
				"20151012_104708_20150918_DREAM10_model2_VCaP",
				"20151012_120342_20150918_DREAM10_model2_VM-CUB-1",
				"20151012_143543_20150918_DREAM10_model2_MDA-MB-175-VII",
				"20151012_155027_20150918_DREAM10_model2_MDA-MB-361",
				"20151012_170641_20150918_DREAM10_model2_NCI-H1437"
		} ;
		
		String[] filenameBooleanModelsArray = {
				"20150918_DREAM10_model2_22RV1_models.txt",
				"20150918_DREAM10_model2_647-V_models.txt",
				"20150918_DREAM10_model2_A549_models.txt",
				"20150918_DREAM10_model2_BFTC-905_models.txt",
				"20150918_DREAM10_model2_BT-20_models.txt",
				"20150918_DREAM10_model2_BT-474_models.txt",
				"20150918_DREAM10_model2_BT-549_models.txt",
				"20150918_DREAM10_model2_C32_models.txt",
				"20150918_DREAM10_model2_CAL-51_models.txt",
				"20150918_DREAM10_model2_CAL-120_models.txt",
				"20150918_DREAM10_model2_CAL-148_models.txt",
				"20150918_DREAM10_model2_Calu-3_models.txt",
				"20150918_DREAM10_model2_Calu-6_models.txt",
				"20150918_DREAM10_model2_CAMA-1_models.txt",
				"20150918_DREAM10_model2_COLO-205_models.txt",
				"20150918_DREAM10_model2_DMS-114_models.txt",
				"20150918_DREAM10_model2_DU-4475_models.txt",
				"20150918_DREAM10_model2_EVSA-T_models.txt",
				"20150918_DREAM10_model2_HCC38_models.txt",
				"20150918_DREAM10_model2_HCC70_models.txt",
				"20150918_DREAM10_model2_HCC1143_models.txt",
				"20150918_DREAM10_model2_HCC1187_models.txt",
				"20150918_DREAM10_model2_HCC1395_models.txt",
				"20150918_DREAM10_model2_HCC1419_models.txt",
				"20150918_DREAM10_model2_HCC1428_models.txt",
				"20150918_DREAM10_model2_HCC1500_models.txt",
				"20150918_DREAM10_model2_HCC1569_models.txt",
				"20150918_DREAM10_model2_HCC1806_models.txt",
				"20150918_DREAM10_model2_HCC1937_models.txt",
				"20150918_DREAM10_model2_HCC1954_models.txt",
				"20150918_DREAM10_model2_HCT-116_models.txt",
				"20150918_DREAM10_model2_Hs-578-T_models.txt",
				"20150918_DREAM10_model2_HT-29_models.txt",
				"20150918_DREAM10_model2_HT-1197_models.txt",
				"20150918_DREAM10_model2_HT-1376_models.txt",
				"20150918_DREAM10_model2_J82_models.txt",
				"20150918_DREAM10_model2_KATOIII_models.txt",
				"20150918_DREAM10_model2_KMS-11_models.txt",
				"20150918_DREAM10_model2_KU-19-19_models.txt",
				"20150918_DREAM10_model2_LS-513_models.txt",
				"20150918_DREAM10_model2_M14_models.txt",
				"20150918_DREAM10_model2_MCF7_models.txt",
				"20150918_DREAM10_model2_MDA-MB-157_models.txt",
				"20150918_DREAM10_model2_MDA-MB-231_models.txt",
				"20150918_DREAM10_model2_MDA-MB-415_models.txt",
				"20150918_DREAM10_model2_MDA-MB-436_models.txt",
				"20150918_DREAM10_model2_MDA-MB-453_models.txt",
				"20150918_DREAM10_model2_MDA-MB-468_models.txt",
				"20150918_DREAM10_model2_MFM-223_models.txt",
				"20150918_DREAM10_model2_NCI-H23_models.txt",
				"20150918_DREAM10_model2_NCI-H226_models.txt",
				"20150918_DREAM10_model2_NCI-H358_models.txt",
				"20150918_DREAM10_model2_NCI-H520_models.txt",
				"20150918_DREAM10_model2_NCI-H522_models.txt",
				"20150918_DREAM10_model2_NCI-H747_models.txt",
				"20150918_DREAM10_model2_NCI-H838_models.txt",
				"20150918_DREAM10_model2_NCI-H1299_models.txt",
				"20150918_DREAM10_model2_NCI-H1563_models.txt",
				"20150918_DREAM10_model2_NCI-H1703_models.txt",
				"20150918_DREAM10_model2_NCI-H1793_models.txt",
				"20150918_DREAM10_model2_NCI-H1975_models.txt",
				"20150918_DREAM10_model2_NCI-H2085_models.txt",
				"20150918_DREAM10_model2_NCI-H2170_models.txt",
				"20150918_DREAM10_model2_NCI-H2228_models.txt",
				"20150918_DREAM10_model2_NCI-H2291_models.txt",
				"20150918_DREAM10_model2_NCI-H3122_models.txt",
				"20150918_DREAM10_model2_NCI-SNU-16_models.txt",
				"20150918_DREAM10_model2_RKO_models.txt",
				"20150918_DREAM10_model2_RT4_models.txt",
				"20150918_DREAM10_model2_SW48_models.txt",
				"20150918_DREAM10_model2_SW620_models.txt",
				"20150918_DREAM10_model2_SW780_models.txt",
				"20150918_DREAM10_model2_SW837_models.txt",
				"20150918_DREAM10_model2_SW900_models.txt",
				"20150918_DREAM10_model2_SW948_models.txt",
				"20150918_DREAM10_model2_T47D_models.txt",
				"20150918_DREAM10_model2_T-24_models.txt",
				"20150918_DREAM10_model2_TCCSUP_models.txt",
				"20150918_DREAM10_model2_UACC-812_models.txt",
				"20150918_DREAM10_model2_UM-UC-3_models.txt",
				"20150918_DREAM10_model2_VCaP_models.txt",
				"20150918_DREAM10_model2_VM-CUB-1_models.txt",
				"20150918_DREAM10_model2_MDA-MB-175-VII_models.txt",
				"20150918_DREAM10_model2_MDA-MB-361_models.txt",
				"20150918_DREAM10_model2_NCI-H1437_models.txt"
		} ;
		
		// TEMPLATE
//		String filenameResponseData = "" ;
//		String filenameDrugs = "" ;
//		String filenameModelOutputs = "" ;
//		String filenameCombinations = "" ;
//		
//		String[] dirProjectsArray = {
//				
//		} ;
//		
//		String[] filenameBooleanModelsArray = {
//				
//		} ;
		
		Thread t ;
		
		for (int i = 0; i < dirProjectsArray.length; i++)
		{
			dirProject = dirProjectsArray[i] + "/" ;
			filenameBooleanModels = dirProject + filenameBooleanModelsArray[i] ;
			filenameOutput = filenameBooleanModels + "_output.txt";
			filenameSummary = filenameBooleanModels + "_summary.txt";
			
			t = new Thread(new Drabme(verbosity, filenameBooleanModels, filenameDrugs, filenameCombinations, filenameResponseData, filenameModelOutputs, filenameOutput, filenameSummary, combosize)) ;
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		Thread t = new Thread(new Drabme(verbosity, filenameBooleanModels, filenameDrugs, filenameCombinations, filenameResponseData, filenameModelOutputs, filenameOutput, filenameSummary, combosize)) ;
		
//		t.start();
		
		
	
		
	}

}
