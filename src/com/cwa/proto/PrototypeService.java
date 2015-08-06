package com.cwa.proto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import serverice.proto.ChangeProtoInfo;
import serverice.proto.ProtoEvent;
import serverice.proto.ProtoInfo;
import baseice.event.IEventListenerPrx;
import baseice.service.FunctionTypeEnum;

import com.cwa.component.functionmanage.IFunctionCluster;
import com.cwa.component.functionmanage.IFunctionService;
import com.cwa.proto.cache.AllPrototype;
import com.cwa.proto.util.Excel2Json;
import com.cwa.service.context.IGloabalContext;
import com.cwa.util.FileUtil;
import com.cwa.util.prototype.StringUtil;

public class PrototypeService implements IPrototypeService {
	protected static final Logger logger = LoggerFactory.getLogger(PrototypeService.class);

	private IGloabalContext gloabalContext;

	private int version;
	private ConcurrentHashMap<Integer, List<String>> versionChangeMap = new ConcurrentHashMap<Integer, List<String>>();// 每个版本改变的条目
	private AllPrototype allPrototype;

	private String path = System.getProperty("user.dir") + "//version//";

	private Map<Integer, Set<Integer>> regionModuleMap = new HashMap<Integer, Set<Integer>>();

	@Override
	public void startup(IGloabalContext gloabalContext) throws Exception {
		this.gloabalContext = gloabalContext;
		// 从文件读取版本号，以及版本修改信息
		readFromFile();
	}

	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * 模块来拿原型
	 */
	@Override
	public ProtoInfo getPrototype(ChangeProtoInfo info) {
		int clientVersion = info.version;// 客户端版本号
		ProtoInfo protoInfo = new ProtoInfo();
		if (clientVersion >= version) {
			protoInfo.version = version;
			return protoInfo;
		}

		List<String> protoNameList = info.protoNameList;
		Map<String, Map<Integer, byte[]>> map = new HashMap<String, Map<Integer, byte[]>>();
		for (String protoName : protoNameList) {
			if (allPrototype.getAllProtoMap().containsKey(protoName)) {
				map.put(protoName, allPrototype.getAllProtoMap().get(protoName));
			}
		}
		protoInfo.map = map;
		protoInfo.version = version;
		return protoInfo;
	}

	/**
	 * 原型改变
	 * 
	 * @param changeNameList
	 *            excel表名
	 */
	public synchronized void protoChange(List<String> changeNameList) {
		version++;
		// 修改缓存的数据信息
		List<String> list = changeDate(changeNameList);
		// list为原型表名
		versionChangeMap.put(version, list);
		// 将版本号与版本修改信息写入到文件
		write2File();
		// 发送事件
		sendEvent();
	}

	/**
	 * 从文件读取
	 */
	public void readFromFile() {
		BufferedReader br = null;
		try {
			File f = new File(path + "version.json");
			File fr = new File(path + "versionChangeMap.json");
			if (!f.exists() || !fr.exists()) {
				return;
			}
			version = Integer.parseInt(FileUtils.readFileToString(f));
			br = new BufferedReader(new FileReader(fr));
			String str = null;
			while ((str = br.readLine()) != null) {
				String[] strs = str.split(":");
				if (strs.length > 1) {
					String s = strs[1].substring(1, strs[1].length() - 1);
					String[] args = s.split(",");
					List<String> list = new ArrayList<String>();
					for (String arg : args) {
						list.add(arg);
					}
					versionChangeMap.put(Integer.parseInt(strs[0]), list);
				}
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * 写到文件中
	 */
	public void write2File() {
		FileWriter fw = null;
		FileUtil.write2File("version", String.valueOf(version), path);
		try {
			String line = System.getProperty("line.separator");
			StringBuffer str = new StringBuffer();
			fw = new FileWriter(path + "versionChangeMap.json", true);
			str.append(version + ":" + versionChangeMap.get(version)).append(line);
			fw.write(str.toString());
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}

	public List<String> changeDate(List<String> changeNameList) {// 修改缓存的数据信息
		List<String> list = Excel2Json.execute(changeNameList, "createdJson/", "com.cwa.prototype", "excel/");
		allPrototype.execute(list);
		return list;
	}

	public void sendEvent() {// 发送事件
		ProtoEvent event = new ProtoEvent();
		event.changeInfo = new ChangeProtoInfo();
		event.changeInfo.version = version;
		event.changeInfo.protoNameList = versionChangeMap.get(version);

		IFunctionService functionService = gloabalContext.getCurrentFunctionService();
		// 发送
		for (Integer iRegion : regionModuleMap.keySet()) {
			for (Integer iModule : regionModuleMap.get(iRegion)) {
				IFunctionCluster functionCluster = functionService.getFunctionCluster(iRegion, FunctionTypeEnum.valueOf(iModule));
				IEventListenerPrx prx = functionCluster.getMasterService(IEventListenerPrx.class);
				prx.answer(event);
			}
		}
	}

	@Override
	public ChangeProtoInfo checkPrototype(ChangeProtoInfo info, int iRegion, int iModule) {
		if (regionModuleMap.get(iRegion) == null) {
			Set<Integer> set = new HashSet<Integer>();
			set.add(iModule);
			regionModuleMap.put(iRegion, set);
		} else {
			regionModuleMap.get(iRegion).add(iModule);
		}

		ChangeProtoInfo retInfo = new ChangeProtoInfo();
		int v = info.version;
		if (v < version) {
			Set<String> set = new HashSet<String>();
			while (version - v > 0) {
				v++;
				for (String s : versionChangeMap.get(v)) {
					set.add(s.trim());
				}
			}
			List<String> names = new ArrayList<String>();
			for (String name : info.protoNameList) {
				name = StringUtil.upperFirstString(name);
				if (set.contains(name)) {
					names.add(name);
				}
			}
			retInfo.version = version;
			retInfo.protoNameList = names;
		}
		return retInfo;
	}

	// ----------------------------------------
	public void setAllPrototype(AllPrototype allPrototype) {
		this.allPrototype = allPrototype;
	}
}
