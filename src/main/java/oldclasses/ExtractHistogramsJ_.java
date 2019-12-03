package oldclasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import esferoides.Utils;
import ij.ImagePlus;
import ij.io.DirectoryChooser;
import ij.process.ImageProcessor;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

//@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Esferoids>Extract Histograms")
public class ExtractHistogramsJ_ implements Command {

	@Parameter
	private int bins = 8;

	@Override
	public void run() {

		ImporterOptions options;
		try {
			options = new ImporterOptions();

			options.setWindowless(true);

			// We ask the user for a directory with nd2 images.
			DirectoryChooser dc = new DirectoryChooser("Select the folder containing the nd2 images");
			String dir = dc.getDirectory();

			File folder = new File(dir);
			List<String> result = new ArrayList<String>();
			Utils.search(".*\\.nd2", folder, result);

			ArrayList<ArrayList<Integer>> results = new ArrayList<>();

			String filename = "histograms.xls";

			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("Results");

			HSSFRow row;
			int k=0;

			for (String name : result) {
				options.setId(name);

				ImagePlus[] imps = BF.openImagePlus(options);
				ImagePlus imp = imps[0];
				ImageProcessor ip = imp.getProcessor();
				int[] histogram = ip.getHistogram();
				row = sheet.createRow((short) k);
				k++;
				row.createCell((short) 0).setCellValue(name.substring(name.lastIndexOf("/")+1));
				for (int i = 0; i < bins; i++) {
					int countpixels = 0;
					for (int j = 0; j < 16384/bins; j++) {
						countpixels = countpixels + histogram[(16384/bins * i) + j];
					}
					row.createCell((short) i+1).setCellValue(countpixels);
				}
			
			}


			FileOutputStream fileOut;
			try {
				fileOut = new FileOutputStream(filename);
				workbook.write(fileOut);
				fileOut.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
