package autonavi.online.framework.support.zookeeper.exception;
/**
 * ZK节点删除异常
 * @author yaming.xu
 *
 */
public class DeleteZooKeeperNodeException extends RuntimeException {
	private static final long serialVersionUID = 3913103344688921767L;
	public DeleteZooKeeperNodeException() {
		super();
	}

	public DeleteZooKeeperNodeException(String message) {
		super(message);
	}

	public DeleteZooKeeperNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeleteZooKeeperNodeException(Throwable cause) {
		super(cause);
	}

}
