package autonavi.online.framework.test.crud.nonspring;

import java.sql.SQLException;

import org.junit.Test;

import autonavi.online.framework.sharding.entry.DaoFactory;
import autonavi.online.framework.sharding.entry.ShardClassPathJsonApplicationContext;
import autonavi.online.framework.sharding.transaction.manager.DefaultTransactionManager;
import autonavi.online.framework.test.crud.TestDao;
import autonavi.online.framework.test.crud.TestXmlDao;

/**
 * Unit test for simple App.
 */
public class NonSpringCRUDTest {
	private TestDao testDao = null;
	private TestXmlDao testXmlDao = null;

	public NonSpringCRUDTest() throws Exception {
		DaoFactory daoFactory = new ShardClassPathJsonApplicationContext(
				"autonavi/online/framework/test/crud/aof-shard.json");
		// 注解方式
		testDao = daoFactory.getDao(TestDao.class);
		// 接口+xml方式
		testXmlDao = daoFactory.getDao(TestXmlDao.class);
	}

	@Test
	public void testCRUD() {
		try {
			/**
			 * 此处的事务管理，是最最简单的事务管理，如果要使用复杂事务，请使用aof-transaction
			 */
			DefaultTransactionManager.begin();// 开始事务
			System.out.println(testDao.select1());

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
