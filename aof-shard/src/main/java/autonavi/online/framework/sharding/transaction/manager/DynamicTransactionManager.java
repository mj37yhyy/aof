package autonavi.online.framework.sharding.transaction.manager;

import javax.sql.DataSource;

import autonavi.online.framework.sharding.dao.ds.LoadBalancingDataSource;

public interface DynamicTransactionManager {
	public void setTargetTransactionManagers(LoadBalancingDataSource ds);
	public void cleanTargetTransactionManagers();
	public  <T> T getTransactionMananger(DataSource ds);
}
