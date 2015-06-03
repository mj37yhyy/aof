package autonavi.online.framework.sharding.dao.ds;

import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import autonavi.online.framework.sharding.dao.ds.strategy.LoadBalancingStrategy;
import autonavi.online.framework.sharding.dao.ds.strategy.RRLoadBalancingStrategy;

/**
 * 该类不是为了实现连接池，而是为了包含多个DataSource对象，
 * 并在获取连接的时候根据传入的LoadBalancingStrategy对象对DataSource对象进行选择。这是实现数据库高可用的一种方式。
 * 
 * @author jia.miao
 * 
 */
public class LoadBalancingDataSource implements DataSource {
	private Map<String, DataSource> realDataSourceMap = new HashMap<String, DataSource>();// 真实的DataSource
	private List<String> dsNames = new ArrayList<String>();// 用于存储数据源唯一名的List
	private LoadBalancingStrategy loadBalancingStrategy = null;// 策略类型

	public LoadBalancingDataSource() {
		this.loadBalancingStrategy = new RRLoadBalancingStrategy();
	}

	/**
	 * 加入DataSource对象。dsName是唯一的，如果相同，新的会覆盖旧的。
	 * 
	 * @param dsName
	 *            DataSource的唯一名
	 * @param dataSource
	 *            DataSource对象
	 */
	public void addDataSource(String dsName, DataSource dataSource) {
		if (dsName == null || dsName.isEmpty() || dataSource == null)
			throw new NullPointerException("dsName或dataSource不能为空");
		this.realDataSourceMap.put(dsName, new InnerDataSource(dsName,
				dataSource));
		this.dsNames.add(dsName);
		this.loadBalancingStrategy.init(this.dsNames);
	}

	/**
	 * 获取所有的真实DataSource
	 * 
	 * @return 所有的真实DataSource
	 */
	public Map<String, DataSource> getRealDataSourceMap() {
		return this.realDataSourceMap;
	}

	/**
	 * 设置负载均衡策略对象。该对象需要实现LoadBalancingStrategy接口，该接口有两个方法：init方法用于设置数据源的唯一名；
	 * balancing方法用于计算选择哪个数据源
	 * 。本方法内部会调用init方法。一个推荐的实现方法是继承AbstractLoadBalancingStrategy抽象类
	 * ，该类已经实现init方法，只需要通过调用super.dsNames就可以得到数据源唯一名列表。<br/>
	 * <em>注意:如果不传递参数，将默认使用RRLoadBalancingStrategy。</em>
	 * 
	 * @param loadBalancingStrategy
	 *            负载均衡策略对象
	 */
	public void setLoadBalancingStrategy(
			LoadBalancingStrategy loadBalancingStrategy) {
		if (loadBalancingStrategy != null) {
			this.loadBalancingStrategy = loadBalancingStrategy;
			this.loadBalancingStrategy.init(this.dsNames);
		}
	}

	/**
	 * 计算数据源唯一名。如果只存在一个数据源名，则直接返回；否则，则要通过负载均衡策略进行计算
	 * 
	 * @return DataSource唯一名
	 */
	private String balanc() {
		if (this.dsNames.size() == 1)
			return this.dsNames.get(0);
		return this.loadBalancingStrategy.balanc();
	}

	/**
	 * 通过计算数据源唯一名后再得到计算后的真实数据源的对象
	 * 
	 * @return 真实数据源的对象，如果不存在，则返回null
	 */
	public DataSource getBalancedRealDataSource() {
		String dsName = this.balanc();
		if (dsName != null)
			return this.realDataSourceMap.get(dsName);
		return null;
	}

	/**
	 * 通过确定的数据源唯一名获取数据源
	 * 
	 * @param dsName
	 *            唯一数据源的名字
	 * @return 真实数据源的对象，如果不存在，则返回null
	 */
	public DataSource getRealDataSourceByDsName(String dsName) {
		if (dsName != null)
			return this.realDataSourceMap.get(dsName);
		return null;
	}

	/**
	 * 通过DataSource对象返回该对象的唯一名
	 * 
	 * @param dataSource
	 * @return DataSource对象的唯一名。如果没有查到，则返回null。
	 */
	public String getDsNameByRealDataSource(DataSource dataSource) {
		for (Entry<String, DataSource> entry : this.realDataSourceMap
				.entrySet()) {
			if (entry.getValue().equals(dataSource))
				return entry.getKey();
		}
		return null;
	}

