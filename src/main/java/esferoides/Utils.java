package esferoides;

import java.io.File;
import java.util.List;

public class Utils {


	// Method to search the list of files that satisfies a pattern in a folder. The
	// list of files
	// is stored in the result list.
	public static void search(final String pattern, final File folder, List<String> result) {
		for (final File f : folder.listFiles()) {

			if (f.isDirectory()) {
				search(pattern, f, result);
			}

			if (f.isFile()) {
				if (f.getName().matches(pattern)) {
					result.add(f.getAbsolutePath());
				}
			}

		}
	}
	
}
