package com.cs.assignment.loganalysis.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;


public class LogEventAlertRepository {

	private static final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

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
			e.printStackTrace();
		} finally {
			tx.commit();
			session.close();
		}
		return entity.size();
	}
	
	public static <T> List<T> loadAllData(Class<T> type) {
		Session session = sessionFactory.openSession();
	    CriteriaBuilder builder = session.getCriteriaBuilder();
	    CriteriaQuery<T> criteria = builder.createQuery(type);
	    criteria.from(type);
	    List<T> data = session.createQuery(criteria).getResultList();
	    return data;
	  }
	
	public static <T> List<T> loadAllDataWhereAlertTrue(Class<T> type) {
		Session session = sessionFactory.openSession();
	    CriteriaBuilder builder = session.getCriteriaBuilder();
	    CriteriaQuery<T> criteria = builder.createQuery(type);
	    Root<T> root = criteria.from(type);
	    criteria.where(builder.equal(root.get("alert"), true));
	    List<T> data = session.createQuery(criteria).getResultList();
	    return data;
	  }



}
