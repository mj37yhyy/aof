package autonavi.online.framework.configcenter.exception;

public class AofException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// error code
	private int errorCode = -1;

	// error message
	private String errorMessage = null;

	private AofExceptionEnum sqlExpEnum;

	public AofException() {
		super();
	}

	/**
	 * 构造函数说明： 
	 * @param errorCode
	 * @param errorMessage
	 * @param sqlExpEnum
	 */
	public AofException(int errorCode, String errorMessage, AofExceptionEnum sqlExpEnum) {
		super();
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.sqlExpEnum = sqlExpEnum;
	}

	public AofException(AofExceptionEnum sqlExpEnum) {
		super(sqlExpEnum.getMessage());
		this.sqlExpEnum = sqlExpEnum;
	}
	public AofException(AofExceptionEnum sqlExpEnum,String desc[]) {
		super(sqlExpEnum.replaceMessage(desc));
		this.sqlExpEnum = sqlExpEnum;
	}

	public AofException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.setErrorCode(errorCode);
		this.setErrorMessage(message);
	}

	public AofExceptionEnum getSqlExpEnum() {
		return this.sqlExpEnum;
	}

	public void setSqlExpEnum(AofExceptionEnum sqlExpEnum) {
		this.sqlExpEnum = sqlExpEnum;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return this.errorCode;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

}
