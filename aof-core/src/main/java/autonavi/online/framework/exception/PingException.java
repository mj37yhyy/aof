package autonavi.online.framework.exception;

public class PingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3373618537087447317L;

	public PingException(String message) {
		super(message);
	}

	public PingException(String message, Throwable cause) {
		super(message, cause);
	}
}
