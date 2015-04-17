package autonavi.online.framework.shard;

import java.sql.SQLException;

import org.junit.Test;

import autonavi.online.framework.sharding.entry.DaoFactory;
import autonavi.online.framework.sharding.entry.ShardClassPathJsonApplicationContext;
import autonavi.online.framework.sharding.transaction.manager.DefaultTransactionManager;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@Test
	public void testApp() {
		try {
			DaoFactory daoFactory = new ShardClassPathJsonApplicationContext(
					"aof-shard.json");
			/**
			 * 此处的事务管理，是最最简单的事务管理，如果要使用复杂事务，请使用支持包
			 */
			DefaultTransactionManager.begin();// 开始事务
			// 注解方式
			TestDao testDao = daoFactory.getDao(TestDao.class);
			System.out.println(testDao.select1());

			// 接口+xml方式
			TestXmlDao testXmlDao = daoFactory.getDao(TestXmlDao.class);
			System.out.println(testXmlDao.queryDemoIds());
			DefaultTransactionManager.commit();// 提交事务
		} catch (Exception e) {
			try {
				DefaultTransactionManager.rollback();// 回滚事务
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				DefaultTransactionManager.release();// 释放资源
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
