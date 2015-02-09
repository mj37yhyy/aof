package autonavi.online.framework.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * 扫描类
 * @author jia.miao
 * @author yaming.xu
 *
 */
public class ScanAllClass {

	public void scanner(String resourcePath,
			ScanAllClassHandle scanAllClassHandle) throws Exception {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		String ENTITY_CLASS_RESOURCE_PATTERN = "/**/*.class";
		try {
			String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ ClassUtils.convertClassNameToResourcePath(resourcePath)
					+ ENTITY_CLASS_RESOURCE_PATTERN;
			Resource[] resources = resourcePatternResolver
					.getResources(pattern);
			MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(
					resourcePatternResolver);
			for (Resource resource : resources) {
				if (resource.isReadable()) {
					MetadataReader reader = readerFactory
							.getMetadataReader(resource);
					scanAllClassHandle.handle(reader);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
