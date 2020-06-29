package esferoides;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import loci.plugins.in.ImporterOptions;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>SpheroidJ>Experiment")
public class Experiment implements Command {

	@Parameter(label = "Iterations find edges", min = "0", max = "5")
	private int findedgesIters = 0;

	@Parameter(label = "Threshold algorithm", choices = { "Default", "Otsu" })
	private String type = "Default";

	@Parameter(label = "Iterations for dilation and erosion", min = "0", max = "5")
	private int erodeDilationIters = 0;

	@Parameter(label = "Apply fill holes")
	private boolean fillholes = false;

	@Parameter(label = "Apply watershed")
	private boolean watershed = false;

	@Parameter(label = "Process", choices = { "Single image", "Folder" })
	private String process = "Single image";

	@Override
	public void run() {

		if (process.equals("Single image")) {

			ImagePlus imp = IJ.getImage();
			if (imp != null) {
				ImagePlus imp2 = imp.duplicate();

				processImage(imp, findedgesIters, type, erodeDilationIters, fillholes, watershed);
				imp2.show();
				RoiManager rm = RoiManager.getInstance();
				if (rm != null) {
					rm.run("Show All");
				}
			}
		} else {
			List<String> result = new ArrayList<String>();
			ResultsTable rt = new ResultsTable();
			ImporterOptions options;
			try {
				options = new ImporterOptions();

				options.setWindowless(true);
				// We ask the user for a directory with nd2 images.
				DirectoryChooser dc = new DirectoryChooser("Select the folder containing the tif images");
				String dir = dc.getDirectory();

				// We store the list of tiff files in the result list.
				File folder = new File(dir);

				Utils.search(".*\\.tif", folder, result);
				Utils.search(".*\\.tiff", folder, result);
				Utils.search(".*\\.JPG", folder, result);
				Utils.search(".*\\.jpg", folder, result);

				Collections.sort(result);
				result.add(0, dir);

				dir = result.get(0);
				result.remove(0);

				// ProgressBar

				IJ.setForegroundColor(255, 0, 0);
				ArrayList<Integer> goodRows = new ArrayList<>();
				JFrame frame = new JFrame("Work in progress");
				JProgressBar progressBar = new JProgressBar();
				progressBar.setValue(0);
				progressBar.setMaximum(result.size());
				progressBar.setString("");
				progressBar.setStringPainted(true);
				progressBar.setIndeterminate(false);
				Border border = BorderFactory.createTitledBorder("Processing...");
				progressBar.setBorder(border);
				Container content = frame.getContentPane();
				content.add(progressBar, BorderLayout.NORTH);
				frame.setSize(300, 100);
				frame.setVisible(true);

				// For each file in the folder we detect the esferoid on it.
				for (String name : result) {
					progressBar.setValue(progressBar.getValue()+1);
					System.out.println(name);
					ImagePlus imp = IJ.openImage(name);
					ImagePlus imp2 = imp.duplicate();
					processImage(imp, findedgesIters, type, erodeDilationIters, fillholes, watershed);
					RoiManager rm = RoiManager.getInstance();
					Utils.showResultsAndSave(dir, name, imp2, rm, goodRows);
					imp.close();
					imp2.close();
					imp = IJ.getImage();
					imp.close();
				}
				
				rt = ResultsTable.getResultsTable();
				
				/// Remove empty rows
				int rows = rt.getCounter();
				for (int i = rows; i > 0; i--) {
					if (!(goodRows.contains(i - 1))) {
						rt.deleteRow(i - 1);
					}
				}
				ExportToExcel ete = new ExportToExcel(rt, dir);
				ete.convertToExcel();

				rt.reset();
				frame.setVisible(false);
				frame.dispose();
				IJ.showMessage("Process finished");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private static void processImage(ImagePlus imp, int findedgesIters, String type, int erodeDilationIters,
			boolean fillholes, boolean watershed) {

		IJ.run(imp, "8-bit", "");
		// Find edges
		for (int i = 0; i < findedgesIters; i++) {
			IJ.run(imp, "Find Edges", "");
		}

		// Threshold
		if (findedgesIters > 0) {
			IJ.setAutoThreshold(imp, type + " dark");
		} else {
			IJ.setAutoThreshold(imp, type);
		}
		IJ.run(imp, "Convert to Mask", "");

		// Dilate
		for (int i = 0; i < erodeDilationIters; i++) {
			IJ.run(imp, "Dilate", "");
		}

		// Fill holes
		if (fillholes) {
			IJ.run(imp, "Fill Holes", "");
		}

		// Erode
		for (int i = 0; i < erodeDilationIters; i++) {
			IJ.run(imp, "Erode", "");
		}

		if (watershed) {
			IJ.run(imp, "Watershed", "");
		}
		IJ.run(imp, "Analyze Particles...", "size=0-Infinity circularity=0.15-1.00 show=Outlines exclude add");
		RoiManager rm = RoiManager.getInstance();

		if (rm != null) {
			Utils.keepBiggestROI(rm);
		}
		imp.changes = false;
		imp.close();
	}

}
