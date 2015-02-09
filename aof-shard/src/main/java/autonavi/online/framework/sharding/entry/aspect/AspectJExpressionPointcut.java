package autonavi.online.framework.sharding.entry.aspect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.patterns.NamePattern;
import org.aspectj.weaver.reflect.ReflectionWorld.ReflectionWorldException;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;

/**
 * AspectJ表达式切入点
 * 
 * @author jia.miao
 * 
 */
public class AspectJExpressionPointcut {

	private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<PointcutPrimitive>();

	static {
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
		SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
	}
	private static final Log logger = LogFactory
			.getLog(AspectJExpressionPointcut.class);
	private Class<?> pointcutDeclarationScope;

	private String[] pointcutParameterNames = new String[0];

	private Class<?>[] pointcutParameterTypes = new Class<?>[0];

	private transient PointcutExpression pointcutExpression;

	private String expression = null;

	private final Map<Method, ShadowMatch> shadowMatchCache = new HashMap<Method, ShadowMatch>();

	private void checkReadyToMatch() {
		if (getExpression() == null) {
			throw new IllegalStateException(
					"Must set property 'expression' before attempting to match");
		}
		if (this.pointcutExpression == null) {
			this.pointcutExpression = buildPointcutExpression();
		}
	}

	private PointcutExpression buildPointcutExpression() {
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
						SUPPORTED_PRIMITIVES, this.getClass().getClassLoader());
		parser.registerPointcutDesignatorHandler(new BeanNamePointcutDesignatorHandler());
		PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
		for (int i = 0; i < pointcutParameters.length; i++) {
			pointcutParameters[i] = parser.createPointcutParameter(
					this.pointcutParameterNames[i],
					this.pointcutParameterTypes[i]);
		}
		return parser.parsePointcutExpression(
				replaceBooleanOperators(getExpression()),
				this.pointcutDeclarationScope, pointcutParameters);
	}

	public boolean matches(Class<?> targetClass) {
		checkReadyToMatch();
		try {
			// Special handling for within
			return this.pointcutExpression
					.couldMatchJoinPointsInType(targetClass);
		} catch (ReflectionWorldException rwe) {
			logger.debug("PointcutExpression matching rejected target class",
					rwe);
			return false;
		} catch (BCException ex) {
			logger.debug("PointcutExpression matching rejected target class",
					ex);
			return false;
		}
	}

	public boolean matches(Method method, Class<?> targetClass) {
		checkReadyToMatch();
		ShadowMatch shadowMatch = getShadowMatch(method);

		// Special handling for this, target, @this, @target, @annotation
		if (shadowMatch.alwaysMatches()) {
			return true;
		} else if (shadowMatch.neverMatches()) {
			return false;
		}
		return false;
	}

	private ShadowMatch getShadowMatch(Method method) {
		ShadowMatch shadowMatch = null;
		if (shadowMatchCache.containsKey(method)) {
			shadowMatch = shadowMatchCache.get(method);
		} else {
			shadowMatch = pointcutExpression.matchesMethodExecution(method);
			shadowMatchCache.put(method, shadowMatch);
		}
		return shadowMatch;
	}

	public boolean isRuntime() {
		checkReadyToMatch();
		return this.pointcutExpression.mayNeedDynamicTest();
	}

	public boolean matches(Method method, Class<?> targetClass, Object[] args)
			throws InstantiationException, IllegalAccessException {
		checkReadyToMatch();
		ShadowMatch shadowMatch = pointcutExpression
				.matchesAdviceExecution(method);
		JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(
				pointcutDeclarationScope.newInstance(),
				targetClass.newInstance(), args);
		return joinPointMatch.matches();
	}

	private String replaceBooleanOperators(String pcExpr) {
		String result = pcExpr.replace(" and ", " && ");
		result = result.replace(" or ", " || ");
		result = result.replace(" not ", " ! ");
		return result;
	}

	private class BeanNamePointcutDesignatorHandler implements
			PointcutDesignatorHandler {

		private static final String BEAN_DESIGNATOR_NAME = "bean";

		public String getDesignatorName() {
			return BEAN_DESIGNATOR_NAME;
		}

		public ContextBasedMatcher parse(String expression) {
			return new BeanNameContextMatcher(expression);
		}
	}

	private class BeanNameContextMatcher implements ContextBasedMatcher {

		private final NamePattern expressionPattern;

		public BeanNameContextMatcher(String expression) {
			this.expressionPattern = new NamePattern(expression);
		}

		@SuppressWarnings("rawtypes")
		@Deprecated
		public boolean couldMatchJoinPointsInType(Class someClass) {
			return (contextMatch(someClass) == FuzzyBoolean.YES);
		}

		@SuppressWarnings("rawtypes")
		@Deprecated
		public boolean couldMatchJoinPointsInType(Class someClass,
				MatchingContext context) {
			return (contextMatch(someClass) == FuzzyBoolean.YES);
		}

		public boolean matchesDynamically(MatchingContext context) {
			return true;
		}

		public FuzzyBoolean matchesStatically(MatchingContext context) {
			return contextMatch(null);
		}

		public boolean mayNeedDynamicTest() {
			return false;
		}

		private FuzzyBoolean contextMatch(Class<?> targetType) {
			return FuzzyBoolean.fromBoolean(matchesBeanName(targetType
					.getName()) || matchesBeanName("&" + targetType.getName()));
		}

		private boolean matchesBeanName(String advisedBeanName) {
			if (this.expressionPattern.matches(advisedBeanName)) {
				return true;
			}
			return false;
		}
	}

	public Class<?> getPointcutDeclarationScope() {
		return pointcutDeclarationScope;
	}

	public void setPointcutDeclarationScope(Class<?> pointcutDeclarationScope) {
		this.pointcutDeclarationScope = pointcutDeclarationScope;
	}

	public String[] getPointcutParameterNames() {
		return pointcutParameterNames;
	}

	public void setPointcutParameterNames(String[] pointcutParameterNames) {
		this.pointcutParameterNames = pointcutParameterNames;
	}

	public Class<?>[] getPointcutParameterTypes() {
		return pointcutParameterTypes;
	}

	public void setPointcutParameterTypes(Class<?>[] pointcutParameterTypes) {
		this.pointcutParameterTypes = pointcutParameterTypes;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
}
