package esferoides;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.io.FileInfo;
import ij.plugin.Thresholder;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

import org.scijava.command.Command;
import org.scijava.command.Previewable;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>EsferoideJ")
public class EsferoideJ_ implements Command {

	@Parameter
	private ImagePlus imp;

	private int countBelowThreshold(ImagePlus imp1, int threshold) {

		ImageProcessor ip = imp1.getProcessor();
		int[] histogram = ip.getHistogram();

		int countpixels = 0;
		for (int i = 0; i < threshold; i++) {
			countpixels = countpixels + histogram[i];
		}

		return countpixels;

	}

	@Override
	public void run() {

		ImagePlus imp2 = imp.duplicate();

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

		IJ.run(imp2, "Analyze Particles...", "size=10000-Infinity show=Outlines exclude add");

		ImagePlus imp3 = IJ.getImage();
		imp2.close();
		imp3.close();

		RoiManager rm = RoiManager.getInstance();
		rm.runCommand("Show None");
		rm.runCommand("Show All");
		
		IJ.run(imp, "RGB Color", "");
		rm.runCommand(imp,"Draw");
		String name = imp.getTitle();

		FileInfo f = imp.getFileInfo();
		name = name.substring(0, name.indexOf("."));
		
		
		IJ.saveAs(imp, "Tiff", f.directory + name + "_pred.tiff");
		//imp.close();
		rm.close();
		
		
		
	}

	

	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		// Launch the "CommandWithPreview" command.
		ij.command().run(EsferoideJ_.class, true);
	}

}
