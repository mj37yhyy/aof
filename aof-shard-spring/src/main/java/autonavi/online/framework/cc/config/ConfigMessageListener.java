package autonavi.online.framework.cc.config;

import io.netty.channel.ChannelHandler;

import java.util.Collections;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.classreading.MetadataReader;

import autonavi.online.framework.cc.Miscellaneous;
import autonavi.online.framework.message.MessageListener;
import autonavi.online.framework.message.annotation.message.MessageListenerHandler;
import autonavi.online.framework.util.ScanAllClassHandle;

public class ConfigMessageListener {
	private Logger log = LogManager.getLogger(getClass());

	/**
	 * 启动消息监听
	 * 
	 * @throws Exception
	 */
	public void initMessageListener(final BeanDefinitionRegistry registry)
			throws Exception {
		final RootBeanDefinition MessageListenerBeanDefinition = new RootBeanDefinition(
				MessageListener.class);
		// 定义本地地址
		MessageListenerBeanDefinition.getPropertyValues().add("localhost",
				Miscellaneous.aof_messageListener_localhost);
		// 定义监听端口
		MessageListenerBeanDefinition.getPropertyValues().add("port",
				Miscellaneous.aof_messageListener_port);
		// 定义netInterfaceName
		MessageListenerBeanDefinition.getPropertyValues().add(
				"netInterfaceName",
				Miscellaneous.aof_messageListener_netInterfaceName);
		// 定义handlers
		final ManagedList<ChannelHandler> handlers = new ManagedList<ChannelHandler>();
		// 扫描所有的定义了@MessageListenerHandler注解且实现了ChannelHandler接口的类并加入到MessageListener的handlers数组中
		// 加入扫描队列
		ShardPipelineHolder.scanPipeline.add(new ScanAllClassHandle() {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			@Override
			public void handle(MetadataReader metadataReader) throws Exception {
				Class<?> clazz = null;
				try {
					// 如果有些依赖包不存在，会报NoClassDefFoundError，对于这样的情况，直接跳过
					clazz = loader.loadClass(metadataReader.getClassMetadata()
							.getClassName());
				} catch (NoClassDefFoundError e) {
					log.warn(e.getMessage() + "不存在，已跳过", e);
				}
				if (clazz != null
						&& clazz.getAnnotation(MessageListenerHandler.class) != null
						&& ChannelHandler.class.isAssignableFrom(clazz)
						&& !clazz.isInterface()) {
					handlers.add((ChannelHandler) clazz.newInstance());
				}
			}
		});

		/**
		 * 将要扫描完处理的东西放入后置处理器
		 */
		ShardPipelineHolder.scanPipelineAfters.add(new ScanPipelineAfter() {
			@Override
			public void handle() throws Exception {
				// 使用order属性进行排序，用于定义管道通过顺序
				Collections.sort(handlers, new Comparator<ChannelHandler>() {

					@Override
					public int compare(ChannelHandler o1, ChannelHandler o2) {
						MessageListenerHandler o1a = o1.getClass()
								.getAnnotation(MessageListenerHandler.class);
						MessageListenerHandler o2a = o1.getClass()
								.getAnnotation(MessageListenerHandler.class);
						if (o1a.order() > o2a.order())
							return 1;
						if (o1a.order() < o2a.order())
							return -1;
						else
							return 0;
					}
				});
				// 插入handlers参数
				MessageListenerBeanDefinition.getPropertyValues().add(
						"handlers", handlers);
				// 定义启动方法
				MessageListenerBeanDefinition
						.setInitMethodName("startListener");
				// 注册到容器
				registry.registerBeanDefinition("messageListener",
						MessageListenerBeanDefinition);
			}
		});
	}

}
