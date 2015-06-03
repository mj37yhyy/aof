package autonavi.online.framework.support.zookeeper.exception;

public class ZKExistingPropException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8107981650125305442L;

	public ZKExistingPropException(String message) {
		super(message);
	}

	public ZKExistingPropException(String message, Throwable cause) {
		super(message, cause);
	}
}
