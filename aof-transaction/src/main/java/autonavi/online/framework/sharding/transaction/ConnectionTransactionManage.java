package autonavi.online.framework.sharding.transaction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import autonavi.online.framework.sharding.transaction.holder.TransactionAttributeHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionManagerHolder;
import autonavi.online.framework.sharding.transaction.holder.TxManagerHolder;
import autonavi.online.framework.sharding.transaction.holder.TxManagerStackHolder;
import autonavi.online.framework.util.ConcurrentStack;

/**
 * 事务管理类，提交或回滚事务。<br/>
 * 注意，事务关闭时是按事务出栈时的顺序进行的。
 * 
 * @author jia.miao
 * 
 */
public class ConnectionTransactionManage {
	private static Logger logger = LogManager
			.getLogger(ConnectionTransactionManage.class);

	/**
	 * 关闭连接 关闭由Spring commit和rollBack控制
	 * 
	 * @param dcMaps
	 */
	@Deprecated
	public static void releaseConnection(
			ConcurrentStack<Map<String, Object>> connectionStack) {
		int size = connectionStack.size();
		for (int i = 0; i < size; i++) {
			Map<String, Object> dcMap = connectionStack.pop();
			DataSourceUtils.releaseConnection((Connection) dcMap.get("conn"),
					(DataSource) dcMap.get("ds"));
		}
	}

	/**
	 * 默认不启动事务 堆一个空对象到事务栈中 便于后期关闭每层嵌套的连接 需要注意在关闭和提交判定是否为空
	 */
	public static void begin(Integer dataSourceKey) {
		// TxManagerHolder holder = new TxManagerHolder("", dataSourceKey, null,
		// null);
		// TransactionHolder.setTransaction(holder);
	}

	/**
	 * 开始事务
	 * 
	 * @param dataSourceKey
	 * @param ds
	 */
	public static void begin(Integer dataSourceKey, DataSource ds,
			PlatformTransactionManager tm) {
		TransactionAttribute tah = null;
		if (TransactionAttributeHolder.getTransactionAttributeStackHolder()
				.size() > 0
				&& TransactionHolder.getTransactionStockHolder().size() + 1 == TransactionAttributeHolder
						.getTransactionAttributeStackHolder().size()) {
			tah = TransactionAttributeHolder
					.getTransactionAttributeStackHolder().pop();// 从本地线程中得到属性
		} else {
			// DAO使用 或者没有配置传播级别时候使用 走默认配置
			tah = TransactionAttributeHolder.getTransactionAttributeHolder();
		}
		TransactionAttributeHolder.getTransactionAttributeStackHolder().push(
				tah);
		getTracsactionManager(dataSourceKey, ds, tah, tm);
	}

	/**
	 * 提交事务
	 * 
	 * @param transactionMaps
	 */
	private static void commit(
			ConcurrentStack<TxManagerHolder> transactionStack,
			TransactionAttribute ta) {
		if (ta == null
				&& TransactionAttributeHolder
						.getTransactionAttributeStackHolder().size() > 0) {
			// 事务管理属性出栈
			// 可能是completeTransactionAfterThrowing传过来的，因此需要判定
			TransactionAttributeHolder.getTransactionAttributeStackHolder()
					.pop();
		}
		int size = transactionStack.size();
		for (int i = 0; i < size; i++) {
			TxManagerHolder holder = (TxManagerHolder) transactionStack.pop();
			PlatformTransactionManager tran = holder.getTm();
			TransactionStatus status = holder.getStatus();

			if (tran != null && status != null) {
				if (status instanceof DefaultTransactionStatus
						&& !((DefaultTransactionStatus) status)
								.hasTransaction()) {
					if (!status.isCompleted()) {
						tran.commit(status);
					}
				} else {
					tran.commit(status);
				}

			}
		}
	}

	/**
	 * 提交事务
	 * 
	 * @param transactionMaps
	 */
	public static void commit(ConcurrentStack<TxManagerHolder> transactionStack) {
		commit(transactionStack, null);
	}

