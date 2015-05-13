package autonavi.online.framework.support.zookeeper.exception;
/**
 * ZK节点查询异常
 * @author yaming.xu
 *
 */
public class CreateZooKeeperNodeException extends RuntimeException {
	private static final long serialVersionUID = 3911103344688821767L;
	public CreateZooKeeperNodeException() {
		super();
	}

	public CreateZooKeeperNodeException(String message) {
		super(message);
	}

	public CreateZooKeeperNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CreateZooKeeperNodeException(Throwable cause) {
		super(cause);
	}

}
