package autonavi.online.framework.sharding.dao;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class DaoContextHolder implements ApplicationContextAware {
	private ConfigurableApplicationContext applicationContext;
	private static DaoContextHolder contextHolder=null;
	
	@PostConstruct
	private void init(){
		contextHolder=this;
	}
	public static DaoContextHolder getInstance(){
		return contextHolder;
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;

	}

}
