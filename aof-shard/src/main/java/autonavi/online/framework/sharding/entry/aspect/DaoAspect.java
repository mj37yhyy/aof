package autonavi.online.framework.sharding.entry.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.dao.DaoEntity;
import autonavi.online.framework.sharding.dao.TableOperation;
import autonavi.online.framework.util.StopWatchLogger;

/**
 * 作者：姬昂 2014年3月14日 说明：
 */
@Aspect
public class DaoAspect {

	/**
	 * 切面的通用方法
	 * 
	 * @param proceedingJoinPoint
	 * @param tableOperation
	 * @return
	 * @throws Throwable
	 */
	private Object customPointcut(ProceedingJoinPoint proceedingJoinPoint,
			TableOperation tableOperation) throws Throwable {
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("customPointcut.initParameters.getDaoEntity");
		InitParameters4Aspect initParameters = new InitParameters4Aspect();
		DaoEntity daoEntity = initParameters.getDaoEntity(proceedingJoinPoint,
				tableOperation);
		swlogger.stop();
		swlogger.writeLog();// 打印耗时
		return this.daoSupport.execute(daoEntity, tableOperation);
	}

	@Pointcut(value = "@annotation(autonavi.online.framework.sharding.entry.aspect.annotation.Select)")
	public void customSelectPointcut() {
	}

	@Pointcut(value = "@annotation(autonavi.online.framework.sharding.entry.aspect.annotation.Update)")
	public void customUpdatePointcut() {
	}

	@Pointcut(value = "@annotation(autonavi.online.framework.sharding.entry.aspect.annotation.Insert)")
	public void customInsertPointcut() {
	}

	@Pointcut(value = "@annotation(autonavi.online.framework.sharding.entry.aspect.annotation.Delete)")
	public void customDeletePointcut() {
	}

	@Around(value = "customSelectPointcut()")
	public Object customSelectPointcut(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		return customPointcut(proceedingJoinPoint, TableOperation.Select);
	}

	@Around(value = "customUpdatePointcut()")
	public Object customUpdatePointcut(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		return customPointcut(proceedingJoinPoint, TableOperation.Update);
	}

	@Around(value = "customInsertPointcut()")
	public Object customInsertPointcut(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		return customPointcut(proceedingJoinPoint, TableOperation.Insert);
	}

	@Around(value = "customDeletePointcut()")
	public Object customDeletePointcut(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		return customPointcut(proceedingJoinPoint, TableOperation.Delete);
	}

	AbstractDaoSupport daoSupport = null;

	public void setDaoSupport(AbstractDaoSupport daoSupport) {
		this.daoSupport = daoSupport;
	}

}
