package autonavi.online.framework.sharding.dao.paging;

public class Paging4MYSQL extends AbstractPaging {

	@Override
	public String getPagingSQL(String sql, long start, long limit) {
		StringBuffer sb = new StringBuffer();
		sb.append("select * from (");
		sb.append(sql);
		sb.append(") t");
		sb.append(" LIMIT ");
		sb.append(start);
		sb.append(",");
		sb.append(limit);
		return sb.toString();
	}
}
