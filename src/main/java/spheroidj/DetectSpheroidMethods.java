package spheroidj;

import java.io.IOException;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class DetectSpheroidMethods {

	// Method to detect esferoides.
	public static void detectSpheroidFluoColageno(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {

		ImagePlus impFluo = IJ.openImage(name);

		name = name.replace("fluo", "");
		ImagePlus impNoFluo = IJ.openImage(name);

		String title = impNoFluo.getTitle();

		ImagePlus imp = impNoFluo.duplicate();
		imp.setTitle(title);

		DetectSpheroidImageMethods.processSpheroidFluo(impFluo, true);
		DetectSpheroidImageMethods.processSpheroidNoFluo(impNoFluo);
		ImageCalculator ic = new ImageCalculator();
		ImagePlus imp3 = ic.run("And create", impFluo, impNoFluo);
		IJ.run(imp3, "Fill Holes", "");
		RoiManager rm = AnalyseParticleMethods.analyzeParticlesFluo(imp3);

		imp3.close();
		impFluo.close();
		impNoFluo.close();
		try {
			Utils.showResultsAndSave(dir, name, imp, rm, goodRows);
			imp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectSpheroidFluoSuspension(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {

		ImagePlus impFluo = IJ.openImage(name);

		name = name.replace("fluo", "");
		ImagePlus impNoFluo = IJ.openImage(name);

		DetectSpheroidImageMethods.processSpheroidFluo(impFluo, false);
		RoiManager rm = AnalyseParticleMethods.analyzeParticlesFluo(impFluo);

		impFluo.close();

		try {
			Utils.showResultsAndSave(dir, name, impNoFluo, rm, goodRows);
			impNoFluo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectSpheroidHectorv2(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {
		options.setId(name);

		options.setWindowless(true);
		ImagePlus[] imps;
		try {
			ImagePlus impb;

			if (name.endsWith(".tiff") || name.endsWith(".tif") || name.endsWith(".jpg") || name.endsWith(".JPG")) {
				impb = IJ.openImage(name);
			} else {
				impb = IJ.openImage(name);
			}
			String title = impb.getTitle();

			ImagePlus imp = impb.duplicate();
			imp.setTitle(title);
			IJ.run(imp, "8-bit", "");
			ImagePlus imp2 = imp.duplicate();
			imp2.setTitle(title);
			RoiManager rm = null;
//
			DetectSpheroidImageMethods.processSpheroidUsingThreshold(imp2, true);
			rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
			if (rm == null || rm.getRoisAsArray().length == 0) {
				DetectSpheroidImageMethods.processSpheroidUsingThreshold(imp2, false);
				rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
			}

			Utils.showResultsAndSave(dir, name, imp, rm, goodRows);
			imp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectSpheroidHectorv1(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {
		options.setId(name);

		try {

			options.setId(name);

			options.setWindowless(true);
			ImagePlus[] imps;
			imps = BF.openImagePlus(options);
			ImagePlus impb;

			if (name.endsWith(".tiff") || name.endsWith(".tif") || name.endsWith(".jpg") || name.endsWith(".JPG")) {
				impb = IJ.openImage(name);
			} else {
				impb = imps[0];
			}

			//ImagePlus impb = imps[0];
			String title = impb.getTitle();

			ImagePlus imp = impb.duplicate();
			imp.setTitle(title);
			IJ.run(imp, "8-bit", "");
			ImagePlus imp2 = imp.duplicate();
			imp2.setTitle(title);
			RoiManager rm = null;
			//
			DetectSpheroidImageMethods.processSpheroidUsingThreshold(imp2, true);
			rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
			if (rm == null || rm.getRoisAsArray().length == 0) {
				DetectSpheroidImageMethods.processSpheroidUsingThreshold(imp2, false);
				rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
			}

			if (rm == null || rm.getRoisAsArray().length == 0) {
				double v = 1.75;

				while ((rm == null || rm.getRoisAsArray().length == 0) && v >= 1.0) {
					imp2 = imp.duplicate();
					DetectSpheroidImageMethods.processSpheroidesGeneralCaseHector(imp2, 3, v);
					rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
					v = v - 0.25;
				}
			}

			if (rm == null || rm.getRoisAsArray().length == 0) {
				double v = 1.75;
				while ((rm == null || rm.getRoisAsArray().length == 0) && v >= 1.0) {
					imp2 = imp.duplicate();
					DetectSpheroidImageMethods.processSpheroidesGeneralCaseHector(imp2, 5, v);
					rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
					v = v - 0.25;
				}
			}

			if (rm == null || rm.getRoisAsArray().length == 0) {
				double v = 1.75;
				while ((rm == null || rm.getRoisAsArray().length == 0) && v >= 1.0) {
					imp2 = imp.duplicate();
					DetectSpheroidImageMethods.processSpheroidesGeneralCaseHector(imp2, 7, v);
					rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);
					v = v - 0.25;
				}
			}

			if (rm == null || rm.getRoisAsArray().length == 0) {
				imp2 = imp.duplicate();
				DetectSpheroidImageMethods.processSpheroidUsingThreshold(imp2, false);
				rm = AnalyseParticleMethods.analyzeParticlesHector(imp2);

			}

			Utils.showResultsAndSave(dir, name, imp, rm, goodRows);
			imp.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectSpheroidTeodora(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {
		options.setId(name);

		options.setWindowless(true);

		ImagePlus[] imps;
		try {
			imps = BF.openImagePlus(options);

			ImagePlus imp;

			if (name.endsWith(".tiff") || name.endsWith(".tif") || name.endsWith(".jpg") || name.endsWith(".JPG")) {
				imp = IJ.openImage(name);
			} else {
				imp = imps[0];
			}
			ImagePlus imp2 = imp.duplicate();

			RoiManager rm;

			DetectSpheroidImageMethods.processSpheroidEdges(imp2, 0);
			rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, false, true);
			int iters = 1;
			while ((rm == null || rm.getRoisAsArray().length == 0) && iters < 7) {
				DetectSpheroidImageMethods.processSpheroidEdges(imp2, iters);
				rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, false, true);
				iters++;
			}

			Utils.showResultsAndSave(dir, name, imp, rm, goodRows);
			imp.close();
		} catch (FormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectSpheroidTeodoraBig(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {

		try {
			options.setId(name);
			options.setWindowless(true);
			ImagePlus[] imps;
			imps = BF.openImagePlus(options);


			ImagePlus imp;

			if (name.endsWith(".tiff") || name.endsWith(".tif") || name.endsWith(".jpg") || name.endsWith(".JPG")) {
				imp = IJ.openImage(name);
			} else {
				imp = imps[0];
			}
			ImagePlus imp2 = imp.duplicate();

			RoiManager rm;

			int count = Utils.countBelowThreshold(imp2, 1100);
			boolean realBlackHole1 = Utils.countBetweenThresholdOver(imp2, 1100, 2000, 1500);
			boolean realBlackHole2 = Utils.countBelowThreshold(imp2, 3000) < 200000;

			if (count > 100 && realBlackHole2 && realBlackHole1) {

				if (count > 10000) {
					DetectSpheroidImageMethods.processBlackHoles(imp2, false);
				} else {
					DetectSpheroidImageMethods.processBlackHoles(imp2, true);
				}
				rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, true, true);
			} else {
				DetectSpheroidImageMethods.processSpheroidBig(imp2);
				rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, false, false);
			}

			imp2 = imp.duplicate();
			int iters = 0;
			while ((rm == null || rm.getRoisAsArray().length == 0) && iters < 7) {
				DetectSpheroidImageMethods.processSpheroidEdges(imp2, iters);
				rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, false, false);
				iters++;
			}

			Utils.showResultsAndSave(dir, name, imp, rm, goodRows);
			imp.close();
		} catch (FormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectSpheroidTeodoraBigNoHoles(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {
		options.setId(name);

		options.setWindowless(true);

		ImagePlus imp = IJ.openImage(name);
		ImagePlus imp2 = imp.duplicate();

		RoiManager rm;

		DetectSpheroidImageMethods.processSpheroidBig(imp2);
		rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, false, false);

		imp2 = imp.duplicate();
		int iters = 0;
		while ((rm == null || rm.getRoisAsArray().length == 0) && iters < 7) {
			DetectSpheroidImageMethods.processSpheroidEdges(imp2, iters);
			rm = AnalyseParticleMethods.analyseParticlesTeodora(imp2, false, false);
			iters++;
		}

		try {
			Utils.showResultsAndSave(dir, name, imp, rm, goodRows);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imp.close();

	}

	// Method to detect esferoides.
	public static void detectSpheroidFluoStack(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {
		options.setId(name);

		options.setWindowless(true);

		ImagePlus[] imps;
		try {
			imps = BF.openImagePlus(options);

			ImagePlus imp = imps[0];

			ImageStack stack = imp.getStack();

			Calibration cal = imp.getCalibration();
			ImagePlus impFluo = new ImagePlus(stack.getSliceLabel(2), stack.getProcessor(2));
			impFluo.setCalibration(cal);

			ImagePlus impNoFluo = new ImagePlus(stack.getSliceLabel(1), stack.getProcessor(1));
			impFluo.setCalibration(cal);

			imp = impNoFluo.duplicate();

			ImagePlus impFluoD = impFluo.duplicate();
			DetectSpheroidImageMethods.processSpheroidFluo(impFluoD, true);
			RoiManager rm = AnalyseParticleMethods.analyzeParticlesHector(impFluoD);
			Utils.keepBiggestROI(rm);

			/*
			 * Roi r = rm.getRoi(0); ImageStatistics stats = r.getStatistics();
			 * impFluoD.close();
			 * 
			 * 
			 * if (stats.area > 10000) { System.out.println("Entra aquí");
			 * rm.runCommand("Select All"); rm.runCommand("Delete");
			 * 
			 * ImagePlus impNoFluoD = impNoFluo.duplicate();
			 * DetectEsferoidImageMethods.processEsferoidNoFluoThreshold(impNoFluoD); rm =
			 * AnalyseParticleMethods.analyzeParticlesHector(impNoFluoD);
			 * Utils.keepBiggestROI(rm);
			 * 
			 * double round = 0;
			 * 
			 * if (rm.getRoisAsArray().length > 0) { r = rm.getRoi(0); stats =
			 * r.getStatistics(); impNoFluoD.close(); round = 4.0 * (stats.area / (Math.PI *
			 * stats.major * stats.major)); } if (round < 0.9) {
			 * rm.runCommand("Select All"); rm.runCommand("Delete"); impFluoD =
			 * impFluo.duplicate(); DetectEsferoidImageMethods.processEsferoidFluo(impFluoD,
			 * true); DetectEsferoidImageMethods.processEsferoidNoFluoBis(impNoFluo);
			 * ImageCalculator ic = new ImageCalculator(); ImagePlus imp3 =
			 * ic.run("And create", impFluoD, impNoFluo); IJ.run(imp3, "Fill Holes", "");
			 * 
			 * imp3 = ic.run("ADD create", imp3, impFluoD); rm =
			 * AnalyseParticleMethods.analyzeParticlesFluo(imp3); imp3.close();
			 * impFluoD.close(); impNoFluo.close();
			 * 
			 * }
			 * 
			 * }
			 */

			Utils.showResultsAndSave(dir, name, imp, rm, goodRows);
			imp.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Method to detect esferoides.
	public static void detectSpheroidTeniposide(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {

		options.setId(name);

		options.setWindowless(true);
		// Cambiar.
		try {
			options.setId(name);

			ImagePlus[] imps;
			
			
			imps = BF.openImagePlus(options);


			ImagePlus imp;

			if (name.endsWith(".tiff") || name.endsWith(".tif") || name.endsWith(".jpg") || name.endsWith(".JPG")) {
				imp = IJ.openImage(name);
			} else {
				imp = imps[0];
			}

			ImagePlus impD = imp.duplicate();

			DetectSpheroidImageMethods.processSpheroidEdgesThreshold(impD, 22, 255);
			RoiManager rm = AnalyseParticleMethods.analyzeParticlesFluo(impD);

			if (rm != null && rm.getRoisAsArray().length > 0) {
				Utils.keepBiggestROI(rm);
				Roi r = rm.getRoi(0);
				ImageStatistics stats = r.getStatistics();
				double solidity = (stats.area / Utils.getArea(r.getConvexHull()));
				System.out.println(solidity);
				int thesh = 23;
				while (solidity < 0.8 && thesh > 15) {
					System.out.println(thesh);
					thesh--;
					impD = imp.duplicate();
					DetectSpheroidImageMethods.processSpheroidEdgesThresholdDilateErode(impD, thesh, 255);
					rm = AnalyseParticleMethods.analyzeParticlesFluo(impD);
					Utils.keepBiggestROI(rm);
					r = rm.getRoi(0);
					stats = r.getStatistics();
					solidity = (stats.area / Utils.getArea(r.getConvexHull()));
					System.out.println(solidity);
				}
			} else {
				DetectSpheroidImageMethods.processSpheroidEdgesThresholdDilateErode(impD, 22, 255);
				rm = AnalyseParticleMethods.analyzeParticlesFluo(impD);
			}

			imp.close();
			Utils.showResultsAndSave(dir, name, imp, rm, goodRows);
			imp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Method to detect esferoides.
	public static void detectSpheroidDeep(ImporterOptions options, String dir, String name,
			ArrayList<Integer> goodRows) {
		RoiManager rm;

		DetectSpheroidImageMethods.processSpheroidDeep(dir, name);
		ImagePlus imp2 = IJ.getImage();
		ImagePlus imp = imp2.duplicate();
		rm = AnalyseParticleMethods.analyseParticlesTeodora(imp, false, false);

		try {
			Utils.showResultsAndSave(dir, name, imp2, rm, goodRows);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imp2.close();

	}

}
