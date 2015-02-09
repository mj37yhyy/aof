package autonavi.online.framework.sharding.dao.exception;

public class ExistingDataSourceKeyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8107981650125305442L;

	public ExistingDataSourceKeyException(String message) {
		super(message);
	}

	public ExistingDataSourceKeyException(String message, Throwable cause) {
		super(message, cause);
	}
}
