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

	/**
	 * Creates an object that can read Excel .xlsx filetypes. Can be used to read cells using row-col coordinates. In its current implementation, opens the last sheet within the file.
	 * @param excelFilenameWithPath Absolutely path to the file to be opened, including the filename.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    public ExcelReader(String excelFilenameWithPath) throws FileNotFoundException, IOException{
        // An excel file name. You can create a file name with a full
        // path information.
        filename = excelFilenameWithPath;

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
    }
    
    /**
     * Return a string containing the contents of the cell at (row, col).
     * @param row 0-indexed row of the cell you wish to retrieve.
     * @param col 0-indexed column of the cell you wish to retrieve.
     * @return String containing the contents of (row, col).
     */
    public String getValueAt(int row, int col){
    	return sheet.getRow(row).getCell(col).toString();
    }
    
    /**
     * Get the number of rows in the Excel file.
     * @return Number of rows read in.
     */
    public int getRows(){
    	return rows;
    }
    /**
     * Get the number of columns in the Excel file.
     * @return Number of columns read in.
     */
    public int getCols(){
    	return cols;
    }
}