	/**
	 * 回滚事务
	 * 
	 * @param transactionStack
	 */
	public static void rollback(
			ConcurrentStack<TxManagerHolder> transactionStack) {
		TransactionAttribute transactionAttribute = null;
		if (TransactionAttributeHolder.getTransactionAttributeStackHolder()
				.size() > 0) {
			// 事务管理属性出栈
			transactionAttribute = TransactionAttributeHolder
					.getTransactionAttributeStackHolder().pop();
		}
		rollback(transactionStack, transactionAttribute);
	}

	/**
	 * 回滚事务
	 * 
	 * @param transactionMaps
	 */
	private static void rollback(
			ConcurrentStack<TxManagerHolder> transactionStack,
			TransactionAttribute ta) {
		TransactionAttribute transactionAttribute = ta;
		int size = transactionStack.size();
		if (transactionAttribute == null
				|| (transactionAttribute.getPropagation().value() != Propagation.NESTED
						.value()
						&& transactionAttribute.getPropagation().value() != Propagation.REQUIRES_NEW
								.value() && transactionAttribute
						.getPropagation().value() != Propagation.NOT_SUPPORTED
						.value())) {
			for (int i = 0; i < size; i++) {
				TxManagerHolder holder = (TxManagerHolder) transactionStack
						.pop();
				PlatformTransactionManager tran = holder.getTm();
				TransactionStatus status = holder.getStatus();
				if (tran != null && status != null) {
					tran.rollback(status);
				}
			}
			// 处理剩余堆栈
			ConcurrentStack<TxManagerStackHolder> stack = TransactionHolder
					.getTransactionStockHolder();
			size = stack.size();
			List<TxManagerStackHolder> _tmList = new ArrayList<TxManagerStackHolder>();
			for (int i = 0; i < size; i++) {
				TxManagerStackHolder _stack = stack.pop();
				transactionAttribute = _stack.getTa();
				_tmList.add(new TxManagerStackHolder(_stack.getTa(),
						new ConcurrentStack<TxManagerHolder>()));
				int _size = _stack.getStack().size();
				for (int j = 0; j < _size; j++) {
					TxManagerHolder holder = (TxManagerHolder) _stack
							.getStack().pop();
					PlatformTransactionManager tran = holder.getTm();
					TransactionStatus status = holder.getStatus();
					if (tran != null && status != null) {
						tran.rollback(status);
					}
				}
				if (transactionAttribute.getPropagation().value() == Propagation.NESTED
						.value()
						|| transactionAttribute.getPropagation().value() == Propagation.REQUIRES_NEW
								.value()
						|| transactionAttribute.getPropagation().value() == Propagation.NOT_SUPPORTED
								.value()) {
					// 截止到此处跳出回滚,此层次的前面层次可以继续转播事务
					break;
				}
			}

			for (ListIterator<TxManagerStackHolder> it = _tmList
					.listIterator(_tmList.size()); it.hasPrevious();) {
				TransactionHolder.getTransactionStockHolder().push(
						it.previous());
			}

		} else {
			// NESTED REQUIRES_NEW 特殊处理 只做本次的回滚操作
			if (transactionAttribute.getPropagation().value() == Propagation.NESTED
					.value()
					|| transactionAttribute.getPropagation().value() == Propagation.REQUIRES_NEW
							.value()
					|| transactionAttribute.getPropagation().value() == Propagation.NOT_SUPPORTED
							.value()) {
				for (int i = 0; i < size; i++) {
					TxManagerHolder holder = (TxManagerHolder) transactionStack
							.pop();
					PlatformTransactionManager tran = holder.getTm();
					TransactionStatus status = holder.getStatus();
					if (tran != null && status != null) {
						tran.rollback(status);
					}
				}
			}
		}
	}

