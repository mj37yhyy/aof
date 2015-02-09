package autonavi.online.framework.sharding.dao.exception;

public class SqlMapperException extends RuntimeException {

	private static final long serialVersionUID = -8283442496291874449L;

	public SqlMapperException() {
		super();
	}

	public SqlMapperException(String message) {
		super(message);
	}

	public SqlMapperException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlMapperException(Throwable cause) {
		super(cause);
	}
}
