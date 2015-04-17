package autonavi.online.framework.sharding.transaction.holder;

import org.springframework.transaction.PlatformTransactionManager;

public class TransactionManagerHolder {

	private static final ThreadLocal<PlatformTransactionManager> transactionManagerHolder = new ThreadLocal<PlatformTransactionManager>();

	public static void cleanAllHolder(){
		transactionManagerHolder.remove();
	}
	
	public static void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		transactionManagerHolder.set(transactionManager);
	}

	public static PlatformTransactionManager getTransactionManager() {
		return transactionManagerHolder.get();
	}
}
