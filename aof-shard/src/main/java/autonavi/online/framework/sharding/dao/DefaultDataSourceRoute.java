package autonavi.online.framework.sharding.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import autonavi.online.framework.sharding.holder.ConnectionHolder;

public class DefaultDataSourceRoute extends AbstractDataSourceRoute {

	@Override
	public Connection getConnection(int dsKey, DataSource ds,
			boolean openTransaction) throws SQLException {
		Connection conn = null;
		if (!ConnectionHolder.isAutoCommit()) {// 如果是手工提交
			Map<DataSource, Connection> map = ConnectionHolder
					.getShardConnectionHolder();
			if (map.containsKey(ds)) {
				conn = map.get(ds);
			} else {
				conn = ds.getConnection();
				map.put(ds, conn);
			}
			conn.setAutoCommit(false);
		} else {// 如果是dao自提交
			conn = ds.getConnection();
			if (openTransaction) {
				conn.setAutoCommit(false);
			}
			ConnectionHolder.setConnectionHolder(conn);
		}
		return conn;
	}

}
