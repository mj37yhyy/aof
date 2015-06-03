package autonavi.online.framework.support.hibernate;

import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.context.internal.ThreadLocalSessionContext;

public class HibernateUtils {

	public void init() {
		// 配置Hibernate
		Configuration configuration = HibernateSessionFactory
				.getConfiguration();
		configuration.setProperties(new Properties() {
			private static final long serialVersionUID = 6731977514116283490L;
			{
				/**
				 * hibernate.current_session_context_class 是hibernate
				 * 4使用session事务的模式 1:
				 * org.hibernate.context.internal.ThreadLocalSessionContext -
				 * 当前session通过当前执行的线程来跟踪和界定。 2:
				 * org.hibernate.context.internal.JTASessionContext-
				 * 当前session根据JTA来跟踪和界定。这和以前的仅支持JTA的方法是完全一样的。
				 * 3：org.hibernate.context.internal.ManagedSessionContext
				 * 使用spring时 默认是 ，用spring的事务管理
				 * 则用org.springframework.orm.hibernate4.SpringSessionContext
				 */
				put(Environment.CURRENT_SESSION_CONTEXT_CLASS,
						ThreadLocalSessionContext.class.getName());// session范围
				put(Environment.DATASOURCE, dataSource);// 数据源
				put(Environment.DIALECT, dialect);
				put(Environment.SHOW_SQL, "true");
				put(Environment.FORMAT_SQL, "true");
			}
		});
		// 引入hbm
		if (resources != null)
			for (String resourceName : resources) {
				configuration.addResource(resourceName);
			}
		if (classes != null)
			for (Class<?> clazz : classes) {
				configuration.addClass(clazz);
			}
		if (annotatedClasses != null)
			for (Class<?> annotatedClass : annotatedClasses) {
				configuration.addAnnotatedClass(annotatedClass);
			}
		if (xmls != null)
			for (String xml : xmls) {
				configuration.addXML(xml);
			}
		if (xmlInputStreams != null)
			for (InputStream xmlInputStream : xmlInputStreams) {
				configuration.addInputStream(xmlInputStream);
			}
		// 初始化sessionFactory
		HibernateSessionFactory.init();
	}

	private DataSource dataSource = null;
	private String dialect = null;
	private String[] resources = null;
	private Class<?>[] classes = null;
	private Class<?>[] annotatedClasses = null;
	private String[] xmls = null;
	private InputStream[] xmlInputStreams = null;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public void setResources(String[] resources) {
		this.resources = resources;
	}

	public void setClasses(Class<?>[] classes) {
		this.classes = classes;
	}

	public void setAnnotatedClasses(Class<?>[] annotatedClasses) {
		this.annotatedClasses = annotatedClasses;
	}

	public void setXmls(String[] xmls) {
		this.xmls = xmls;
	}

	public void setXmlInputStreams(InputStream[] xmlInputStreams) {
		this.xmlInputStreams = xmlInputStreams;
	}

}
