package esferoides;

import java.io.IOException;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class DetectEsferoidMethods {

	// Method to detect esferoides.
	public static void detectEsferoideFluoColageno(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {

		ImagePlus impFluo = IJ.openImage(name);

		name = name.replace("fluo", "");
		ImagePlus impNoFluo = IJ.openImage(name);

		String title = impNoFluo.getTitle();

		ImagePlus imp = impNoFluo.duplicate();
		imp.setTitle(title);

		DetectEsferoidImageMethods.processEsferoidFluo(impFluo, true);
		DetectEsferoidImageMethods.processEsferoidNoFluo(impNoFluo);
		ImageCalculator ic = new ImageCalculator();
		ImagePlus imp3 = ic.run("Add create", impFluo, impNoFluo);
		IJ.run(imp3, "Fill Holes", "");
		RoiManager rm = AnalyseParticleMethods.analyzeParticlesFluo(imp3);

		imp3.close();
		impFluo.close();
		impNoFluo.close();
		try {
			Utils.showResultsAndSave(dir,name, imp, rm, goodRows);
			imp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectEsferoideFluoSuspension(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {

		ImagePlus impFluo = IJ.openImage(name);

		name = name.replace("fluo", "");
		ImagePlus impNoFluo = IJ.openImage(name);

		DetectEsferoidImageMethods.processEsferoidFluo(impFluo, false);
		RoiManager rm = AnalyseParticleMethods.analyzeParticlesFluo(impFluo);

		impFluo.close();

		try {
			Utils.showResultsAndSave(dir,name, impNoFluo, rm, goodRows);
			impNoFluo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectEsferoideHectorv2(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {
		ImagePlus impb = IJ.openImage(name);
		String title = impb.getTitle();

		ImagePlus imp = impb.duplicate();
		imp.setTitle(title);
		IJ.run(imp, "8-bit", "");
		ImagePlus imp2 = imp.duplicate();
		imp2.setTitle(title);
		RoiManager rm = null;
//
		DetectEsferoidImageMethods.processEsferoidUsingThreshold(imp2, true);
		rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
		if (rm == null || rm.getRoisAsArray().length == 0) {
			DetectEsferoidImageMethods.processEsferoidUsingThreshold(imp2, false);
			rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
		}

		try {
			Utils.showResultsAndSave(dir,name, imp, rm, goodRows);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imp.close();

	}

	// Method to detect esferoides.
	public static void detectEsferoideHectorv1(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {
		ImagePlus impb = IJ.openImage(name);
		String title = impb.getTitle();

		ImagePlus imp = impb.duplicate();
		imp.setTitle(title);
		IJ.run(imp, "8-bit", "");
		ImagePlus imp2 = imp.duplicate();
		imp2.setTitle(title);
		RoiManager rm = null;
		//
		DetectEsferoidImageMethods.processEsferoidUsingThreshold(imp2, true);
		rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
		if (rm == null || rm.getRoisAsArray().length == 0) {
			DetectEsferoidImageMethods.processEsferoidUsingThreshold(imp2, false);
			rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
		}

		if (rm == null || rm.getRoisAsArray().length == 0) {
			double v = 1.75;

			while ((rm == null || rm.getRoisAsArray().length == 0) && v >= 1.0) {
				imp2 = imp.duplicate();
				DetectEsferoidImageMethods.processEsferoidesGeneralCaseHector(imp2, 3, v);
				rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
				v = v - 0.25;
			}
		}

		if (rm == null || rm.getRoisAsArray().length == 0) {
			double v = 1.75;
			while ((rm == null || rm.getRoisAsArray().length == 0) && v >= 1.0) {
				imp2 = imp.duplicate();
				DetectEsferoidImageMethods.processEsferoidesGeneralCaseHector(imp2, 5, v);
				rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
				v = v - 0.25;
			}
		}

		if (rm == null || rm.getRoisAsArray().length == 0) {
			double v = 1.75;
			while ((rm == null || rm.getRoisAsArray().length == 0) && v >= 1.0) {
				imp2 = imp.duplicate();
				DetectEsferoidImageMethods.processEsferoidesGeneralCaseHector(imp2, 7, v);
				rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
				v = v - 0.25;
			}
		}

		if (rm == null || rm.getRoisAsArray().length == 0) {
			imp2 = imp.duplicate();
			DetectEsferoidImageMethods.processEsferoidUsingThreshold(imp2, false);
			rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);

		}

		try {
			Utils.showResultsAndSave(dir,name, imp, rm, goodRows);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imp.close();

	}

	// Method to detect esferoides.
	public static void detectEsferoideTeodora(ImporterOptions options, String dir, String name, ArrayList<Integer> goodRows) {
		options.setId(name);

		ImagePlus[] imps;
		try {
			imps = BF.openImagePlus(options);

			ImagePlus imp = imps[0];
			ImagePlus imp2 = imp.duplicate();

			/// We consider two cases, when there is a "black hole" in the image (the first
			/// case), there is a lot of pixels below a given threshold, and those pixels
			/// belong to the Esferoide. In addition to be a black hole, there must be a
			/// difference between that region and the rest of the image.
//			int count = countBelowThreshold(imp2, 1100);
//			boolean realBlackHole = countBetweenThresholdOver(imp2, 1100, 2000, 1500);
//			System.out.println(realBlackHole);
			RoiManager rm;

			DetectEsferoidImageMethods.processEsferoidEdges(imp2, 0);
			rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, false);

			int iters = 1;
			while (rm == null || rm.getRoisAsArray().length == 0) {
				DetectEsferoidImageMethods.processEsferoidEdges(imp2, iters);
				rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, false);
				iters++;
			}

			Utils.showResultsAndSave(dir,name, imp, rm, goodRows);
			imp.close();
		} catch (FormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
