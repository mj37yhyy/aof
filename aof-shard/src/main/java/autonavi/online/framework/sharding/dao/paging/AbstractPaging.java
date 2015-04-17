package autonavi.online.framework.sharding.dao.paging;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractPaging implements Paging {

	@Override
	public String getPagingSQL(String sql, long start, long limit) {
		return null;
	}

	@Override
	public String getCountSQL(String sql, String column) {
		StringBuffer sb = new StringBuffer();
		sb.append("select count(");
		sb.append(column);
		sb.append(") c from (");
		sb.append(sql);
		sb.append(") t");
		return sb.toString();
	}

}
