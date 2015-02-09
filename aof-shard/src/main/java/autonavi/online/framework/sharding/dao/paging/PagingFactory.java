package autonavi.online.framework.sharding.dao.paging;

import java.sql.Connection;
import java.sql.SQLException;

public class PagingFactory {

	/**
	 * 通过数据源得到数据源类型
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private String getDatabaseProductName(Connection conn) throws SQLException {
		return conn.getMetaData().getDatabaseProductName();
	}

	/**
	 * 通过连接得到paging实体
	 * @param conn
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Paging getPaging(Connection conn) throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		String databaseProductName = this.getDatabaseProductName(conn);
		return (Paging) Class.forName(
				this.getClass()
						.getName()
						.replace(".PagingFactory",
								".Paging4" + databaseProductName.toUpperCase()))
				.newInstance();
	}

}
