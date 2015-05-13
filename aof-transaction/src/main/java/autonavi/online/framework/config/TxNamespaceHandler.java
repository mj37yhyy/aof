package autonavi.online.framework.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

public class TxNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {

		registerBeanDefinitionParser("advice",
				new TxAdviceBeanDefinitionParser());
		registerBeanDefinitionParser("annotation-driven",
				new AnnotationDrivenBeanDefinitionParser());
	}

}
