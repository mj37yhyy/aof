package autonavi.online.framework.test;

import org.junit.Test;

public class IdWorkerTest {

	@Test
	public void test() {
		try {
			IdWorker iw = new IdWorker(1, 2);
			for (int i = 0; i < 10; i++) {
				//Thread.sleep(1);
				System.out.println(iw.nextId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
