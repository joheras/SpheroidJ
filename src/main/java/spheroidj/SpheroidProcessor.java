package spheroidj;

import java.util.ArrayList;
import java.util.List;

import loci.plugins.in.ImporterOptions;

public class SpheroidProcessor {
	
	private searchFilesFunction searchFiles;
	private detectSpheroidFunction detectEsferoid;
	
	public SpheroidProcessor(searchFilesFunction searchFiles, detectSpheroidFunction detectSpheroid) {
		super();
		this.searchFiles = searchFiles;
		this.detectEsferoid = detectSpheroid;
	}

	public searchFilesFunction getSearchFiles() {
		return searchFiles;
	}

	public detectSpheroidFunction getDetectEsferoid() {
		return detectEsferoid;
	}


}

@FunctionalInterface
interface searchFilesFunction {
    public List<String>  apply();
}

@FunctionalInterface
interface detectSpheroidFunction {
    public void apply(ImporterOptions options, String dir, String name,ArrayList<Integer> goodRows);
}