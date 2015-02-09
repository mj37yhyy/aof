package autonavi.online.framework.sharding.dao;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;

import org.apache.commons.beanutils.PropertyUtils;

import autonavi.online.framework.metadata.DialectUtils;
import autonavi.online.framework.sharding.dao.constant.RegularExpressions;
import autonavi.online.framework.sharding.dao.constant.ReservedWord;
import autonavi.online.framework.sharding.transaction.holder.TimeOutHolder;
import autonavi.online.framework.sharding.uniqueid.IdWorker;
import autonavi.online.framework.sharding.uniqueid.IdWorkerType;
import autonavi.online.framework.sharding.uniqueid.UniqueIDFactory;
import autonavi.online.framework.util.StopWatchLogger;

public class NamedParameterStatement {

	public NamedParameterStatement(Connection connection, String query,
			Map<String, Object> parameterMap) throws Exception {
		StopWatchLogger swlogger = new StopWatchLogger(this.getClass());// 打印耗时日志
		indexMap = new HashMap();
		swlogger.start("解析SQL语句，把${...}和#{...}替换掉");
		// 解析SQL语句，把${...}和#{...}替换掉
		parsedQuery = parse2(query, parameterMap, indexMap);
		swlogger.stop();
		swlogger.start("得到prepareStatement");
		// 针对mysql的特别处理，因为mysql会缓存所有的rs结果。（注意，需要配合useCursorFetch=true参数）
		if (DialectUtils.getDialect(connection, false)
				.equalsIgnoreCase("mysql")) {
			// 此处改为ResultSet.CONCUR_UPDATABLE的原因是mysql会把过长的字符串截断，但开发时就要注意不要改字段的值了
			this.statement = connection.prepareStatement(parsedQuery,
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			this.statement.setFetchSize(10);
		} else {
			this.statement = connection.prepareStatement(parsedQuery);
		}
		if (TimeOutHolder.getTimeOut() > 0)
			this.statement.setQueryTimeout(TimeOutHolder.getTimeOut());
		swlogger.stop();
		swlogger.writeLog();
	}

	/**
	 * 解析:...串
	 * 
	 * @param query
	 * @param paramMap
	 * @return
	 */
	final String parse(String query, Map paramMap) {

		int length = query.length();
		StringBuffer parsedQuery = new StringBuffer(length);
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int index = 1;

		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);
			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			} else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			} else {
				if (c == '\'') {
					inSingleQuote = true;
				} else if (c == '"') {
					inDoubleQuote = true;
				} else if (c == ':' && i + 1 < length
						&& Character.isJavaIdentifierStart(query.charAt(i + 1))) {
					int j = i + 2;
					while (j < length
							&& Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}
					String name = query.substring(i + 1, j);
					c = '?';
					i += name.length();

					List indexList = (List) paramMap.get(name);
					if (indexList == null) {
						indexList = new LinkedList();
						paramMap.put(name, indexList);
					}
					indexList.add(new Integer(index));

					index++;
				}
			}
			parsedQuery.append(c);
		}

		for (Iterator itr = paramMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry entry = (Map.Entry) itr.next();
			List list = (List) entry.getValue();
			int[] indexes = new int[list.size()];
			int i = 0;
			for (Iterator itr2 = list.iterator(); itr2.hasNext();) {
				Integer x = (Integer) itr2.next();
				indexes[i++] = x.intValue();
			}
			entry.setValue(indexes);
		}

		return parsedQuery.toString();
	}

	/**
	 * 解析#{}和${}串
	 * 
	 * @param query
	 * @param parameterMap
	 * @param indexMap
	 * @return
	 * @throws Exception
	 */
	final String parse2(String query, Map parameterMap, Map indexMap)
			throws Exception {
		// 替换所有${...}的字符串
		Pattern pattern = Pattern
				.compile(RegularExpressions.PARAM_RIGHT_STRING);
		Matcher matcher = pattern.matcher(query);
		while (matcher.find()) {
			String param = matcher.group();
			if (param.indexOf(ReservedWord.snowflake) > -1) {
				query = query
						.replace(
								param,
								idWorker.nextId(CustomerContextHolder
										.getCustomerType()) + "");
			} else {
				Object obj = PropertyUtils.getProperty(parameterMap, param
						.replace("${", "").replace("}", ""));
				if (obj == null) {
					throw new RuntimeException("要替换的参数[" + param + "] 在入参中没有找到");
				}
				String replacement = null;
				try {
					replacement = (String) obj;
				} catch (ClassCastException cce) {
					try {
						replacement = obj.toString();
					} catch (Exception e) {
						try {
							replacement = String.valueOf(obj);
						} catch (Exception e2) {
							throw new RuntimeException("要替换的参数[" + param + "] 无法转换为String。");
						}
					}
				}
				query = query.replace(param, replacement);
			}
		}

		// 将字段名与顺序对应起来
		pattern = Pattern.compile(RegularExpressions.PARAM_RIGHT_ASK);
		matcher = pattern.matcher(query);
		int index = 1;
		while (matcher.find()) {
			String matching = matcher.group();
			String paramName = matching.substring(2, matching.length() - 1);
			List indexList = (List) indexMap.get(paramName);
			if (indexList == null) {
				indexList = new LinkedList();
				indexMap.put(paramName, indexList);
			}
			indexList.add(new Integer(index));
			index++;
		}

		for (Iterator itr = indexMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry entry = (Map.Entry) itr.next();
			List list = (List) entry.getValue();
			int[] indexes = new int[list.size()];
			int i = 0;
			for (Iterator itr2 = list.iterator(); itr2.hasNext();) {
				Integer x = (Integer) itr2.next();
				indexes[i++] = x.intValue();
			}
			entry.setValue(indexes);
		}

		return query.replaceAll(RegularExpressions.PARAM_RIGHT_ASK, "?");// 替换所有#{...}的字符串为?
	}

	/**
	 * 解析SQL，看是否是insert语句
	 * 
	 * @param query
	 * @return
	 * @throws JSQLParserException
	 */
	public final boolean isInsert(String query) throws JSQLParserException {
		StopWatchLogger swlogger = new StopWatchLogger(
				NamedParameterStatement.class);// 打印耗时日志
		boolean isInsert = false;
		swlogger.start("初始化CCJSqlParserManager");
		CCJSqlParserManager parserManager = new CCJSqlParserManager();
		swlogger.stop();
		swlogger.start("parserManager.parse");
		Statement sqlStatement = parserManager.parse(new StringReader(query));
		swlogger.stop();
		swlogger.start("parserManager.parse");
		if (sqlStatement instanceof Insert) {// 如果是insert操作
			Insert insert = (Insert) sqlStatement;
			if (insert.getItemsList() instanceof ExpressionList) {// 如果是正常插入才做操作，如果是子查询就不做操作
				isInsert = true;
			}
		}
		swlogger.stop();
		swlogger.writeLog();
		return isInsert;
	}

	/**
	 * 解析SQL语句，添加ID
	 * 
	 * @param parsedQuery
	 * @return
	 * @throws Exception
	 */
	private final String addId(String parsedQuery) throws Exception {
		String newParsedQuery = null;
		CCJSqlParserManager parserManager = new CCJSqlParserManager();
		Statement sqlStatement = parserManager.parse(new StringReader(
				parsedQuery));
		if (sqlStatement instanceof Insert) {// 如果是insert操作
			Insert insert = (Insert) sqlStatement;

			ItemsList itemsList = insert.getItemsList();
			if (itemsList instanceof ExpressionList) {// 如果是正常插入才做操作，如果是子查询就不做操作
				// 插入字段ID到第一个位置
				List<Column> columns = insert.getColumns();
				Column column = new Column();
				column.setColumnName("ID");
				columns.add(0, column);
				insert.setColumns(columns);

				// 插入?到values中的第一个位置
				List<Expression> expressions = ((ExpressionList) itemsList)
						.getExpressions();
				Expression expression = new JdbcParameter();
				expressions.add(0, expression);
				((ExpressionList) insert.getItemsList())
						.setExpressions(expressions);

				newParsedQuery = insert.toString();// 将新生成的SQL返回
			}
		}
		return newParsedQuery;
	}

	private int[] getIndexes(String name) {
		int[] indexes = (int[]) indexMap.get(name);
		if (indexes == null) {
			throw new IllegalArgumentException("Parameter not found: " + name);
		}
		return indexes;
	}

	public void setNull(String name) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setNull(indexes[i], Types.NULL);
		}
	}

	public void setObject(String name, Object value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setObject(indexes[i], value);
		}
	}

	public void setString(String name, String value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setString(indexes[i], value);
		}
	}

	public void setInt(String name, int value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setInt(indexes[i], value);
		}
	}

	public void setLong(String name, long value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setLong(indexes[i], value);
		}
	}

	public void setDouble(String name, double value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setDouble(indexes[i], value);
		}
	}

	public void setTimestamp(String name, Timestamp value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setTimestamp(indexes[i], value);
		}
	}

	public void setDate(String name, Date value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setDate(indexes[i], value);
		}
	}

	public PreparedStatement getStatement() {
		return statement;
	}

	public boolean execute() throws SQLException {
		return statement.execute();
	}

	public ResultSet executeQuery() throws SQLException {
		return statement.executeQuery();
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		return statement.executeQuery(sql);
	}

	public int executeUpdate() throws SQLException {
		return statement.executeUpdate();
	}

	public void close() throws SQLException {
		statement.close();
	}

	public void addBatch() throws SQLException {
		statement.addBatch();
	}

	public int[] executeBatch() throws SQLException {
		return statement.executeBatch();
	}

	public boolean isClosed() throws SQLException {
		return statement.isClosed();
	}

	private final PreparedStatement statement;

	private String parsedQuery;

	private final Map indexMap;

	private final IdWorker idWorker = UniqueIDFactory
			.getIdWorker(IdWorkerType.snowflake);

	public String getParsedQuery() {
		return parsedQuery;
	}
}
