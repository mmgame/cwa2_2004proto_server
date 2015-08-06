package com.cwa.proto.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwa.component.prototype.IPrototype;
import com.cwa.util.FileUtil;
import com.cwa.util.prototype.JsonUtil;

public class Excel2Json {
	protected static final Logger logger = LoggerFactory.getLogger(Excel2Json.class);
	//excel列表，  json生成路径，包装类路径，excel目录
	public static List<String> execute(List<String> nameList,String jsonFolderPath,String javaPath,String excelFolderPath) {
		List<String> retList = new ArrayList<String>();
		try {
			nameList.remove("enum.xls");
			String excelPath = null;
			logger.info("将要读取的Excel的文件数目是" + nameList.size());
			for (String className : nameList) {
				excelPath = excelFolderPath + className;
				logger.info("excel name  " + excelPath);
				// 读取excel生成javaBean，返回bean的数组
				Map<String, List<Object>> sheetNameSheet = ExcelUtil.readExcel2Bean(excelPath, javaPath);
				if (sheetNameSheet == null) {
					return null;
				}
				for (String sheetName : sheetNameSheet.keySet()) {
					List<Object> list = sheetNameSheet.get(sheetName);
					// 将javaBean转化成正常类型的json
					List<String> jsonList = new ArrayList<String>();
					for (int j = 0; j < list.size(); j++) {
						IPrototype pri = (IPrototype) list.get(j);
						String str = JsonUtil.transferJavaBeanToJson(pri);
						jsonList.add(str);
					}
					String normalJson = toString(jsonList);
					logger.info(sheetName + ":" + "\n" + normalJson);
					// 将正常json写进文件
					FileUtil.write2File(sheetName, normalJson, jsonFolderPath);
					retList.add(sheetName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retList;
	}

	private static String toString(List<String> list) {
		Iterator<String> it = list.iterator();
		if (!it.hasNext())
			return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (;;) {
			String e = it.next();
			sb.append(e);
			if (!it.hasNext())
				return sb.append(']').toString();
			sb.append(',').append(' ');
			sb.append("\r\n");
		}
	}
}