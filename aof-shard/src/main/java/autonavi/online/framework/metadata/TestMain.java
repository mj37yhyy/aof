package autonavi.online.framework.metadata;

import java.util.ArrayList;
import java.util.List;

import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.metadata.entity.FormTable;
import autonavi.online.framework.metadata.xml.SessionFactoryEntity;

/**
 * @author Michael
 * 
 */
public class TestMain {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TestMain test = new TestMain();
		SessionFactoryEntity sfe = new SessionFactoryEntity();
		sfe.setDialect("org.hibernate.dialect.MySQLDialect");
		sfe.setConnection_driver_class("com.mysql.jdbc.Driver");
		sfe.setConnection_url("jdbc:mysql://10.19.3.158:3306/addresscollect?rewriteBatchedStatements=true");
		sfe.setConnection_username("root");
		sfe.setConnection_password("93f0cb0614");
		sfe.setShow_sql("true");
		sfe.setHibernate_autoReconnect("true");
		sfe.setHibernate_hbm2ddl_auto("update");
		FormTable fromTable = test.initData();

		TableGenerator tg = new TableGenerator(sfe, fromTable);
		tg.generatorTable();

	}

	/**
	 * 初始化数据
	 * 
	 * @return
	 */
	private FormTable initData() {
		FormTable form = new FormTable();
		form.setName("aofServerMonitor");
		form.setTableName("AOF_SERVER_MONITOR");
		ColumnAttribute pk = new ColumnAttribute();
		pk.setName("ip");
		pk.setColumnType("string");
		pk.setColumnName("IP");
		pk.setLength(100);
		form.setPk(pk);

		List<ColumnAttribute> list = new ArrayList<ColumnAttribute>();
		ColumnAttribute attr2 = new ColumnAttribute();
		attr2.setName("version");
		attr2.setColumnType("string");
		attr2.setColumnName("VERSION");
		attr2.setLength(100);
		list.add(attr2);
		ColumnAttribute attr3 = new ColumnAttribute();
		attr3.setName("mtime");
		attr3.setColumnType("string");
		attr3.setColumnName("MTIME");
		attr3.setLength(100);
		list.add(attr3);
		form.setFormAttributeList(list);
		return form;
	}

}
