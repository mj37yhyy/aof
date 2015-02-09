package autonavi.online.framework.support.hibernate;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;

/**
 * hibernate的方法封装，没啥好说的
 * 
 * @author jia.miao
 * 
 */
public class HibernateSupport {

	public Serializable save(Object entity) {
		Session session = HibernateSessionFactory.getSession();
		// LobHelper lobHelper = session.getLobHelper();
		return session.save(entity);
	}

	public Serializable save(String entityName, Object entity) {
		Session session = HibernateSessionFactory.getSession();
		return session.save(entityName, entity);
	}

	public void saveOrUpdate(Object entity) {
		Session session = HibernateSessionFactory.getSession();
		session.saveOrUpdate(entity);
	}

	public void saveOrUpdate(String entityName, Object entity) {
		Session session = HibernateSessionFactory.getSession();
		session.saveOrUpdate(entityName, entity);
	}

	public void update(Object entity) {
		Session session = HibernateSessionFactory.getSession();
		session.update(entity);
	}

	public void update(String entityName, Object entity) {
		Session session = HibernateSessionFactory.getSession();
		session.update(entityName, entity);
	}

	public void delete(Object entity) {
		Session session = HibernateSessionFactory.getSession();
		session.delete(entity);
	}

	public void delete(String entityName, Object entity) {
		Session session = HibernateSessionFactory.getSession();
		session.delete(entityName, entity);
	}

	public <T> List<T> select(Object entity, Class<T> clazz) {
		Session session = HibernateSessionFactory.getSession();
		return session.createCriteria(clazz).add(Example.create(entity)).list();
	}

	public List<?> select(DetachedCriteria query) {
		Session session = HibernateSessionFactory.getSession();
		return query.getExecutableCriteria(session).list();
	}

	public List<?> select(DetachedCriteria query, int fetchSize,
			int firstResult, int maxResults) {
		Session session = HibernateSessionFactory.getSession();
		Criteria criteria = query.getExecutableCriteria(session);
		if (fetchSize > 0)
			criteria.setFetchSize(fetchSize);
		if (firstResult > 0)
			criteria.setFirstResult(firstResult);
		if (maxResults > 0)
			criteria.setMaxResults(maxResults);
		return criteria.list();
	}

	public List<?> selectHQL(String hql) {
		Session session = HibernateSessionFactory.getSession();
		return session.createQuery(hql).list();
	}

	public List<?> selectSQL(String sql) {
		Session session = HibernateSessionFactory.getSession();
		return session.createSQLQuery(sql).list();
	}
}
