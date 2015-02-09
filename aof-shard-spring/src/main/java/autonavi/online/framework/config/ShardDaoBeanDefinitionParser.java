package autonavi.online.framework.config;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.SqlSource;
import autonavi.online.framework.sharding.entry.xml.builder.support.session.SqlSession;
import autonavi.online.framework.sharding.holder.XmlBuilderHolder;

public class ShardDaoBeanDefinitionParser implements BeanDefinitionParser {

	private static final String DAO_ELEMENT = "dao";
	private static final String SCAN_ELEMENT = "scan";
	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";
	private static final String RESOURCE_PATTERN_ATTRIBUTE = "resource-pattern";
	private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "use-default-filters";
	private static final String EXCLUDE_FILTER_ELEMENT = "exclude-filter";
	private static final String INCLUDE_FILTER_ELEMENT = "include-filter";
	private static final String FILTER_TYPE_ATTRIBUTE = "type";
	private static final String FILTER_EXPRESSION_ATTRIBUTE = "expression";

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition();
		this.parseDao(element, parserContext, builder);

		// 将Map置入ManagedMap中去
		ManagedMap<Class<?>, Map<String, SqlSource>> classes = new ManagedMap<Class<?>, Map<String, SqlSource>>() {
			{
				putAll(XmlBuilderHolder.classesHolder.get());// 从本地线程中得到组合好的map
			}
		};
		XmlBuilderHolder.classesHolder = null;// 析构classesHolder

