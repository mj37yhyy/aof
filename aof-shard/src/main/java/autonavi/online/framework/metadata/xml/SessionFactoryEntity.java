package autonavi.online.framework.metadata.xml;

import java.io.Serializable;

public class SessionFactoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;  
	private String dialect;
	private String connection_driver_class;
	private String connection_url;
	private String connection_username;
	private String connection_password;
	private String show_sql;
	private String hibernate_hbm2ddl_auto;
	private String hibernate_autoReconnect;

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getConnection_driver_class() {
		return connection_driver_class;
	}

	public void setConnection_driver_class(String connection_driver_class) {
		this.connection_driver_class = connection_driver_class;
	}

	public String getConnection_url() {
		return connection_url;
	}

	public void setConnection_url(String connection_url) {
		this.connection_url = connection_url;
	}

	public String getConnection_username() {
		return connection_username;
	}

	public void setConnection_username(String connection_username) {
		this.connection_username = connection_username;
	}

	public String getConnection_password() {
		return connection_password;
	}

	public void setConnection_password(String connection_password) {
		this.connection_password = connection_password;
	}

	public String getShow_sql() {
		return show_sql;
	}

	public void setShow_sql(String show_sql) {
		this.show_sql = show_sql;
	}

	public String getHibernate_hbm2ddl_auto() {
		return hibernate_hbm2ddl_auto;
	}

	public void setHibernate_hbm2ddl_auto(String hibernate_hbm2ddl_auto) {
		this.hibernate_hbm2ddl_auto = hibernate_hbm2ddl_auto;
	}

	public String getHibernate_autoReconnect() {
		return hibernate_autoReconnect;
	}

	public void setHibernate_autoReconnect(String hibernate_autoReconnect) {
		this.hibernate_autoReconnect = hibernate_autoReconnect;
	}

}
