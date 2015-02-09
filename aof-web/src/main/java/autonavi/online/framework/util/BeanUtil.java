package autonavi.online.framework.util;

import java.util.Map;

import org.springframework.context.ConfigurableApplicationContext;

public class BeanUtil {
	private static ConfigurableApplicationContext cxt = null;

	public void setCxt(ConfigurableApplicationContext cxtx) {
		cxt = cxtx;
	}

	public Object getBean(String beanName) {
		return cxt.getBean(beanName);
	}

	public boolean containBean(String beanName) {
		return cxt.containsBean(beanName);
	}

	public Object getBean(Class<?> clazz) {
		return cxt.getBean(clazz);
	}

	public int getBeanCountOfType(Class<?> clazz) {
		return cxt.getBeansOfType(clazz).size();
	}

	public Map<String, ?> getBeansOfType(Class<?> clazz) {
		return cxt.getBeansOfType(clazz);
	}

}
