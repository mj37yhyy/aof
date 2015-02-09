package autonavi.online.framework.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.metadata.DialectUtils;
import autonavi.online.framework.util.net.HostUtils;

/**
 * 用于实现Server的监控
 * 
 * @author jia.miao
 * 
 */
public class ServerMonitor {

	/**
	 * 每分钟发送一次心跳信息
	 * 
	 * @throws Exception
	 */
	public void heartbeat() throws Exception {
		new Thread() {
			public void run() {
				while (true) {
					try {
						sendHeartbeat();
						Thread.sleep(60 * 1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/**
	 * 发送心跳信息<br/>
	 * 先查新是否有记录，如果有，更新心跳时间，如果没有，插入一条
	 * 
	 * @throws Exception
	 */

	public void sendHeartbeat() throws Exception {
		// String ip = StringUtils.collectionToDelimitedString(
		// HostUtils.getLocalHosts(), ",");
		String version = this.getAOFVersion();
		String ip = this.getLocalHost();
		Connection conn = this.dataSource.getConnection();
		String dialect = DialectUtils.getDbDateDialect(conn, false);
		String _ip = (String) queryRunner.query(conn,
				"select ip from AOF_SERVER_MONITOR where ip=?",
				new ScalarHandler(), ip);
		if (_ip != null && _ip.equals(ip)) {
			queryRunner.update(conn, "update AOF_SERVER_MONITOR set mtime="
					+ dialect + ",version=? where ip=?", version, ip);
		} else {
			queryRunner.update(conn,
					"insert into AOF_SERVER_MONITOR (ip,version,mtime) values (?,?,"
							+ dialect + ")", ip, version);
		}
		DbUtils.closeQuietly(conn);
	}

	/**
	 * 获取当前框架的版本号s
	 * 
	 * @return
	 * @throws IOException
	 */
	private String getAOFVersion() throws IOException {
		if (this.version == null) {
			InputStream is = getClass().getResourceAsStream(
					"/META-INF/version.txt");
			try {
				this.version = IOUtils.toString(is);
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		return this.version;
	}

	/**
	 * 获得所有活着的服务
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getAllServer() throws Exception {
		Connection conn = this.dataSource.getConnection();
		String dialect = DialectUtils.getDialect(conn, false);
		try {
			String sql = "";
			if (dialect.equalsIgnoreCase("mysql")) {
				sql = "select ip from AOF_SERVER_MONITOR where (UNIX_TIMESTAMP(sysdate())-UNIX_TIMESTAMP(mtime))<"
						+ (3 * 60);// 转化为时间戳，单位为秒
			} else if (dialect.equalsIgnoreCase("oracle")) {
				sql = "select ip from AOF_SERVER_MONITOR where (SYSDATE-mtime)<"
						+ ((1 / (24 * 60)) * 3);// oracle没有转化为时间戳的function,所以直接计算：一天除以3分钟
			}
			return (List<String>) queryRunner.query(conn, sql,
					new ColumnListHandler());
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * 根据入参获取本机的IP
	 * 
	 * @return
	 * @throws SocketException
	 */
	private String getLocalHost() throws SocketException {
		String ip = null;
		if (localhost != null) {
			ip = localhost;// 用户定义的IP地址
		} else if (netInterfaceName != null) {
			ip = HostUtils.getLocalHost(netInterfaceName);// 得到对应网卡的地址
		} else {
			ip = HostUtils.getFristLocalHost();// 得到第一个网卡的地址
		}
		return ip;
	}

	QueryRunner queryRunner = new QueryRunner(true);
	private Logger log = LogManager.getLogger(this.getClass());
	private DataSource dataSource;
	private String localhost = null;
	private String netInterfaceName = null;
	private String version = null;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setLocalhost(String localhost) {
		this.localhost = localhost;
	}

	public void setNetInterfaceName(String netInterfaceName) {
		this.netInterfaceName = netInterfaceName;
	}

}
