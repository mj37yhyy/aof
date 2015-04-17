package autonavi.online.framework.sharding.transaction.holder;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import autonavi.online.framework.sharding.transaction.TransactionAttribute;
import autonavi.online.framework.util.ConcurrentStack;

/**
 * 事务持有者
 * @author jia.miao
 *
 */
public class TransactionHolder {
	private static final ThreadLocal<ConcurrentStack<TxManagerHolder>> transactionHolder = new ThreadLocal<ConcurrentStack<TxManagerHolder>>();
	private static final ThreadLocal<Boolean> isServiceTransactionHolder = new ThreadLocal<Boolean>();
	private static final ThreadLocal<ConcurrentStack<TxManagerStackHolder>> transactionStackHolder=new ThreadLocal<ConcurrentStack<TxManagerStackHolder>>();
	private static final ThreadLocal<ConcurrentHashMap<String,Object>> transactionManagerMap  = new ThreadLocal<ConcurrentHashMap<String,Object>>();
	

	public static void cleanAllHolder(){
		transactionHolder.remove();
		isServiceTransactionHolder.remove();
		transactionStackHolder.remove();
		transactionManagerMap.remove();
	}
	public static void cleanTransactionManagerMap(){
		Map<String,Object> _m=transactionManagerMap.get();
		List<String> _l=new ArrayList<String>();
		if(_m!=null){
			for(String _key:_m.keySet()){
				if(_key.endsWith("_"+beforeStock().size())){
					//清理本层的存储的事务引用
					_l.add(_key);
				}
			}
			for(String key:_l){
				_m.remove(key);
			}
		}
		
	}
	
	/**
	 * 获取事务持有者
	 * @return
	 */
	public static ConcurrentStack<TxManagerHolder> getTransactionHolder() {
		ConcurrentStack<TxManagerHolder> transactionStack = before();
		return transactionStack;
	}
	/**
	 * 获取嵌套事务的持有者
	 * @return
	 */
	public static ConcurrentStack<TxManagerStackHolder> getTransactionStockHolder() {
		ConcurrentStack<TxManagerStackHolder> transactionStacks=beforeStock();
		return transactionStacks;
	}
	/**
	 * 刷新事务持有者
	 * @param isNew
	 */
	public static void refreshTransactionHolder(boolean isNew){
		if(isNew||beforeStock().size()==0){
			ConcurrentStack<TxManagerHolder> transactionStack=new ConcurrentStack<TxManagerHolder>();
			transactionHolder.set(transactionStack);
		}else{
			transactionHolder.set(beforeStock().pop().getStack());
		}
		
	}

	/**
	 * 插入事务
	 * @param dsKey
	 * @param tran
	 * @param status
	 */
	public static void setTransaction(TxManagerHolder holder) {
		
		ConcurrentStack<TxManagerHolder> transactionStack = before();
		int size = transactionStack.size();
		boolean isNew=true;
		List<TxManagerHolder> tempList=new ArrayList<TxManagerHolder>();
		for(int i=0;i<size;i++){
			TxManagerHolder _holder=transactionStack.pop();
			if(holder==_holder){
				isNew=false;
			}
			tempList.add(_holder);
		}
		// 恢复堆栈
		for (ListIterator<TxManagerHolder> it = tempList
				.listIterator(tempList.size()); it.hasPrevious();) {
			transactionStack.push(it.previous());
		}
		if(isNew){
			transactionStack.push(holder);	
		}
		transactionHolder.set(transactionStack);
	}
	/**
	 * 插入嵌套事务
	 * @param stock
	 */
	
	public static void setTransactionStack(ConcurrentStack<TxManagerHolder> stock){
		if(TransactionAttributeHolder.getTransactionAttributeStackHolder().size()>1){
			TransactionAttribute _new=TransactionAttributeHolder.getTransactionAttributeStackHolder().pop();
			TransactionAttribute _old=TransactionAttributeHolder.getTransactionAttributeStackHolder().pop();
			TxManagerStackHolder holder=new TxManagerStackHolder(_old,stock);
			beforeStock().push(holder);
			TransactionAttributeHolder.getTransactionAttributeStackHolder().push(_old);
			TransactionAttributeHolder.getTransactionAttributeStackHolder().push(_new);
		}
		
	}

	/**
	 * 初始化
	 * @return
	 */
	private static ConcurrentStack<TxManagerHolder> before() {
		ConcurrentStack<TxManagerHolder> transactionStack = transactionHolder.get();
		if (transactionStack == null) {
			transactionStack = new ConcurrentStack<TxManagerHolder>();
			transactionHolder.set(transactionStack);
		}
		return transactionStack;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	private static ConcurrentStack<TxManagerStackHolder> beforeStock() {
		ConcurrentStack<TxManagerStackHolder> transactionStacks = transactionStackHolder.get();
		if (transactionStacks == null) {
			transactionStacks = new ConcurrentStack<TxManagerStackHolder>();
			transactionStackHolder.set(transactionStacks);
		}
		return transactionStacks;
	}

	/**
	 * 插入是否有Dao以外的类在使用事务
	 * @param isServiceTransaction
	 */
	public static void setIsServiceTransaction(boolean isServiceTransaction) {
		isServiceTransactionHolder.set(isServiceTransaction);
	}

	/**
	 * 是否有Dao以外的类在使用事务
	 * @return
	 */
	public static boolean isServiceTransaction() {
		if (isServiceTransactionHolder.get() != null)
			return isServiceTransactionHolder.get();
		return false;
	}
	
	/**
	 * 事务管理器哈希表
	 * @return
	 */
	public static Map<String, Object> getTransactionManagerMap() {
		if(transactionManagerMap.get()==null){
			transactionManagerMap.set(new ConcurrentHashMap<String,Object>());
		}
		return transactionManagerMap.get();
	}
	
	
}
