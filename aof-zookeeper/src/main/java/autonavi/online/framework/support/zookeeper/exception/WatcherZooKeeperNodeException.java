package autonavi.online.framework.support.zookeeper.exception;
/**
 * ZK节点查询异常
 * @author yaming.xu
 *
 */
public class WatcherZooKeeperNodeException extends RuntimeException {
	private static final long serialVersionUID = 3915103344688821767L;
	public WatcherZooKeeperNodeException() {
		super();
	}

	public WatcherZooKeeperNodeException(String message) {
		super(message);
	}

	public WatcherZooKeeperNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WatcherZooKeeperNodeException(Throwable cause) {
		super(cause);
	}

}
