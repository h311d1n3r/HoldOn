package me.helldiner.holdon.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class Utils {

	public static InputStream loadResource(String res) {
		try {
			if(Utils.isRunningFromJar()) {
				return Utils.class.getResourceAsStream(res.substring(1));
			} else {
				return new FileInputStream(new File(res));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static final boolean isRunningFromJar() {
		return Utils.class.getResource("Utils.class").toString().startsWith("jar:");
	}
	
	public static void navToURL(String url) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	
}
