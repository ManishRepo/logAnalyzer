package com.cs.assignment.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.cs.assignment.loganalysis.data.LogEventPersistModel;
import com.cs.assignment.loganalysis.service.LogAnalyzerService;

public class LogAlertTest {
	
	@Test
	public void validatePersistanceLog() throws InterruptedException, ExecutionException {
		List<LogEventPersistModel> modellst = new ArrayList<>();
		LogAnalyzerService serviceLog = new LogAnalyzerService();
		LogEventPersistModel persistM = new LogEventPersistModel();
		persistM.setId("temp-log-id");
		persistM.setAlert(false);
		persistM.setDuration(2);
		persistM.setHost("test-server");
		persistM.setType("application-log");
		
		modellst.add(persistM);
		
		serviceLog.callWorkerToPersistData(modellst);
		
		serviceLog.getFutureLst().get(0).get();
		
		LogEventPersistModel persistedModel = serviceLog.getLogEvent(persistM.getId());
		
		assertTrue(persistedModel.getId().equals(persistM.getId()));
	}

}
