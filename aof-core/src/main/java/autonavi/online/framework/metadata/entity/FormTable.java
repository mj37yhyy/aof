package autonavi.online.framework.metadata.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Michael
 * 
 */
public class FormTable {

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 表名
	 */
	private String tableName;

	private ColumnAttribute pk = null;
	/**
	 * 字段属性列表
	 */
	private List<ColumnAttribute> columnAttrList = null;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the columnAttrList
	 */
	public List<ColumnAttribute> getColumnAttrList() {
		return columnAttrList;
	}

	/**
	 * @param pName
	 *            the name to set
	 */
	public void setName(String pName) {
		name = pName;
	}

	/**
	 * @param pTableName
	 *            the tableName to set
	 */
	public void setTableName(String pTableName) {
		tableName = pTableName;
	}

	/**
	 * @param pColumnAttrList
	 *            the columnAttrList to set
	 */
	public void setFormAttributeList(List<ColumnAttribute> pColumnAttrList) {
		columnAttrList = pColumnAttrList;
	}

	public ColumnAttribute getPk() {
		return pk;
	}

	public void setPk(ColumnAttribute pk) {
		this.pk = pk;
	}

}
