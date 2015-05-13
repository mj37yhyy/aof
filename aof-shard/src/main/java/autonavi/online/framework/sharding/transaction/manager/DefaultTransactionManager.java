package autonavi.online.framework.sharding.transaction.manager;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.sharding.holder.ConnectionHolder;

/**
 * 默认事务管理器，只实现最简单的手工事务管理，如果想使用完整的事务管理，请使用事务依赖包
 * 
 * @author jia.miao
 * 
 */
public class DefaultTransactionManager {
	private static Logger logger = LogManager.getLogger(DefaultTransactionManager.class);

	/**
	 * 开始事务
	 */
	public static void begin() {
		ConnectionHolder.setAutoCommit(false);
	}

	/**
	 * 提交事务
	 * 
	 * @throws SQLException
	 */
	public static void commit() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			if(ConnectionHolder.getShardConnectionHolder().size()==0){
				if(logger.isWarnEnabled())
				    logger.warn("已经使用Spring事务进行了控制导致简单事务无效或者简单事务已经完成");
				return;
			}
			for (Connection conn : ConnectionHolder.getShardConnectionHolder()
					.values()) {
				if (conn != null && !conn.isClosed()&&!conn.getAutoCommit()) {
					conn.commit();
				}
			}
		}
	}

	/**
	 * 回滚事物
	 * 
	 * @throws SQLException
	 */
	public static void rollback() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			if(ConnectionHolder.getShardConnectionHolder().size()==0){
				if(logger.isWarnEnabled())
				    logger.warn("已经使用Spring事务进行了控制导致简单事务无效或者简单事务已经完成");
				return;
			}
			for (Connection conn : ConnectionHolder.getShardConnectionHolder()
					.values()) {
				if (conn != null && !conn.isClosed()&&!conn.getAutoCommit()) {
					conn.rollback();
				}
			}
		}
	}

	/**
	 * 释放连接
	 * 
	 * @throws SQLException
	 */
	public static void release() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			if(ConnectionHolder.getShardConnectionHolder().size()==0){
				if(logger.isWarnEnabled())
				    logger.warn("已经使用Spring事务进行了控制导致简单事务无效或者简单事务已经完成");
				ConnectionHolder.cleanAutoCommitHolder();
				return;
			}
			for (Connection conn : ConnectionHolder.getShardConnectionHolder()
					.values()) {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			}
			ConnectionHolder.cleanAutoCommitHolder();
			ConnectionHolder.cleanShardConnectionHolder();
		}
	}

}
