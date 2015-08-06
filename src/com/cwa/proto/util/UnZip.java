package com.cwa.proto.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {

	public static void main(String args[]) {

		unZipFiles("E:/r1.zip", "E:/");

	}

	/**
	 * 解压文件到指定目录
	 * 
	 * @param zipFile
	 * @param descDir
	 */
	@SuppressWarnings("rawtypes")
	public static List<String> unZipFiles(String zipPath, String descDir) {
		List<String> fileNames = new ArrayList<String>();
		try {
			File zipFile = new File(zipPath);
			File pathFile = new File(descDir);
			if (!pathFile.exists()) {
				pathFile.mkdirs();
			}
			ZipFile zip = new ZipFile(zipFile);

			for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String zipEntryName = entry.getName();

				InputStream in = zip.getInputStream(entry);
				String path = (descDir + zipEntryName).replaceAll("\\*", "/");

				String sr[] = path.split("/");
				// 将压缩文件中的文件夹名去掉
				String outPath = sr[0] + "/" + sr[2];
				fileNames.add(sr[2]);
				// 判断路径是否存在,不存在则创建文件路径
				File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
				if (!file.exists()) {
					file.mkdirs();
				}
				// 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
				if (new File(outPath).isDirectory()) {
					continue;
				}
				// 输出文件路径信息
				System.out.println(outPath);

				OutputStream out = new FileOutputStream(outPath);
				byte[] buf1 = new byte[1024];
				int len;
				while ((len = in.read(buf1)) > 0) {
					out.write(buf1, 0, len);
				}
				in.close();
				out.close();
			
			}
			zip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("******************解压完毕********************");
		return fileNames;
	}
}
