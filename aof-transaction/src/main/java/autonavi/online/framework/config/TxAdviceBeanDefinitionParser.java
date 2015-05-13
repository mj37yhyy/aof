package autonavi.online.framework.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import autonavi.online.framework.sharding.transaction.ShardingTransactionInterceptor;
import autonavi.online.framework.sharding.transaction.TransactionAttribute;

public class TxAdviceBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {

	private static final String METHOD_ELEMENT = "method";

	private static final String METHOD_NAME_ATTRIBUTE = "name";

	private static final String ATTRIBUTES_ELEMENT = "attributes";

	private static final String TIMEOUT_ATTRIBUTE = "timeout";

	private static final String READ_ONLY_ATTRIBUTE = "read-only";

	private static final String PROPAGATION_ATTRIBUTE = "propagation";

	private static final String ISOLATION_ATTRIBUTE = "isolation";

	private static final String ROLLBACK_FOR_ATTRIBUTE = "rollback-for";

	private static final String NO_ROLLBACK_FOR_ATTRIBUTE = "no-rollback-for";

	static final String TRANSACTION_MANAGER_ATTRIBUTE = "transaction-manager";

	@Override
	protected Class<?> getBeanClass(Element element) {
		return ShardingTransactionInterceptor.class;
	}

	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		if (element.hasAttribute(TRANSACTION_MANAGER_ATTRIBUTE)) {
			builder.addPropertyReference("transactionManager",
					element.getAttribute(TRANSACTION_MANAGER_ATTRIBUTE));
		}

		List<Element> txAttributes = DomUtils.getChildElementsByTagName(
				element, ATTRIBUTES_ELEMENT);
		if (txAttributes.size() > 1) {
			parserContext
					.getReaderContext()
					.error("Element <attributes> is allowed at most once inside element <advice>",
							element);
		} else if (txAttributes.size() == 1) {
			// Using attributes source.
			Element attributeSourceElement = txAttributes.get(0);
			builder.addPropertyValue("transactionAttributes",
					parseAttributeSource(attributeSourceElement, parserContext));
		}

	}

	private List<TransactionAttribute> parseAttributeSource(Element attrEle,
			ParserContext parserContext) {
		List<Element> methods = DomUtils.getChildElementsByTagName(attrEle,
				METHOD_ELEMENT);
		List<TransactionAttribute> transactionAttributes = new ArrayList<TransactionAttribute>();

		for (Element methodEle : methods) {
			TransactionAttribute transactionAttribute = new TransactionAttribute();

			String name = methodEle.getAttribute(METHOD_NAME_ATTRIBUTE);
			transactionAttribute.setName(name);

			String propagation = methodEle.getAttribute(PROPAGATION_ATTRIBUTE);
			transactionAttribute.setPropagation(propagation);

			String isolation = methodEle.getAttribute(ISOLATION_ATTRIBUTE);
			transactionAttribute.setIsolation(isolation);

			String timeout = methodEle.getAttribute(TIMEOUT_ATTRIBUTE);
			transactionAttribute.setTimeout(Integer.valueOf(timeout));

			String readOnly = methodEle.getAttribute(READ_ONLY_ATTRIBUTE);
			transactionAttribute.setReadOnly(Boolean.valueOf(readOnly));

			if (methodEle.hasAttribute(ROLLBACK_FOR_ATTRIBUTE)) {
				String rollbackForValue = methodEle
						.getAttribute(ROLLBACK_FOR_ATTRIBUTE);
				transactionAttribute.setRollbackForClassName(StringUtils
						.commaDelimitedListToStringArray(rollbackForValue));
			}
			if (methodEle.hasAttribute(NO_ROLLBACK_FOR_ATTRIBUTE)) {
				String noRollbackForValue = methodEle
						.getAttribute(NO_ROLLBACK_FOR_ATTRIBUTE);
				transactionAttribute.setNoRollbackForClassName(StringUtils
						.commaDelimitedListToStringArray(noRollbackForValue));
			}
			transactionAttributes.add(transactionAttribute);
		}

		return transactionAttributes;
	}

	private void addRollbackRuleAttributesTo(
			List<RollbackRuleAttribute> rollbackRules, String rollbackForValue) {
		String[] exceptionTypeNames = StringUtils
				.commaDelimitedListToStringArray(rollbackForValue);
		for (String typeName : exceptionTypeNames) {
			rollbackRules.add(new RollbackRuleAttribute(StringUtils
					.trimWhitespace(typeName)));
		}
	}

	private void addNoRollbackRuleAttributesTo(
			List<RollbackRuleAttribute> rollbackRules, String noRollbackForValue) {
		String[] exceptionTypeNames = StringUtils
				.commaDelimitedListToStringArray(noRollbackForValue);
		for (String typeName : exceptionTypeNames) {
			rollbackRules.add(new NoRollbackRuleAttribute(StringUtils
					.trimWhitespace(typeName)));
		}
	}

}
