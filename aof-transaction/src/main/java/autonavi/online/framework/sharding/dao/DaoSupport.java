package autonavi.online.framework.sharding.dao;

import autonavi.online.framework.sharding.annotation.DaoSupportCase;
import autonavi.online.framework.sharding.transaction.ConnectionTransactionManage;
import autonavi.online.framework.sharding.transaction.holder.TimeOutHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionAttributeHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionManagerHolder;
@DaoSupportCase
public class DaoSupport extends AbstractDaoSupport {
	
	/**
	 * 提交事务
	 */
	protected void commit() {
		if (!TransactionHolder.isServiceTransaction()) {
			// 如果没有service管理事务，则自己提交事务
			ConnectionTransactionManage.commit(TransactionHolder
					.getTransactionHolder());
		}
	}

	/**
	 * 回滚事务
	 */
	protected void rollback() {
		if (!TransactionHolder.isServiceTransaction()) {
			// 如果没有service管理事务，则自己回滚事务
			ConnectionTransactionManage.rollback(TransactionHolder
					.getTransactionHolder());
		}
	}

	/**
	 * 释放资源
	 */
	protected void release() {
		if (!TransactionHolder.isServiceTransaction()) {
			// 初始化DAO提交判定 防止多线程复用问题
			TransactionAttributeHolder.cleanAllHolder();
			TransactionManagerHolder.cleanAllHolder();
			TransactionHolder.cleanAllHolder();
			TimeOutHolder.cleanAllHolder();
		}
	}

	

}
