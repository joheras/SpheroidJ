package esferoides;

import ij.IJ;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.io.DirectoryChooser;
import ij.io.FileInfo;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import org.scijava.command.Command;
import org.scijava.command.Previewable;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

//@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Esferoids>EsferoideJ")
@Plugin(type = Command.class, headless = true, menuPath = "Plugins>EsferoideJ")
public class EsferoideJ_ implements Command {

//	@Parameter
//	private ImagePlus imp;

	private static ArrayList<Integer> goodRows;

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
	// Method to draw the results stored in the roi manager into the image, and then
	// save the
	// image in a given directory. Since we know that there is only one esferoide
	// per image, we
	// only keep the ROI with the biggest area stored in the ROI Manager.
	private static void showResultsAndSave(String dir, ImagePlus imp1, RoiManager rm) throws IOException {
		IJ.run(imp1, "RGB Color", "");

		String name = imp1.getTitle();
		// FileInfo f = imp1.getFileInfo();
		name = name.substring(0, name.indexOf("."));

		ImageStatistics stats = null;
		double[] vFeret;// = 0;
		double perimeter = 0;
		if (rm != null) {
			rm.setVisible(false);
			keepBiggestROI(rm);
			rm.runCommand("Show None");
			rm.runCommand("Show All");

			ImagePlus impN = IJ.createImage("Untitled", "16-bit white", imp1.getWidth(), imp1.getHeight(), 1);
			rm.select(0);
			rm.runCommand(impN, "Fill");
			rm.runCommand("Delete");
			IJ.setAutoThreshold(impN, "Default");
			IJ.run(impN, "Convert to Mask", "");
			IJ.run(impN, "Shape Smoothing",
					"relative_proportion_fds=5 absolute_number_fds=2 keep=[Relative_proportion of FDs]");
			IJ.run(impN, "Analyze Particles...", "exclude add");
			impN.close();
			rm = RoiManager.getInstance();
			rm.runCommand("Show None");
			rm.runCommand("Show All");

			rm.runCommand(imp1, "Draw");
			rm.runCommand("Save", dir + name + ".zip");// saving the roi

			Roi[] roi = rm.getRoisAsArray();
			rm.close();
			// compute the statistics (without calibrate)
			stats = roi[0].getStatistics();
			

			vFeret = roi[0].getFeretValues();// .getFeretsDiameter();
			perimeter = roi[0].getLength();
			Calibration cal = imp1.getCalibration();
			double pw, ph;
			if (cal != null) {
				pw = cal.pixelWidth;
				ph = cal.pixelHeight;
			} else {
				pw = 1.0;
				ph = 1.0;
			}
			// calibrate the measures
			double area = stats.area * pw * ph;
			double w = imp1.getWidth() * pw;
			double h = imp1.getHeight() * ph;
			double aFraction = area / (w * h) * 100;
			double perim = perimeter * pw;

			ResultsTable rt = ResultsTable.getResultsTable();
//            if (rt == null) {
//
//                rt = new ResultsTable();
//            }
			int nrows = Analyzer.getResultsTable().getCounter();
			goodRows.add(nrows - 1);

			rt.setPrecision(2);
			rt.setLabel(name, nrows - 1);
			rt.addValue("Area", area);
			rt.addValue("Area Fraction", aFraction);
			rt.addValue("Perimeter", perim);
			double circularity = perimeter == 0.0 ? 0.0 : 4.0 * Math.PI * (area / (perim * perim));
			if (circularity > 1.0) {
				circularity = 1.0;
			}
			rt.addValue("Circularity", circularity);
			rt.addValue("Diam. Feret", vFeret[0]);
			rt.addValue("Angle. Feret", vFeret[1]);
			rt.addValue("Min. Feret", vFeret[2]);
			rt.addValue("X Feret", vFeret[3]);
			rt.addValue("Y Feret", vFeret[4]);

		}

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

		if (rois.length >= 1) {
			rm.runCommand("Select All");
			rm.runCommand("Delete");

			Roi biggestROI = rois[0];

			for (int i = 1; i < rois.length; i++) {

				if (getArea(biggestROI.getPolygon()) < getArea(rois[i].getPolygon())) {

					biggestROI = rois[i];
				}

			}
//			IJ.showMessage(""+getArea(biggestROI.getPolygon()));
			rm.addRoi(biggestROI);

		}

	}

	private void processBlackHoles(ImagePlus imp2, boolean dilate) {
		IJ.setThreshold(imp2, 0, 2300);
		IJ.run(imp2, "Convert to Mask", "");
		if (dilate) {
			IJ.run(imp2, "Fill Holes", "");
			IJ.run(imp2, "Dilate", "");
		}
		IJ.run(imp2, "Watershed", "");
//		IJ.run(imp2, "Shape Smoothing", "relative_proportion_fds=5 absolute_number_fds=2 keep=[Relative_proportion of FDs]");

	}

