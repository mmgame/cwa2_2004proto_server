package com.cwa.proto.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cwa.proto.PrototypeService;
import com.cwa.proto.util.UnZip;


public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(UploadServlet.class);
	
	private PrototypeService prototypeService;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload uploader = new ServletFileUpload(factory);
		List<FileItem> list;
		String filepath = "/";
		try {
			list = uploader.parseRequest(request);
			for (FileItem item : list) {
				if (item.isFormField()) {
					// 处理普通表单字段
					String field = item.getFieldName();// 这个是name
					String value = item.getString("UTF-8");// 这个是name对应的值
					// 对数据进行逻辑处理
				} else {
					// 将文件保存到指定目录
					String fileName = item.getName();// 文件名称
					filepath = "excel/" + fileName;
					item.write(new File(filepath));
				}
			}
			response.getWriter().println("success");
		} catch (Exception e) {
			logger.error("",e);
		}
		List<String> fileNames = UnZip.unZipFiles(filepath, "excel/");
		
		prototypeService.protoChange(fileNames);
	}

	public void setPrototypeService(PrototypeService prototypeService) {
		this.prototypeService = prototypeService;
	}

}