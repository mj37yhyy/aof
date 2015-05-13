package autonavi.online.framework.sharding.dao.constant;

/**
 * 通用正则表达式存放的地方
 * @author jia.miao
 *
 */
public class RegularExpressions {
	
	//#{}
	public static String PARAM_RIGHT_ASK = "#\\{[\\w\\.\\[\\]]+\\}";

	//${}
	public static String PARAM_RIGHT_STRING = "\\$\\{[\\w\\.\\[\\]]+\\}";

	//$ST{}
	public static String SHARDING_TABLE ="\\$ST\\{\\w*\\}";
	
	//$SQL{} 
	public static String SQL_IDENTIFICATION ="\\$SQL\\{\\}";

}
