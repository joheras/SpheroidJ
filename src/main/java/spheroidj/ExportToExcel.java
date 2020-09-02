package spheroidj;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



import ij.measure.ResultsTable;
import org.apache.poi.hssf.usermodel.*;

public class ExportToExcel {
	
	private ResultsTable rt;
	private String dir;
	
	
	public ExportToExcel(ResultsTable rt, String dir) {
		super();
		this.rt = rt;
		this.dir = dir;
	}
	
	
	public void convertToExcel() {
		
		String filename = this.dir + "results.xls";
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Results");
//		HSSFCellStyle style = workbook.createCellStyle();
//		style.setFillBackgroundColor(IndexedColors.ROYAL_BLUE.getIndex());
//		CellStyle style2 = workbook.createCellStyle();
//		style.setFillBackgroundColor(IndexedColors.LIGHT_BLUE.getIndex()); 
		
		HSSFRow rowhead = sheet.createRow((short) 0);
		
		String[] headings = this.rt.getHeadings();
		for(int i=0;i<headings.length;i++) {
			rowhead.createCell((short) i).setCellValue(headings[i]);
		}
		
		int rows = this.rt.getCounter();
		
		HSSFRow row;
		for(int i=0;i<rows;i++) {
			row = sheet.createRow((short) i+1);
			String[] rowi =  this.rt.getRowAsString(i).split("\\t");
			for(int j=1;j<=headings.length;j++) {
				
				row.createCell((short) j-1).setCellValue(rowi[j]);
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
		
		
		
		
		
	}
	
	
	
	
	
	

}
