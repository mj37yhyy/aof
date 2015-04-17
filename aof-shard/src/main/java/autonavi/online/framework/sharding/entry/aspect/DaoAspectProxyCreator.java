package autonavi.online.framework.sharding.entry.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

import autonavi.online.framework.sharding.dao.AbstractDaoSupport;

/**
 * DaoAspect代理构造者
 * @author jia.miao
 *
 */
public class DaoAspectProxyCreator {

	private final static Map<Method, AspectJExpressionPointcut> aspectJExpressionPointcutCache = new HashMap<Method, AspectJExpressionPointcut>();

	private final static List<Method> pointcutMethodList = new ArrayList<Method>();
	private final static DaoAspect daoAspect = new DaoAspect();

	static {
		for (Method method : DaoAspect.class.getMethods()) {
			if (method.isAnnotationPresent(Pointcut.class)) {
				continue;
			} else if (method.isAnnotationPresent(Around.class)) {
				pointcutMethodList.add(method);
			}
		}
	}

	/**
	 * 代理目录对象
	 * 
	 * @param string
	 * 
	 * @param targetClass
	 * @param daoSupport 
	 * @return
	 */
	public Object getProxy(final Class<?> targetClass) {
		for (Method targetMethod : targetClass.getMethods()) {
			for (final Method aroundMethod : pointcutMethodList) {
				if (this.getAspectJExpressionPointcut(aroundMethod).matches(
						targetMethod, targetClass)) {// 说明是代理方法
					// 生成代理类
					Enhancer en = new Enhancer();
					en.setSuperclass(targetClass);
					en.setCallback(new MethodInterceptor() {
						public Object intercept(Object target, Method method,
								Object[] args, MethodProxy proxy)
								throws Throwable {
							ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(
									targetClass.newInstance(), method, args,
									proxy);
							Object o = aroundMethod.invoke(daoAspect, pjp);
							return o;
						}
					});
					return en.create();
				}
			}
		}
		return null;
	}

	/**
	 * 从缓存中获取AspectJExpressionPointcut
	 * 
	 * @param aroundMethod
	 * @return
	 */
	private AspectJExpressionPointcut getAspectJExpressionPointcut(
			final Method aroundMethod) {
		AspectJExpressionPointcut aspectJExpressionPointcut = null;
		if (aspectJExpressionPointcutCache.containsKey(aroundMethod)) {
			aspectJExpressionPointcut = aspectJExpressionPointcutCache
					.get(aroundMethod);
		} else {
			aspectJExpressionPointcut = new AspectJExpressionPointcut();
			aspectJExpressionPointcut
					.setPointcutDeclarationScope(DaoAspect.class);
			// 节入点表达式
			aspectJExpressionPointcut.setExpression(aroundMethod.getAnnotation(
					Around.class).value());
			aspectJExpressionPointcutCache.put(aroundMethod,
					aspectJExpressionPointcut);
		}
		return aspectJExpressionPointcut;
	}

