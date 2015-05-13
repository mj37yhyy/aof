package autonavi.online.framework.sharding.holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 数据源线程安全区
 * @author yaming.xu
 *
 */
public class DataSourceHolder {
	//一个线程保证一个分片的使用数据源的唯一性
	private static final ThreadLocal<ConcurrentHashMap<Integer,String>> dataSourceHolder=new ThreadLocal<ConcurrentHashMap<Integer,String>>();

	public static void cleanAllHolder(){
		dataSourceHolder.remove();
	}
	
	public static Map<Integer,String> getDataSourceHolder(){
		if(dataSourceHolder.get()==null){
			dataSourceHolder.set(new ConcurrentHashMap<Integer,String>());
		}
		return dataSourceHolder.get();
	}

}