	private void processEsferoidesGeneralCase(ImagePlus imp2) {
		IJ.run(imp2, "Convolve...",
				"text1=[-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 50 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n] normalize");
//		IJ.run(imp2, "Convolve...",
//				"text1=[-1 -1 -1 -1 -1\n-1 -1 -1 -1 -1\n-1 -1 24 -1 -1\n-1 -1 -1 -1 -1\n-1 -1 -1 -1 -1\n] normalize");

		IJ.run(imp2, "Maximum...", "radius=2");
		Prefs.blackBackground = false;
		IJ.run(imp2, "Convert to Mask", "");

		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");
//		IJ.run(imp2, "Shape Smoothing", "relative_proportion_fds=5 absolute_number_fds=2 keep=[Relative_proportion of FDs]");

	}

	private void processEsferoidUsingThreshold(ImagePlus imp2) {
		IJ.setAutoThreshold(imp2, "Otsu");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");
//		IJ.run(imp2, "Shape Smoothing", "relative_proportion_fds=5 absolute_number_fds=2 keep=[Relative_proportion of FDs]");

	}

	private ImagePlus processEsferoidUsingThresholdCombination(ImagePlus imp2) {

		ImagePlus imp1 = imp2.duplicate();
//		IJ.run(imp1, "Convolve...",
//				"text1=[-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 50 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n-1 -1 -1 -1 -1 -1 -1\n] normalize");
//		IJ.run(imp1, "Maximum...", "radius=2");
//		Prefs.blackBackground = false;
//		IJ.run(imp1, "Convert to Mask", "");
//		IJ.run(imp1, "Dilate", "");
//		IJ.run(imp1, "Dilate", "");
//		IJ.run(imp1, "Dilate", "");
//		IJ.run(imp1, "Fill Holes", "");
		IJ.run(imp1, "Find Edges", "");
		IJ.run(imp1, "Convert to Mask", "");
		IJ.run(imp1, "Morphological Filters", "operation=[Black Top Hat] element=Square radius=5");
		imp1.changes = false;
		imp1.close();
		ImagePlus imp4 = IJ.getImage();
//		imp3.close();
//		imp3= IJ.getImage();
		IJ.run(imp4, "Dilate", "");
		IJ.run(imp4, "Dilate", "");
		IJ.run(imp4, "Fill Holes", "");

		IJ.setAutoThreshold(imp2, "Otsu");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");

		ImageCalculator ic = new ImageCalculator();
		ImagePlus imp3 = ic.run("OR create", imp4, imp2);
//		IJ.run(imp3, "Shape Smoothing", "relative_proportion_fds=5 absolute_number_fds=2 keep=[Relative_proportion of FDs]");

		imp4.changes = false;
		imp4.close();
		imp2.close();
		return imp3;

	}

	private void processEsferoidUsingThresholdWithWatershed(ImagePlus imp2) {
		IJ.setAutoThreshold(imp2, "Otsu");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Dilate", "");
		IJ.run(imp2, "Fill Holes", "");
		IJ.run(imp2, "Watershed", "");
//		IJ.run(imp2, "Shape Smoothing", "relative_proportion_fds=5 absolute_number_fds=2 keep=[Relative_proportion of FDs]");

	}

	private void processEsferoidUsingFindEdges(ImagePlus imp2) {
		IJ.run(imp2, "Find Edges", "");
		IJ.run(imp2, "Convert to Mask", "");
		IJ.run(imp2, "Morphological Filters", "operation=[Black Top Hat] element=Square radius=5");
		imp2.close();
		ImagePlus imp3 = IJ.getImage();
//		imp3.close();
//		imp3= IJ.getImage();
		IJ.run(imp3, "Dilate", "");
		IJ.run(imp3, "Dilate", "");
		IJ.run(imp3, "Fill Holes", "");
//		IJ.run(imp3, "Shape Smoothing", "relative_proportion_fds=5 absolute_number_fds=2 keep=[Relative_proportion of FDs]");

	}

