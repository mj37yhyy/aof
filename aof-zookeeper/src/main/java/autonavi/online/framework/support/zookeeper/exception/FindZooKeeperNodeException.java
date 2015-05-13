package autonavi.online.framework.support.zookeeper.exception;
/**
 * ZK节点查询异常
 * @author yaming.xu
 *
 */
public class FindZooKeeperNodeException extends RuntimeException {
	private static final long serialVersionUID = 3910103344688821767L;
	public FindZooKeeperNodeException() {
		super();
	}

	public FindZooKeeperNodeException(String message) {
		super(message);
	}

	public FindZooKeeperNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public FindZooKeeperNodeException(Throwable cause) {
		super(cause);
	}

}
