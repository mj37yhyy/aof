package autonavi.online.framework.test.service;

import java.util.List;
import java.util.Map;

import autonavi.online.framework.test.entity.Demo;

public interface TestService {
	public List<Long> userCallbackSelect(String name);

	public Long singleFieldSelect(String name, int dskey);

	public List<Demo> pagingSelect(String name, int skip, int size);

	public List<Map> batchSelect(String[] names);

	public List<Map> batchSelect2(String name, long[] id);

	public void insert(String name);

	public void batchInsert(String[] names);

	public void update(String name);

	public void batchUpdate(String[] names);

	public void delete(String name);

	public void batchDelete(String[] names);
}
