package spheroidj;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;

public class AnalyseParticleMethods {

	public static RoiManager analyzeParticlesFluo(ImagePlus imp2) {
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

	public static RoiManager analyzeParticlesHector(ImagePlus imp2) {
		IJ.run(imp2, "Analyze Particles...", "size=0.01-Infinity circularity=0.15-2.00 show=Outlines exclude add");
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

	public static RoiManager analyseParticlesTeodora(ImagePlus imp2, boolean blackHole, boolean exclude) {
		if (blackHole) {
			IJ.run(imp2, "Analyze Particles...", "size=20000-Infinity circularity=0.5-1.00 show=Outlines exclude add");
		} else {
			if (exclude) {
				IJ.run(imp2, "Analyze Particles...",
						"size=20000-Infinity circularity=0.15-1.00 show=Outlines exclude add");
			}else {
				IJ.run(imp2, "Analyze Particles...",
						"size=20000-Infinity circularity=0.00-1.00 show=Outlines add");
			}
		}

		ImagePlus imp3 = IJ.getImage();
		imp2.close();
		imp3.close();
		// imp2.show();
		RoiManager rm = RoiManager.getInstance();
		if (rm != null) {
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

}
