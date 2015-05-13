package autonavi.online.framework.sharding.dao;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class SqlParserUtils {

	/**
	 * 获得SQL中所有涉及到的表名
	 * 
	 * @param sql
	 * @return 表名数组
	 * @throws Exception
	 */
	public String[] getTablesNames(String sql) throws Exception {
		List<String> tableList = new ArrayList<String>();
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		CCJSqlParserManager parserManager = new CCJSqlParserManager();
		Statement statement = parserManager.parse(new StringReader(sql));
		if (statement instanceof Select) {
			Select select = (Select) statement;
			tableList = tablesNamesFinder.getTableList(select);
		} else if (statement instanceof Update) {
			Update update = (Update) statement;
			tableList = tablesNamesFinder.getTableList(update);
		} else if (statement instanceof Insert) {
			Insert insert = (Insert) statement;
			tableList = tablesNamesFinder.getTableList(insert);
		} else if (statement instanceof Delete) {
			Delete delete = (Delete) statement;
			tableList = tablesNamesFinder.getTableList(delete);
		}  else if (statement instanceof Replace) {
			Replace replace = (Replace) statement;
			tableList = tablesNamesFinder.getTableList(replace);
		} 
		return tableList.toArray(new String[0]);
	}

	public String setTableAndJoins(String sql,String oldTableName,String newTableName) throws Exception {
		CCJSqlParserManager parserManager = new CCJSqlParserManager();
		Statement statement = parserManager.parse(new StringReader(sql));
		if (statement instanceof Select) {// 如果是Select，修改表名和连接表名
			Select select = (Select) statement;
			PlainSelect ps = (PlainSelect) select.getSelectBody();
			if(ps.getFromItem().toString().equalsIgnoreCase(oldTableName)){
				ps.setFromItem(new Table(newTableName));
			}
			if (ps.getJoins() != null) {
				for (Join join : ps.getJoins()) {
					if(join.toString().equalsIgnoreCase(oldTableName)){
						join.setRightItem(new Table(newTableName));
					}
				}
			}
			sql = select.toString();
		} else if (statement instanceof Update) {// 如果是Update，解析所有的表名的连接表名，全部返回
			Update update = (Update) statement;
			if(update.getFromItem().toString().equalsIgnoreCase(oldTableName)){
				update.setFromItem(new Table(newTableName));
			}
			if (update.getJoins() != null) {
				for (Join join : update.getJoins()) {
					if(join.toString().equalsIgnoreCase(oldTableName)){
						join.setRightItem(new Table(newTableName));
					}
				}
			}
			sql = update.toString();
		} else if (statement instanceof Insert) {// 如果是Insert，直接返回表名。不包含where中的部分
			Insert insert = (Insert) statement;
			insert.setTable(new Table(newTableName));
			sql = insert.toString();
		} else if (statement instanceof Delete) {// 如果是Insert，直接返回表名。不包含where中的部分
			Delete delete = (Delete) statement;
			delete.setTable(new Table(newTableName));
			sql = delete.toString();
		} else if (statement instanceof Replace) {// 如果是Replace，直接返回表名
			Replace replace = (Replace) statement;
			replace.setTable(new Table(newTableName));
			sql = replace.toString();
		}
		return sql;
	}

	public static void main(String[] agrs) throws Exception {
		SqlParserUtils spu = new SqlParserUtils();
		long st = System.currentTimeMillis();
		String sql = "SELECT * FROM MYTABLE,demo WHERE COLUMN_A = ? AND COLUMN_B <> ? and a=2 and b=3 or c=4 and id in (select id from demo1) order by d";
//		for (String name : spu.getTablesNames(sql)) {
//			System.out.println(name);
//		}
		System.out.println(spu.setTableAndJoins(sql, "demo", "demo_123"));
		System.out.println("所用时间："+(System.currentTimeMillis()-st)+"ms");
//
//		sql = "update a1234fd set b=1,c=2 where id=121";
//		for (String name : spu.getTablesNames(sql)) {
//			System.out.println(name);
//		}
//
//		sql = "insert into test (id,name) values(1,'12')";
//		for (String name : spu.getTablesNames(sql)) {
//			System.out.println(name);
//		}
//
//		sql = "delete Ader where id in(select id from test3)";
//		for (String name : spu.getTablesNames(sql)) {
//			System.out.println(name);
//		}
		
//
//		sql = "insert into test (id,name) values(?,?)";
//		CCJSqlParserManager parserManager = new CCJSqlParserManager();
//		Statement statement = parserManager.parse(new StringReader(sql));
//		if (statement instanceof Insert) {
//			Insert insert = (Insert) statement;
//			
//			List<Column> l = insert.getColumns();
//			Column column = new Column();
//			column.setColumnName("value");
//			l.add(0,column);
//			insert.setColumns(l);
//
//			List<Expression> l2 = ((ExpressionList)insert.getItemsList()).getExpressions();
//			Expression expression = new LongValue(12345679);
//			l2.add(0,expression);
//			
//			((ExpressionList)insert.getItemsList()).setExpressions(l2);
//			
//			for(Column c : insert.getColumns()){
//				System.out.println("Column="+c.getColumnName());
//			}
//			for(Expression e:((ExpressionList)insert.getItemsList()).getExpressions()){
//				System.out.println(e);
//			}
//			
//			System.out.println(insert.toString());
//		} 
		//System.out.println(spu.setTableAndJoins(sql, "苗家"));
	}

}
