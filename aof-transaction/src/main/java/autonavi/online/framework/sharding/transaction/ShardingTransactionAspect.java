package autonavi.online.framework.sharding.transaction;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import autonavi.online.framework.sharding.transaction.annotation.ShardingTransactional;
import autonavi.online.framework.sharding.transaction.holder.TimeOutHolder;
import autonavi.online.framework.sharding.transaction.holder.TransactionAttributeHolder;
import autonavi.online.framework.sharding.transaction.support.ShardingTransactionAspectSupport;

@Aspect
public class ShardingTransactionAspect extends ShardingTransactionAspectSupport {

	public ShardingTransactionAspect() {

	}

	@Pointcut("@annotation(autonavi.online.framework.sharding.transaction.annotation.ShardingTransactional)")
	public void shardingTransactional() {

	}

	/**
	 * 最终通知，用于关闭资源<br/>
	 * 从本地线程里获取所有用到连接，当service方法结束时，一起关闭
	 * 
	 * @return
	 * @throws Throwable
	 */
	@Around("shardingTransactional()")
	public Object shardingTransactionalAround(
			final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Class targetClass = proceedingJoinPoint.getTarget().getClass();
		ShardingTransactional shardingTransactional = (ShardingTransactional) targetClass
				.getAnnotation(ShardingTransactional.class);// 得到方法上的ShardingTransactional注解

		MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint
				.getSignature();
		Method method = methodSignature.getMethod();// 得到目标方法
		ShardingTransactional subShardingTransactional = method
				.getAnnotation(ShardingTransactional.class);// 得到方法上的ShardingTransactional注解
		if (subShardingTransactional == null
				&& method.getDeclaringClass().isInterface()) {
			Method implMethod = targetClass.getMethod(method.getName(),
					method.getParameterTypes());
			subShardingTransactional = implMethod
					.getAnnotation(ShardingTransactional.class);
		}

		if (subShardingTransactional != null) {// 如果方法上存在该注解，则覆盖类上的
			shardingTransactional = subShardingTransactional;
		}

		/**
		 * 将注解中的信息转移到实体类中
		 */
		TransactionAttribute transactionAttribute = new TransactionAttribute();

		transactionAttribute.setIsolation(shardingTransactional.isolation());
		transactionAttribute
				.setPropagation(shardingTransactional.propagation());
		transactionAttribute.setReadOnly(shardingTransactional.readOnly());
		transactionAttribute.setTimeout(shardingTransactional.timeout());
		transactionAttribute
				.setRollbackFor(shardingTransactional.rollbackFor());
		transactionAttribute.setRollbackForClassName(shardingTransactional
				.rollbackForClassName());
		transactionAttribute.setNoRollbackFor(shardingTransactional
				.noRollbackFor());
		transactionAttribute.setNoRollbackForClassName(shardingTransactional
				.noRollbackForClassName());

		// 将事务的属性放到本地线程中，方便使用
		// TransactionAttributeHolder
		// .setTransactionAttribute(transactionAttribute);
		TimeOutHolder.setTimeOut(transactionAttribute.getTimeout());
		TransactionAttributeHolder.getTransactionAttributeStackHolder().push(
				transactionAttribute);

		return invokeWithinTransaction(method, proceedingJoinPoint.getTarget()
				.getClass(), new InvocationCallback() {
			@Override
			public Object proceedWithInvocation() throws Throwable {
				return proceedingJoinPoint.proceed();
			}
		});

	}
}
