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

public class LogAnalyzerService {

	private static Logger logger = LoggerFactory.getLogger(LogAnalyzerService.class);
	
	List<CompletableFuture<Void>> futureLst = new ArrayList<CompletableFuture<Void>>();

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
	
	private CompletableFuture<Void> persistBatchAsync(List<?> modellst)
			throws InterruptedException, ExecutionException {
		CompletableFuture<Void> persistedData = CompletableFuture.runAsync(() -> {
			logger.info("Execution under :" + Thread.currentThread().getName());
			LogEventAlertRepository.persistBatch(modellst);
			logger.info("Execution under :" + Thread.currentThread().getName()+" completed ....");
		});
		return persistedData;
	}
	
	public List<LogEventPersistModel> getAllLogEvent(){
		return LogEventAlertRepository.loadAllData(LogEventPersistModel.class);
	}
	
	public LogEventPersistModel getLogEvent(String Id) {
		return LogEventAlertRepository.getLogAlertByID(LogEventPersistModel.class, Id);
	}

	public List<CompletableFuture<Void>> getFutureLst() {
		return futureLst;
	}

	public void setFutureLst(List<CompletableFuture<Void>> futureLst) {
		this.futureLst = futureLst;
	}

}