	/**
	 * 针对配置的rollback-for 和no-rollback-for处理异常抛出后是否回滚
	 * 
	 * @param ta
	 * @param t
	 */
	public static void completeTransactionAfterThrowing(Throwable t)
			throws Throwable {
		TransactionAttribute ta = null;
		if (TransactionAttributeHolder.getTransactionAttributeStackHolder()
				.size() > 0) {
			ta = TransactionAttributeHolder
					.getTransactionAttributeStackHolder().pop();
		}
		if (ta != null) {
			logger.debug("检查异常的rollback-for和no-rollback-for状态");
			if (ta.rollbackOn(t)) {
				logger.error(t.getMessage(), t);
				ConnectionTransactionManage.rollback(
						TransactionHolder.getTransactionHolder(), ta);
				throw t;
			} else {
				logger.error(t.getMessage(), t);
				ConnectionTransactionManage.commit(
						TransactionHolder.getTransactionHolder(), ta);
			}
		} else {
			logger.error(t.getMessage(), t);
			ConnectionTransactionManage.rollback(
					TransactionHolder.getTransactionHolder(), null);
			throw t;
		}
	}

	/**
	 * 设置当前数据库操作的事务管理器
	 * 
	 * @param dataSourceKey
	 * @param ds
	 * @param tah
	 * @param transactionManager
	 */
	private static void getTracsactionManager(Integer dataSourceKey,
			DataSource ds, TransactionAttribute tah,
			PlatformTransactionManager transactionManager) {
		PlatformTransactionManager ptm = TransactionManagerHolder
				.getTransactionManager();
		if (ptm != null && ptm instanceof JtaTransactionManager) {
			transactionManager = ptm;
			// JTA为单管理器 直接设置数据源永远为1 和下面代码可以做到兼容
			// 实际上每次第一层切面进来这个就定下来了 各个层次在JTA下面只要传播即可，不会出现找层的情况
			dataSourceKey = 1;
		}
		if (transactionManager == null) {
			throw new RuntimeException("必须使用jdbc数据源或者jta数据源,请检查配置");
		}
		Integer level = TransactionHolder.getTransactionStockHolder().size();
		String key = dataSourceKey + "_" + level;
		TxManager tx = findExistTransactionMananger(dataSourceKey, level);
		TxManagerHolder txh = tx.getTxManagerHolder();
		if (txh != null) {
			PlatformTransactionManager tm = txh.getTm();
			if (!key.equals(tx.getKey())) {
				transactionManager = tm;
				TransactionStatus status = getStatus(tah, transactionManager);
				TxManagerHolder holder = new TxManagerHolder(dataSourceKey,
						transactionManager, status);
				stackTransactionManager(holder);
				TransactionHolder.getTransactionManagerMap().put(key, holder);

			} else {
				// 如果前面回滚过本层，会把已经回滚的事务管理器放入堆栈，等待Spring抛出事务已经结束的异常
				// 这种问题一般是默认传播级别下 用户使用try/catch吃掉上层异常导致
				// 放入就是为了规避这种用法，因为吃掉异常不符合事务传播机制
				// 请使用Nested或者no-rollback for解决这类问题
				stackTransactionManager(txh);
			}
		} else {
			// 在查找不到的情况下，从首层或者第一个REQUIRES_NEW的层次重新传播
			TransactionStatus status = rebuildTransactionsStatus(dataSourceKey,
					transactionManager, tah);
			TxManagerHolder holder = new TxManagerHolder(dataSourceKey,
					transactionManager, status);
			stackTransactionManager(holder);
			TransactionHolder.getTransactionManagerMap().put(key, holder);
		}
	}

