package autonavi.online.framework.sharding.entry.xml.builder.support;

import org.w3c.dom.Node;

public interface CodeGeneration {
	public String doGenerator(Class<?> interfaceClass, Node node) throws Exception;
}
