package autonavi.online.framework.sharding.transaction.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import autonavi.online.framework.sharding.annotation.DynamicTransactionManagerCase;
import autonavi.online.framework.sharding.dao.ds.LoadBalancingDataSource;
@DynamicTransactionManagerCase
public class DynamicTransactionManagerImpl implements DynamicTransactionManager {
	private static Map<DataSource, PlatformTransactionManager> targetTransactionManagers = Collections
			.synchronizedMap(new HashMap<DataSource, PlatformTransactionManager>());
	@Override
	public synchronized void setTargetTransactionManagers(LoadBalancingDataSource ds) {
		Map<String, DataSource> realMap = ds.getRealDataSourceMap();
		for (String key : realMap.keySet()) {
			DataSource realDataSource = realMap.get(key);
			targetTransactionManagers.put(realDataSource,
					new DataSourceTransactionManager(realDataSource));
		}
	}

	@Override
	public synchronized void cleanTargetTransactionManagers() {
		targetTransactionManagers.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public PlatformTransactionManager getTransactionMananger(DataSource ds) {
		return targetTransactionManagers.get(ds);
	}

}
