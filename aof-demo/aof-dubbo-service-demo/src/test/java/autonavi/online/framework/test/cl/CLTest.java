package autonavi.online.framework.test.cl;

import org.junit.Test;

public class CLTest {

	@Test
	public void TestCL() {
		try {
			MyClassLoader loader1 = new MyClassLoader("load1");
			loader1.setPath("D:/");
			Class c1 = loader1
					.loadClass("autonavi.online.framework.test.cl.C1");
			c1.getMethod("c1", null).invoke(c1.newInstance(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
