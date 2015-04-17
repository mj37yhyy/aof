package autonavi.online.framework.sharding.entry.xml.builder.support.xmltags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import autonavi.online.framework.sharding.dao.exception.BuilderException;
import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.SqlSource;

public class XMLScriptBuilder {
	Node context;

	public XMLScriptBuilder(Node context) {
		this.context = context;
	}

	public SqlSource parseScriptNode() throws Exception {
		List<SqlNode> contents = parseDynamicTags(context);
		MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
		SqlSource sqlSource = new DynamicSqlSource(rootSqlNode);
		return sqlSource;
	}

	private List<SqlNode> parseDynamicTags(Node node) {
		List<SqlNode> contents = new ArrayList<SqlNode>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String nodeName = child.getNodeName();
			if (child.getNodeType() == Node.CDATA_SECTION_NODE
					|| child.getNodeType() == Node.TEXT_NODE) {
				String data = child.getNodeValue();
				contents.add(new TextSqlNode(data));
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				NodeHandler handler = nodeHandlers.get(nodeName);
				if (handler == null) {
					throw new RuntimeException("Unknown element <" + nodeName
							+ "> in SQL statement.");
				}
				handler.handleNode(child, contents);
			}
		}
		return contents;
	}

	private Map<String, NodeHandler> nodeHandlers = new HashMap<String, NodeHandler>() {

		private static final long serialVersionUID = -3548694635442244865L;

		{
			put("trim", new TrimHandler());
			put("where", new WhereHandler());
			put("set", new SetHandler());
			put("foreach", new ForEachHandler());
			put("if", new IfHandler());
			put("choose", new ChooseHandler());
			put("when", new IfHandler());
			put("otherwise", new OtherwiseHandler());
			//put("bind", new BindHandler());
		}
	};

	private interface NodeHandler {
		void handleNode(Node nodeToHandle, List<SqlNode> targetContents);
	}

	private class WhereHandler implements NodeHandler {
		public void handleNode(Node nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			WhereSqlNode where = new WhereSqlNode(mixedSqlNode);
			targetContents.add(where);
		}
	}

	private class TrimHandler implements NodeHandler {
		public void handleNode(Node nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			String prefix = ((Element) nodeToHandle).getAttribute("prefix");
			String prefixOverrides = ((Element) nodeToHandle)
					.getAttribute("prefixOverrides");
			String suffix = ((Element) nodeToHandle).getAttribute("suffix");
			String suffixOverrides = ((Element) nodeToHandle)
					.getAttribute("suffixOverrides");
			TrimSqlNode trim = new TrimSqlNode(mixedSqlNode, prefix,
					prefixOverrides, suffix, suffixOverrides);
			targetContents.add(trim);
		}
	}

	private class SetHandler implements NodeHandler {
		public void handleNode(Node nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			SetSqlNode set = new SetSqlNode(mixedSqlNode);
			targetContents.add(set);
		}
	}

	private class ForEachHandler implements NodeHandler {
		public void handleNode(Node nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			String collection = ((Element) nodeToHandle)
					.getAttribute("collection");
			String item = ((Element) nodeToHandle).getAttribute("item");
			String index = ((Element) nodeToHandle).getAttribute("index");
			String open = ((Element) nodeToHandle).getAttribute("open");
			String close = ((Element) nodeToHandle).getAttribute("close");
			String separator = ((Element) nodeToHandle)
					.getAttribute("separator");
			ForEachSqlNode forEachSqlNode = new ForEachSqlNode(mixedSqlNode,
					collection, index, item, open, close, separator);
			targetContents.add(forEachSqlNode);
		}
	}

	private class IfHandler implements NodeHandler {
		public void handleNode(Node nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			String test = ((Element) nodeToHandle).getAttribute("test");
			IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
			targetContents.add(ifSqlNode);
		}
	}

	private class OtherwiseHandler implements NodeHandler {
		public void handleNode(Node nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			targetContents.add(mixedSqlNode);
		}
	}

	private class ChooseHandler implements NodeHandler {
		public void handleNode(Node nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> whenSqlNodes = new ArrayList<SqlNode>();
			List<SqlNode> otherwiseSqlNodes = new ArrayList<SqlNode>();
			handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes,
					otherwiseSqlNodes);
			SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
			ChooseSqlNode chooseSqlNode = new ChooseSqlNode(whenSqlNodes,
					defaultSqlNode);
			targetContents.add(chooseSqlNode);
		}

		private void handleWhenOtherwiseNodes(Node chooseSqlNode,
				List<SqlNode> ifSqlNodes, List<SqlNode> defaultSqlNodes) {
			NodeList children = chooseSqlNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				String nodeName = child.getNodeName();
				NodeHandler handler = nodeHandlers.get(nodeName);
				if (handler instanceof IfHandler) {
					handler.handleNode(child, ifSqlNodes);
				} else if (handler instanceof OtherwiseHandler) {
					handler.handleNode(child, defaultSqlNodes);
				}
			}
		}

		private SqlNode getDefaultSqlNode(List<SqlNode> defaultSqlNodes) {
			SqlNode defaultSqlNode = null;
			if (defaultSqlNodes.size() == 1) {
				defaultSqlNode = defaultSqlNodes.get(0);
			} else if (defaultSqlNodes.size() > 1) {
				throw new BuilderException(
						"Too many default (otherwise) elements in choose statement.");
			}
			return defaultSqlNode;
		}
	}

	/*private class BindHandler implements NodeHandler {
		public void handleNode(Node nodeToHandle, List<SqlNode> targetContents) {
			final String name = ((Element) nodeToHandle).getAttribute("name");
			final String expression = ((Element) nodeToHandle).getAttribute("value");
			final VarDeclSqlNode node = new VarDeclSqlNode(name, expression);
			targetContents.add(node);
		}
	}*/
}
