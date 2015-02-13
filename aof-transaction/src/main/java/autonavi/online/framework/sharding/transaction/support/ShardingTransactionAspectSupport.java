package autonavi.online.framework.sharding.transaction.support;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;

import autonavi.online.framework.sharding.holder.DataSourceHolder;
import autonavi.online.framework.sharding.holder.TimeOutHolder;
import autonavi.online.framework.sharding.transaction.ConnectionTransactionManage;
import autonavi.online.framework.sharding.transaction.holder.TransactionAttributeHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionManagerHolder;
import autonavi.online.framework.util.StopWatchLogger;

public abstract class ShardingTransactionAspectSupport {
	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(this.getClass());
	private PlatformTransactionManager transactionManager = null;

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	protected Object invokeWithinTransaction(Method method,
			Class<?> targetClass, final InvocationCallback invocation)
			throws Throwable {
		
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("调用用户方法");
		TransactionManagerHolder.setTransactionManager(this.transactionManager);// 将事务管理器放入本地线程供路由调用
		Object result = null;
//		TransactionAttribute transactionAttribute = TransactionAttributeHolder
//				.getTransactionAttributeHolder();// 获取当前线程的事务属性
		//Integer index = TransactionAttributeHolder.getTransactionCommotHolder();
		try {
			//新方法 先处理上次事务的堆栈 最外层跳过 只有出现切面调用切面 才做初始化 最外层不做
			if(TransactionAttributeHolder.getTransactionAttributeStackHolder().size()>1){
				TransactionHolder.setTransactionStack(TransactionHolder.getTransactionHolder());
				TransactionHolder.refreshTransactionHolder(true);
			}
			
			// 此时代表事务切面已经找到，关闭DAO层的事物管理功能
			TransactionHolder.setIsServiceTransaction(true);
			// 执行操作
			result = invocation.proceedWithInvocation();
			swlogger.stop();
			swlogger.start("提交事务");
			// 提交事务
//			if (index == 1)
				ConnectionTransactionManage.commit(TransactionHolder
						.getTransactionHolder());
			swlogger.stop();
		} catch (Throwable t) {
			swlogger.start("回滚事务");
			// 根据用户配置的rollback-for和no-rollback-for进行回滚
			ConnectionTransactionManage.completeTransactionAfterThrowing(t);
			swlogger.stop();
		} finally {
			swlogger.start("释放资源");
			// 释放资源
//			if (index == 1)
				//切换堆栈 向上递归 最外层跳过
				if(TransactionAttributeHolder.getTransactionAttributeStackHolder().size()>0){
					TransactionHolder.cleanTransactionManagerMap();
					TransactionHolder.refreshTransactionHolder(false);
					TimeOutHolder.refreshTimeOut();
				}else{
					//初始化DAO提交判定 防止多线程复用问题
					TransactionAttributeHolder.cleanAllHolder();
					TransactionManagerHolder.cleanAllHolder();
					TransactionHolder.cleanAllHolder();
					TimeOutHolder.cleanAllHolder();
					DataSourceHolder.cleanAllHolder();
				}
			swlogger.stop();
			swlogger.writeLog();
		}

		return result;
	}

	/**
	 * Simple callback interface for proceeding with the target invocation.
	 * Concrete interceptors/aspects adapt this to their invocation mechanism.
	 */
	protected interface InvocationCallback {

		Object proceedWithInvocation() throws Throwable;
	}

}
