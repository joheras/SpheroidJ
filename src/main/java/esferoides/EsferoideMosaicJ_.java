package esferoides;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.plugin.ImagesToStack;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;

//@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Esferoids>EsferoideMosaicJ")
public class EsferoideMosaicJ_ implements Command {

	@Parameter
	private ImagePlus imp;

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

		IJ.showMessage("" + countpixelsbelow * 1.0 / (countpixelsbelow+countpixelsover));
		IJ.showMessage("" + countpixelsover * 1.0 / (countpixelsbelow+countpixelsover));
				
		

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

	@Override
	public void run() {
		IJ.run(imp, "Histogram", "");

		ImagePlus imp1 = imp.duplicate();
		ImagePlus imp2 = imp.duplicate();
		ImagePlus imp3 = imp.duplicate();
		ImagePlus imp4 = imp.duplicate();

		processEsferoidesGeneralCase(imp1);
//		draw(imp1);

		processEsferoidUsingThreshold(imp2);
//		draw(imp2);

		processEsferoidUsingThresholdWithWatershed(imp3);
//		draw(imp3);

		processEsferoidUsingFindEdges(imp4);
		imp4 = IJ.getImage();
//		draw(imp4);

		ImagePlus impStack = ImagesToStack.run(new ImagePlus[] { imp1, imp2, imp3, imp4 });
		imp1.close();
		imp2.close();
		imp3.close();
		imp4.close();
		IJ.run(impStack, "Make Montage...", "columns=2 rows=2 scale=0.5");

	}

}
