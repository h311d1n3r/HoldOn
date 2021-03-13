package me.helldiner.holdon.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

	public static InputStream loadResource(String res) {
		try {
			if(Utils.isRunningFromJar()) {
				return Utils.class.getResourceAsStream(res);
			} else {
				return new FileInputStream(new File(res));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static final boolean isRunningFromJar() {
		return Utils.class.getResource("Utils.class").toString().startsWith("jar:");
	}
	
}
