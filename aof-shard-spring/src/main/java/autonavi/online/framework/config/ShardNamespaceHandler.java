package autonavi.online.framework.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class ShardNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {

		registerBeanDefinitionParser("dao",
				new ShardDaoBeanDefinitionParser());
	}

}
