package autonavi.online.framework.support.zookeeper.exception;
/**
 * ZK节点查询异常
 * @author yaming.xu
 *
 */
public class GetZooKeeperNodeException extends RuntimeException {
	private static final long serialVersionUID = 3912103344688821767L;
	public GetZooKeeperNodeException() {
		super();
	}

	public GetZooKeeperNodeException(String message) {
		super(message);
	}

	public GetZooKeeperNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public GetZooKeeperNodeException(Throwable cause) {
		super(cause);
	}

}
