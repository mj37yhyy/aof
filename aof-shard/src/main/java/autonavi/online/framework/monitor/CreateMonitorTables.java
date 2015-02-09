package autonavi.online.framework.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.metadata.CreateTable;
import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.metadata.entity.FormTable;

public class CreateMonitorTables implements CreateTable {

	private Logger log = LogManager.getLogger(this.getClass());
	protected static String MONITOR_TABLE = "AOF_SERVER_MONITOR";

	public FormTable createTable2IndexShard() throws Exception {
		FormTable form = new FormTable();
		form.setName(MONITOR_TABLE.toLowerCase());
		form.setTableName(MONITOR_TABLE);

		ColumnAttribute pk = new ColumnAttribute();
		pk.setName("ip");
		pk.setColumnType("string");
		pk.setColumnName("IP");
		pk.setLength(100);
		form.setPk(pk);

		List<ColumnAttribute> list = new ArrayList<ColumnAttribute>();
		ColumnAttribute version = new ColumnAttribute();
		version.setName("version");
		version.setColumnType("string");
		version.setColumnName("VERSION");
		version.setLength(100);
		list.add(version);

		ColumnAttribute mtime = new ColumnAttribute();
		mtime.setName("mtime");
		mtime.setColumnType("string");
		mtime.setColumnName("MTIME");
		mtime.setLength(100);
		list.add(mtime);

		form.setFormAttributeList(list);
		return form;
	}

	public Map<Integer, FormTable> createTable2AppointShard() {
		// TODO Auto-generated method stub
		return null;
	}

	public FormTable createTable2AllShard() {
		// TODO Auto-generated method stub
		return null;
	}
}
