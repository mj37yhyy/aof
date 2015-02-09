package autonavi.online.framework.sharding.transaction;

import org.springframework.transaction.PlatformTransactionManager;

import autonavi.online.framework.sharding.transaction.holder.TransactionAttributeHolder;
import autonavi.online.framework.sharding.transaction.support.ShardingTransactionAspectSupport;

/**
 * 该类用于编码调用事务的情况
 * 
 * @author jia.miao
 * 
 */
public class ShardingTransactionTemplate extends
		ShardingTransactionAspectSupport {

	public ShardingTransactionTemplate(
			PlatformTransactionManager transactionManager) {
		super.setTransactionManager(transactionManager);
	}

	public Object execute(
			final ShardingTransactionCallback shardingTransactionCallback)
			throws Throwable {

		TransactionAttributeHolder.getTransactionAttributeStackHolder().push(
				transactionAttribute);
		return invokeWithinTransaction(null, null, new InvocationCallback() {
			@Override
			public Object proceedWithInvocation() throws Throwable {
				return shardingTransactionCallback.doInTransaction();
			}
		});
	}

	private TransactionAttribute transactionAttribute = new TransactionAttribute();

	public void setTransactionAttribute(
			TransactionAttribute transactionAttribute) {
		this.transactionAttribute = transactionAttribute;
	}

}
