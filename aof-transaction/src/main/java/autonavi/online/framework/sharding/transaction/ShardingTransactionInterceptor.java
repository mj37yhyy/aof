package autonavi.online.framework.sharding.transaction;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import autonavi.online.framework.sharding.transaction.holder.TimeOutHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionAttributeHolder;
import autonavi.online.framework.sharding.transaction.support.ShardingTransactionAspectSupport;

@SuppressWarnings("serial")
public class ShardingTransactionInterceptor extends ShardingTransactionAspectSupport implements MethodInterceptor,
		Serializable {

	private List<TransactionAttribute> transactionAttributes = null;

	public void setTransactionAttributes(
			List<TransactionAttribute> transactionAttributes) {
		for (TransactionAttribute transactionAttribute : transactionAttributes) {
			transactionAttribute.setName(transactionAttribute.getName()
					.replace("*", ".*"));
		}
		this.transactionAttributes = transactionAttributes;
	}

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();// 得到目标方法
		String methodName = method.getName();// 得到方法名
        /**
         * 此处AOP需要注意
         * 如果出现同一个Service中方法调用方法的情况
         * 请不要使用this调用
         * 请使用Service注入Bean的方式进行调用
         * @Service("demoService")
           public class DemoServiceImpl implements DemoService {
              @Resource(name="demoService")
              private DemoService demoService;
           }
         */
		for (TransactionAttribute transactionAttribute : transactionAttributes) {
			Pattern pattern = Pattern.compile(transactionAttribute.getName());
			Matcher matcher = pattern.matcher(methodName);
			if (matcher.matches()) { // 当条件满足时，将返回true，否则返回false
				// 将事务的属性放到本地线程中，方便使用
//				TransactionAttributeHolder.setTransactionAttribute(transactionAttribute);
				TimeOutHolder.setTimeOut(transactionAttribute.getTimeout());
				TransactionAttributeHolder.getTransactionAttributeStackHolder().push(transactionAttribute);
				/**注意，如一个方法多次满足条件，将以最后一次的为准*/
				return invokeWithinTransaction(invocation.getMethod(), invocation.getThis().getClass(), new InvocationCallback() {
					@Override
					public Object proceedWithInvocation() throws Throwable {
						return invocation.proceed();
					}
				});
			}
		}
		return invocation.proceed();

		
	}

}
