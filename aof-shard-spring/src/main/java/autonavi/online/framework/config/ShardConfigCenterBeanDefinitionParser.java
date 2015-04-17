package autonavi.online.framework.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import autonavi.online.framework.cc.CcBaseConfig;
import autonavi.online.framework.cc.InitBaseConfig;
import autonavi.online.framework.cc.entity.CcBaseEntity;
import autonavi.online.framework.cc.entity.CcEntity;
import autonavi.online.framework.sharding.exception.ShardSpringException;
import autonavi.online.framework.util.json.JsonBinder;


public class ShardConfigCenterBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {
	// private static final String CONFIG_ELEMENT = "config";
	private Logger log = LogManager.getLogger(getClass());

	private static final String SHARD_INFO_CONFIG_ELEMENT = "info";
	private static final String SHARD_REF_CONFIG_ELEMENT = "ref";
	private static final String SHARD_CONFIGJSON_CONFIG_ELEMENT = "configJson";

	// private static final String ZK_CONFIG_ELEMENT = "zkConfig";
	// private static final String ZK_CONFIG_ADDRESS_ATTRIBUTE = "address";
	// private static final String ZK_CONFIG_SESSION_TIMEOUT_ATTRIBUTE =
	// "sessionTimeout";
	// private static final String ZK_CONFIG_PROJECT_ATTRIBUTE = "project";
	// private static final String ZK_CONFIG_PASSWORD_ATTRIBUTE = "password";

	// GetPropertiesData getPropertiesData = new GetPropertiesData();
	// ConfigCenterBeanDefinitonParserFromZookeeper configCenter = new
	// ConfigCenterBeanDefinitonParserFromZookeeper();

	@Override
	protected Class<?> getBeanClass(Element element) {
		return CcBaseConfig.class;
	}

	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		// ManagedProperties managedProperties = new ManagedProperties();
		// this.parseZkConfig(element, parserContext.getRegistry());// 解析zk部分
		this.parseCCConfig(element, builder);

		// try {
		// this.transformMap2Properties(getPropertiesData.getProperties(),
		// managedProperties);// 将返回值从map变为Properties
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// builder.addPropertyValue("properties", managedProperties);

