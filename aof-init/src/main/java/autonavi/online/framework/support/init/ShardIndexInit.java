package autonavi.online.framework.support.init;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.dao.DaoEntity;
import autonavi.online.framework.sharding.dao.TableOperation;
import autonavi.online.framework.sharding.index.ShardingHandle;
import autonavi.online.framework.sharding.index.ShardingHandleSupport;

public class ShardIndexInit {
	private static Logger log = LogManager.getLogger(ShardIndexInit.class);
	private static AbstractDaoSupport daoSupport;
	

	public void setDaoSupport(AbstractDaoSupport daoSupport) {
		ShardIndexInit.daoSupport = daoSupport;
	}

	public static void initShardIndex(final String tableName, File file,final ShardingHandle handle) throws Exception{
		if(daoSupport==null){
			  log.warn("daoSupport初始化失败,请检查是否启动了数据支持");
			  throw new RuntimeException("框架初始化工具不可用");
	    }
		int maxThread=10;
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		int size=lines.size();
		int counts=size/maxThread;
		final CountDownLatch ll=new CountDownLatch(10);
		for(int i=0;i<=maxThread;i++){
			int start=counts*i;
			int end=counts*i+counts;
			if(i==maxThread)end=lines.size();
			final List<String> l=lines.subList(start, end);
			Thread t=new Thread(){
				public void run(){
					for(String name:l){
						try {
							DaoEntity daoEntity=new DaoEntity();
							daoEntity.setIndexName(tableName);
							daoEntity.setIndexColumn(new String[]{"name"});
							Map<String,Object> parameterMap=new HashMap<String,Object>();
							parameterMap.put("name",name );
							daoEntity.setParameterMap(parameterMap);
							daoEntity.setSql("select 1");
							daoEntity.setSingleDataSourceKey(-1);
							daoEntity.setShardingHandle(handle);
							daoSupport.execute(daoEntity, TableOperation.Select);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					ll.countDown();
				}
			};
			t.start();
		}
		ll.await();
		
	}
	public static void initShardIndex(final String tableName, File file) throws Exception{
		int maxThread=10;
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		final ShardingHandle handle=ShardingHandleSupport.class.newInstance();
		int size=lines.size();
		int counts=size/maxThread;
		final CountDownLatch ll=new CountDownLatch(10);
		for(int i=0;i<=maxThread;i++){
			int start=counts*i;
			int end=counts*i+counts;
			if(i==maxThread)end=lines.size();
			final List<String> l=lines.subList(start, end);
			Thread t=new Thread(){
				public void run(){
					for(String name:l){
						try {
							DaoEntity daoEntity=new DaoEntity();
							daoEntity.setIndexName(tableName);
							daoEntity.setIndexColumn(new String[]{"name"});
							Map<String,Object> parameterMap=new HashMap<String,Object>();
							parameterMap.put("name",name );
							daoEntity.setParameterMap(parameterMap);
							daoEntity.setSql("select 1");
							daoEntity.setSingleDataSourceKey(-1);
							daoEntity.setShardingHandle(handle);
							daoSupport.execute(daoEntity, TableOperation.Select);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					ll.countDown();
				}
			};
			t.start();
		}
		ll.await();
		
	}

}
