package autonavi.online.framework.support.table.aspect;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import autonavi.online.framework.sharding.dao.DaoEntity;
import autonavi.online.framework.sharding.dao.TableOperation;
import autonavi.online.framework.sharding.dao.exception.CoreException;
import autonavi.online.framework.sharding.entry.aspect.InitParameters4Aspect;
import autonavi.online.framework.sharding.entry.aspect.annotation.Author;
import autonavi.online.framework.sharding.entry.aspect.annotation.SqlParameter;
import autonavi.online.framework.support.table.annotation.Ddl;
import autonavi.online.framework.support.table.annotation.Sql;
import autonavi.online.framework.util.StopWatchLogger;

public class InitTableParameters4Aspect extends InitParameters4Aspect {

	protected DaoEntity getTableEntity(ProceedingJoinPoint proceedingJoinPoint,
			TableOperation tableOperation) throws Throwable {
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("TableAspectEntity");
		DaoEntity tableEntity = new DaoEntity();
		Method method = this.getMethod(proceedingJoinPoint);// 得到目标方法
		Annotation[][] allParameterAnnotations = method
				.getParameterAnnotations();// 得到入参注解二维数组
		Object[] parameters = proceedingJoinPoint.getArgs();// 得到入参
		/**
		 * 循环所有参数得到供SQL使用的参数
		 */
		Map<String, Object> parameterMap = this.initParameterMap(method,
				allParameterAnnotations, parameters);
		tableEntity.setParameterMap(parameterMap);

		String author = this.checkAuthor(method);// 校验是否写了用户
		tableEntity.setAuthor(author);

		int singleDataSourceKey = super.getSingleDataSource(method,parameterMap);// 获得单数据源key
		tableEntity.setSingleDataSourceKey(singleDataSourceKey);

		this.initIndex(tableOperation, method, tableEntity);// 得到类上的indexName和indexColumn注解
		
		this.initTx(tableOperation, method, tableEntity);//初始化是否需要开启事务

		// sql = proceedingJoinPoint.proceed().toString();// 得到的SQL返回
		String sql = (String) method.invoke(proceedingJoinPoint.getTarget(),
				parameters);// 得到的SQL返回
		Pattern pattern = Pattern
				.compile("AOF.snowflake|AOF.index|#\\{[\\w\\.\\[\\]]+\\}");
		Matcher matcher = pattern.matcher(sql.toLowerCase());
		if (matcher.find()) {
			throw new RuntimeException("语句中不允许出现AOF.snowflake AOF.index #{..}");
		}
		tableEntity.setSql(sql);

		swlogger.stop();
		swlogger.writeLog();
		return tableEntity;
	}

	private Method getMethod(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint
				.getSignature();
		Method m = methodSignature.getMethod();
		if (m.getDeclaringClass().isInterface()) {
			m = proceedingJoinPoint.getTarget().getClass()
					.getMethod(m.getName(), m.getParameterTypes());
		}
		return m;
	}

	/**
	 * 初始化入参MAP
	 * 
	 * @param method
	 * 
	 * @param allParameterAnnotations
	 * @return
	 * @throws IOException
	 */
	private Map<String, Object> initParameterMap(Method method,
			Annotation[][] allParameterAnnotations, Object[] parameters)
			throws IOException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();// 存放所有可能的参数
		if (allParameterAnnotations.length > 0) {
			for (int i = 0; i < allParameterAnnotations.length; i++) {
				Annotation[] _parameterAnnotations = allParameterAnnotations[i];
				if (_parameterAnnotations.length == 0) {// 没有注解代表普通参数，跳过
					continue;
				}
				for (Annotation _parameterAnnotation : _parameterAnnotations) {
					if (_parameterAnnotation instanceof SqlParameter) {// 如果有@SqlParameter注解，就放入MAP中供调用
						SqlParameter _sqlParameter = (SqlParameter) _parameterAnnotation;
						parameterMap.put(_sqlParameter.value(), parameters[i]);// 放入MAP中待用
					}
				}
			}
		}
		// if (method.getParameterTypes().length > 0 && parameterMap.size() ==
		// 0) {// 如果一个注解也没有，直接取字段名
		// String[] parameterNames = MethodUtils
		// .getMethodParamNames4Asm(method);
		// for (int i = 0; i < parameterNames.length; i++) {
		// parameterMap.put(parameterNames[i], parameters[i]);// 放入MAP中待用
		// }
		// }
		return parameterMap;
	}

	/**
	 * 校验是否写了用户
	 * 
	 * @param method
	 * @return
	 * @throws CoreException
	 */
	private String checkAuthor(Method method) throws CoreException {
		Author author = method.getAnnotation(Author.class);
		if (author == null) {
			throw new CoreException("必须填写作者名称! DAO名["
					+ method.getDeclaringClass().getName() + "]");
		}
		Pattern pattern = Pattern.compile("([a-zA-Z0-9_.])+");
		Matcher matcher = pattern.matcher(author.value());
		if (!matcher.find()) {
			throw new CoreException("必须填写作者名称! DAO名["
					+ method.getDeclaringClass().getName() + "]");
		}
		return author.value();
	}


	private void initIndex(TableOperation tableOperation, Method method,
			DaoEntity tableEntity) throws Exception {
		String indexName = "";
		String[] indexColumn = {};
		if (tableOperation == TableOperation.Sql) {// 如果是Select
			Sql sqlAnnotation = method.getAnnotation(Sql.class);
			indexName = sqlAnnotation.indexName();
			indexColumn = sqlAnnotation.indexColumn().split(",");
		}
		tableEntity.setIndexName(indexName);
		tableEntity.setIndexColumn(indexColumn);
	}

	private void initTx(TableOperation tableOperation, Method method,
			DaoEntity tableEntity) throws Exception {
		if (tableOperation == TableOperation.Sql) {
			Sql sqlAnnotation = method.getAnnotation(Sql.class);
			tableEntity.setOpenTx(sqlAnnotation.openTx());
		} else if (tableOperation == TableOperation.Ddl) {
			Ddl ddlAnnotation = method.getAnnotation(Ddl.class);
			tableEntity.setOpenTx(ddlAnnotation.openTx());
		}
	}

}