		// 生成SqlSession的Bean
		RootBeanDefinition SqlSessionBeanDefinition = new RootBeanDefinition(
				SqlSession.class);
		SqlSessionBeanDefinition.getPropertyValues().add("classes", classes);
		parserContext.getRegistry().registerBeanDefinition("sqlSession",
				SqlSessionBeanDefinition);
		return null;
	}

	/**
	 * 解析dao
	 * 
	 * @param daoEle
	 * @param parserContext
	 * @param builder
	 */
	private void parseDao(Element daoEle, ParserContext parserContext,
			BeanDefinitionBuilder builder) {

		List<Element> scanEles = DomUtils.getChildElementsByTagName(daoEle,
				SCAN_ELEMENT);
		for (Element scanEle : scanEles) {
			String[] basePackages = StringUtils.tokenizeToStringArray(
					scanEle.getAttribute(BASE_PACKAGE_ATTRIBUTE),
					ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);

			// Actually scan for bean definitions and register them.
			MyClassPathBeanDefinitionScanner scanner = configureScanner(
					parserContext, scanEle);
			Set<BeanDefinitionHolder> beanDefinitions = scanner
					.doScan(basePackages);
			registerComponents(parserContext.getReaderContext(),
					beanDefinitions, scanEle);
		}
	}

	/**
	 * 配置扫描类
	 * 
	 * @param parserContext
	 * @param element
	 * @return
	 */
	private MyClassPathBeanDefinitionScanner configureScanner(
			ParserContext parserContext, Element element) {
		XmlReaderContext readerContext = parserContext.getReaderContext();

		boolean useDefaultFilters = false;
		if (element.hasAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE)) {
			useDefaultFilters = Boolean.valueOf(element
					.getAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE));
		}

		// Delegate bean definition registration to scanner class.
		MyClassPathBeanDefinitionScanner scanner = createScanner(readerContext,
				useDefaultFilters);
		scanner.setResourceLoader(readerContext.getResourceLoader());
		/*
		 * scanner.setEnvironment(parserContext.getDelegate().getEnvironment());
		 * scanner.setBeanDefinitionDefaults(parserContext.getDelegate()
		 * .getBeanDefinitionDefaults());
		 * scanner.setAutowireCandidatePatterns(parserContext.getDelegate()
		 * .getAutowireCandidatePatterns());
		 * 
		 * if (element.hasAttribute(RESOURCE_PATTERN_ATTRIBUTE)) {
		 * scanner.setResourcePattern(element
		 * .getAttribute(RESOURCE_PATTERN_ATTRIBUTE)); }
		 */
		parseTypeFilters(element, scanner, readerContext, parserContext);

		return scanner;
	}

	/**
	 * 创建扫描类
	 * 
	 * @param readerContext
	 * @param useDefaultFilters
	 * @return
	 */
	protected MyClassPathBeanDefinitionScanner createScanner(
			XmlReaderContext readerContext, boolean useDefaultFilters) {
		return new MyClassPathBeanDefinitionScanner(
				readerContext.getRegistry(), useDefaultFilters);
	}

	/**
	 * 解析类型过滤器
	 * 
	 * @param element
	 * @param scanner
	 * @param readerContext
	 * @param parserContext
	 */
	protected void parseTypeFilters(Element element,
			MyClassPathBeanDefinitionScanner scanner,
			XmlReaderContext readerContext, ParserContext parserContext) {

		// Parse exclude and include filter elements.
		ClassLoader classLoader = scanner.getResourceLoader().getClassLoader();
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String localName = parserContext.getDelegate().getLocalName(
						node);
				try {
					if (INCLUDE_FILTER_ELEMENT.equals(localName)) {
						TypeFilter typeFilter = createTypeFilter(
								(Element) node, classLoader);
						scanner.addIncludeFilter(typeFilter);
					} else if (EXCLUDE_FILTER_ELEMENT.equals(localName)) {
						TypeFilter typeFilter = createTypeFilter(
								(Element) node, classLoader);
						scanner.addExcludeFilter(typeFilter);
					}
				} catch (Exception ex) {
					readerContext
							.error(ex.getMessage(),
									readerContext.extractSource(element),
									ex.getCause());
				}
			}
		}
	}

	/**
	 * 创建类型过滤器
	 * 
	 * @param element
	 * @param classLoader
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected TypeFilter createTypeFilter(Element element,
			ClassLoader classLoader) {
		String filterType = element.getAttribute(FILTER_TYPE_ATTRIBUTE);
		String expression = element.getAttribute(FILTER_EXPRESSION_ATTRIBUTE);
		try {
			if ("annotation".equals(filterType)) {
				return new AnnotationTypeFilter(
						(Class<Annotation>) classLoader.loadClass(expression));
			} else if ("assignable".equals(filterType)) {
				return new AssignableTypeFilter(
						classLoader.loadClass(expression));
			} else if ("aspectj".equals(filterType)) {
				return new AspectJTypeFilter(expression, classLoader);
			} else if ("regex".equals(filterType)) {
				return new RegexPatternTypeFilter(Pattern.compile(expression));
			} else if ("custom".equals(filterType)) {
				Class<?> filterClass = classLoader.loadClass(expression);
				if (!TypeFilter.class.isAssignableFrom(filterClass)) {
					throw new IllegalArgumentException(
							"Class is not assignable to ["
									+ TypeFilter.class.getName() + "]: "
									+ expression);
				}
				return (TypeFilter) BeanUtils.instantiateClass(filterClass);
			} else {
				throw new IllegalArgumentException("Unsupported filter type: "
						+ filterType);
			}
		} catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Type filter class not found: "
					+ expression, ex);
		}
	}

	/**
	 * 注册组件
	 * 
	 * @param readerContext
	 * @param beanDefinitions
	 * @param element
	 */
	private void registerComponents(XmlReaderContext readerContext,
			Set<BeanDefinitionHolder> beanDefinitions, Element element) {

		Object source = readerContext.extractSource(element);
		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(
				element.getTagName(), source);

		for (BeanDefinitionHolder beanDefHolder : beanDefinitions) {
			compositeDef.addNestedComponent(new BeanComponentDefinition(
					beanDefHolder));
		}

		// Register annotation config processors, if necessary.
		/*
		 * boolean annotationConfig = true; if
		 * (daoEle.hasAttribute(ANNOTATION_CONFIG_ATTRIBUTE)) { annotationConfig
		 * = Boolean.valueOf(daoEle .getAttribute(ANNOTATION_CONFIG_ATTRIBUTE));
		 * } if (annotationConfig) { Set<BeanDefinitionHolder>
		 * processorDefinitions = AnnotationConfigUtils
		 * .registerAnnotationConfigProcessors( readerContext.getRegistry(),
		 * source); for (BeanDefinitionHolder processorDefinition :
		 * processorDefinitions) { compositeDef.addNestedComponent(new
		 * BeanComponentDefinition( processorDefinition)); } }
		 */

		readerContext.fireComponentRegistered(compositeDef);
	}

}
