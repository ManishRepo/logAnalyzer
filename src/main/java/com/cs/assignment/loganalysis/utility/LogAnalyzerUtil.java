package com.cs.assignment.loganalysis.utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class to get the application properties
 * @author Manish K Singh
 *
 */
public class LogAnalyzerUtil {
	
	static Properties prop = null;
	static {
		InputStream input = LogAnalyzerUtil.class.getResourceAsStream("/application.properties");
		try {
			prop = new Properties();
			prop.load(input);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get specific property
	 * @param propName
	 * @return key value as string
	 * @throws IOException
	 */
	public static String getProperty(String propName) throws IOException {
		return (String) prop.get(propName);
	}

}
