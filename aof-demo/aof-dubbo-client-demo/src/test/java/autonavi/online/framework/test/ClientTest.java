package autonavi.online.framework.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import autonavi.online.framework.test.service.TestService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:consumer.xml" })
public class ClientTest {
	@Autowired
	private TestService testService = null;

	@Test
	public void test() throws Exception {
		try {
			testService.insert("mj1");
			testService.insert("mj2");
			testService.insert("mj3");
			testService.insert("mj4");
			testService
					.batchInsert(new String[] { "mj1", "mj2", "mj3", "mj4" });
			testService.userCallbackSelect("mj1");
			testService.singleFieldSelect("mj1", 1);
			testService.pagingSelect("mj1", 0, 5);
			testService
					.batchSelect(new String[] { "mj1", "mj2", "mj3", "mj4" });
			testService.batchSelect2("mj1", new long[] { 537195804461367296L,
					537195804524281856L });
			testService.update("mj1");
			testService
					.batchUpdate(new String[] { "mj1", "mj2", "mj3", "mj4" });
			testService.delete("mj1");
			testService
					.batchDelete(new String[] { "mj1", "mj2", "mj3", "mj4" });
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
