package autonavi.online.framework.shard;

import autonavi.online.framework.sharding.entry.aspect.annotation.Author;
import autonavi.online.framework.sharding.entry.aspect.annotation.Select;
import autonavi.online.framework.sharding.entry.aspect.annotation.SingleDataSource;

public class TestDao {
	
	@Author("jia.miao")
	@SingleDataSource(1)
	@Select
	//@Shard(indexName="demo_index",indexColumn="")
	public Object select1() {
		return "select 1";
	}
}
