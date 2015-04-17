package autonavi.online.framework.sharding.dao.exception;

@SuppressWarnings("serial")
public class ColumnNameMatchingNotException extends Exception {

	public ColumnNameMatchingNotException(String message) {
		super(message);
	}
	public ColumnNameMatchingNotException(String message, Throwable cause) {
		super(message, cause);
	}

}
