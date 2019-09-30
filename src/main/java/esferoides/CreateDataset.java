package esferoides;

import java.io.File;
import java.io.IOException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;
import ij.plugin.frame.RoiManager;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

//@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Esferoids>CreateDataset")
public class CreateDataset implements Command {

	
	public void saveImageAndMask(ImporterOptions options, String dir, String name) throws FormatException, IOException {
		System.out.println(name);
		options.setId(name);

		ImagePlus[] imps = BF.openImagePlus(options);
		ImagePlus imp = imps[0];
		String name1 = name.substring(name.lastIndexOf("/")+1, name.indexOf("."));
		IJ.run(imp, "RGB Color", "");
		IJ.saveAs(imp, "Tiff", dir + "Images/"+ name1 + ".tiff");
		RoiManager rm = RoiManager.getInstance();
		rm.runCommand("Open",dir + name1 + ".roi");
		rm.setVisible(false);
		rm.addRoi(imp.getRoi());
		imp.show();
		ImagePlus impN = IJ.createImage("Untitled", "16-bit white", imp.getWidth(), imp.getHeight(), 1);System.out.println("1");
		rm.select(0);
		rm.runCommand(impN, "Fill");
		rm.runCommand("Delete");
		IJ.setAutoThreshold(impN, "Default");
		IJ.run(impN, "Convert to Mask", "");
		IJ.saveAs(impN, "Tiff", dir + "Labels/"+ name1 + ".tiff");
		
		rm.runCommand("Select All");
		rm.runCommand("Delete");
		imp.close();
		impN.close();
		
	}
	
	@Override
	public void run() {
		
		ImporterOptions options;
		try {
			options = new ImporterOptions();

			options.setWindowless(true);

			// We ask the user for a directory with nd2 images.
			DirectoryChooser dc = new DirectoryChooser("Select the folder containing the nd2 images");
			String dir = dc.getDirectory();

			// We store the list of nd2 files in the result list.
			File folder = new File(dir);
			List<String> result = new ArrayList<String>();

			Utils.search(".*\\.nd2", folder, result);
			Collections.sort(result);
			RoiManager rm = RoiManager.getRoiManager();
			
		


			for (String name : result) {
				saveImageAndMask(options, dir, name);

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		
	}

}
