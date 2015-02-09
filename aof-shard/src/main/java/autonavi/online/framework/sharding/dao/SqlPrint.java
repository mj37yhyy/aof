package autonavi.online.framework.sharding.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.cc.Miscellaneous;

public class SqlPrint {
	private static Logger log = LogManager.getLogger(SqlPrint.class);
	private static int costLimit = 10;

	/**
	 * 打印SQL和参数
	 * 
	 * @param sql
	 * @param sqlParameters
	 */
	public static void sqlAndParamConsuming(Class<?> tagetClass, String sql,
			String sqlParameters) {
		if (Miscellaneous.show_sql) {
			log = LogManager.getLogger(tagetClass);
			if (null != sql && sql.length() > 0) {
				log.debug("Preparing:[ " + sql + " ]");
			}
			if (null != sqlParameters && sqlParameters.length() > 0) {
				log.debug("Parameters:[ "
						+ sqlParameters.substring(0, sqlParameters.length() - 2)
						+ " ]");
			}
		}

	}

	/**
	 * 打印时间长于10ms的SQL
	 * 
	 * @param className
	 * @param methodName
	 * @param author
	 * @param startTime
	 * @param sql
	 */
	public static void sqlTimeconsuming(String author, long startTime,
			String sql) {
		long endTime = System.currentTimeMillis();
		long timeconsuming = endTime - startTime;
		if (timeconsuming >= costLimit) {
			log.warn("开发者:[" + author + "] 的SQL执行耗时:[" + timeconsuming
					+ "]ms SQL:[ " + sql + " ]");
			// 查询耗时很高考虑给予提醒，比如是否创建了索引，但是这个不能开放给应用层配置，因为那些应用层程序员肯定会将值改的很大
		}
	}
}
