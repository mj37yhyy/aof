package autonavi.online.framework.sharding.dao;

import java.sql.Connection;
import java.sql.SQLException;

import autonavi.online.framework.sharding.holder.ConnectionHolder;
/**
 * 默认DAO实现
 * 只有在走DAO自动提交，自动回滚的情况下才会能从ConnectionHolder.getConnectionHolder()取到连接
 * 如果走简单事务方式 DefaultTransactionManager 那么从ConnectionHolder.getConnectionHolder()是获取不到连接的 也就是不会提交或者回滚或者关闭连接，事务完成后走DefaultTransactionManager.commit()完成
 * @author xuyaming
 *
 */
public class DefaultDaoSupport extends AbstractDaoSupport {

	@Override
	protected void commit() throws SQLException {
		Connection conn = ConnectionHolder.getConnectionHolder();
		if (conn != null && !conn.isClosed() && !conn.getAutoCommit()) {
			conn.commit();
		}
		
	}

	@Override
	protected void rollback() throws SQLException {
		Connection conn = ConnectionHolder.getConnectionHolder();
		if (conn != null && !conn.isClosed() && !conn.getAutoCommit()) {
			conn.rollback();
		}
	}

	@Override
	protected void release() throws SQLException {
		Connection conn = ConnectionHolder.getConnectionHolder();
		if (conn != null && !conn.isClosed()) {
			ConnectionHolder.getConnectionHolder().close();
			ConnectionHolder.cleanConnectionHolder();
		}
		
	}

}
