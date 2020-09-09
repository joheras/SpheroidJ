package oldclasses;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Polygon;
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
import ij.gui.Roi;
import ij.io.DirectoryChooser;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;
import loci.formats.FormatException;
import loci.plugins.in.ImporterOptions;
import spheroidj.ExportToExcel;
import spheroidj.Utils;

//@Plugin(type = Command.class, headless = true, menuPath = "Plugins>EsferoideJFluo")
public class EsferoideJFluo_ implements Command {
	
	@Parameter(label = "Suspension or Colageno", choices = {"suspension","colageno"})
	private String type="suspension";

	private static ArrayList<Integer> goodRows;

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
	private static void showResultsAndSave(String dir, String name, ImagePlus imp1, RoiManager rm) throws IOException {
		IJ.run(imp1, "RGB Color", "");

		/*String name = imp1.getTitle();*/

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

			Roi[] roi = rm.getRoisAsArray();

			if (roi.length != 0) {

				imp1.show();
				rm.select(0);
				IJ.run(imp1, "Fit Spline", "");
				rm.addRoi(imp1.getRoi());
				rm.select(0);
				rm.runCommand(imp1, "Delete");

				roi = rm.getRoisAsArray();

				rm.runCommand(imp1, "Draw");
				rm.runCommand("Save", dir + name + ".zip");
				rm.close();
				// saving the roi
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
//	            if (rt == null) {
				//
//	                rt = new ResultsTable();
//	            }
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
//				IJ.showMessage(""+getArea(biggestROI.getPolygon()));
			rm.addRoi(biggestROI);

		}

	}

	private RoiManager analyzeParticles(ImagePlus imp2, boolean blackHole) {
		IJ.run(imp2, "Analyze Particles...", "size=0.01-Infinity circularity=0.0-2.00 show=Outlines exclude add");
		imp2.changes = false;
		ImagePlus imp3 = IJ.getImage();
		imp2.close();
		imp3.close();

		RoiManager rm = RoiManager.getInstance();
		if (rm != null) {
			rm.setVisible(false);
		}
		return rm;
	}

	private void processEsferoidNoFluo(ImagePlus imp2) {
		IJ.run(imp2, "8-bit", "");
		IJ.run(imp2, "Find Edges", "");
		IJ.setAutoThreshold(imp2, "Default dark");
		IJ.setRawThreshold(imp2, 30, 255, null);
		IJ.run(imp2, "Convert to Mask", "");
	}

	private void processEsferoidFluo(ImagePlus imp2, boolean threshold) {
		IJ.run(imp2, "8-bit", "");
		IJ.setAutoThreshold(imp2, "Otsu dark");
		if (threshold) {
			IJ.setRawThreshold(imp2, 40, 255, null);// 40
		}
		IJ.run(imp2, "Convert to Mask", "");
	}

	// Method to detect esferoides.
	private void detectEsferoideFluoColageno(ImporterOptions options, String dir, String name)
			throws FormatException, IOException {

		ImagePlus impFluo = IJ.openImage(name);

		name = name.replace("fluo", "");
		ImagePlus impNoFluo = IJ.openImage(name);

		String title = impNoFluo.getTitle();

		ImagePlus imp = impNoFluo.duplicate();
		imp.setTitle(title);

		processEsferoidFluo(impFluo, true);
		processEsferoidNoFluo(impNoFluo);
		ImageCalculator ic = new ImageCalculator();
		ImagePlus imp3 = ic.run("Add create", impFluo, impNoFluo);
		IJ.run(imp3, "Fill Holes", "");
		RoiManager rm = analyzeParticles(imp3, false);

		imp3.close();
		impFluo.close();
		impNoFluo.close();
		showResultsAndSave(dir, name,imp, rm);
		imp.close();

	}

	// Method to detect esferoides.
	private void detectEsferoideFluoSuspension(ImporterOptions options, String dir, String name)
			throws FormatException, IOException {

		ImagePlus impFluo = IJ.openImage(name);

		name = name.replace("fluo", "");
		ImagePlus impNoFluo = IJ.openImage(name);

		/*String title = impNoFluo.getTitle();

		ImagePlus imp = impNoFluo.duplicate();
		imp.setTitle(title);*/

		processEsferoidFluo(impFluo,false);
		/*processEsferoidNoFluo(impNoFluo);
		IJ.run(impFluo, "Dilate", "");
		
		
		ImageCalculator ic = new ImageCalculator();
		ImagePlus imp3 = ic.run("AND create", impFluo, impNoFluo);
		IJ.run(impFluo, "Erode", "");
		IJ.run(impFluo, "Erode", "");
		ImagePlus imp4 = ic.run("Add create", impFluo, imp3);
		
		IJ.run(imp4, "Fill Holes", "");*/
		RoiManager rm = analyzeParticles(impFluo, false);

		
		impFluo.close();
		
		
		showResultsAndSave(dir,name, impNoFluo, rm);
		impNoFluo.close();

	}

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
			DirectoryChooser dc = new DirectoryChooser("Select the folder containing the images");
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

			// We store the list of tiff files in the result list.
			File folder = new File(dir);
			List<String> result = new ArrayList<String>();

			Utils.search(".*fluo.tif", folder, result);
			Collections.sort(result);
			// We initialize the ResultsTable
			ResultsTable rt = new ResultsTable();
//			rt.show("Results");

			// For each nd2 file, we detect the esferoide. Currently, this means that it
			// creates
			// a new image with the detected region marked in red.
			for (String name : result) {
				if(type.equals("colageno")) {
					detectEsferoideFluoColageno(options, dir, name);
				}else {
					detectEsferoideFluoSuspension(options, dir, name);
				}
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
			/*
			 * rt.deleteColumn("Mean"); rt.deleteColumn("Min"); rt.deleteColumn("Max");
			 * rt.deleteColumn("Circ."); rt.deleteColumn("Median"); rt.deleteColumn("Skew");
			 * rt.deleteColumn("Kurt");
			 */
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

}
