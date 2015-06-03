package autonavi.online.framework.metadata.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CreateHibernateCfg {

	/**
	 * 创建createSessionFactory的部分
	 * 
	 * @param entity
	 * @return
	 * @throws ParserConfigurationException
	 */
	public Document createSessionFactory(SessionFactoryEntity entity)
			throws ParserConfigurationException {
		Document doc = null;
		// 得到DOM解析器的工厂实例
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		// 从DOM工厂中获得DOM解析器
		DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
		// 创建文档树模型对象
		doc = dbBuilder.newDocument();

		Element hibernate_configuration, session_factory;

		hibernate_configuration = doc.createElement("hibernate-configuration");
		session_factory = doc.createElement("session-factory");

		if (doc != null && entity != null) {

			// dialect
			if (entity.getDialect() != null && !entity.getDialect().equals("")) {
				Element dialect = doc.createElement("property");
				dialect.setAttribute("name", "dialect");
				dialect.appendChild(doc.createTextNode(entity.getDialect()));
				session_factory.appendChild(dialect);
			}

			// connection.driver_class
			if (entity.getConnection_driver_class() != null
					&& !entity.getConnection_driver_class().equals("")) {
				Element connection_driver_class = doc.createElement("property");
				connection_driver_class.setAttribute("name",
						"connection.driver_class");
				connection_driver_class.appendChild(doc.createTextNode(entity
						.getConnection_driver_class()));
				session_factory.appendChild(connection_driver_class);
			}

			// connection.url
			if (entity.getConnection_url() != null
					&& !entity.getConnection_url().equals("")) {
				Element connection_url = doc.createElement("property");
				connection_url.setAttribute("name", "connection.url");
				connection_url.appendChild(doc.createTextNode(entity
						.getConnection_url()));
				session_factory.appendChild(connection_url);
			}

			// connection.username
			if (entity.getConnection_username() != null
					&& !entity.getConnection_username().equals("")) {
				Element connection_username = doc.createElement("property");
				connection_username.setAttribute("name", "connection.username");
				connection_username.appendChild(doc.createTextNode(entity
						.getConnection_username()));
				session_factory.appendChild(connection_username);
			}

			// connection.password
			if (entity.getConnection_password() != null
					&& !entity.getConnection_password().equals("")) {
				Element connection_password = doc.createElement("property");
				connection_password.setAttribute("name", "connection.password");
				connection_password.appendChild(doc.createTextNode(entity
						.getConnection_password()));
				session_factory.appendChild(connection_password);
			}

			// show_sql
			if (entity.getShow_sql() != null
					&& !entity.getShow_sql().equals("")) {
				Element show_sql = doc.createElement("property");
				show_sql.setAttribute("name", "show_sql");
				show_sql.appendChild(doc.createTextNode(entity.getShow_sql()));
				session_factory.appendChild(show_sql);
			}

			// hibernate.hbm2ddl.auto
			if (entity.getHibernate_hbm2ddl_auto() != null
					&& !entity.getHibernate_hbm2ddl_auto().equals("")) {
				Element hibernate_hbm2ddl_auto = doc.createElement("property");
				hibernate_hbm2ddl_auto.setAttribute("name",
						"hibernate.hbm2ddl.auto");
				hibernate_hbm2ddl_auto.appendChild(doc.createTextNode(entity
						.getHibernate_hbm2ddl_auto()));
				session_factory.appendChild(hibernate_hbm2ddl_auto);
			}

			// hibernate.autoReconnect
			if (entity.getHibernate_autoReconnect() != null
					&& !entity.getHibernate_autoReconnect().equals("")) {
				Element hibernate_autoReconnect = doc.createElement("property");
				hibernate_autoReconnect.setAttribute("name",
						"hibernate.autoReconnect");
				hibernate_autoReconnect.appendChild(doc.createTextNode(entity
						.getHibernate_autoReconnect()));
				session_factory.appendChild(hibernate_autoReconnect);
			}
		}
		hibernate_configuration.appendChild(session_factory);
		doc.appendChild(hibernate_configuration);

		return doc;
	}
}
