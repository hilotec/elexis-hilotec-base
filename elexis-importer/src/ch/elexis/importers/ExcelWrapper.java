/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *  $Id: ExcelWrapper.java 3195 2007-09-24 14:57:25Z rgw_ch $
 *******************************************************************************/

package ch.elexis.importers;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A Class that wraps a Microsoft(tm) Excel(tm) Spreadsheet using
 * Apache's HSSF (Horrible Spread Sheet Format) as used in Excel
 * 97 thru 2002.
 * This class simplifies POI in that it gives only read access and only
 * for string data. Refernces to cells containing non-string-values will
 * try to return an appropriate conversion to String.
 * 
 * @author Gerry
 *
 */
public class ExcelWrapper {
	POIFSFileSystem fs;
	HSSFSheet sheet;
	
	/**
	 * Load a specific page of the given Excel Spreadsheet
	 * @param file filename of the Excel file 
	 * @param page page to use
	 * @return true on success
	 */
	public boolean load(final String file,final int page){
		try{
			fs=new POIFSFileSystem(new FileInputStream(file));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			sheet = wb.getSheetAt(page);
			return true;
		}catch(Exception ex){
			return false;
		}
	}
	
	/**
	 * Return a row of data from the sheet.
	 * @param rowNr zero based index of the desired row
	 * @return a List of Strings with the row values or
	 * null if no such row exists.
	 */
	@SuppressWarnings("unchecked")
	public List<String> getRow(final int rowNr){
		HSSFRow row = sheet.getRow(rowNr);
		if(row==null){
			return null;
		}
		ArrayList<String> ret=new ArrayList<String>();
		short first=row.getFirstCellNum();
		short last=row.getLastCellNum();
		for(short i=first;i<last;i++){
			HSSFCell cell=row.getCell(i);
			if (cell != null) {
				switch(cell.getCellType()){
				case HSSFCell.CELL_TYPE_BLANK: ret.add(""); break;
				case HSSFCell.CELL_TYPE_BOOLEAN: ret.add(Boolean.toString(cell.getBooleanCellValue())); break; 
				case HSSFCell.CELL_TYPE_NUMERIC:
				case HSSFCell.CELL_TYPE_FORMULA:
					ret.add(Double.toString(cell.getNumericCellValue())); break;
				case HSSFCell.CELL_TYPE_STRING: ret.add(cell.getStringCellValue()); break;
				default: ret.add("unknown cell type");
				}
			} else {
				// empty cell
				ret.add("");
			}
		}
		return ret;
	}
	/**
	 * return the index of the first row containing data
	 * @return
	 */
	public int getFirstRow(){
		return sheet.getFirstRowNum();
	}
	
	/**
	 * return the index of the last row containing data
	 * @return
	 */
	public int getLastRow(){
		return sheet.getLastRowNum();
	}
}
