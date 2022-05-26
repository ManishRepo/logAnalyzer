package com.cs.assignment.loganalysis.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to define persistence methods for the persistent layer 
 * @author Manish K Singh
 *
 */
public class LogEventAlertRepository {
	
	private static Logger logger = LoggerFactory.getLogger(LogEventAlertRepository.class);

	private static final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
	
	/**
	 * Method to persist the batch of all the alerts 
	 * @param entity
	 * @return number of alerts persisted
	 */
	public static int persistBatch(List<?> entity) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			for (Object obj : entity) {
				session.save(obj);
				session.flush();
				session.clear();
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			tx.commit();
			session.close();
		}
		return entity.size();
	}
	
	/**
	 * Method to load/get all the domain type T
	 * @param <T>
	 * @param type
	 * @return List of generic domain type T
	 */
	public static <T> List<T> loadAllData(Class<T> type) {
		Session session = sessionFactory.openSession();
	    CriteriaBuilder builder = session.getCriteriaBuilder();
	    CriteriaQuery<T> criteria = builder.createQuery(type);
	    criteria.from(type);
	    List<T> data = session.createQuery(criteria).getResultList();
	    return data;
	  }
	
	/**
	 * Method to get all the alerts that have crossed the threshold
	 * @param <T>
	 * @param type
	 * @return List of generic domain type T
	 */
	public static <T> List<T> loadAllDataWhereAlertTrue(Class<T> type) {
		Session session = sessionFactory.openSession();
	    CriteriaBuilder builder = session.getCriteriaBuilder();
	    CriteriaQuery<T> criteria = builder.createQuery(type);
	    Root<T> root = criteria.from(type);
	    criteria.where(builder.equal(root.get("alert"), true));
	    List<T> data = session.createQuery(criteria).getResultList();
	    return data;
	  }

	/**
	 * Method to get specific domain type T, identified by id
	 * @param <T>
	 * @param type
	 * @param logId
	 * @return Specific domain T
	 */
	public static <T> T getLogAlertByID(Class<T> type,String logId) {
		Session session = sessionFactory.openSession();
	    T logAlert = session.get(type, logId);
	    return logAlert;
	  }


}
