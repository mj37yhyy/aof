package autonavi.online.framework.cc;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.classreading.MetadataReader;

import autonavi.online.framework.cc.config.ConfigContextHolder;
import autonavi.online.framework.cc.config.ConfigMessageListener;
import autonavi.online.framework.cc.config.ConfigShardAndDS;
import autonavi.online.framework.cc.config.ScanPipelineAfter;
import autonavi.online.framework.cc.config.ShardPipelineHolder;
import autonavi.online.framework.sharding.annotation.DaoSupportCase;
import autonavi.online.framework.sharding.annotation.DataSourceRouteCase;
import autonavi.online.framework.sharding.annotation.DynamicTransactionManagerCase;
import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.dao.AbstractDataSourceRoute;
import autonavi.online.framework.sharding.holder.DataSourceHolder;
import autonavi.online.framework.sharding.index.ShardInitEntity;
import autonavi.online.framework.sharding.transaction.manager.DynamicTransactionManager;
import autonavi.online.framework.util.ScanAllClass;
import autonavi.online.framework.util.ScanAllClassHandle;

public class ShardingConfig {
	private InitBaseConfig initBaseConfig;
	private BeanDefinitionRegistry registry;
//	private CcBaseEntity ccBaseEntity;
	private static ShardingConfig obj;
	private AbstractDaoSupport daoSupport;
	private ShardInitEntity shardInitEntity;
	
	/**
	 * 得到base的配置实体
	 * 
	 * @param initBaseConfig
	 */
	private void setInitBaseConfig(InitBaseConfig initBaseConfig) {
		this.initBaseConfig = initBaseConfig;
//		CcConfigUtils.setInitBaseConfig(initBaseConfig);
		
	}
	
	
	private void setRegistry(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}
	
	public void setDaoSupport(AbstractDaoSupport daoSupport) {
		this.daoSupport = daoSupport;
	}


	public static AbstractDaoSupport ShardingConfigInit(BeanDefinitionRegistry registry,InitBaseConfig initBaseConfig)throws Exception{
		if(obj==null)
		obj=new ShardingConfig(registry,initBaseConfig);
		return obj.daoSupport;
	}
	private ShardingConfig(BeanDefinitionRegistry registry,InitBaseConfig initBaseConfig)throws Exception{
		this.setInitBaseConfig(initBaseConfig);
		this.setRegistry(registry);
		this.init();
	}
	private void init()throws Exception{
		
		/**
		 * 设置启动的路由等信息
		 */
		initShardStartInfo();

		/**
		 * 启动核心配置
		 */
		this.daoSupport=new ConfigShardAndDS().initShard(initBaseConfig, registry,shardInitEntity);

		/**
		 * 启动消息监听
		 */
		new ConfigMessageListener().initMessageListener(registry);
		/**
		 * 初始化核心上下文Holder
		 */
		new ConfigContextHolder().initContextHolder(registry);
		
		scanAndRunAfter();
		/**
		 * 清理
		 */
		DataSourceHolder.cleanAllHolder();
	}
	private void initShardStartInfo()throws Exception{
		shardInitEntity=new ShardInitEntity();
		ScanAllClassHandle handle = new ScanAllClassHandle(){
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			public void handle(MetadataReader metadataReader) throws Exception {
				if(metadataReader.getAnnotationMetadata().isAnnotated(DaoSupportCase.class.getName())
						&&metadataReader.getClassMetadata().getSuperClassName().equals(AbstractDaoSupport.class.getName())){
					if(shardInitEntity.getDaoSupport()==null)
					    shardInitEntity.setDaoSupport((AbstractDaoSupport)loader.loadClass(metadataReader.getClassMetadata().getClassName()).newInstance());
					else
						throw new RuntimeException("只能加载一个AbstractDaoSupport实现类");
				}
				if(metadataReader.getAnnotationMetadata().isAnnotated(DataSourceRouteCase.class.getName())
						&&metadataReader.getClassMetadata().getSuperClassName().equals(AbstractDataSourceRoute.class.getName())){
					if(shardInitEntity.getDataSourceRoute()==null)
					    shardInitEntity.setDataSourceRoute((AbstractDataSourceRoute)loader.loadClass(metadataReader.getClassMetadata().getClassName()).newInstance());
					else
						throw new RuntimeException("只能加载一个AbstractDataSourceRoute实现类");
				}
				if(metadataReader.getAnnotationMetadata().isAnnotated(DynamicTransactionManagerCase.class.getName())){
					String[] face=metadataReader.getClassMetadata().getInterfaceNames();
					for(String _face:face){
						if(_face.equals(DynamicTransactionManager.class.getName())){
							if(shardInitEntity.getDynamicTransactionManager()==null){
								Class<?> clazz=loader.loadClass(metadataReader.getClassMetadata().getClassName());
								shardInitEntity.setDynamicTransactionManager((DynamicTransactionManager)clazz.newInstance());
							}
							else
								throw new RuntimeException("只能加载一个DynamicTransactionManager实现类");
							break;
						}
					}
					
				}
			}
		};
		ScanAllClass scanAllClass = new ScanAllClass();
		scanAllClass.scanner("autonavi.online.framework", handle);
	}
	/**
	 * 统一进行扫描并运行后置处理器
	 * 
	 * @throws Exception
	 */
	private void scanAndRunAfter() throws Exception {
		// 运行扫描，并依此调用管道里的接口
		ScanAllClass scanAllClass = new ScanAllClass();
		scanAllClass.scanner("autonavi.online.framework", new ScanAllClassHandle() {

			@Override
			public void handle(MetadataReader metadataReader) throws Exception {
				for (ScanAllClassHandle _scanAllClassHandle : ShardPipelineHolder.scanPipeline)
					_scanAllClassHandle.handle(metadataReader);
			}

		});
		// 运行后置处理器
		for (ScanPipelineAfter scanPipelineAfter : ShardPipelineHolder.scanPipelineAfters) {
			scanPipelineAfter.handle();
		}
	}

}
