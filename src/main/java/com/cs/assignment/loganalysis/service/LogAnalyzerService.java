package com.cs.assignment.loganalysis.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.assignment.loganalysis.data.LogEventAlertRepository;
import com.cs.assignment.loganalysis.data.LogEventMap;
import com.cs.assignment.loganalysis.data.LogEventModel;
import com.cs.assignment.loganalysis.data.LogEventPersistModel;
import com.cs.assignment.loganalysis.utility.LogAnalyzerUtil;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Log service with non-blocking batch based processing to persist alerts.
 * @author Manish K Singh
 *
 */
public class LogAnalyzerService {

	private static Logger logger = LoggerFactory.getLogger(LogAnalyzerService.class);
	
	List<CompletableFuture<Void>> futureLst = new ArrayList<CompletableFuture<Void>>();
	/**
	 * Method to process log lines as JSON String in a Async way. Calculate the duration and add the log event to the batch list.
	 * The batch selects the object from list and calls the async non-blocking thread to persist the batch.
	 * @param lines
	 * @return Number of alerts generated
	 */
	
	public int processLogs(LineIterator lines) {
		List<LogEventPersistModel> modellst = new ArrayList<>();
		Map<String, LogEventMap> tempMap = new HashMap<String, LogEventMap>();
		
		while (lines.hasNext()) {
			String eventStr = lines.nextLine();
			try {
				LogEventModel lmd = new ObjectMapper().readValue(eventStr, LogEventModel.class);
				LogEventMap mapEvent = new LogEventMap();
				BeanUtils.copyProperties(mapEvent, lmd);
				if (tempMap.containsKey(mapEvent.getId())) {
					LogEventMap tmpMapEvent = tempMap.get(mapEvent.getId());
					long duration = mapEvent.getTimestamp() - tmpMapEvent.getTimestamp();
					LogEventPersistModel persistM = new LogEventPersistModel();
					BeanUtils.copyProperties(persistM, lmd);
					persistM.setDuration(Math.abs(duration));
					if (Math.abs(duration) > Integer.parseInt(LogAnalyzerUtil.getProperty("alert_threshold"))) {
						persistM.setAlert(true);
					}
					modellst.add(persistM);
					tempMap.remove(mapEvent.getId());
				} else {
					tempMap.put(mapEvent.getId(), mapEvent);
				}

				if (modellst.size() == Integer.parseInt(LogAnalyzerUtil.getProperty("batch_size"))) {
					callWorkerToPersistData(modellst);
				} else if (!lines.hasNext()) {
					callWorkerToPersistData(modellst);
				}

			} catch (JsonEOFException ef) {
				logger.error("Invalid JSON String : " + eventStr);
			} catch (Exception ef) {
				logger.error("Not able to process JSON String : " + eventStr + " with error " + ef.getMessage());
			}
		}
		return modellst.size();

	}	
	
	/**
	 * A method copies the batch list and calls the non-blocking persist process and clears the initial list for further batches
	 * @param modellst
	 */
	@SuppressWarnings("unchecked")
	public void callWorkerToPersistData(List<?> modellst) {
		List<LogEventPersistModel> copyList = new ArrayList<LogEventPersistModel>();
		try {
			copyList.addAll((Collection<? extends LogEventPersistModel>) modellst);
			futureLst.add(persistBatchAsync(copyList));
			modellst.clear();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Method that returns the future to persist the batch to database
	 * @param modellst
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private CompletableFuture<Void> persistBatchAsync(List<?> modellst)
			throws InterruptedException, ExecutionException {
		CompletableFuture<Void> persistedData = CompletableFuture.runAsync(() -> {
			logger.info("Execution under :" + Thread.currentThread().getName());
			LogEventAlertRepository.persistBatch(modellst);
			logger.info("Execution under :" + Thread.currentThread().getName()+" completed ....");
		});
		return persistedData;
	}
	
	/**
	 * Method to return all the log events on the persistent storage
	 * @return List of LogEventPersistModel
	 */
	public List<LogEventPersistModel> getAllLogEvent(){
		return LogEventAlertRepository.loadAllData(LogEventPersistModel.class);
	}
	
	/**
	 * Method to return specific log event, identified by input id
	 * @param Id
	 * @return LogEventPersistModel
	 */
	public LogEventPersistModel getLogEvent(String Id) {
		return LogEventAlertRepository.getLogAlertByID(LogEventPersistModel.class, Id);
	}

	/**
	 * Bean method to get the future list of all the async threads initiated.
	 * @return
	 */
	public List<CompletableFuture<Void>> getFutureLst() {
		return futureLst;
	}

}
