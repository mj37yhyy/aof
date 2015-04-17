package autonavi.online.framework.sharding.index;

import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.dao.AbstractDataSourceRoute;
import autonavi.online.framework.sharding.transaction.manager.DynamicTransactionManager;

public class ShardInitEntity {
	private AbstractDataSourceRoute dataSourceRoute;
	private AbstractDaoSupport daoSupport;
	private DynamicTransactionManager dynamicTransactionManager;

	public AbstractDataSourceRoute getDataSourceRoute() {
		return dataSourceRoute;
	}

	public void setDataSourceRoute(AbstractDataSourceRoute dataSourceRoute) {
		this.dataSourceRoute = dataSourceRoute;
	}

	public AbstractDaoSupport getDaoSupport() {
		return daoSupport;
	}

	public void setDaoSupport(AbstractDaoSupport daoSupport) {
		this.daoSupport = daoSupport;
	}

	public DynamicTransactionManager getDynamicTransactionManager() {
		return dynamicTransactionManager;
	}

	public void setDynamicTransactionManager(
			DynamicTransactionManager dynamicTransactionManager) {
		this.dynamicTransactionManager = dynamicTransactionManager;
	}

}
