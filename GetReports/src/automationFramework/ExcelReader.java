package automationFramework;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ExcelReader {
	
	private String filename;
	private XSSFWorkbook workbook;
	private int sheetCount;
	private XSSFSheet sheet;
	private int rows;
	private int cols;

    public ExcelReader(String filenamearg) throws FileNotFoundException, IOException{
        // An excel file name. You can create a file name with a full
        // path information.
        filename = filenamearg;

        try (FileInputStream fis = new FileInputStream(filename)) {
            // Create an excel workbook from the file system.
            workbook = new XSSFWorkbook(fis);
            // get last sheet
            sheetCount = workbook.getNumberOfSheets();
            sheet = workbook.getSheetAt(sheetCount-1);
            
            rows = sheet.getLastRowNum();
            cols = sheet.getRow(1).getLastCellNum();
                      
            workbook.close();
            
        }

        //showExcelData(sheetData);
        System.out.println(rows);
        System.out.println(cols);
    }
    
    
    public String getValueAt(int row, int col){
    	return sheet.getRow(row).getCell(col).toString();
    }
    
    public int getRows(){
    	return rows;
    }
    public int getCols(){
    	return cols;
    }
}