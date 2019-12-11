package esferoides;

public class EsferoidProcessorFactory {

	public static EsferoidProcessor createEsferoidProcessor(String name) {

		EsferoidProcessor esferoidProcessor = null;

		switch (name) {
		case "suspension": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesFluo,
					DetectEsferoidMethods::detectEsferoideFluoSuspension);
			break;
		}
		case "colageno": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesFluo,
					DetectEsferoidMethods::detectEsferoideFluoColageno);
			break;
		}
		case "Hector no fluo v1": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesHectorNoFluo,
					DetectEsferoidMethods::detectEsferoideHectorv1);
			break;
		}
		case "Hector no fluo v2": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesHectorNoFluo,
					DetectEsferoidMethods::detectEsferoideHectorv2);
			break;
		}
		case "Teodora v1": {
			esferoidProcessor = new EsferoidProcessor(SearchFilesMethods::searchFilesTeodora,
					DetectEsferoidMethods::detectEsferoideTeodora);
			break;
		}

		}

		return esferoidProcessor;

	}

}
