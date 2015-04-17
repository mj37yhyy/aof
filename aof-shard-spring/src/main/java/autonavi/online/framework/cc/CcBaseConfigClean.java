package autonavi.online.framework.cc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class CcBaseConfigClean implements ApplicationContextAware {
	private ConfigurableApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
		BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) this.applicationContext
				.getBeanFactory();
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			try {
				if (CcBaseConfig.class.isAssignableFrom(this
						.getClass()
						.getClassLoader()
						.loadClass(
								beanFactory.getBeanDefinition(beanName)
										.getBeanClassName()))) {
					beanFactory.removeBeanDefinition(beanName);

				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
