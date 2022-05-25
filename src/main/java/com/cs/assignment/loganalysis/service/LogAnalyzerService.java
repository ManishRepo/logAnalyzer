package com.cs.assignment.loganalysis.service;

import java.util.ArrayList;
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
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogAnalyzerService {

	private static Logger logger = LoggerFactory.getLogger(LogAnalyzerService.class);

	private static Map<String, LogEventMap> tempMap = new HashMap<String, LogEventMap>();

	public int processLogs(LineIterator lines) {
		List<LogEventPersistModel> modellst = new ArrayList<>();
		List<CompletableFuture<Integer>> futureLst = new ArrayList<CompletableFuture<Integer>>();
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
					if (Math.abs(duration) > 4) {
						persistM.setAlert(true);
					}
					modellst.add(persistM);
					tempMap.remove(mapEvent.getId());
				} else {
					tempMap.put(mapEvent.getId(), mapEvent);
				}

				if (modellst.size() == 2) {
					List<LogEventPersistModel> copyList = new ArrayList<LogEventPersistModel>();
					copyList.addAll(modellst);
					futureLst.add(persistDataWorker(copyList));
					modellst.clear();
				} else if (!lines.hasNext()) {
					List<LogEventPersistModel> copyList = new ArrayList<LogEventPersistModel>();
					copyList.addAll(modellst);
					futureLst.add(persistDataWorker(copyList));
					modellst.clear();
				}

			} catch (JsonEOFException ef) {
				logger.error("Invalid JSON String : " + eventStr);
			} catch (Exception ef) {
				logger.error("Not able to process JSON String : " + eventStr + " with error " + ef.getMessage());
			}
		}
		for (CompletableFuture<?> future : futureLst) {
			try {
				future.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return modellst.size();

	}

	private CompletableFuture<Integer> persistDataWorker(List<?> modellst)
			throws InterruptedException, ExecutionException {
		CompletableFuture<Integer> persistedData = CompletableFuture.supplyAsync(() -> {
			logger.info("Execution under :" + Thread.currentThread().getName());
			LogEventAlertRepository.persistBatch(modellst);
			return modellst.size();
		});
		return persistedData;
	}
	
	public List<LogEventPersistModel> getAllLogEvent(){
		return LogEventAlertRepository.loadAllData(LogEventPersistModel.class);
	}

}
