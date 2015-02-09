package autonavi.online.framework.test.crud;

import org.springframework.stereotype.Repository;

import autonavi.online.framework.sharding.entry.aspect.annotation.Author;
import autonavi.online.framework.sharding.entry.aspect.annotation.Select;
import autonavi.online.framework.sharding.entry.aspect.annotation.SingleDataSource;

@Repository
public class TestDao {

	@Author("jia.miao")
	@SingleDataSource(1)
	@Select
	// @Shard(indexName="demo_index",indexColumn="")
	public Object select1() {
		return "select 1";
	}
}
