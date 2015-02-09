package autonavi.online.framework.support.zookeeper.exception;
/**
 * ZK节点查询异常
 * @author yaming.xu
 *
 */
public class CommitZooKeeperNodeException extends RuntimeException {
	private static final long serialVersionUID = 3914103344688821767L;
	public CommitZooKeeperNodeException() {
		super();
	}

	public CommitZooKeeperNodeException(String message) {
		super(message);
	}

	public CommitZooKeeperNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommitZooKeeperNodeException(Throwable cause) {
		super(cause);
	}

}
