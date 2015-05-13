package autonavi.online.framework.sharding.exception;
/**
 * ZK节点查询异常
 * @author yaming.xu
 *
 */
public class ShardSpringException extends RuntimeException {
	private static final long serialVersionUID = 3914103344688821767L;
	public ShardSpringException() {
		super();
	}

	public ShardSpringException(String message) {
		super(message);
	}

	public ShardSpringException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShardSpringException(Throwable cause) {
		super(cause);
	}

}
