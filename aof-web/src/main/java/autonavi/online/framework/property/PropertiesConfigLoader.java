package autonavi.online.framework.property;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class PropertiesConfigLoader implements BeanFactoryPostProcessor {

	private PropertiesData propertiesData = null;

	public void setPropertiesData(PropertiesData propertiesData) {
		this.propertiesData = propertiesData;
	}

	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory configurableListableBeanFactory)
			throws BeansException {
		try {
			PropertiesConfigUtil.refresh(this.propertiesData);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
