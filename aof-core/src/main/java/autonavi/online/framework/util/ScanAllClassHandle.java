package autonavi.online.framework.util;

import org.springframework.core.type.classreading.MetadataReader;

public interface ScanAllClassHandle {

	public void handle(MetadataReader metadataReader) throws Exception;

}
