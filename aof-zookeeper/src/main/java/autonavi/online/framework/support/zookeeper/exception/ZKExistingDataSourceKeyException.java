package autonavi.online.framework.support.zookeeper.exception;

public class ZKExistingDataSourceKeyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8107981650125305442L;

	public ZKExistingDataSourceKeyException(String message) {
		super(message);
	}

	public ZKExistingDataSourceKeyException(String message, Throwable cause) {
		super(message, cause);
	}
}
