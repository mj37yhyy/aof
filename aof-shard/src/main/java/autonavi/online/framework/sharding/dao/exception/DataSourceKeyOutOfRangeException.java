package autonavi.online.framework.sharding.dao.exception;

public class DataSourceKeyOutOfRangeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7782467718859463324L;

	public DataSourceKeyOutOfRangeException(String message) {
		super(message);
	}

	public DataSourceKeyOutOfRangeException(String message, Throwable cause) {
		super(message, cause);
	}
}