	/**
	 * 注意：该方法会根据负载均衡策略重新获取不同的DataSource对象，所以使用的时候要小心。如果能够确定数据源的唯一名，
	 * 请选择使用getLogWriter(String realDsName)方法
	 * 
	 * @return PrintWriter
	 * @throws SQLException
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return this.getBalancedRealDataSource().getLogWriter();
	}

	/**
	 * getLogWriter()的重载方法，选择确定的数据源进行设置
	 * 
	 * @param realDsName
	 *            数据源的唯一名
	 * @return PrintWriter
	 * @throws SQLException
	 */
	public PrintWriter getLogWriter(String realDsName) throws SQLException {
		return this.realDataSourceMap.get(realDsName).getLogWriter();
	}

	/**
	 * 注意：该方法会根据负载均衡策略重新获取不同的DataSource对象，所以使用的时候要小心。如果能够确定数据源的唯一名，
	 * 请选择使用setLogWriter(String realDsName,PrintWriter out)方法
	 */
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.getBalancedRealDataSource().setLogWriter(out);
	}

	/**
	 * setLogWriter(PrintWriter out)的重载方法，选择确定的数据源进行设置
	 * 
	 * @param realDsName
	 *            数据源的唯一名
	 * @param out
	 * @throws SQLException
	 */
	public void setLogWriter(String realDsName, PrintWriter out)
			throws SQLException {
		this.realDataSourceMap.get(realDsName).setLogWriter(out);
	}

	/**
	 * 注意：该方法会根据负载均衡策略重新获取不同的DataSource对象，所以使用的时候要小心。如果能够确定数据源的唯一名，
	 * 请选择使用setLoginTimeout(String realDsName,int seconds)方法
	 */
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.getBalancedRealDataSource().setLoginTimeout(seconds);
	}

	/**
	 * setLoginTimeout(int seconds)的重载方法，选择确定的数据源进行设置
	 * 
	 * @param realDsName
	 *            数据源的唯一名
	 * @param seconds
	 * @throws SQLException
	 */
	public void setLoginTimeout(String realDsName, int seconds)
			throws SQLException {
		this.realDataSourceMap.get(realDsName).setLoginTimeout(seconds);
	}

	/**
	 * 注意：该方法会根据负载均衡策略重新获取不同的DataSource对象，所以使用的时候要小心。如果能够确定数据源的唯一名，
	 * 请选择使用getLoginTimeout(String realDsName)方法
	 */
	@Override
	public int getLoginTimeout() throws SQLException {
		return this.getBalancedRealDataSource().getLoginTimeout();
	}

	/**
	 * getLoginTimeout()的重载方法，选择确定的数据源进行设置
	 * 
	 * @param realDsName
	 *            数据源的唯一名
	 * @return int
	 * @throws SQLException
	 */
	public int getLoginTimeout(String realDsName) throws SQLException {
		return this.realDataSourceMap.get(realDsName).getLoginTimeout();
	}

	/**
	 * 注意：该方法会根据负载均衡策略重新获取不同的DataSource对象，所以使用的时候要小心。如果能够确定数据源的唯一名，
	 * 请选择使用unwrap(String realDsName,Class<T> iface)方法
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.getBalancedRealDataSource().unwrap(iface);
	}

	/**
	 * unwrap(Class<T> iface)的重载方法，选择确定的数据源进行设置
	 * 
	 * @param realDsName
	 *            数据源的唯一名
	 * @param iface
	 * @return T
	 * @throws SQLException
	 */
	public <T> T unwrap(String realDsName, Class<T> iface) throws SQLException {
		return this.realDataSourceMap.get(realDsName).unwrap(iface);
	}

	/**
	 * 注意：该方法会根据负载均衡策略重新获取不同的DataSource对象，所以使用的时候要小心。如果能够确定数据源的唯一名，
	 * 请选择使用isWrapperFor(String realDsName, Class<?> iface)方法
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getBalancedRealDataSource().isWrapperFor(iface);
	}

	/**
	 * isWrapperFor(Class<?> iface)的重载方法，选择确定的数据源进行设置
	 * 
	 * @param realDsName
	 *            数据源的唯一名
	 * @param iface
	 * @return
	 * @throws SQLException
	 */
	public boolean isWrapperFor(String realDsName, Class<?> iface)
			throws SQLException {
		return this.realDataSourceMap.get(realDsName).isWrapperFor(iface);
	}

	/**
	 * 注意：该方法会根据负载均衡策略重新获取不同的DataSource对象，所以使用的时候要小心。如果能够确定数据源的唯一名，
	 * 请选择使用getConnection(String realDsName)方法
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return this.getBalancedRealDataSource().getConnection();
	}

	/**
	 * getConnection()的重载方法，选择确定的数据源进行设置
	 * 
	 * @param realDsName
	 *            数据源的唯一名
	 * @return Connection
	 * @throws SQLException
	 */
	public Connection getConnection(String realDsName) throws SQLException {
		return this.realDataSourceMap.get(realDsName).getConnection();
	}

	/**
	 * 注意：该方法会根据负载均衡策略重新获取不同的DataSource对象，所以使用的时候要小心。如果能够确定数据源的唯一名，
	 * 请选择使用getConnection(String realDsName,String username, String password)方法
	 */
	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		return this.getBalancedRealDataSource().getConnection(username,
				password);
	}

	/**
	 * getConnection(String username, String password)的重载方法，选择确定的数据源进行设置
	 * 
	 * @param realDsName
	 *            数据源的唯一名
	 * @param username
	 * @param password
	 * @return Connection
	 * @throws SQLException
	 */
	public Connection getConnection(String realDsName, String username,
			String password) throws SQLException {
		return this.realDataSourceMap.get(realDsName).getConnection(username,
				password);
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 再次包装，目的是为了计数
	 * 
	 * @author jia.miao
	 * 
	 */
	class InnerDataSource implements DataSource {
		private String dsName = null;
		private DataSource dataSource = null;

		public InnerDataSource(String dsName, DataSource dataSource) {
			this.dsName = dsName;
			this.dataSource = dataSource;
		}

		@Override
		public PrintWriter getLogWriter() throws SQLException {
			return this.dataSource.getLogWriter();
		}

		@Override
		public void setLogWriter(PrintWriter out) throws SQLException {
			this.dataSource.setLogWriter(out);
		}

		@Override
		public void setLoginTimeout(int seconds) throws SQLException {
			this.dataSource.setLoginTimeout(seconds);
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			return this.dataSource.getLoginTimeout();
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return this.dataSource.unwrap(iface);
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return this.dataSource.isWrapperFor(iface);
		}

		@Override
		public Connection getConnection() throws SQLException {
			ConnectCounter.counterIncrement(dsName);// 连接计数器加1
			return new InnerConnection(dsName, this.dataSource.getConnection());// 返回封装类，用了计数器
		}

		@Override
		public Connection getConnection(String username, String password)
				throws SQLException {
			ConnectCounter.counterIncrement(dsName);// 连接计数器加1
			return new InnerConnection(dsName, this.dataSource.getConnection(
					username, password));// 返回封装类，用了计数器
		}

	}

	/**
	 * 包装Connection，主要为了计数
	 * 
	 * @author jia.miao
	 * 
	 */
	class InnerConnection implements Connection {
		private String dsName = null;
		private Connection conn = null;

		public InnerConnection(String dsName, Connection conn) {
			this.dsName = dsName;
			this.conn = conn;
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return this.conn.unwrap(iface);
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return this.conn.isWrapperFor(iface);
		}

		@Override
		public Statement createStatement() throws SQLException {
			return this.conn.createStatement();
		}

		@Override
		public PreparedStatement prepareStatement(String sql)
				throws SQLException {
			return this.conn.prepareStatement(sql);
		}

		@Override
		public CallableStatement prepareCall(String sql) throws SQLException {
			return this.conn.prepareCall(sql);
		}

		@Override
		public String nativeSQL(String sql) throws SQLException {
			return this.conn.nativeSQL(sql);
		}

		@Override
		public void setAutoCommit(boolean autoCommit) throws SQLException {
			this.conn.setAutoCommit(autoCommit);
		}

		@Override
		public boolean getAutoCommit() throws SQLException {
			return this.conn.getAutoCommit();
		}

		@Override
		public void commit() throws SQLException {
			this.conn.commit();
		}

		@Override
		public void rollback() throws SQLException {
			this.conn.rollback();
		}

		@Override
		public void close() throws SQLException {
			ConnectCounter.counterDecrease(dsName);// 连接计数器减1
			this.conn.close();
		}

		@Override
		public boolean isClosed() throws SQLException {
			return this.conn.isClosed();
		}

		@Override
		public DatabaseMetaData getMetaData() throws SQLException {
			return this.conn.getMetaData();
		}

		@Override
		public void setReadOnly(boolean readOnly) throws SQLException {
			this.conn.setReadOnly(readOnly);
		}

		@Override
		public boolean isReadOnly() throws SQLException {
			return this.conn.isReadOnly();
		}

		@Override
		public void setCatalog(String catalog) throws SQLException {
			this.conn.setCatalog(catalog);
		}

		@Override
		public String getCatalog() throws SQLException {
			return this.conn.getCatalog();
		}

		@Override
		public void setTransactionIsolation(int level) throws SQLException {
			this.conn.setTransactionIsolation(level);
		}

		@Override
		public int getTransactionIsolation() throws SQLException {
			return this.conn.getTransactionIsolation();
		}

		@Override
		public SQLWarning getWarnings() throws SQLException {
			return this.conn.getWarnings();
		}

		@Override
		public void clearWarnings() throws SQLException {
			this.conn.clearWarnings();

		}

		@Override
		public Statement createStatement(int resultSetType,
				int resultSetConcurrency) throws SQLException {
			return this.conn.createStatement(resultSetType,
					resultSetConcurrency);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int resultSetType, int resultSetConcurrency)
				throws SQLException {
			return this.conn.prepareStatement(sql, resultSetType,
					resultSetConcurrency);
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType,
				int resultSetConcurrency) throws SQLException {
			return this.conn.prepareCall(sql, resultSetType,
					resultSetConcurrency);
		}

		@Override
		public Map<String, Class<?>> getTypeMap() throws SQLException {
			return this.conn.getTypeMap();
		}

		@Override
		public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
			this.conn.setTypeMap(map);
		}

		@Override
		public void setHoldability(int holdability) throws SQLException {
			this.conn.setHoldability(holdability);
		}

		@Override
		public int getHoldability() throws SQLException {
			return this.conn.getHoldability();
		}

		@Override
		public Savepoint setSavepoint() throws SQLException {
			return this.conn.setSavepoint();
		}

		@Override
		public Savepoint setSavepoint(String name) throws SQLException {
			return this.conn.setSavepoint(name);
		}

		@Override
		public void rollback(Savepoint savepoint) throws SQLException {
			this.conn.rollback(savepoint);

		}

		@Override
		public void releaseSavepoint(Savepoint savepoint) throws SQLException {
			this.conn.releaseSavepoint(savepoint);
		}

		@Override
		public Statement createStatement(int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			return this.conn.createStatement(resultSetType,
					resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int resultSetType, int resultSetConcurrency,
				int resultSetHoldability) throws SQLException {
			return this.conn.prepareStatement(sql, resultSetType,
					resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType,
				int resultSetConcurrency, int resultSetHoldability)
				throws SQLException {
			return this.conn.prepareCall(sql, resultSetType,
					resultSetConcurrency);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int autoGeneratedKeys) throws SQLException {
			return this.conn.prepareStatement(sql, autoGeneratedKeys);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				int[] columnIndexes) throws SQLException {
			return this.conn.prepareStatement(sql, columnIndexes);
		}

		@Override
		public PreparedStatement prepareStatement(String sql,
				String[] columnNames) throws SQLException {
			return this.conn.prepareStatement(sql, columnNames);
		}

		@Override
		public Clob createClob() throws SQLException {
			return this.conn.createClob();
		}

		@Override
		public Blob createBlob() throws SQLException {
			return this.conn.createBlob();
		}

		@Override
		public NClob createNClob() throws SQLException {
			return this.conn.createNClob();
		}

		@Override
		public SQLXML createSQLXML() throws SQLException {
			return this.conn.createSQLXML();
		}

		@Override
		public boolean isValid(int timeout) throws SQLException {
			return this.conn.isValid(timeout);
		}

		@Override
		public void setClientInfo(String name, String value)
				throws SQLClientInfoException {
			this.conn.setClientInfo(name, value);
		}

		@Override
		public void setClientInfo(Properties properties)
				throws SQLClientInfoException {
			this.conn.setClientInfo(properties);
		}

		@Override
		public String getClientInfo(String name) throws SQLException {
			return this.conn.getClientInfo(name);
		}

		@Override
		public Properties getClientInfo() throws SQLException {
			return this.conn.getClientInfo();
		}

		@Override
		public Array createArrayOf(String typeName, Object[] elements)
				throws SQLException {
			return this.conn.createArrayOf(typeName, elements);
		}

		@Override
		public Struct createStruct(String typeName, Object[] attributes)
				throws SQLException {
			return this.conn.createStruct(typeName, attributes);
		}

	}
}
