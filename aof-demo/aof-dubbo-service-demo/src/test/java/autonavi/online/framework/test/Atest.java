package autonavi.online.framework.test;

import org.junit.Test;

public class Atest {

	@Test
	public void test() {
		System.out.println(Long.toBinaryString(134727224187L));
		System.out.println(Long.toBinaryString(134727224187L<<22));
		System.out.println(Long.toBinaryString(2<<17));
		System.out.println(Long.toBinaryString(1<<12));
		System.out.println(Long.toBinaryString(134727224187L<<22|2<<17|1<<12|3));
	}

}
