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

	private int countBelowThreshold(ImagePlus imp1, int threshold) {

		ImageProcessor ip = imp1.getProcessor();
		int[] histogram = ip.getHistogram();

		int countpixels = 0;
		for (int i = 0; i < threshold; i++) {
			countpixels = countpixels + histogram[i];
		}

		return countpixels;

	}

	private static void showResultsAndSave(String dir, ImagePlus imp1, RoiManager rm) {
		IJ.run(imp1, "RGB Color", "");
		if (rm != null) {
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

	private void detectEsferoide(ImporterOptions options, String dir, String name) throws FormatException, IOException {
		options.setId(name);
		
		ImagePlus[] imps = BF.openImagePlus(options);
		ImagePlus imp = imps[0];
		ImagePlus imp2 = imp.duplicate();

		/// We consider two cases, when there is a "black hole" in the image (the first
		/// case),
		// there is a lot of pixels below a given threshold.
		int count = countBelowThreshold(imp2, 1800);
		if (count > 10000) {

			IJ.setThreshold(imp2, 0, 1500);
			IJ.run(imp2, "Convert to Mask", "");
			IJ.run(imp2, "Dilate", "");

		} else {

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

		IJ.run(imp2, "Analyze Particles...", "size=10000-Infinity circularity=0.10-1.00 show=Outlines exclude add");
		ImagePlus imp3 = IJ.getImage();
		imp2.close();
		imp3.close();

		RoiManager rm = RoiManager.getInstance();
		// We have to check whether the program has detected something (that is, whether
		// the
		// RoiManager is not null).

		if (rm != null) {
			showResultsAndSave(dir, imp, rm);
		} else {
			// We try to find it using a threshold directly.
			imp2 = imp.duplicate();
			IJ.setThreshold(imp2, 0, 4500);
			IJ.run(imp2, "Convert to Mask", "");
			IJ.run(imp2, "Dilate", "");
			IJ.run(imp2, "Fill Holes", "");
			IJ.run(imp2, "Analyze Particles...", "size=10000-Infinity circularity=0.10-1.00 show=Outlines exclude add");
			imp3 = IJ.getImage();
			imp2.close();
			imp3.close();

			rm = RoiManager.getInstance();
			showResultsAndSave(dir, imp, rm);
		}
		imp.close();

	}

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
			ImporterOptions options = new ImporterOptions();
			options.setWindowless(true);

			DirectoryChooser dc = new DirectoryChooser("Select the folder containing the nd2 images");
			String dir = dc.getDirectory();

			File folder = new File(dir);
			List<String> result = new ArrayList<String>();

			search(".*\\.nd2", folder, result);

			for (String name : result) {
				detectEsferoide(options, dir, name);
			}

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
