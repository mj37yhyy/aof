package autonavi.online.framework.configcenter.exception;

public enum AofExceptionEnum
{
  INIT_ZOOKEEPER_ERROR("101", "zookeeper初始化错误"),
  ZOOKEEPER_USE_ERROR("102", "zookeeper操作异常[{0}]"),
  ZOOKEEPER_DEPLOY_ERROR("103", "应用节点服务器热部署不能并发"),

  
  
  APPROOT_IS_EXIST("201", "应用程序已经存在"),
  APPROOT_LOGIN_ERROR("202", "应用程序权限校验失败"),
  APPROOT_IS_NOT_EXIST("203", "应用程序配置信息不存在"),
  APPROOT_XML_ERROR("204", "初始化XML文件错误"),
  APPROOT_HAS_LOGIN("205", "应用已经登录"),
  
  MVC_TREE_ROOT_IS_INVALID("301", "根节点无法解析"),
  MVC_TREE_ROOT_IS_NULL("302", "根节点为空"),
  
  JSON_IS_NOT_FOUND("401","JSON文件不存在"),
  JSON_IS_GE_ERROR("402","JSON文件生产错误"),
  
  BIZ_SPLIT_IS_EXIST("501","BIZ分目录{0}已经存在"),
  BIZ_IS_EXIST("502","BIZ信息{0}已经存在");
  
  

  private String code;
  private String message;

  public String getCode() { return this.code; }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  private AofExceptionEnum(String code, String message)
  {
    this.code = code;
    this.message = message;
  }
  public String replaceMessage(String[] desc){
	  String msg=this.message;
	  if(desc!=null){
		  for(int i=0;i<desc.length;i++){
			  msg=msg.replaceAll("\\{"+i+"\\}", desc[i]);
		  }
	  } 
	 return msg; 
  }

  public static String getMessage(String code)
  {
    for (AofExceptionEnum exp : values()) {
      if (exp.getCode().equals(code)) {
        return exp.getMessage();
      }
    }
    return null;
  }
}