package autonavi.online.framework.sharding.dao.exception;

/**
 * 作者：姬昂 2014年3月14日 说明：
 */
public class MustContainAnnotationException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4437547275380258356L;

	public MustContainAnnotationException(String message) {
		super(message);
	}

	public MustContainAnnotationException(String message, Throwable cause) {
		super(message, cause);
	}

}
