package autonavi.online.framework.test.configcenter;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import autonavi.online.framework.sharding.transaction.manager.DefaultTransactionManager;
import autonavi.online.framework.test.crud.TestDao;
import autonavi.online.framework.test.crud.TestXmlDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:autonavi/online/framework/test/configcenter/applicationContext.xml",
		"classpath:autonavi/online/framework/test/crud/applicationContext.xml" })
public class ConfigCenterTest {
	@Autowired
	TestDao testDao = null;
	@Autowired
	TestXmlDao testXmlDao = null;

	@Test
	public void test() {
		try {
			/**
			 * 此处的事务管理，是最最简单的事务管理，如果要使用复杂事务，请使用aof-transaction
			 */
			DefaultTransactionManager.begin();// 开始事务
			// 注解方式
			System.out.println(testDao.select1());

			System.out.println(testDao.select1());

			// 接口+xml方式
//			//System.out.println(testXmlDao.queryDemoIds());
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
