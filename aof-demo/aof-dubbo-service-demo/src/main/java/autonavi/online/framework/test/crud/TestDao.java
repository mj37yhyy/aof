package autonavi.online.framework.test.crud;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import autonavi.online.framework.sharding.dao.ResultSetCallback;
import autonavi.online.framework.sharding.dao.constant.ReservedWord;
import autonavi.online.framework.sharding.entry.aspect.annotation.Author;
import autonavi.online.framework.sharding.entry.aspect.annotation.Delete;
import autonavi.online.framework.sharding.entry.aspect.annotation.Insert;
import autonavi.online.framework.sharding.entry.aspect.annotation.Select;
import autonavi.online.framework.sharding.entry.aspect.annotation.Select.Paging;
import autonavi.online.framework.sharding.entry.aspect.annotation.Shard;
import autonavi.online.framework.sharding.entry.aspect.annotation.SingleDataSource;
import autonavi.online.framework.sharding.entry.aspect.annotation.SqlParameter;
import autonavi.online.framework.sharding.entry.aspect.annotation.Update;
import autonavi.online.framework.sharding.entry.entity.CollectionType;
import autonavi.online.framework.test.entity.Demo;

@Repository
public class TestDao {

	@Author("jia.miao")
	@SingleDataSource(1)
	@Select
	// @Shard(indexName="demo_index",indexColumn="")
	public Object select1() {
		return "select 1";
	}

	/**
	 * 用户自定义返回结果
	 * 
	 * @param m
	 * @param others
	 * @param callback
	 * @return
	 */
	@Author("ang.ji")
	@Shard(indexName = "demo_index", indexColumn = "m.name")
	@Select
	public Object userCallbackSelect(@SqlParameter("m") Demo m,
			ResultSetCallback<?> callback) {
		return "select id from demo where name=#{m.name}";
	}

	/**
	 * 使用系统定的返回方式(单字段)<br/>
	 * 使用@SingleDataSource标示为只使用某一数据源
	 * 
	 * @param m
	 * @param dskey
	 * @return
	 */
	@Author("ang.ji")
	@SingleDataSource(keyName = "dskey")
	@Shard(indexName = "demo_index", indexColumn = "m.name")
	@Select(collectionType = CollectionType.column, resultType = long.class)
	public Object singleFieldSelect(@SqlParameter("m") Demo m,
			@SqlParameter("dskey") int dskey) {
		return "select id from demo where name=#{m.name}";
	}

	/**
	 * 使用系统定的返回方式(beanList)，并使用分页
	 * 
	 * @param m
	 * @param start
	 * @param limit
	 * @return
	 */
	@Author("ang.ji")
	@Shard(indexName = "demo_index", indexColumn = "m.name")
	@Select(paging = @Paging(skip = "start", size = "limit"), collectionType = CollectionType.beanList, resultType = Demo.class, queryCount = true)
	public Object pagingSelect(@SqlParameter("m") Demo m,
			@SqlParameter("start") int start, @SqlParameter("limit") int limit) {
		return "select id,name from demo where name=#{m.name}";
	}

	/**
	 * 批量查询(union all)
	 * 
	 * @param l8
	 * @return
	 */
	@Author("jia.miao")
	@Shard(indexName = "demo_index", indexColumn = "m." + ReservedWord.index
			+ ".name")
	@Select
	public Object batchSelect(@SqlParameter("m") List<Demo> l8) {
		return "select id,name from demo where name=#{m." + ReservedWord.index
				+ ".name}";
	}

	/**
	 * 另一种批量查询（自己组织SQL）
	 * 
	 * @param m
	 * @param others
	 * @return
	 */
	@Author("ang.ji")
	@Shard(indexName = "demo_index", indexColumn = "name")
	@Select
	public Object batchSelect2(@SqlParameter("name") String name, long... m) {
		String sql = "select id,name from demo where id in(";
		for (long _id : m) {
			sql += _id + ",";
		}
		return sql = sql.substring(0, sql.length() - 1) + ")";
	}

	/**
	 * 插入
	 * 
	 * @param m
	 * @param others
	 * @return
	 */
	@Author("shipeng.hou")
	@Shard(indexName = "demo_index", indexColumn = "m.name")
	@Insert
	public Object insert(@SqlParameter("m") Demo m) {
		return "insert into demo(id,name) values(#{" + ReservedWord.snowflake
				+ "},#{m.name})";
	}

	/**
	 * 批量插入
	 * 
	 * @param List
	 * @param others
	 * @return
	 */
	@Author("shipeng.hou")
	@Shard(indexName = "demo_index", indexColumn = "list." + ReservedWord.index
			+ ".name")
	@Insert
	public Object batchInsert(
			@SqlParameter("list") List<Demo> list) {
		String sql = "insert into demo(id,name) values (#{"
				+ ReservedWord.snowflake + "},#{list." + ReservedWord.index
				+ ".name})";
		return sql;
	}

	/**
	 * 更新
	 * 
	 * @param List
	 * @return
	 */
	@Author("jia.miao")
	@Shard(indexName = "demo_index", indexColumn = "m.name")
	@Update
	public Object update(@SqlParameter("m") Demo m) {
		return "update demo set name=name where name=#{m.name}";
	}

	/**
	 * 批量更新
	 * 
	 * @param List
	 * @return
	 */
	@Author("jia.miao")
	@Shard(indexName = "demo_index", indexColumn = "m."
			+ ReservedWord.index + ".name")
	@Update
	public Object batchUpdate(@SqlParameter("m") List<Demo> List) {
		String sql = "update demo set name=name where name=#{m."
				+ ReservedWord.index + ".name}";
		return sql;
	}

	/**
	 * 删除
	 * 
	 * @param m
	 * @param others
	 * @return
	 */
	@Author("shipeng.hou")
	@Shard(indexName = "demo_index", indexColumn = "m.name")
	@Delete
	public Object delete(@SqlParameter("m") Demo m) {
		return "delete from demo where name=#{m.name}";
	}

	/**
	 * 批量删除
	 * 
	 * @param List
	 * @return
	 */
	@Author("shipeng.hou")
	@Shard(indexName = "demo_index", indexColumn = "m." + ReservedWord.index
			+ ".name")
	@Delete
	public Object batchDelete(@SqlParameter("m") List<Demo> List) {
		return "delete from demo where name=#{m." + ReservedWord.index + ".name}";
	}

}
