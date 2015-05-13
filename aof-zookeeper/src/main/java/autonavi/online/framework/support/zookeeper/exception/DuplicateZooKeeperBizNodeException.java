package autonavi.online.framework.support.zookeeper.exception;
/**
 * 重复的Biz节点异常
 * @author yaming.xu
 *
 */
public class DuplicateZooKeeperBizNodeException extends RuntimeException {
	private static final long serialVersionUID = 3912103344688821767L;
	public DuplicateZooKeeperBizNodeException() {
		super();
	}

	public DuplicateZooKeeperBizNodeException(String message) {
		super(message);
	}

	public DuplicateZooKeeperBizNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateZooKeeperBizNodeException(Throwable cause) {
		super(cause);
	}

}
