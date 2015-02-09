package autonavi.online.framework.metadata.entity;

import java.io.Serializable;

public class ColumnAttribute implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4694046893990411747L;

	private String name;

    private String columnType;

    private String columnName;

    private Integer length;
    
    private Object columnVlaue;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the columnType
     */
    public String getColumnType() {
        return columnType;
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @return the length
     */
    public Integer getLength() {
        return length;
    }

    /**
     * @param pName the name to set
     */
    public void setName(String pName) {
        name = pName;
    }

    /**
     * @param pColumnType the columnType to set
     */
    public void setColumnType(String pColumnType) {
        columnType = pColumnType;
    }

    /**
     * @param pColumnName the columnName to set
     */
    public void setColumnName(String pColumnName) {
        columnName = pColumnName;
    }

    /**
     * @param pLength the length to set
     */
    public void setLength(Integer pLength) {
        length = pLength;
    }

	public Object getColumnVlaue() {
		return columnVlaue;
	}

	public void setColumnVlaue(Object columnVlaue) {
		this.columnVlaue = columnVlaue;
	}

}
