package com.cwa.proto.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.cwa.component.prototype.IPrototype;
import com.cwa.util.prototype.StringUtil;

public class ExcelUtil {

	/**
	 * 读取excel生成javaBean，返回bean的数组
	 * 
	 * @throws Exception
	 */
	public static Map<String, List<Object>> readExcel2Bean(String excelPath, String javaPath) throws Exception {
		InputStream instream = new FileInputStream(excelPath);
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook(instream);
		Map<String, List<Object>> sheetNameSheet = new HashMap<String, List<Object>>();

		List<CatalogueBean> cataList = new ArrayList<CatalogueBean>();
		cataList = ExcelUtil.beforeRead(excelPath);
		// 循环工作表Sheet
		for (int numSheet = 1; numSheet < cataList.size() + 1; numSheet++) {
			CatalogueBean cataBean = cataList.get(numSheet - 1);

			List<Object> list = new ArrayList<Object>();
			HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
			if (hssfSheet == null) {
				continue;
			}
			// 获取hssfSheet的名字当类名字
			String className = StringUtil.upperFirstString(hssfSheet.getSheetName()) + "Prototype";
			// 获取第一行得到类型行
			HSSFRow typeRow = hssfSheet.getRow(13);
			// 循环行Row
			for (int rowNum = 16; rowNum <= cataBean.getLastRow() - 1; rowNum++) {
				HSSFRow hssfRow = hssfSheet.getRow(rowNum);
				if (hssfRow == null) {
					continue;
				}
				// 一个构造函数的参数列表
				Object[] classParas = new Object[cataBean.getLastCell() + 1];
				// 循环列
				for (int cellNum = 0; cellNum < cataBean.getLastCell() + 1; cellNum++) {
					// 得到每行对应的类型
					HSSFCell typeCell = typeRow.getCell(cellNum);
					String type = String.valueOf(typeCell.getStringCellValue());

					try {
						classParas[cellNum] = (Object) getValue(hssfRow.getCell(cellNum), type);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(className + "出错" + (rowNum + 1) + "," + (cellNum + 1));
						return null;
					}
				}
				IPrototype obj = getInstance(javaPath + "." + className, classParas);
				list.add(obj);
			}
			sheetNameSheet.put(className, list);
		}
		return sheetNameSheet;
	}

	/**
	 * 得到Excel表中的值（readExcel2Bean的辅助方法）
	 * 
	 */

