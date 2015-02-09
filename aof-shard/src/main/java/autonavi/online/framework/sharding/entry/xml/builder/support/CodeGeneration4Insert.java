package autonavi.online.framework.sharding.entry.xml.builder.support;

import java.util.Map;

import org.dom4j.Element;
import org.w3c.dom.Node;

public class CodeGeneration4Insert extends AbstractCodeGeneration implements
		CodeGeneration {

	@Override
	public String doGenerator(Class<?> interfaceClass, Node node)
			throws Exception {
		Map<String, Object> paramMap = super.getParamMap(interfaceClass, node);// 从XML中得到参数
		paramMap.put("tableOperation", "TableOperation.Insert");// 操作类型
		return super.doGenerator(paramMap);// 生成代码
	}

}
