package esferoides;

import ij.IJ;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.io.DirectoryChooser;
import ij.io.FileInfo;
import ij.measure.ResultsTable;
import ij.plugin.Thresholder;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.command.Command;
import org.scijava.command.Previewable;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>EsferoideJ")
public class EsferoideJ_ implements Command {

//	@Parameter
//	private ImagePlus imp;

	// Method to count the number of pixels whose value is below a threshold.
	private int countBelowThreshold(ImagePlus imp1, int threshold) {

		ImageProcessor ip = imp1.getProcessor();
		int[] histogram = ip.getHistogram();

		int countpixels = 0;
		for (int i = 0; i < threshold; i++) {
			countpixels = countpixels + histogram[i];
		}

		return countpixels;

	}

	// Method to draw the results stored in the roi manager into the image, and then
	// save the
	// image in a given directory. Since we know that there is only one esferoide
	// per image, we
	// only keep the ROI with the biggest area stored in the ROI Manager.
	private static void showResultsAndSave(String dir, ImagePlus imp1, RoiManager rm) {
		IJ.run(imp1, "RGB Color", "");
		if (rm != null) {
			rm.setVisible(false);
			keepBiggestROI(rm);
			rm.runCommand("Show None");
			rm.runCommand("Show All");

			rm.runCommand(imp1, "Draw");
			// imp.close();
			rm.close();
		}
		String name = imp1.getTitle();
		FileInfo f = imp1.getFileInfo();
		name = name.substring(0, name.indexOf("."));
		IJ.saveAs(imp1, "Tiff", dir + name + "_pred.tiff");

	}

	// Method to obtain the area from a polygon. Probably, there is a most direct
	// method to do this.
	private static final double getArea(Polygon p) {
		if (p == null)
			return Double.NaN;
		int carea = 0;
		int iminus1;
		for (int i = 0; i < p.npoints; i++) {
			iminus1 = i - 1;
			if (iminus1 < 0)
				iminus1 = p.npoints - 1;
			carea += (p.xpoints[i] + p.xpoints[iminus1]) * (p.ypoints[i] - p.ypoints[iminus1]);
		}
		return (Math.abs(carea / 2.0));
	}

	// Method to keep the ROI with the biggest area stored in the ROIManager, the
	// rest of ROIs are
	// deleted.
	private static void keepBiggestROI(RoiManager rm) {

		Roi[] rois = rm.getRoisAsArray();

		if (rois.length > 1) {
			rm.runCommand("Select All");
			rm.runCommand("Delete");

			Roi biggestROI = rois[0];

			for (int i = 1; i < rois.length; i++) {

				if (getArea(biggestROI.getPolygon()) < getArea(rois[i].getPolygon())) {

					biggestROI = rois[i];
				}

			}
			rm.addRoi(biggestROI);

		}

	}

	private void processBlackHoles(ImagePlus imp2) {
		IJ.setThreshold(imp2, 0, 2300);
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Fill Holes", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Watershed", "");
	}

	private void processEsferoidesGeneralCase(ImagePlus imp2) {
		IJ.run(imp2, "Convolve...",
				"text1=[-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 50 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n] normalize");
		IJ.run(imp2, "Maximum...", "radius=2");
		Prefs.blackBackground = false;
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");
	}
	
	private void processEsferoidUsingThreshold(ImagePlus imp2) {
		IJ.setAutoThreshold(imp2, "Otsu");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");
	}

	private RoiManager analyzeParticles(ImagePlus imp2) {
		IJ.run(imp2, "Analyze Particles...", "size=10000-Infinity circularity=0.10-1.00 show=Outlines exclude add");
		ImagePlus imp3 = IJ.getImage();
		imp2.close();
		imp3.close();

		RoiManager rm = RoiManager.getInstance();
		return rm;
	}
	
	// Method to detect esferoides.
	private void detectEsferoide(ImporterOptions options, String dir, String name) throws FormatException, IOException {
		options.setId(name);

		ImagePlus[] imps = BF.openImagePlus(options);
		ImagePlus imp = imps[0];
		ImagePlus imp2 = imp.duplicate();

		/// We consider two cases, when there is a "black hole" in the image (the first
		/// case), there is a lot of pixels below a given threshold, and those pixels
		/// belong to the Esferoide.
		int count = countBelowThreshold(imp2, 1800);
		if (count > 10000) {
			processBlackHoles(imp2);
		} else {
			processEsferoidesGeneralCase(imp2);
		}
		
		RoiManager rm = analyzeParticles(imp2);
		// We have to check whether the program has detected something (that is, whether
		// the RoiManager is not null). If the ROIManager is empty, we try a different
		// approach using a threshold.
		if (rm == null) {
			// We try to find the esferoide using a threshold directly.
			imp2 = imp.duplicate();
			processEsferoidUsingThreshold(imp2);
			rm = analyzeParticles(imp2);
		}
		
		showResultsAndSave(dir, imp, rm);
		imp.close();

	}

	// Method to search the list of files that satisfies a pattern in a folder. The
	// list of files
	// is stored in the result list.
	private static void search(final String pattern, final File folder, List<String> result) {
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

	@Override
	public void run() {
		IJ.setForegroundColor(255, 0, 0);
		try {

			// Since we are working with nd2 images that are imported with the Bio-formats
			// plugins, we must set to true the option windowless to avoid that the program
			// shows us a confirmation dialog every time.
			ImporterOptions options = new ImporterOptions();
			options.setWindowless(true);

			// We ask the user for a directory with nd2 images.
			DirectoryChooser dc = new DirectoryChooser("Select the folder containing the nd2 images");
			String dir = dc.getDirectory();

			// We store the list of nd2 files in the result list.
			File folder = new File(dir);
			List<String> result = new ArrayList<String>();
			search(".*\\.nd2", folder, result);

			// For each nd2 file, we detect the esferoide. Currently, this means that it
			// creates
			// a new image with the detected region marked in red.
			for (String name : result) {
				detectEsferoide(options, dir, name);
			}
			// When the process is finished, we show a message to inform the user.
			IJ.showMessage("Process finished");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		// Launch the "CommandWithPreview" command.
		ij.command().run(EsferoideJ_.class, true);
	}

}
