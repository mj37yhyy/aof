package autonavi.online.framework.sharding.dao;

import java.sql.Connection;
import java.sql.SQLException;

import autonavi.online.framework.sharding.holder.ConnectionHolder;

public class DefaultDaoSupport extends AbstractDaoSupport {

	@Override
	protected void commit() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			Connection conn = ConnectionHolder.getConnectionHolder();
			if (conn != null && !conn.isClosed() && !conn.getAutoCommit()) {
				conn.commit();
			}
			this.release();
		}
	}

	@Override
	protected void rollback() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			Connection conn = ConnectionHolder.getConnectionHolder();
			if (conn != null && !conn.isClosed() && !conn.getAutoCommit()) {
				conn.rollback();
			}
			this.release();
		}
	}

	@Override
	protected void release() throws SQLException {
		if (!ConnectionHolder.isAutoCommit()) {
			Connection conn = ConnectionHolder.getConnectionHolder();
			if (conn != null && !conn.isClosed()) {
				ConnectionHolder.getConnectionHolder().close();
			}
			ConnectionHolder.cleanConnectionHolder();
		}
	}

}
