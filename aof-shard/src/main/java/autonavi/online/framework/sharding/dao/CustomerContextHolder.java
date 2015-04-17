package autonavi.online.framework.sharding.dao;

public class CustomerContextHolder {
	private static final ThreadLocal<Integer> contextHolder = new ThreadLocal<Integer>();

	protected static void setCustomerType(Integer customerType) {
		contextHolder.set(customerType);
	}

	public static Integer getCustomerType() {
		return contextHolder.get();
	}

	protected static void clearCustomerType() {
		contextHolder.remove();
	}
}