	private static TransactionStatus rebuildTransactionsStatus(
			Integer dataSourceKey,
			PlatformTransactionManager transactionManager,
			TransactionAttribute tah) {
		TransactionStatus status = null;
		if (tah.propagation.value() == Propagation.REQUIRES_NEW.value()
				|| tah.propagation.value() == Propagation.NOT_SUPPORTED.value()) {
			status = getStatus(tah, transactionManager);
		} else {
			if (TransactionHolder.getTransactionStockHolder().size() > 0) {
				List<TxManagerStackHolder> _txsl = new ArrayList<TxManagerStackHolder>();
				// 共有栈大小
				int pubStackSize = TransactionHolder
						.getTransactionStockHolder().size();
				for (int i = 0; i < pubStackSize; i++) {
					TxManagerStackHolder _txsh = TransactionHolder
							.getTransactionStockHolder().pop();
					_txsl.add(_txsh);
					if (_txsh.getTa().propagation.value() == Propagation.REQUIRES_NEW
							.value()
							|| _txsh.getTa().propagation.value() == Propagation.NOT_SUPPORTED
									.value()) {
						break;
					}
				}
				// 处理私有栈
				for (ListIterator<TxManagerStackHolder> it = _txsl
						.listIterator(_txsl.size()); it.hasPrevious();) {
					TxManagerStackHolder _tsh = it.previous();
					TransactionStatus _status = getStatus(_tsh.getTa(),
							transactionManager);
					TxManagerHolder holder = new TxManagerHolder(dataSourceKey,
							transactionManager, _status);
					_tsh.getStack().push(holder);
					String key = dataSourceKey
							+ "_"
							+ TransactionHolder.getTransactionStockHolder()
									.size();
					TransactionHolder.getTransactionManagerMap().put(key,
							holder);
					TransactionHolder.getTransactionStockHolder().push(_tsh);
				}
				// 传播到当前层次
				status = getStatus(tah, transactionManager);

			} else {
				status = getStatus(tah, transactionManager);
			}
		}
		return status;
	}

	/**
	 * 开启事务传播
	 * 
	 * @param tah
	 * @param transactionManager
	 * @return
	 */
	private static TransactionStatus getStatus(TransactionAttribute tah,
			PlatformTransactionManager transactionManager) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition(); // 事务定义类
		def.setPropagationBehavior(tah.getPropagation().value());// 传播模式
		def.setIsolationLevel(tah.getIsolation().value());// 隔离级别
		def.setReadOnly(tah.isReadOnly());// 只读
		def.setTimeout(tah.getTimeout());// 超时时间
		TransactionStatus status = transactionManager.getTransaction(def); // 开始事务并返回事务状态
		return status;
	}

	/**
	 * 查找存在的事务管理器
	 * 
	 * @param dataSourceKey
	 * @param level
	 * @return
	 */
	private static TxManager findExistTransactionMananger(
			Integer dataSourceKey, Integer level) {
		Object _obj = null;
		String findKey = "";
		List<TransactionAttribute> tempList = new ArrayList<TransactionAttribute>();
		for (int i = level; i >= 0; i--) {
			TransactionAttribute _ta = TransactionAttributeHolder
					.getTransactionAttributeStackHolder().pop();
			findKey = dataSourceKey + "_" + i;
			_obj = TransactionHolder.getTransactionManagerMap().get(findKey);
			tempList.add(_ta);
			if (_ta.propagation.value() == Propagation.REQUIRES_NEW.value()
					|| _ta.propagation.value() == Propagation.NOT_SUPPORTED
							.value()) {
				break;
			}
			if (_obj != null) {
				break;
			}
		}
		// 恢复堆栈
		for (ListIterator<TransactionAttribute> it = tempList
				.listIterator(tempList.size()); it.hasPrevious();) {
			TransactionAttributeHolder.getTransactionAttributeStackHolder()
					.push(it.previous());
		}
		TxManagerHolder holder = (TxManagerHolder) _obj;
		TxManager tx = new TxManager();
		tx.setTxManagerHolder(holder);
		tx.setKey(findKey);
		return tx;
	}

	private static void stackTransactionManager(TxManagerHolder holder) {
		TransactionHolder.setTransaction(holder);
	}

	public static class TxManager {
		private String key;
		private TxManagerHolder txManagerHolder;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public TxManagerHolder getTxManagerHolder() {
			return txManagerHolder;
		}

		public void setTxManagerHolder(TxManagerHolder txManagerHolder) {
			this.txManagerHolder = txManagerHolder;
		}
	}
}