	private class MethodInvocationProceedingJoinPoint implements
			ProceedingJoinPoint, JoinPoint.StaticPart {
		private Object target;
		private Method method;
		private Object[] args;
		private MethodProxy proxy;

		/** Lazily initialized signature object */
		private Signature signature;

		/** Lazily initialized source location object */
		private SourceLocation sourceLocation;

		public MethodInvocationProceedingJoinPoint(Object target,
				Method method, Object[] args, MethodProxy proxy) {
			this.target = target;
			this.method = method;
			this.args = args;
			this.proxy = proxy;
		}

		@Override
		public String toShortString() {
			return "execution(" + getSignature().toShortString() + ")";
		}

		@Override
		public String toLongString() {
			return "execution(" + getSignature().toLongString() + ")";
		}

		@Override
		public Object getThis() {
			return target;
		}

		@Override
		public Object getTarget() {
			return target;
		}

		@Override
		public Object[] getArgs() {
			return args;
		}

		@Override
		public Signature getSignature() {
			if (this.signature == null) {
				this.signature = new MethodSignatureImpl();
			}
			return signature;

		}

		@Override
		public SourceLocation getSourceLocation() {
			if (this.sourceLocation == null) {
				this.sourceLocation = new SourceLocationImpl();
			}
			return this.sourceLocation;
		}

		@Override
		public String getKind() {
			return ProceedingJoinPoint.METHOD_EXECUTION;
		}

		@Override
		public StaticPart getStaticPart() {
			return this;
		}

		@Override
		public int getId() {
			// TODO: It's just an adapter but returning 0 might still have side
			// effects...
			return 0;
		}

		@Override
		public void set$AroundClosure(AroundClosure arc) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object proceed() throws Throwable {
			return proxy.invokeSuper(target, null);
		}

		@Override
		public Object proceed(Object[] args) throws Throwable {
			return proxy.invokeSuper(target, args);
		}

		private class SourceLocationImpl implements SourceLocation {

			@Override
			public Class<?> getWithinType() {
				if (getThis() == null) {
					throw new UnsupportedOperationException(
							"No source location joinpoint available: target is null");
				}
				return getThis().getClass();
			}

			@Override
			public String getFileName() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int getLine() {
				throw new UnsupportedOperationException();
			}

			@Override
			@Deprecated
			public int getColumn() {
				throw new UnsupportedOperationException();
			}
		}

		private class MethodSignatureImpl implements MethodSignature {

			private volatile String[] parameterNames;

			@Override
			public String getName() {
				return method.getName();
			}

			@Override
			public int getModifiers() {
				return method.getModifiers();
			}

			@Override
			public Class<?> getDeclaringType() {
				return method.getDeclaringClass();
			}

			@Override
			public String getDeclaringTypeName() {
				return method.getDeclaringClass().getName();
			}

			@Override
			public Class<?> getReturnType() {
				return method.getReturnType();
			}

			@Override
			public Method getMethod() {
				return method;
			}

			@Override
			public Class<?>[] getParameterTypes() {
				return method.getParameterTypes();
			}

			@Override
			public String[] getParameterNames() {
				if (this.parameterNames == null) {
					// this.parameterNames =
					// parameterNameDiscoverer.getParameterNames(getMethod());
				}
				return this.parameterNames;
			}

			@Override
			public Class<?>[] getExceptionTypes() {
				return method.getExceptionTypes();
			}

			@Override
			public String toShortString() {
				return toString(false, false, false, false);
			}

			@Override
			public String toLongString() {
				return toString(true, true, true, true);
			}

			@Override
			public String toString() {
				return toString(false, true, false, true);
			}

			private String toString(boolean includeModifier,
					boolean includeReturnTypeAndArgs,
					boolean useLongReturnAndArgumentTypeName,
					boolean useLongTypeName) {
				StringBuilder sb = new StringBuilder();
				if (includeModifier) {
					sb.append(Modifier.toString(getModifiers()));
					sb.append(" ");
				}
				if (includeReturnTypeAndArgs) {
					appendType(sb, getReturnType(),
							useLongReturnAndArgumentTypeName);
					sb.append(" ");
				}
				appendType(sb, getDeclaringType(), useLongTypeName);
				sb.append(".");
				sb.append(getMethod().getName());
				sb.append("(");
				Class<?>[] parametersTypes = getParameterTypes();
				appendTypes(sb, parametersTypes, includeReturnTypeAndArgs,
						useLongReturnAndArgumentTypeName);
				sb.append(")");
				return sb.toString();
			}

			private void appendTypes(StringBuilder sb, Class<?>[] types,
					boolean includeArgs,
					boolean useLongReturnAndArgumentTypeName) {
				if (includeArgs) {
					for (int size = types.length, i = 0; i < size; i++) {
						appendType(sb, types[i],
								useLongReturnAndArgumentTypeName);
						if (i < size - 1) {
							sb.append(",");
						}
					}
				} else {
					if (types.length != 0) {
						sb.append("..");
					}
				}
			}

			private void appendType(StringBuilder sb, Class<?> type,
					boolean useLongTypeName) {
				if (type.isArray()) {
					appendType(sb, type.getComponentType(), useLongTypeName);
					sb.append("[]");
				} else {
					sb.append(useLongTypeName ? type.getName() : type
							.getSimpleName());
				}
			}
		}
	}

	public void setDaoSupport(AbstractDaoSupport daoSupport) {
		daoAspect.setDaoSupport(daoSupport);
	}
}
