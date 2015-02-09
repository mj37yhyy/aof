package autonavi.online.framework.support.zookeeper.exception;
/**
 * ZK节点查询异常
 * @author yaming.xu
 *
 */
public class SetZooKeeperNodeException extends RuntimeException {
	private static final long serialVersionUID = 3913103344688821767L;
	public SetZooKeeperNodeException() {
		super();
	}

	public SetZooKeeperNodeException(String message) {
		super(message);
	}

	public SetZooKeeperNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SetZooKeeperNodeException(Throwable cause) {
		super(cause);
	}

}
