package com.cwa.proto.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwa.component.prototype.IPrototype;
import com.cwa.util.prototype.JsonUtil;
import com.cwa.util.prototype.StringUtil;

public class AllPrototype {
	protected static final Logger logger = LoggerFactory.getLogger(AllPrototype.class);

	private String separator = File.separator;
	private Map<String, Map<Integer, byte[]>> allProtoMap = new HashMap<String, Map<Integer, byte[]>>();
	private String workspacePath;

	public AllPrototype() {
		try {
			File directory = new File("..");// 设定为当前文件夹
			workspacePath = directory.getCanonicalPath();
			List<String> list = new ArrayList<String>();
			File dir = new File(workspacePath + separator+"cwa2_3002wow_prototype"+ separator+"src"+ separator+"com"+ separator+"cwa"+ separator+"prototype");
			File[] file = dir.listFiles();
			for (int i = 0; i < file.length; i++) {
				if (file[i].getName().endsWith(".java")) {
					String name = file[i].getName();
					String str = name.substring(0, name.lastIndexOf(".java"));
					list.add(str);
				}
			}
			execute(list);
		} catch (IOException e) {
			logger.error("",e);
		}
	}

	@SuppressWarnings("unchecked")
	public void execute(List<String> nameList) {
		for (String name : nameList) {
			String className = "com.cwa.prototype." + name;
			try {
				Class<? extends IPrototype> clazz = (Class<? extends IPrototype>) Class.forName(className);
				Map<Integer, byte[]> protoMap = new HashMap<Integer, byte[]>();
				String fileName = "createdJson//" + clazz.getSimpleName() + ".json";
				File f = new File(fileName);
				StringBuilder fileContent = new StringBuilder();
				List<String> rows = org.apache.commons.io.FileUtils.readLines(f, "utf-8");
				for (String row : rows) {
					fileContent.append(row);
				}
				List<String> jsonList = StringUtil.getJsonList(fileContent.toString());
				for (int j = 0; j < jsonList.size(); j++) {
					IPrototype proto = JsonUtil.transferJsonTOJavaBean(jsonList.get(j), clazz);
					protoMap.put(proto.getKeyId(), proto.toBytes());
				}
				name = name.substring(0, name.indexOf("Prototype"));
				allProtoMap.put(name, protoMap);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	public Map<String, Map<Integer, byte[]>> getAllProtoMap() {
		return allProtoMap;
	}

	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}
}
