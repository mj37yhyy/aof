package autonavi.online.framework.sharding.entry.xml.builder;

import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import autonavi.online.framework.sharding.entry.xml.builder.support.CodeGeneration;
import autonavi.online.framework.util.xml.DOMUtils;

public class XmlBuilder {

	/**
	 * 解析SQL
	 * 
	 * @param i
	 * 
	 * @param in
	 * @throws Exception
	 * 
	 * @retrun 返回解析后的JAVA代码Map
	 */
	public Map<String, String> createMethodsFromXml(Class<?> interfaceClass,
			InputStream in) throws Exception {
		if (in == null)
			return null;
		Document doc = DOMUtils.parseXMLDocument(in);
		Element root = doc.getDocumentElement();
		if (!this.isXMLValid(DOMUtils.toStringFromDoc(doc))) {
			throw new Exception("XML不符合aof-mapper.xsd的规则");
		}

		Map<String, String> methodsMap = new HashMap<String, String>();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		if (root.getTagName().equals("mapper")) {
			String namespace = root.getAttribute("namespace");
			NodeList nodeList = root.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node child = nodeList.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					String elName = child.getNodeName();
					CodeGeneration codeGeneration = (CodeGeneration) classLoader
							.loadClass(
									CodeGeneration.class.getName()
											+ "4"
											+ elName.substring(0, 1)
													.toUpperCase()
											+ elName.substring(1))
							.newInstance();
					/**
					 * 得到解析的字符串并插入其中
					 */
					Element el = (Element) child;
					methodsMap.put(el.getAttribute("id"),
							codeGeneration.doGenerator(interfaceClass, child));
				}
			}
		}
		return methodsMap;
	}

	/**
	 * 校验XML是否符合aof-mapper.xsd
	 * 
	 * @param targetXML
	 * @return
	 */
	private boolean isXMLValid(String targetXML) {
		boolean flag = true;
		try {
			Source schemaFile = new StreamSource(getClass()
					.getResourceAsStream("xml/aof-mapper.xsd"));

			SchemaFactory factory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new StringReader(targetXML)));
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
}