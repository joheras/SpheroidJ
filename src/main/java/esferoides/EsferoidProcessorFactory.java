package esferoides;

public class EsferoidProcessorFactory {

	public static EsferoidProcessor createEsferoidProcessor(String name) {

		EsferoidProcessor esferoidProcessor = null;

		switch (name) {
		case "Fluorescence": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesFluo,
					DetectEsferoidMethods::detectEsferoideFluoSuspension);
			break;
		}
		case "colageno": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesFluo,
					DetectEsferoidMethods::detectEsferoideFluoColageno);
			break;
		}
		case "Edges": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesHectorNoFluo,//searchFilesHectorNoFluo
					DetectEsferoidMethods::detectEsferoideHectorv1);
			break;
		}
		case "Hector no fluo v2": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesHectorNoFluo,//searchFilesHectorNoFluo,
					DetectEsferoidMethods::detectEsferoideHectorv2);
			break;
		}
		case "Threshold plus edges": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesTeodora,//searchFilesTeodora,
					DetectEsferoidMethods::detectEsferoideTeodora);
			break;
		}
		
		
		case "Threshold and edges": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesTeodora,//searchFilesTeodora,
					DetectEsferoidMethods::detectEsferoideTeodoraBig);
			break;
		}
		
		case "Teodora No Holes": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesJPG,//searchFilesTeodora,
					DetectEsferoidMethods::detectEsferoideTeodoraBigNoHoles);
			break;
		}
		
		case "Hector fluo stack": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesTeodora,
					DetectEsferoidMethods::detectEsferoideFluoStack);
			break;
		}
		
		case "Threshold": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesJPG,//searchFilesJPG,
					DetectEsferoidMethods::detectEsferoideTeniposide);
			break;
		}

		}

		return esferoidProcessor;

	}

}