	private RoiManager analyzeParticles(ImagePlus imp2, boolean blackHole) {
		if (blackHole) {
			IJ.run(imp2, "Analyze Particles...", "size=20000-Infinity circularity=0.5-1.00 show=Outlines exclude add");
		} else {
			IJ.run(imp2, "Analyze Particles...", "size=20000-Infinity circularity=0.15-1.00 show=Outlines exclude add");
		}

		ImagePlus imp3 = IJ.getImage();
		imp2.close();
		imp3.close();

		RoiManager rm = RoiManager.getInstance();
		if (rm != null ) {
			rm.setVisible(false);
//			Roi[] rois = rm.getRoisAsArray();
//			rm.runCommand("Select All");
//			rm.runCommand("Delete");
//			ImageStatistics stats ;
//			for(int i =0;i<rois.length;i++) {
//				stats = rois[i].getStatistics();
//				double round = 4*stats.area / (3.14 * stats.major * stats.major);
//				if(round>0.75) {
//					rm.addRoi(rois[i]);
//				}
//				
//			}
			
			
		}
		return rm;
	}

	private int analyzeSmallParticles(ImagePlus imp2) {
		IJ.run(imp2, "Analyze Particles...", "size=10-300 circularity=0-1.00 show=Outlines exclude add");
		ImagePlus imp3 = IJ.getImage();
		imp2.close();
		imp3.close();

		RoiManager rm = RoiManager.getInstance();
		if (rm != null) {
			rm.setVisible(false);
		}
		return rm.getRoisAsArray().length;
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
		int count = countBelowThreshold(imp2, 1100);
		RoiManager rm;
		if (count > 100) {
			if (count > 10000) {
				processBlackHoles(imp2, false);
			} else {
				processBlackHoles(imp2, true);
			}

			rm = analyzeParticles(imp2, true);

		} else {
			processEsferoidesGeneralCase(imp2);

			rm = analyzeParticles(imp2, false);

			if (rm != null  ) {

				Roi[] r = rm.getRoisAsArray();
				rm.runCommand("Select All");
				rm.runCommand("Delete");

				int smallParticles = analyzeSmallParticles(imp2);
				if (smallParticles > 20) {
					rm.runCommand("Select All");
					rm.runCommand("Delete");
					imp2 = imp.duplicate();
					processEsferoidUsingFindEdges(imp2);
					imp2 = IJ.getImage();
					imp2.changes = false;

					rm = analyzeParticles(imp2, false);

				} else {
					for (int i = 0; i < r.length; i++) {
						rm.addRoi(r[i]);
					}
				}

			}

			// We have to check whether the program has detected something (that is, whether
			// the RoiManager is not null). If the ROIManager is empty, we try a different
			// approach using a threshold.
			if (rm == null || rm.getRoisAsArray().length == 0) {

				// We try to find the esferoide using a threshold directly.
				imp2 = imp.duplicate();
				imp2 = processEsferoidUsingThresholdCombination(imp2);
				rm = analyzeParticles(imp2, false);
			}

			// We have to check whether the program has detected something (that is, whether
			// the RoiManager is not null). If the ROIManager is empty, we try a different
			// approach using a threshold combined with watershed.
			if (rm == null || rm.getRoisAsArray().length == 0) {

				// We try to find the esferoide using a threshold directly.
				imp2 = imp.duplicate();
				processEsferoidUsingThresholdWithWatershed(imp2);
				rm = analyzeParticles(imp2, false);

			}

			if (rm == null || rm.getRoisAsArray().length == 0) {
				imp2 = imp.duplicate();
				processEsferoidUsingFindEdges(imp2);
				imp2 = IJ.getImage();
				imp2.changes = false;
				rm = analyzeParticles(imp2, false);

			}

			// Idea: Probar varias alternativas y ver cuál es la que produce mejor
			// resultado.
			// ¿Cómo se define mejor resultado?
		}
		showResultsAndSave(dir, imp, rm);
		imp.close();

	}

//	
//	private static int getOtsuThreshold(ImagePlus imp1) {
//		ImagePlus imp = imp1.duplicate();
//		IJ.setAutoThreshold(imp, "Otsu");
//		ImageProcessor ip = imp.getProcessor();
//		int thresh = (int) ip.getMaxThreshold();
//		imp.close();
//		return thresh;
//				
//	}
//	
//	private static double getPercentageUnderOtsu(ImagePlus imp,int thresh) {
//		ImageProcessor ip = imp.getProcessor();
//		int[] histogram = ip.getHistogram();
//		int countpixelsbelow = 0;
//		for (int i = 0; i < thresh; i++) {
//			countpixelsbelow = countpixelsbelow + histogram[i];
//		}
//
//		int countpixelsover = 0;
//		for (int i = thresh; i < histogram.length; i++) {
//			countpixelsover = countpixelsover + histogram[i];
//		}
//
//		if(countpixelsbelow>countpixelsover) {
//			return countpixelsover * 1.0 / (countpixelsbelow+countpixelsover);
//		}else {
//			return countpixelsbelow * 1.0 / (countpixelsbelow+countpixelsover);
//		}
//	}
//	
//	
//	
//
//	
//	
//	private void newDetectEsferoide(ImporterOptions options, String dir, String name) throws FormatException, IOException {
//		options.setId(name);
//
//		ImagePlus[] imps = BF.openImagePlus(options);
//		ImagePlus imp = imps[0];
//		ImagePlus imp2 = imp.duplicate();
//
//		/// We consider two cases, when there is a "black hole" in the image (the first
//		/// case), there is a lot of pixels below a given threshold, and those pixels
//		/// belong to the Esferoide.
//		RoiManager rm;
//		int count = countBelowThreshold(imp2, 1800);
//		if (count > 10000) {
//			processBlackHoles(imp2);
//			rm  = analyzeParticles(imp2);
//		}else {
//			int thresh = getOtsuThreshold(imp2);
//			double perc = getPercentageUnderOtsu(imp2, thresh);
//			
//			// If the threshold is not over 7000 and the division using Otsu produces 
//			// two good regions, then the threshold method is employed. Otherwise, we 
//			// apply the filter
//			
//			if(thresh<7000 && perc>=0.1 && perc <=0.4) {
//				processEsferoidUsingThreshold(imp2);
//				rm = analyzeParticles(imp2);
//				if(rm==null) {
//					imp2 = imp.duplicate();
//					processEsferoidUsingThresholdWithWatershed(imp2);
//					rm = analyzeParticles(imp2);					
//				}
//			}else {
//				processEsferoidesGeneralCase(imp2);
//				rm = analyzeParticles(imp2);
//				if(rm==null) {
//					imp2 = imp.duplicate();
//					processEsferoidUsingFindEdges(imp2);
//					rm = analyzeParticles(imp2);					
//				}
//			}
//		}
//
//		showResultsAndSave(dir, imp, rm);
//		imp.close();
//
//	}

