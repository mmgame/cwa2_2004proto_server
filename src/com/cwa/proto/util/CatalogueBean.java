package com.cwa.proto.util;

public class CatalogueBean {
	String sheetName;
	int firstCell;
	int lastCell;
	int typeRow;
	int firstRow;
	int lastRow;

	public CatalogueBean(String sheetName, int firstCell, int lastCell, int typeRow, int firstRow, int lastRow) {
		super();
		this.sheetName = sheetName;
		this.firstCell = firstCell;
		this.lastCell = lastCell;
		this.typeRow = typeRow;
		this.firstRow = firstRow;
		this.lastRow = lastRow;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public int getFirstCell() {
		return firstCell;
	}

	public void setFirstCell(int firstCell) {
		this.firstCell = firstCell;
	}

	public int getLastCell() {
		return lastCell;
	}

	public void setLastCell(int lastCell) {
		this.lastCell = lastCell;
	}

	public int getTypeRow() {
		return typeRow;
	}

	public void setTypeRow(int typeRow) {
		this.typeRow = typeRow;
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public int getLastRow() {
		return lastRow;
	}

	public void setLastRow(int lastRow) {
		this.lastRow = lastRow;
	}

}
