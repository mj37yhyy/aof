package autonavi.online.framework.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import autonavi.online.framework.sharding.transaction.ShardingTransactionAspect;

public class AnnotationDrivenBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {

	static final String TRANSACTION_MANAGER_ATTRIBUTE = "transaction-manager";

	@Override
	protected Class<?> getBeanClass(Element element) {
		return ShardingTransactionAspect.class;
	}

	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		if (element.hasAttribute(TRANSACTION_MANAGER_ATTRIBUTE)) {
			builder.addPropertyReference("transactionManager",
					element.getAttribute(TRANSACTION_MANAGER_ATTRIBUTE));
		}

	}
}
