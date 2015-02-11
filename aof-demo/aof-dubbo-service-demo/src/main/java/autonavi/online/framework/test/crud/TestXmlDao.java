package autonavi.online.framework.test.crud;

import java.util.List;

import autonavi.online.framework.sharding.entry.aspect.annotation.Author;

public interface TestXmlDao {

	@Author("jia.miao")
	public List<Long> queryDemoIds();
}
