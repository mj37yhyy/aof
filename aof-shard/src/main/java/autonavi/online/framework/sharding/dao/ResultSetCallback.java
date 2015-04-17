package autonavi.online.framework.sharding.dao;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集游标回调
 * 此方法实现在DAO层提供此方法实现，处理传入的游标
 * @author yaming.xu
 * @since 1.0
 * @param <T>
 */
public interface ResultSetCallback<T> {
	/**
	 * 回调方法
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	T process(ResultSet rs) throws SQLException;

}
