package autonavi.online.framework.sharding.entry.xml.builder.support;

import java.util.Map;

import org.w3c.dom.Node;

public class CodeGeneration4Delete extends AbstractCodeGeneration implements
		CodeGeneration {

	@Override
	public String doGenerator(Class<?> interfaceClass, Node node)
			throws Exception {
		Map<String, Object> paramMap = super.getParamMap(interfaceClass, node);// 从XML中得到参数
		paramMap.put("tableOperation", "TableOperation.Delete");// 操作类型
		return super.doGenerator(paramMap);// 生成代码
	}

}
