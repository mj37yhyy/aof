package autonavi.online.framework.configcenter.util;


/**
 * 数据库监控，zk目录结构
 * @author chunsheng.zhang
 *
 */
public interface DBConstants {
	
	
	/**
	 * zk 整体目录结构
	 * aof_db_monitor 根目录
	 * 		--	register 
	 * 			--	app-name
	 * 				-- 监控程序名称`s
	 * 
	 * 		-- db
	 * 			-- app-name
	 * 				-- 数据库名称	
	 * 					-- 数据库连接阈值max_connection
	 * 					-- 数据库监控结果信息 monitor_result
	 * 
	 * 		-- m_db
	 * 			-- app-name 数据源变化更新这个目录
	 * 
			-- slock
				-- app-name
					-- 数据库名称
						-- 临时目录
	 *          	
	 */
	
	//根目录
	public final static String DB_MONITOR  = "/aof_db_monitor";
	
	//自主注册
	public final static String REGISTER = "/register";
	//监控结果
	public final static String MONITOR_RESULT_INFO = "/monitor_result_info";
	
	
	//自主注册目录
	public final static String REGISTER_PATH =DB_MONITOR + REGISTER;
	
	//监控结果目录
//	public final static String MONITOR_RESULT_INFO_PATH = DB_MONITOR + MONITOR_RESULT_INFO;
	
	public final static String DB = "/db";		
	
	// 数据库根目录
	public final static String DB_ROOT = DB_MONITOR + DB;
	
	//数据源变化状态修改监控目录
	public final static String MONITOR_DBSTATUS = "/m_db";
	
	//数据源变化，监控根目录
	public final static String MONITOR_DBSTATUS_ROOT = DB_MONITOR + MONITOR_DBSTATUS;
	
	//数据库监控结果信息，写入目录
	public final static String MONITOR_RESULT = "/monitor_result";
	
	public final static String SLOCK = "/lock";
	
	//存放同步锁的根目录
	public final static String SLOCK_ROOT = DB_MONITOR + SLOCK;
	
	//应用名称
	public final static String APPNAME = "appname";
	
	
	public final static String CHARSET = "utf-8";
	
	//将状态信息修改为false
	public final static String M_RESULT_STATE_FALSE = "false";
	
	//装状态信息修改为true
	public final static String M_RESULT_STATE_TRUE = "true";
	
	
	//最大连接数目录
	public final static String MAX_CONNECTION = "/max_connection";
	
	//目录分隔符
	public final static String SEPERATOR = "/";
	
	public final static String TIMESTAMP = "/Timestamp";
	
	
}
