package autonavi.online.framework.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class PropsConfigCenterNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {

		registerBeanDefinitionParser("config",
				new PropsConfigCenterBeanDefinitionParser());
	}

}
