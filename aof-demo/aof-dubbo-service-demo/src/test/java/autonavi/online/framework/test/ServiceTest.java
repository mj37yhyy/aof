package autonavi.online.framework.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import autonavi.online.framework.test.service.TestService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:autonavi/online/framework/test/service/applicationContext-tx.xml",
		"classpath:autonavi/online/framework/test/service/provider.xml",
		"classpath:autonavi/online/framework/test/crud/applicationContext.xml" })
public class ServiceTest {
    @Autowired
    TestService testService;
	@Test
	public void start() throws Exception {
		testService.insert("xym123");
		while (true)
			;
	}
}
