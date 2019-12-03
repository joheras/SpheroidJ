package esferoides;

import java.util.ArrayList;
import java.util.List;

import loci.plugins.in.ImporterOptions;

public class EsferoidProcessor {
	
	private searchFilesFunction searchFiles;
	private detectEsferoidFunction detectEsferoid;
	
	public EsferoidProcessor(searchFilesFunction searchFiles, detectEsferoidFunction detectEsferoid) {
		super();
		this.searchFiles = searchFiles;
		this.detectEsferoid = detectEsferoid;
	}

	public searchFilesFunction getSearchFiles() {
		return searchFiles;
	}

	public detectEsferoidFunction getDetectEsferoid() {
		return detectEsferoid;
	}


}

@FunctionalInterface
interface searchFilesFunction {
    public List<String>  apply();
}

@FunctionalInterface
interface detectEsferoidFunction {
    public void apply(ImporterOptions options, String dir, String name,ArrayList<Integer> goodRows);
}