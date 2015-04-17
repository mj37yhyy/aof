package autonavi.online.framework.sharding.dao.exception;

public class StrategyNameNotFound extends Exception {

	private static final long serialVersionUID = 247666081762817355L;

	public StrategyNameNotFound() {
		super();
	}

	public StrategyNameNotFound(String message) {
		super(message);
	}

	public StrategyNameNotFound(String message, Throwable cause) {
		super(message, cause);
	}

	public StrategyNameNotFound(Throwable cause) {
		super(cause);
	}

}
