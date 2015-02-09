package autonavi.online.framework.support.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * Configures and provides access to Hibernate sessions, tied to the current
 * thread of execution. Follows the Thread Local Session pattern, see
 * {@link http://hibernate.org/42.html }.
 */
public class HibernateSessionFactory {

	/**
	 * Location of hibernate.cfg.xml file. Location should be on the classpath
	 * as Hibernate uses #resourceAsStream style lookup for its configuration
	 * file. The default classpath location of the hibernate config file is in
	 * the default package. Use #setConfigFile() to update the location of the
	 * configuration file for the current session.
	 */
	private static org.hibernate.SessionFactory sessionFactory;

	private static Configuration configuration = new Configuration();
	private static ServiceRegistry serviceRegistry;

	private HibernateSessionFactory() {
	}

	static void init() {

		try {
			serviceRegistry = new StandardServiceRegistryBuilder()
					.applySettings(configuration.getProperties()).build();
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (Exception e) {
			System.err.println("%%%% Error Creating SessionFactory %%%%");
			e.printStackTrace();
		}
	}

	/**
	 * Returns the ThreadLocal Session instance. Lazy initialize the
	 * <code>SessionFactory</code> if needed.
	 * 
	 * @return Session
	 * @throws HibernateException
	 */
	static Session getSession() throws HibernateException {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * Close the single hibernate session instance.
	 * 
	 * @throws HibernateException
	 */
	static void closeSession() throws HibernateException {
		Session session = sessionFactory.getCurrentSession();
		if (session != null) {
			session.close();
		}
	}

	/**
	 * return session factory
	 * 
	 */
	static org.hibernate.SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * return hibernate configuration
	 * 
	 */
	static Configuration getConfiguration() {
		return configuration;
	}

}