	public static Object getValue(HSSFCell hssfCell, String type) throws Exception {
		if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
			throw new Exception("值填空");
		}
		if (type.equals("String")) {
			// 返回字符串类型的值
			try {
				return String.valueOf(hssfCell.getStringCellValue());
			} catch (IllegalStateException e) {
				Object o = hssfCell.getNumericCellValue();
				String str = o.toString();
				String str1 = str.replaceAll(".0", "");
				return str1;
			}
		} else if (type.equals("int")) {
			try {
				// 返回数值类型的值
				int i = (int) hssfCell.getNumericCellValue();
				return i;
			} catch (IllegalStateException e) {
				String str = String.valueOf(hssfCell.getStringCellValue());
				int i = Integer.parseInt(str);
				return i;
			}
		} else if (type.equals("List<Integer>")) {
			try {
				String str = hssfCell.getStringCellValue();
				// list类型
				String num[] = str.split(",");
				List<Integer> numList = new ArrayList<Integer>();
				for (int i = 0; i < num.length; i++) {
					numList.add(Integer.parseInt(num[i]));
				}
				return numList;
			} catch (IllegalStateException e) {
				int i = (int) hssfCell.getNumericCellValue();
				List<Integer> numList = new ArrayList<Integer>();
				numList.add(i);
				return numList;
			}

		}  else if (type.equals("float")) {
			float num = (float) hssfCell.getNumericCellValue();
			return num;
		} else if (type.equals("List<String>")){ 
			try {
				String str = hssfCell.getStringCellValue();
				// list类型
				String num[] = str.split(",");
				List<String> numList = new ArrayList<String>();
				for (int i = 0; i < num.length; i++) {
					numList.add(num[i]);
				}
				return numList;
			} catch (IllegalStateException e) {
				int i = (int) hssfCell.getNumericCellValue();
				List<Integer> numList = new ArrayList<Integer>();
				numList.add(i);
				return numList;
			}
		}else {
			throw new Exception("类型不对");
		}

	}

	/**
	 * 通过类名和构造参数列表，通过反射返回一个对象（readExcel2Bean的辅助方法）
	 */
	public static <T> T getInstance(String name, Object paras[]) throws Exception {
		Class protoClass = Class.forName(name);
		int len = paras.length;
		Class[] classParas = new Class[len];
		for (int i = 0; i < len; ++i) {
			if (paras[i].getClass().equals(Integer.class)) {
				classParas[i] = int.class;
			} else if (paras[i].getClass().equals(ArrayList.class)) {
				classParas[i] = List.class;
			} else if (paras[i].getClass().equals(Long.class)) {
				classParas[i] = long.class;
			} else
				classParas[i] = paras[i].getClass();// 返回类信息
		}
		Constructor con = protoClass.getConstructor(classParas);
		T proto = (T) con.newInstance(paras);// 传入当前构造函数要的参数列表
		return proto;// 返回这个用Object引用的对象
	}

	/**
	 * read 的辅助方法
	 * 
	 * @param path
	 * @param excelPath
	 * @return
	 * @throws Exception
	 */
	public static List<CatalogueBean> beforeRead(String exelPath) throws Exception {
		InputStream instream = new FileInputStream(exelPath);
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook(instream);

		// 循环工作表Sheet

		HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
		if (hssfSheet == null || !(hssfSheet.getSheetName().equals("catalogue"))) {
			System.out.println("没有 catalogue");
		}
		List<CatalogueBean> cataList = new ArrayList<CatalogueBean>();
		// 循环行
		for (int rowNum = 1; rowNum < hssfSheet.getPhysicalNumberOfRows(); rowNum++) {
			HSSFRow row = hssfSheet.getRow(rowNum);
			// 得到每行对应的名字
			HSSFCell cell = row.getCell(0);
			String sheetName = String.valueOf(cell.getStringCellValue());
			// 得到每行对应的开始列
			cell = row.getCell(1);
			String firstCell = String.valueOf(cell.getStringCellValue());
			// 得到每行对应的结束列
			cell = row.getCell(2);
			String lastCell = String.valueOf(cell.getStringCellValue());
			// 得到每行对应的类型行的行号
			cell = row.getCell(3);
			int typeRowNum = (int) cell.getNumericCellValue();
			// 得到每行对应的开始行
			cell = row.getCell(4);
			int firstRow = (int) cell.getNumericCellValue();
			// 得到每行对应的结束行
			cell = row.getCell(5);
			int lastRow = (int) cell.getNumericCellValue();
			// 封装了catalogue的信息
			CatalogueBean catalogueBean = new CatalogueBean(sheetName, letterToNum(firstCell), letterToNum(lastCell), typeRowNum, firstRow, lastRow);
			cataList.add(catalogueBean);
		}
		return cataList;
	}

	/**
	 * read的辅助方法
	 * 
	 * @param javaType
	 * @return
	 */
	public static String getType(String javaType) {
		if (javaType.equals("String")) {
			return "string";
		} else if (javaType.equals("int")) {
			return "int32";
		} else if (javaType.equals("float")) {
			return "float";
		} else if (javaType.equals("List<Integer>")) {
			return "int32";
		} else {
			return "string";
		}
	}

	// 将字母转换成数字_1
	public static int letterToNum(String input) {
		String reg = "[a-zA-Z]";
		StringBuffer strBuf = new StringBuffer();
		input = input.toLowerCase();
		if (null != input && !"".equals(input)) {
			for (char c : input.toCharArray()) {
				if (String.valueOf(c).matches(reg)) {
					strBuf.append(c - 97);
				} else {
					strBuf.append(c);
				}
			}
			return Integer.parseInt(strBuf.toString());
		} else {
			return Integer.parseInt(input);
		}
	}
}