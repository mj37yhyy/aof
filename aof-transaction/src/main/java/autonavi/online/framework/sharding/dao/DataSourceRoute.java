package autonavi.online.framework.sharding.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;

import autonavi.online.framework.sharding.annotation.DataSourceRouteCase;
import autonavi.online.framework.sharding.transaction.ConnectionTransactionManage;
import autonavi.online.framework.sharding.transaction.manager.DynamicTransactionManager;

@DataSourceRouteCase
public class DataSourceRoute extends AbstractDataSourceRoute {

	/**
	 * 开启事务，得到连接
	 * 
	 * @param shardingIndexEntity
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Connection getConnection(int dsKey, DataSource ds,
			boolean openTransaction) throws SQLException {
		DynamicTransactionManager dtm = getDynamicDataSource()
				.getDynamicTransactionManager();
		if (dtm == null) {
			throw new RuntimeException("事务管理器实现对象不存在,请检查");
		}

		PlatformTransactionManager tm = dtm.getTransactionMananger(ds); // 获取当前的事务管理器
		Connection conn = null;
		if (openTransaction) {
			ConnectionTransactionManage.begin(dsKey, ds, tm);// 开始事务
			conn = DataSourceUtils.getConnection(ds);
		} else {
			ConnectionTransactionManage.begin(dsKey);// 占据事务栈的一个位置 保证事务栈和连接栈同步
			conn = ds.getConnection();
		}
		return conn;
	}

	// public Connection getConnection() {
	// return DataSourceUtils.getConnection(this.dynamicDataSource);
	// }
	// private Logger log = LogManager.getLogger(this.getClass());

}
