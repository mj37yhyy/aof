package autonavi.online.framework.sharding.transaction.holder;

import autonavi.online.framework.sharding.transaction.TransactionAttribute;
import autonavi.online.framework.util.ConcurrentStack;

/**
 * 事务属性持有者
 * 
 * @author jia.miao
 * @author yaming.xu
 * 
 */
public class TransactionAttributeHolder {
	private static final ThreadLocal<TransactionAttribute> transactionAttributeHolder = new ThreadLocal<TransactionAttribute>();
	private static final ThreadLocal<ConcurrentStack<TransactionAttribute>> transactionAttributeStackHolder = new ThreadLocal<ConcurrentStack<TransactionAttribute>>();
	private static final ThreadLocal<Integer> transactionCommotHolder = new ThreadLocal<Integer>();

	public static void cleanAllHolder(){
		transactionAttributeHolder.remove();
		transactionAttributeStackHolder.remove();
		transactionCommotHolder.remove();
	}
	
	public static Integer getTransactionCommotHolder() {
		if (transactionCommotHolder.get() == null) {
			Integer l = new Integer(1);
			transactionCommotHolder.set(l);
			return l;
		}
		Integer l = transactionCommotHolder.get();
		l++;
		transactionCommotHolder.set(l);
		return l;

	}
	/**
	 * 获取事务所有者的堆栈
	 * @return
	 */
	public static ConcurrentStack<TransactionAttribute> getTransactionAttributeStackHolder(){
		ConcurrentStack<TransactionAttribute> stack=beforeStack();
		return stack;
	}

	/**
	 * 获取事务属性持有者
	 * 
	 * @return
	 */
	public static TransactionAttribute getTransactionAttributeHolder() {
		TransactionAttribute transactionAttribute = before();
		return transactionAttribute;
	}

	/**
	 * 插入事务属性
	 * 
	 * @param propagation
	 * @param isolation
	 * @param timeout
	 * @param readOnly
	 */
	public static void setTransactionAttribute(
			TransactionAttribute transactionAttribute) {
		transactionAttributeHolder.set(transactionAttribute);
	}

	/**
	 * 初始化。
	 * 
	 * @return
	 */
	private static TransactionAttribute before() {
		TransactionAttribute transactionAttribute = transactionAttributeHolder
				.get();
		if (transactionAttribute == null) {
			transactionAttribute = new TransactionAttribute();
			transactionAttributeHolder.set(transactionAttribute);
		}
		return transactionAttribute;
	}
	/**
	 * 初始化
	 * @return
	 */
	private static ConcurrentStack<TransactionAttribute> beforeStack() {
		ConcurrentStack<TransactionAttribute> stack=transactionAttributeStackHolder.get();
		if(stack==null){
			stack=new ConcurrentStack<TransactionAttribute>();
			transactionAttributeStackHolder.set(stack);
		}
		return stack;
	}
}
