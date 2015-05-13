package autonavi.online.framework.cc.config;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.support.ManagedProperties;

import autonavi.online.framework.property.PropertiesConfigUtil;
import autonavi.online.framework.property.PropertiesData;

public class ConfigBiz {

	/**
	 * 初始化Biz
	 * 
	 * @param propertiesData
	 * @param registry
	 * @throws Exception
	 */
	public ManagedProperties initBiz(PropertiesData propertiesData,
			ManagedProperties managedProperties) throws Exception {
		Map<String, String> map = PropertiesConfigUtil.refresh(propertiesData);
		// EL表达式支持
		try {
			transformMap2Properties(map, managedProperties);// 将返回值从map变为Properties
		} catch (Exception e) {
			e.printStackTrace();
		}
		return managedProperties;

	}

	/**
	 * 将Map转化为Properties
	 * 
	 * @param from
	 * @param to
	 */
	private void transformMap2Properties(Map<?, ?> from, Properties to) {
		Set<?> propertySet = from.entrySet();
		for (Object o : propertySet) {
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
			if (entry.getValue() != null)
				to.put(entry.getKey(), entry.getValue());
		}
	}
}
