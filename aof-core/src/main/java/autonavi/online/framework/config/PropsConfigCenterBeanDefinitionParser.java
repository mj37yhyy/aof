package autonavi.online.framework.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import autonavi.online.framework.cc.CcBizConfig;
import autonavi.online.framework.cc.CcBizEntity;
import autonavi.online.framework.cc.CcEntity;
import autonavi.online.framework.property.PropertiesData;
import autonavi.online.framework.util.json.JsonBinder;

import com.fasterxml.jackson.databind.JavaType;


public class PropsConfigCenterBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {
	// private static final String CONFIG_ELEMENT = "config";
	private Logger log = LogManager.getLogger(getClass());
	
	private static final String CC_INFO_CONFIG_ELEMENT = "info";
	private static final String CC_REF_CONFIG_ELEMENT = "ref";
	private static final String CC_CONFIGJSON_CONFIG_ELEMENT = "configJson";

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
		return CcBizConfig.class;
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
		
		List<Element> configChilds = DomUtils.getChildElementsByTagName(element,
				CC_INFO_CONFIG_ELEMENT);
		final CcEntity _ccEntity = new CcEntity();
		for (int i = 0; i < configChilds.size();) {
			Element el = configChilds.get(i);
			if (el.hasAttribute(CC_REF_CONFIG_ELEMENT)
					&& !el.getAttribute(CC_REF_CONFIG_ELEMENT).equals("")) {
				_ccEntity.setBiz(el.getAttribute(CC_REF_CONFIG_ELEMENT));
			} else if (el.hasAttribute(CC_CONFIGJSON_CONFIG_ELEMENT)) {
				_ccEntity.setBizFile(el
						.getAttribute(CC_CONFIGJSON_CONFIG_ELEMENT));
			} else {
				throw new RuntimeException("BIZ的属性必须配置" + CC_REF_CONFIG_ELEMENT
						+ "或者" + CC_CONFIGJSON_CONFIG_ELEMENT);
			}
			break;
		}
		if (_ccEntity.getBiz() != null) {
			builder.addPropertyReference("propertiesData", _ccEntity.getBiz());
		} else {
			PropertiesData propertiesData = new PropertiesData() {

				@Override
				@SuppressWarnings("unchecked")
				public Map<String, String> getProperties() throws Exception {
					log.warn("请注意,本次启动或者BIZ刷新使用的是基于本地JSON文件的方式,如果当前环境是运行环境,则需要配置成为从ZooKeeper刷新!");
					if(_ccEntity.getBizFile()==null||_ccEntity.getBizFile().equals("")){
						log.warn("没有读取到BIZ配置的JSON文件或者配置文件为空,请检查,将采用默认配置启动");
						return new HashMap<String, String>();
					}
					final Map<String, String> result=new HashMap<String,String>();
					String[] fileNames=_ccEntity.getBizFile().split(";");
					//支持多文件
					for(String fileName:fileNames){
						if(fileName.equals("")||!fileName.endsWith(".json")){
							log.warn("没有读取到["+fileName+"]BIZ配置的JSON文件或者配置文件为空");
							continue;
						}
						
						InputStream input = null;
						InputStreamReader reader = null;
						BufferedReader buffer = null;
						try {
							input = this.getClass().getResourceAsStream(
									fileName);
							if(input==null){
								log.warn("没有读取到BIZ配置的JSON文件或者配置文件为空,请检查,将采用默认配置启动");
								return new HashMap<String, String>();
							}
							reader = new InputStreamReader(input);
							buffer = new BufferedReader(reader);
							StringBuffer sb = new StringBuffer();
							String line = null;
							while ((line = buffer.readLine()) != null) {
								sb.append(line);
							}
							final Map<String, String> splitResult=new HashMap<String,String>();
							//不在兼容旧版的配置文件
							JsonBinder builder=JsonBinder.buildNonDefaultBinder(false);
							JavaType javaType=builder.getCollectionType(HashMap.class, String.class,CcBizEntity.class);
							final Map<String, CcBizEntity> ccBizEntityMap=builder.fromJson(sb.toString(), HashMap.class, javaType);
							//解析biz封装类
							if(ccBizEntityMap!=null){
								for(String key:ccBizEntityMap.keySet()){
									splitResult.put(key, ccBizEntityMap.get(key).getValue());
								}
							}
//							try {
//								final Map<String, CcBizEntity> ccBizEntityMap=builder.fromJson(sb.toString(), HashMap.class, javaType);
//								//解析biz封装类
//								if(ccBizEntityMap!=null){
//									for(String key:ccBizEntityMap.keySet()){
//										splitResult.put(key, ccBizEntityMap.get(key).getValue());
//									}
//								}
//							} catch (Exception e) {
//								//转换异常 使用兼容模式
//								log.warn("使用兼容模式启动,请尽快替换替换新的BIZ文件格式");
//								javaType=builder.getCollectionType(HashMap.class, String.class,String.class);
//								final Map<String,String> entity = JsonBinder
//										.buildNonDefaultBinder().fromJson(sb.toString(),
//												HashMap.class,javaType);
//								if(entity!=null){
//									splitResult.putAll(entity);
//								}
//							}
//							final Map<String,String> entity = JsonBinder
//									.buildNonDefaultBinder().fromJson(sb.toString(),
//											HashMap.class);
//							if(entity!=null){
//								splitResult.putAll(entity);
//							}
							//往总结果中合并分开的BIZ结果，需要检测是否有重复
							for(String key:splitResult.keySet()){
								if(!result.containsKey(key)){
									result.put(key, splitResult.get(key));
								}else{
									throw new RuntimeException("Biz信息中的["+key+"]重复,无法启动");
								}
							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							log.error(e.getMessage(),e);
							throw e;
						} finally {
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
								throw e;
							}
						}
					}
					return result;
					
					
				}

			};
			builder.addPropertyValue("propertiesData", propertiesData);
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
