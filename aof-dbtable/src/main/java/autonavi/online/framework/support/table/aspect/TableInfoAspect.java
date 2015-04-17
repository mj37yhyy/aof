package autonavi.online.framework.support.table.aspect;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.dao.DynamicDataSource;
import autonavi.online.framework.sharding.dao.DaoEntity;
import autonavi.online.framework.sharding.dao.TableOperation;
import autonavi.online.framework.util.StopWatchLogger;

@Aspect
public class TableInfoAspect {
	private Logger log = LogManager.getLogger(getClass());
	private AbstractDaoSupport daoSupport;

	public void setDaoSupport(AbstractDaoSupport daoSupport) {
		this.daoSupport = daoSupport;
	}

	@Pointcut(value = "@annotation(autonavi.online.framework.support.table.annotation.Ddl)")
	public void customDDLPointcut() {
	}

	@Pointcut(value = "@annotation(autonavi.online.framework.support.table.annotation.Hbm)")
	public void customHbmPointcut() {
	}
	
	@Pointcut(value = "@annotation(autonavi.online.framework.support.table.annotation.Sql)")
	public void customSqlPointcut() {
	}

	@Around(value = "customDDLPointcut()")
	public Object customDdlPointcut(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		return customPointcut(proceedingJoinPoint, TableOperation.Ddl);
	}

	@Around(value = "customHbmPointcut()")
	public Object customHbmPointcut(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		return customPointcut(proceedingJoinPoint, TableOperation.Hbm);
	}
	@Around(value = "customSqlPointcut()")
	public Object customSqlPointcut(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		return customPointcut(proceedingJoinPoint, TableOperation.Sql);
	}

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
		if(daoSupport==null){
			  log.warn("daoSupport初始化失败,请检查是否启动了数据支持");
			  throw new RuntimeException("建表工具功能不可用");
	    }
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		swlogger.start("customPointcut.initParameters.getDaoEntity");
		InitTableParameters4Aspect initParameters = new InitTableParameters4Aspect();
		DaoEntity tableInfoEntity = initParameters.getTableEntity(
				proceedingJoinPoint, tableOperation);
		swlogger.stop();
		swlogger.writeLog();// 打印耗时
		if(tableOperation != TableOperation.Sql){
			if (tableInfoEntity.getSingleDataSourceKey() > 0) {
				Set<Integer> keySet = DynamicDataSource.getKeySet();
				for (Integer dsKey : keySet) {
					if (dsKey == tableInfoEntity.getSingleDataSourceKey()) {
						return this.daoSupport.execute(tableInfoEntity,
								tableOperation);
					}
				}
				throw new RuntimeException("提供的数据源ID="
						+ tableInfoEntity.getSingleDataSourceKey() + " 未找到 接口作者 "
						+ tableInfoEntity.getAuthor());
			} else {
				Set<Integer> keySet = DynamicDataSource.getKeySet();
				int count = 0;
				for (Integer dsKey : keySet) {
					tableInfoEntity.setSingleDataSourceKey(dsKey);
					this.daoSupport.execute(tableInfoEntity, tableOperation);
					count++;
				}
				return count;
			}
		}else{
			return this.daoSupport.execute(tableInfoEntity, tableOperation);
		}
		
	}

}
