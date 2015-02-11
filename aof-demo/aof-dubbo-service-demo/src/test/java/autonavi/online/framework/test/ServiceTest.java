package autonavi.online.framework.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:autonavi/online/framework/test/service/applicationContext.xml",
		"classpath:autonavi/online/framework/test/service/applicationContext-tx.xml",
		"classpath:autonavi/online/framework/test/service/provider.xml",
		"classpath:autonavi/online/framework/test/crud/applicationContext.xml" })
public class ServiceTest {

	@Test
	public void start() throws Exception {
		while (true)
			;
	}
}
