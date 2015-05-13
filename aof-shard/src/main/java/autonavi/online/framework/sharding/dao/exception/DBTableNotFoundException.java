package autonavi.online.framework.sharding.dao.exception;

public class DBTableNotFoundException extends Exception {

	public DBTableNotFoundException(String message) {
		super(message);
	}

	public DBTableNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
