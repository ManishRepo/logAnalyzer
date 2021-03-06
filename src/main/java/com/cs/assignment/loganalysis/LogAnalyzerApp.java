package com.cs.assignment.loganalysis;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.assignment.loganalysis.data.LogEventPersistModel;
import com.cs.assignment.loganalysis.service.LogAnalyzerService;
import com.cs.assignment.loganalysis.utility.LogAnalyzerUtil;

/**
 * The Log Analyzer Application to process the log file using a service class.
 * The Class validates the ingestion by getting all the log events persisted and counts the size.
 * @author Manish K Singh
 *
 */
public class LogAnalyzerApp {

	private static Logger logger = LoggerFactory.getLogger(LogAnalyzerApp.class);

	private static LogAnalyzerService serviceLog = new LogAnalyzerService();

	public static void main(String... args) {
		logger.info("Starting the log analysis application ....");

		logger.info("Read the log files to feed in events ...");

		try {
			LineIterator lines = FileUtils.lineIterator(new File(LogAnalyzerApp.class.getResource(LogAnalyzerUtil.getProperty("log_file")).toURI()));
			serviceLog.processLogs(lines);
			List<LogEventPersistModel> persistRows = serviceLog.getAllLogEvent();
			logger.info("Total alerts generated "+persistRows.size());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Finishing the log analysis application ....");

	}

}
