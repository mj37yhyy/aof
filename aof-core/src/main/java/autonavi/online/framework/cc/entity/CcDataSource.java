package autonavi.online.framework.cc.entity;

import java.util.Map;

public class CcDataSource {
	private String beanClass;
	private String name;
	private Map<String,Object> props;
	/**
	 * 激活状态
	 */
	private Boolean acitve;
	
	
	public Boolean isAcitve() {
		return acitve;
	}

	public void setAcitve(Boolean acitve) {
		this.acitve = acitve;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}

	public String getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(String beanClass) {
		this.beanClass = beanClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
