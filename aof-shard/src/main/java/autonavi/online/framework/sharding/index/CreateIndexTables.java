package autonavi.online.framework.sharding.index;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.metadata.DialectUtils;
import autonavi.online.framework.metadata.TableGenerator;
import autonavi.online.framework.metadata.entity.ColumnAttribute;
import autonavi.online.framework.metadata.entity.FormTable;
import autonavi.online.framework.metadata.xml.SessionFactoryEntity;
import autonavi.online.framework.sharding.dao.exception.ColumnNameMatchingNotException;

public class CreateIndexTables {

	public CreateIndexTables(DataSource dataSource,
			Map<String, List<ColumnAttribute>> indexTableMap) throws Exception {
		this.dataSource = dataSource;
		this.indexTableMap = indexTableMap;
		// this.init();
	}

	/**
	 * 创建索引表
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		Connection conn=null;
		try {
			conn = this.dataSource.getConnection();// 得到Spring管理的连接
			DatabaseMetaData meta = conn.getMetaData();// 得到原数据

			/**
			 * 创建用户索引表
			 */
			initUserIndexTable(conn, meta);
			/**
			 * 创建分表配置表，该表用于存放想要在单个数据源中分成多个表的情况。该表主要有两列，一是各索引表的外键ID，二是表名。
			 * 框架会分析SQL确定是否存在要分表的表名，然后再查询该表，如果当前分不存在这样的记录，则生成一条，并替换SQL中的表名
			 */
			initSegmentTableConfigTable(conn, meta);
		} finally {
			if (conn != null && !conn.isClosed()) {
				conn.close();
				conn = null;
			}
		}
	}

	/**
	 * 创建用户索引表
	 * 
	 * @param conn
	 * @param meta
	 * @throws SQLException
	 * @throws Exception
	 * @throws ColumnNameMatchingNotException
	 */
	private void initUserIndexTable(Connection conn, DatabaseMetaData meta)
			throws SQLException, Exception, ColumnNameMatchingNotException {
		Set<String> tableNameSet = indexTableMap.keySet();
		for (String tableName : tableNameSet) {
			ResultSet rsTables = meta.getTables(null, null,
					tableName.toUpperCase(), null);// 得到表，用于判断表是否存在，如果不存则创建之
			List<ColumnAttribute> columns = indexTableMap.get(tableName);// 从Map中得到列信息
			if (!rsTables.next()) {// 如果不存在，则创建之，并随即分配一个数据源给当前索引
				// 表信息
				FormTable form = new FormTable();
				form.setName(tableName + "indexTable");
				form.setTableName(tableName.toUpperCase());
				// 列信息
				List<ColumnAttribute> list = new ArrayList<ColumnAttribute>();
				String indexKey = "";
				for (ColumnAttribute column : columns) {// 将用户传入的列信息插入
					// 校验类型
					ShardingSupportType columnType = ShardingSupportType
							.getSupportByType(column.getColumnType());
					if (columnType == null) {
						if (log.isErrorEnabled())
							log.error("column=[" + column.getColumnName()
									+ "] type=[" + column.getColumnType()
									+ "] is not supprt for shard!");
						System.exit(0);
						throw new RuntimeException("column=["
								+ column.getColumnName() + "] type=["
								+ column.getColumnType()
								+ "] is not supprt for shard!");

					}
					list.add(column);
					indexKey = indexKey + column.getColumnName() + ",";
				}

				// 片儿ID
				ColumnAttribute shard_id = new ColumnAttribute();
				shard_id.setName("shard_id");
				shard_id.setColumnType("long");
				shard_id.setColumnName("SHARD_ID");
				shard_id.setLength(4);
				list.add(shard_id);

				// 迁移乐观锁
				ColumnAttribute migration_lock = new ColumnAttribute();
				migration_lock.setName("migration_lock ");
				migration_lock.setColumnType("long");
				migration_lock.setColumnName("MIGRATION_LOCK");
				migration_lock.setLength(4);
				list.add(migration_lock);

				// 旧片儿id
				ColumnAttribute old_shard_id = new ColumnAttribute();
				old_shard_id.setName("old_shard_id");
				old_shard_id.setColumnType("long");
				old_shard_id.setColumnName("OLD_SHARD_ID");
				old_shard_id.setLength(4);
				list.add(old_shard_id);

				form.setFormAttributeList(list);

				SessionFactoryEntity sessionFactoryEntity = new SessionFactoryEntity();
				sessionFactoryEntity.setDialect(DialectUtils
						.getDialect4Hibernate(meta));// 得到方言
				TableGenerator tg = new TableGenerator(sessionFactoryEntity,
						form);
				tg.generatorTable(this.dataSource);// 创建表
				// 创建唯一索引
				conn.createStatement().execute(
						"create unique index " + tableName + "_unique on "
								+ tableName + " ("
								+ indexKey.substring(0, indexKey.length() - 1)
								+ ")");
			} else {
				if (!this.isMatchingTableColumnName(tableName, meta, columns)) {
					throw new ColumnNameMatchingNotException(
							"传入的索引字段与实际表中的字段不匹配");
				}
			}
		}
	}

	/**
	 * 创建分表配置表，该表用于存放想要在单个数据源中分成多个表的情况。该表主要有两列，一是各索引表的外键ID，二是表名。
	 * 框架会分析SQL确定是否存在要分表的表名，然后再查询该表，如果当前分不存在这样的记录，则生成一条，并替换SQL中的表名
	 */
	private void initSegmentTableConfigTable(Connection conn,
			DatabaseMetaData meta) throws SQLException {
		ResultSet itnTable = meta.getTables(null, null, SEGMENT_TABLE_CONFIG,
				null);// 得到表，用于判断表是否存在，如果不存则创建之
		if (!itnTable.next()) {// 如果AOF_SEGMENT_TABLE_CONFIG表不存在，则进行创建
			// 表信息
			FormTable form = new FormTable();
			form.setName(SEGMENT_TABLE_CONFIG);
			form.setTableName(SEGMENT_TABLE_CONFIG);
			// 列信息
			List<ColumnAttribute> list = new ArrayList<ColumnAttribute>();

			// 所有索引表的外键
			ColumnAttribute indexTablesFK = new ColumnAttribute();
			indexTablesFK.setName("indexTableFK");
			indexTablesFK.setColumnType("long");
			indexTablesFK.setColumnName("INDEX_TABLE_FK");
			indexTablesFK.setLength(64);
			list.add(indexTablesFK);
			// 表名
			ColumnAttribute tableName = new ColumnAttribute();
			tableName.setName("tableName");
			tableName.setColumnType("string");
			tableName.setColumnName("TABLE_NAME");
			tableName.setLength(300);
			list.add(tableName);
			// 索引号
			ColumnAttribute indexVlaue = new ColumnAttribute();
			indexVlaue.setName("indexValue");
			indexVlaue.setColumnType("int");
			indexVlaue.setColumnName("INDEX_VALUE");
			indexVlaue.setLength(11);
			list.add(indexVlaue);

			form.setFormAttributeList(list);

			SessionFactoryEntity sessionFactoryEntity = new SessionFactoryEntity();
			sessionFactoryEntity.setDialect(DialectUtils
					.getDialect4Hibernate(meta));// 得到方言
			TableGenerator tg = new TableGenerator(sessionFactoryEntity, form);
			tg.generatorTable(this.dataSource);// 创建表

			// 创建INDEX_TABLES_FK,TABLE_NAME联合索引，提高查询速度
			// conn.createStatement().execute(
			// "create index INDEX_" + System.currentTimeMillis() + " on "
			// + SEGMENT_TABLE_CONFIG
			// + " (INDEX_TABLE_FK,TABLE_NAME)");
		}
	}

	private boolean isMatchingTableColumnName(String tableName,
			DatabaseMetaData meta, List<ColumnAttribute> columns)
			throws Exception {
		// 从表里获取字段信息
		List<String> tableColumnNames = new ArrayList<String>();
		List<String> userColumnNames = new ArrayList<String>();
		ResultSet rs = meta
				.getColumns(null, null, tableName.toUpperCase(), "%");
		while (rs.next()) {
			String columnName = rs.getString("COLUMN_NAME");
			if (!columnName.equalsIgnoreCase("ID")
					&& !columnName.equalsIgnoreCase("SHARD_ID")
					&& !columnName.equalsIgnoreCase("MIGRATION_LOCK")
					&& !columnName.equalsIgnoreCase("OLD_SHARD_ID"))
				tableColumnNames.add(columnName.toUpperCase());
		}
		Collections.sort(tableColumnNames);// 对表里的名字进行排序

		for (ColumnAttribute _columnAttribute : columns) {
			userColumnNames.add(_columnAttribute.getColumnName().toUpperCase());
		}
		Collections.sort(userColumnNames);// 对用户的名字进行排序
		return tableColumnNames.equals(userColumnNames);
	}

	private Logger log = LogManager.getLogger(this.getClass());
	private DataSource dataSource = null;
	private Map<String, List<ColumnAttribute>> indexTableMap = null;
	protected static String SEGMENT_TABLE_CONFIG = "AOF_SEGMENT_TABLE_CONFIG";
}
