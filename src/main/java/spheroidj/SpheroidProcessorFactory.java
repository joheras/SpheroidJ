package spheroidj;

public class SpheroidProcessorFactory {

	public static SpheroidProcessor createEsferoidProcessor(String name) {

		SpheroidProcessor spheroidProcessor = null;

		switch (name) {
		case "Fluorescence": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesFluo,
					DetectSpheroidMethods::detectSpheroidFluoSuspension);
			break;
		}
		case "colageno": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesFluo,
					DetectSpheroidMethods::detectSpheroidFluoColageno);
			break;
		}
		case "Edges": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesHectorNoFluo,//searchFilesHectorNoFluo
					DetectSpheroidMethods::detectSpheroidHectorv1);
			break;
		}
		case "Hector no fluo v2": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesHectorNoFluo,//searchFilesHectorNoFluo,
					DetectSpheroidMethods::detectSpheroidHectorv2);
			break;
		}
		case "Threshold plus edges": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesTeodora,//searchFilesTeodora,
					DetectSpheroidMethods::detectSpheroidTeodora);
			break;
		}
		
		
		case "Threshold and edges": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesTeodora,//searchFilesTeodora,
					DetectSpheroidMethods::detectSpheroidTeodoraBig);
			break;
		}
		
		case "Teodora No Holes": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesJPG,//searchFilesTeodora,
					DetectSpheroidMethods::detectSpheroidTeodoraBigNoHoles);
			break;
		}
		
		case "Hector fluo stack": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesTeodora,
					DetectSpheroidMethods::detectSpheroidFluoStack);
			break;
		}
		
		case "Threshold": {
			spheroidProcessor = new SpheroidProcessor(SearchFilesMethods::searchFilesJPG,//searchFilesJPG,
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
