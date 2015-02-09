package autonavi.online.framework.sharding.dao.paging;

public class Paging4ORACLE extends AbstractPaging {

	@Override
	public String getPagingSQL(String sql, long start, long limit) {
		StringBuffer sb = new StringBuffer();
		sb.append("select * from (select table1.*,rownum as rownum1 from (select * from (");
		sb.append(sql);
		sb.append(") t");
		sb.append(") table1 where rownum<= ");
		sb.append(limit);
		sb.append(")where rownum1> ");
		sb.append(start);
		return sb.toString();
	}
}