	@Override
	public void run() {
		IJ.setForegroundColor(255, 0, 0);
		goodRows = new ArrayList<>();
		try {

			// Since we are working with nd2 images that are imported with the Bio-formats
			// plugins, we must set to true the option windowless to avoid that the program
			// shows us a confirmation dialog every time.
			ImporterOptions options = new ImporterOptions();
			options.setWindowless(true);

			// We ask the user for a directory with nd2 images.
			DirectoryChooser dc = new DirectoryChooser("Select the folder containing the nd2 images");
			String dir = dc.getDirectory();

			JFrame frame = new JFrame("Work in progress");
			JProgressBar progressBar = new JProgressBar();
			progressBar.setValue(0);
			progressBar.setString("");
			progressBar.setStringPainted(true);
			progressBar.setIndeterminate(true);
			Border border = BorderFactory.createTitledBorder("Processing...");
			progressBar.setBorder(border);
			Container content = frame.getContentPane();
			content.add(progressBar, BorderLayout.NORTH);
			frame.setSize(300, 100);
			frame.setVisible(true);

			// We store the list of nd2 files in the result list.
			File folder = new File(dir);
			List<String> result = new ArrayList<String>();

			Utils.search(".*\\.nd2", folder, result);
			Collections.sort(result);
			// We initialize the ResultsTable
			ResultsTable rt = new ResultsTable();
//			rt.show("Results");

			// For each nd2 file, we detect the esferoide. Currently, this means that it
			// creates
			// a new image with the detected region marked in red.
			for (String name : result) {
				detectEsferoide(options, dir, name);
			}
			rt = ResultsTable.getResultsTable();
			/// Remove empty rows
			int rows = rt.getCounter();
			for (int i = rows; i > 0; i--) {
				if (!(goodRows.contains(i - 1))) {
					rt.deleteRow(i - 1);
				}
			}
			/// Remove unnecessary columns
			rt.deleteColumn("Mean");
			rt.deleteColumn("Min");
			rt.deleteColumn("Max");
			rt.deleteColumn("Circ.");
			rt.deleteColumn("Median");
			rt.deleteColumn("Skew");
			rt.deleteColumn("Kurt");
//			rt.deleteColumn("AR");
//			rt.deleteColumn("Round");
//			rt.deleteColumn("Solidity");

//			rt.saveAs(dir + "results.csv");
			// When the process is finished, we show a message to inform the user.

			ExportToExcel ete = new ExportToExcel(rt, dir);
			ete.convertToExcel();

			rt.reset();

			frame.setVisible(false);
			frame.dispose();
			IJ.showMessage("Process finished");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//	public static void main(final String... args) throws Exception {
//		// Launch ImageJ as usual.
//		final ImageJ ij = new ImageJ();
//		ij.launch(args);
//
//		// Launch the "CommandWithPreview" command.
//		ij.command().run(EsferoideJ_.class, true);
//	}

}
