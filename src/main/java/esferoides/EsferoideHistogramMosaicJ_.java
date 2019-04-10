package esferoides;

import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.io.DirectoryChooser;
import ij.measure.ResultsTable;
import ij.plugin.ImagesToStack;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;
import net.imagej.ImageJ;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Esferoids>EsferoideHistogramMosaicJ")
public class EsferoideHistogramMosaicJ_ implements Command {


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

	private boolean findLocalMaxima(int array[], int start, int end, int window) {

		boolean found;
		for (int i = start + window; i < end - window; i++) {
			if (array[i] > 0) {
				found = true;
				for (int j = 1; j <= window; j++) {
					found = found && (array[i] >= array[i - j]) && (array[i] >= array[i + j]);
				}
				if (found) {
					System.out.println(i);
					return true;
				}
			}
		}
		return false;

	}

	private void processEsferoidUsingThreshold(ImagePlus imp2) {
		IJ.setAutoThreshold(imp2, "Otsu");
		ImageProcessor ip = imp2.getProcessor();
		int[] histogram = ip.getHistogram();
		int thresh = (int) ip.getMaxThreshold();

		int countpixelsbelow = 0;
		for (int i = 0; i < thresh; i++) {
			countpixelsbelow = countpixelsbelow + histogram[i];
		}

		int countpixelsover = 0;
		for (int i = thresh; i < histogram.length; i++) {
			countpixelsover = countpixelsover + histogram[i];
		}

//		IJ.showMessage("" + countpixelsbelow * 1.0 / (countpixelsbelow+countpixelsover));
//		IJ.showMessage("" + countpixelsover * 1.0 / (countpixelsbelow+countpixelsover));

		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");
	}

	private void processEsferoidUsingThresholdWithWatershed(ImagePlus imp2) {
		IJ.setAutoThreshold(imp2, "Otsu");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");
		IJ.run(imp2, "Watershed", "");
	}

	private void processEsferoidUsingFindEdges(ImagePlus imp2) {
		IJ.run(imp2, "Find Edges", "");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Morphological Filters", "operation=[Black Top Hat] element=Square radius=5");
		imp2 = IJ.getImage();
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");

	}

	private RoiManager analyzeParticles(ImagePlus imp2) {
		IJ.run(imp2, "Analyze Particles...", "size=10000-Infinity circularity=0.15-1.00 show=Outlines exclude add");
//		ImagePlus imp3 = IJ.getImage();
		imp2.close();
//		imp3.close();

		RoiManager rm = RoiManager.getInstance();
		if (rm != null) {
			rm.setVisible(false);
		}
		return rm;
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

	private void draw(ImagePlus imp2) {
		RoiManager rm = analyzeParticles(imp2);
		imp2 = IJ.getImage();
		IJ.run(imp2, "RGB Color", "");
		if (rm != null) {
			rm.setVisible(false);
			keepBiggestROI(rm);
			rm.runCommand("Show None");
			rm.runCommand("Show All");
			rm.runCommand(imp2, "Draw");
		}

	}

	private void processImage(ImporterOptions options, String dir, String name) throws FormatException, IOException {
		options.setId(name);

		ImagePlus[] imps = BF.openImagePlus(options);
		ImagePlus imp = imps[0];

//		IJ.run(imp, "Histogram", "");

		ImagePlus imp1 = imp.duplicate();
		ImagePlus imp2 = imp.duplicate();
		ImagePlus imp3 = imp.duplicate();
		ImagePlus imp4 = imp.duplicate();

		// Histogram
		IJ.run(imp, "Histogram", "");
		ImagePlus impHist = IJ.getImage();
		IJ.run(impHist, "Scale...", "x=- y=- width=1002 height=1002 interpolation=Bilinear average create");
		impHist.close();
		impHist = IJ.getImage();

		processEsferoidesGeneralCase(imp1);
//		draw(imp1);

		processEsferoidUsingThreshold(imp2);
//		draw(imp2);

		processEsferoidUsingThresholdWithWatershed(imp3);
//		draw(imp3);

		processEsferoidUsingFindEdges(imp4);
		imp4 = IJ.getImage();
//		draw(imp4);

		ImagePlus impStack = ImagesToStack.run(new ImagePlus[] { imp, impHist, imp1, imp2, imp3, imp4 });
		imp1.close();
		imp2.close();
		imp3.close();
		imp4.close();
		impHist.close();
		IJ.run(impStack, "Make Montage...", "columns=2 rows=3 scale=0.5");
		imp = IJ.getImage();
		IJ.saveAs(imp, "Tiff",  name + "_hist.tiff");
		impStack.close();
		
		
		
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

			search(".*\\.nd2", folder, result);
			Collections.sort(result);
//		rt.show("Results");

			// For each nd2 file, we detect the esferoide. Currently, this means that it
			// creates
			// a new image with the detected region marked in red.
			for (String name : result) {

				processImage(options, dir, name);

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		ij.command().run(EsferoideHistogramMosaicJ_.class, true);
	}

}
