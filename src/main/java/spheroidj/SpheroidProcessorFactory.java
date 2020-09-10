package spheroidj;

public class SpheroidProcessorFactory {

	public static SpheroidProcessor createEsferoidProcessor(String name) {

		SpheroidProcessor spheroidProcessor = null;

		switch (name) {
		case "Fluorescence v1": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesFluo,
					DetectSpheroidMethods::detectSpheroidFluoSuspension);
			break;
		}
		case "Fluorescence v2": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesFluo,
					DetectSpheroidMethods::detectSpheroidFluoColageno);
			break;
		}
		case "Edges": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFiles,//searchFilesHectorNoFluo
					DetectSpheroidMethods::detectSpheroidHectorv1);
			break;
		}
		case "Hector no fluo v2": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesHectorNoFluo,//searchFilesHectorNoFluo,
					DetectSpheroidMethods::detectSpheroidHectorv2);
			break;
		}
		case "Threshold plus edges": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFiles,//searchFilesTeodora,
					DetectSpheroidMethods::detectSpheroidTeodora);
			break;
		}
		
		
		case "Threshold and edges": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFiles,//searchFilesTeodora,
					DetectSpheroidMethods::detectSpheroidTeodoraBig);
			break;
		}
		
		case "Teodora No Holes": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesJPG,//searchFilesTeodora,
					DetectSpheroidMethods::detectSpheroidTeodoraBigNoHoles);
			break;
		}
		
		case "Fluorescence v3": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesTeodora,
					DetectSpheroidMethods::detectSpheroidFluoStack);
			break;
		}
		
		case "Threshold": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFiles,//searchFilesJPG,
					DetectSpheroidMethods::detectSpheroidTeniposide);
			break;
		}
		
		case "HRNSeg": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFiles,
					DetectSpheroidMethods::detectSpheroidDeep);
			break;
		}

		}

		return spheroidProcessor;

	}

}
