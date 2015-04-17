package autonavi.online.framework.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import autonavi.online.framework.sharding.entry.xml.builder.ClassGeneration;
import autonavi.online.framework.util.ScanAllClass;
import autonavi.online.framework.util.ScanAllClassHandle;

public class MyClassPathBeanDefinitionScanner {
	private final BeanDefinitionRegistry registry;
	private boolean useDefaultFilters = false;
	private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();
	private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
			this.resourcePatternResolver);

	private final List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();

	public MyClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	public MyClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry,
			boolean useDefaultFilters) {
		this.registry = registry;
		this.useDefaultFilters = useDefaultFilters;
	}

	/**
	 * Perform a scan within the specified base packages, returning the
	 * registered bean definitions.
	 * <p>
	 * This method does <i>not</i> register an annotation config processor but
	 * rather leaves this up to the caller.
	 * 
	 * @param basePackages
	 *            the packages to check for annotated classes
	 * @param builder
	 * @return set of beans registered if any for tooling registration purposes
	 *         (never {@code null})
	 */
	protected Set<BeanDefinitionHolder> doScan(String[] basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
		try {
			for (String basePackage : basePackages) {
				Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
				if (candidates != null) {
					for (BeanDefinition candidate : candidates) {
						candidate
								.setScope(ConfigurableBeanFactory.SCOPE_SINGLETON);
						String beanName = this.beanNameGenerator
								.generateBeanName(candidate, this.registry);
						if (checkCandidate(beanName, candidate)) {
							BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(
									candidate, beanName);
							beanDefinitions.add(definitionHolder);
							registerBeanDefinition(definitionHolder,
									this.registry);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return beanDefinitions;
	}

	/**
	 * 查找接口方法实例化DAO类
	 * 
	 * @param basePackage
	 * @param builder
	 * @return
	 * @throws Exception
	 */
	private Set<BeanDefinition> findCandidateComponents(String basePackage)
			throws Exception {
		final Set<BeanDefinition> set = new HashSet<BeanDefinition>();
		ScanAllClass scanAllClass = new ScanAllClass();
		// 扫描指定目录
		scanAllClass.scanner(basePackage, new ScanAllClassHandle() {
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();

			ClassGeneration classGeneration = new ClassGeneration();
			RuntimeBeanReference daoSupportRef = new RuntimeBeanReference(
					"daoSupport");
			RuntimeBeanReference sqlSessionRef = new RuntimeBeanReference(
					"sqlSession");

			@Override
			public void handle(MetadataReader metadataReader) throws Exception {
				if (isCandidateComponent(metadataReader)) {// 判断是否符合excludeFilters和includeFilters和条件
					ClassMetadata classMetadata = metadataReader
							.getClassMetadata();
					// dao类必须是接口
					if (classMetadata.isInterface()) {
						// 通过接口生成实现类
						Class<?> implClass = classGeneration.createImplClassFromInterface(classLoader
								.loadClass(classMetadata.getClassName()));
						if (implClass != null) {
							RootBeanDefinition implClassBeanDefinition = new RootBeanDefinition(
									implClass);
							implClassBeanDefinition.getPropertyValues()
									.add("daoSupport", daoSupportRef)
									.add("sqlSession", sqlSessionRef);
							set.add(implClassBeanDefinition);
						}
					}
				}
			}

		});

		return set;
	}

	/**
	 * 注册BeanDefinition
	 * 
	 * @param definitionHolder
	 * @param registry
	 */
	protected void registerBeanDefinition(
			BeanDefinitionHolder definitionHolder,
			BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder,
				registry);
	}

	/**
	 * 判断BEAN是否已经存在，如果存在，就跳过
	 * 
	 * @param beanName
	 * @param beanDefinition
	 * @return
	 * @throws Exception
	 */
	protected boolean checkCandidate(String beanName,
			BeanDefinition beanDefinition) throws Exception {
		if (!this.registry.containsBeanDefinition(beanName)) {
			return true;
		}
		BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
		BeanDefinition originatingDef = existingDef
				.getOriginatingBeanDefinition();
		if (originatingDef != null) {
			existingDef = originatingDef;
		}
		if (isCompatible(beanDefinition, existingDef)) {
			return false;
		}
		throw new Exception("Annotation-specified bean name '" + beanName
				+ "' for bean class [" + beanDefinition.getBeanClassName()
				+ "] conflicts with existing, "
				+ "non-compatible bean definition of same name and class ["
				+ existingDef.getBeanClassName() + "]");
	}

	/**
	 * 判断两个BEAN是否是同一个
	 * 
	 * @param newDefinition
	 * @param existingDefinition
	 * @return
	 */
	protected boolean isCompatible(BeanDefinition newDefinition,
			BeanDefinition existingDefinition) {
		return (!(existingDefinition instanceof ScannedGenericBeanDefinition) || // explicitly
																					// registered
																					// overriding
																					// bean
				newDefinition.getSource()
						.equals(existingDefinition.getSource()) || // scanned
																	// same file
																	// twice
		newDefinition.equals(existingDefinition)); // scanned equivalent class
													// twice
	}

	/**
	 * 判断是否符合excludeFilters和includeFilters和条件
	 * 
	 * @param metadataReader
	 * @return
	 * @throws IOException
	 */
	protected boolean isCandidateComponent(MetadataReader metadataReader)
			throws IOException {
		if (this.excludeFilters != null && !this.excludeFilters.isEmpty()
				&& this.includeFilters != null
				&& !this.includeFilters.isEmpty()) {
			for (TypeFilter tf : this.excludeFilters) {
				if (tf.match(metadataReader, this.metadataReaderFactory)) {
					return false;
				}
			}
			for (TypeFilter tf : this.includeFilters) {
				if (tf.match(metadataReader, this.metadataReaderFactory)) {
					return true;
				}
			}
			return false;
		} else
			return true;
	}

	/**
	 * Set the BeanNameGenerator to use for detected bean classes.
	 * <p>
	 * Default is a {@link AnnotationBeanNameGenerator}.
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator
				: new AnnotationBeanNameGenerator());
	}

	/**
	 * Add an include type filter to the <i>end</i> of the inclusion list.
	 */
	public void addIncludeFilter(TypeFilter includeFilter) {
		this.includeFilters.add(includeFilter);
	}

	/**
	 * Add an exclude type filter to the <i>front</i> of the exclusion list.
	 */
	public void addExcludeFilter(TypeFilter excludeFilter) {
		this.excludeFilters.add(0, excludeFilter);
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils
				.getResourcePatternResolver(resourceLoader);
		this.metadataReaderFactory = new CachingMetadataReaderFactory(
				resourceLoader);
	}

	/**
	 * Return the ResourceLoader that this component provider uses.
	 */
	public final ResourceLoader getResourceLoader() {
		return this.resourcePatternResolver;
	}
}
