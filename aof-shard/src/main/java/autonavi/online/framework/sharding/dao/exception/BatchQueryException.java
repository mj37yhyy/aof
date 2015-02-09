package autonavi.online.framework.sharding.dao.exception;

public class BatchQueryException extends Exception {

	private static final long serialVersionUID = -3509549963402727155L;
	public BatchQueryException(String message){
		super(message);
	}
	public BatchQueryException(String message, Throwable cause){
		super(message,cause);
	}

}
