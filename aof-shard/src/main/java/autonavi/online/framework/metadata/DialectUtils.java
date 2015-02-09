package autonavi.online.framework.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 我不说话
 * 
 * @author jia.miao
 * 
 */
public class DialectUtils {

	public static String getDialect4Hibernate(Connection conn,
			boolean closeConnection) throws SQLException {
		String dialect = "";
		try {
			String dpn = conn.getMetaData().getDatabaseProductName();
			if (dpn != null) {
				if (dpn.equalsIgnoreCase("oracle")) {
					dialect = "org.hibernate.dialect.Oracle10gDialect";
				} else if (dpn.equalsIgnoreCase("mysql")) {
					dialect = "org.hibernate.dialect.MySQL5Dialect";
				}
			}
			return dialect;
		} finally {
			if (closeConnection)
				conn.close();
		}
	}

	public static String getDialect4Hibernate(DatabaseMetaData meta)
			throws SQLException {
		String dialect = "";
		String dpn = meta.getDatabaseProductName();
		if (dpn != null) {
			if (dpn.equalsIgnoreCase("oracle")) {
				dialect = "org.hibernate.dialect.Oracle10gDialect";
			} else if (dpn.equalsIgnoreCase("mysql")) {
				dialect = "org.hibernate.dialect.MySQL5Dialect";
			}
		}
		return dialect;
	}

	public static String getDialect(Connection conn, boolean closeConnection)
			throws SQLException {
		try {
			return conn.getMetaData().getDatabaseProductName();
		} finally {
			if (closeConnection)
				conn.close();
		}
	}

	public static String getDbDateDialect(Connection conn,
			boolean closeConnection) throws SQLException {
		String dialect = "";
		try {
			String dpn = conn.getMetaData().getDatabaseProductName();
			if (dpn != null) {
				if (dpn.equalsIgnoreCase("oracle")) {
					dialect = "sysdate";
				} else if (dpn.equalsIgnoreCase("mysql")) {
					dialect = "sysdate()";
				}
			}
			return dialect;
		} finally {
			if (closeConnection)
				conn.close();
		}
	}
}
