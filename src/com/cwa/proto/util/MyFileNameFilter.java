package com.cwa.proto.util;

import java.io.File;
import java.io.FilenameFilter;

public class MyFileNameFilter implements FilenameFilter {

	public boolean accept(File dir, String name) {
		if (name.contains(".xls")) {
			return true;
		}
		return false;
	}
}