		// 初始化本地属性类
		// RootBeanDefinition propertiesConfigLoaderBeanDefinition = new
		// RootBeanDefinition(
		// PropertiesConfigLoader.class);
		// propertiesConfigLoaderBeanDefinition.getPropertyValues().add(
		// "propertiesData", getPropertiesData);// 插入数据源实体
		// parserContext.getRegistry().registerBeanDefinition(
		// "propertiesConfigLoader", propertiesConfigLoaderBeanDefinition);//
		// 将本地属性类注册进容器
		// try {
		// Map<String, String> map = PropertiesConfigUtil
		// .refresh(getPropertiesData);
		// ManagedProperties managedProperties = new ManagedProperties();
		// try {
		// transformMap2Properties(map, managedProperties);//
		// 将返回值从map变为Properties
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// builder.addPropertyValue("properties", managedProperties);
		// } catch (Exception e) {
		// e.printStackTrace();
		// System.exit(0);
		// }

	}

	/**
	 * 解析ZkConfig
	 * 
	 * @param element
	 * @param builder
	 */
	// private void parseZkConfig(Element element,
	// BeanDefinitionRegistry beanDefinitionRegistry) {
	// List<ZkEntity> zkEntitys = new ArrayList<ZkEntity>();
	// List<Element> configChilds = DomUtils.getChildElementsByTagName(
	// element, ZK_CONFIG_ELEMENT);
	// for (int i = 0; i < configChilds.size(); i++) {
	// Element el = configChilds.get(i);
	// ZkEntity _zkEntity = new ZkEntity();
	// _zkEntity.setAddress(el.getAttribute(ZK_CONFIG_ADDRESS_ATTRIBUTE));
	// _zkEntity.setSessionTimeout(Integer.valueOf(el
	// .getAttribute(ZK_CONFIG_SESSION_TIMEOUT_ATTRIBUTE)));
	// _zkEntity.setPath(el.getAttribute(ZK_CONFIG_PROJECT_ATTRIBUTE));
	// _zkEntity.setProject(el.getAttribute(ZK_CONFIG_PROJECT_ATTRIBUTE));
	// _zkEntity.setPasword(el.getAttribute(ZK_CONFIG_PASSWORD_ATTRIBUTE));
	// zkEntitys.add(_zkEntity);
	// }
	// configCenter.init(zkEntitys, beanDefinitionRegistry);// 初始化数据源和系统核心组件
	// getPropertiesData.setZkEntitys(zkEntitys.toArray(new ZkEntity[0]));//
	// 初始化本地配置工具
	// }

	private void parseCCConfig(Element element, BeanDefinitionBuilder builder) {
		List<Element> configChilds = DomUtils.getChildElementsByTagName(
				element, SHARD_INFO_CONFIG_ELEMENT);
		final CcEntity _ccEntity = new CcEntity();
		for (int i = 0; i < configChilds.size();) {
			Element el = configChilds.get(i);
			if (el.hasAttribute(SHARD_REF_CONFIG_ELEMENT)
					&& !el.getAttribute(SHARD_REF_CONFIG_ELEMENT).equals("")) {
				_ccEntity.setBase(el.getAttribute(SHARD_REF_CONFIG_ELEMENT));
			} else if (el.hasAttribute(SHARD_CONFIGJSON_CONFIG_ELEMENT)) {
				_ccEntity.setBaseFile(el
						.getAttribute(SHARD_CONFIGJSON_CONFIG_ELEMENT));
			} else {
				throw new RuntimeException("base的属性必须配置"
						+ SHARD_REF_CONFIG_ELEMENT + "或者"
						+ SHARD_CONFIGJSON_CONFIG_ELEMENT);
			}
			break;
		}
		// 注入
		if (_ccEntity.getBase() != null) {
			builder.addPropertyReference("initBaseConfig", _ccEntity.getBase());

		} else {
			InitBaseConfig initBaseConfig = new InitBaseConfig() {
				@Override
				public CcBaseEntity getBeseConfig() throws Exception {
					log.warn("请注意,本次启动或者数据源信息刷新使用的是基于本地JSON文件的方式,如果当前环境是运行环境,则需要配置成为从ZooKeeper刷新!");
					if(_ccEntity.getBaseFile()==null||_ccEntity.getBaseFile().equals("")||!_ccEntity.getBaseFile().endsWith(".json")){
						log.warn("没有读取到Base配置的JSON文件或者配置文件为空,请检查,将采用默认配置启动");
						CcBaseEntity entity=new CcBaseEntity(true);
						return entity;
					}
					InputStream input = null;
					InputStreamReader reader = null;
					BufferedReader buffer = null;
					try {
						input = this.getClass().getResourceAsStream(
								_ccEntity.getBaseFile());
						if(input==null){
							log.warn("没有读取到Base配置的JSON文件或者配置文件为空,请检查,将采用默认配置启动");
							CcBaseEntity entity=new CcBaseEntity(true);
							return entity;
						}
						reader = new InputStreamReader(input);
						buffer = new BufferedReader(reader);
						StringBuffer sb = new StringBuffer();
						String line = null;
						while ((line = buffer.readLine()) != null) {
							sb.append(line);
						}
						CcBaseEntity entity= JsonBinder.buildNonDefaultBinder(false)
									.fromJson(sb.toString(), CcBaseEntity.class);
						return entity;

					} catch (IOException e) {
						// TODO Auto-generated catch block
						log.error(e.getMessage(),e);
						throw e;
						
					} catch (Exception e){
						log.error("配置文件格式错误,请使用配置中心开发模式配置或者提供正确的配置文件");
						throw new ShardSpringException("配置文件格式错误,请使用配置中心开发模式配置或者提供正确的配置文件");
					}finally {
						try {
							if (buffer != null)
								buffer.close();
							if (reader != null)
								reader.close();
							if (input != null)
								input.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}

			};
			builder.addPropertyValue("initBaseConfig", initBaseConfig);
		}
	}

	// /**
	// * 将Map转化为Properties
	// *
	// * @param from
	// * @param to
	// */
	// private void transformMap2Properties(Map<?, ?> from, Properties to) {
	// Set<?> propertySet = from.entrySet();
	// for (Object o : propertySet) {
	// Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
	// if (entry.getValue() != null)
	// to.put(entry.getKey(), entry.getValue());
	// }
	// }

}
