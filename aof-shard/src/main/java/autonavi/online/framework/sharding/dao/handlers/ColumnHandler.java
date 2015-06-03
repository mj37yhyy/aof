package autonavi.online.framework.sharding.dao.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

public class ColumnHandler<T> extends ColumnDefaultHandler<T> implements ResultSetHandler<T> {


	/**
	 * Creates a new instance of ColumnHandler. The first column will be
	 * returned from <code>handle()</code>.
	 */
	public ColumnHandler(Class<T> resultType) {
		this(resultType, 1, null);
	}

	/**
	 * Creates a new instance of ColumnHandler.
	 * 
	 * @param columnIndex
	 *            The index of the column to retrieve from the
	 *            <code>ResultSet</code>.
	 */
	public ColumnHandler(Class<T> resultType, int columnIndex) {
		this(resultType, columnIndex, null);
	}

	/**
	 * Creates a new instance of ColumnHandler.
	 * 
	 * @param columnName
	 *            The name of the column to retrieve from the
	 *            <code>ResultSet</code>.
	 */
	public ColumnHandler(Class<T> resultType, String columnName) {
		this(resultType, 1, columnName);
	}

	/**
	 * Helper constructor
	 * 
	 * @param columnIndex
	 *            The index of the column to retrieve from the
	 *            <code>ResultSet</code>.
	 * @param columnName
	 *            The name of the column to retrieve from the
	 *            <code>ResultSet</code>.
	 */
	private ColumnHandler(Class<T> resultType, int columnIndex,
			String columnName) {
		this.resultType = resultType;
		this.columnIndex = columnIndex;
		this.columnName = columnName;
	}

	/**
	 * Returns one <code>ResultSet</code> column as an object via the
	 * <code>ResultSet.getObject()</code> method that performs type conversions.
	 * 
	 * @param rs
	 *            <code>ResultSet</code> to process.
	 * @return The column or <code>null</code> if there are no rows in the
	 *         <code>ResultSet</code>.
	 * 
	 * @throws SQLException
	 *             if a database access error occurs
	 * @throws ClassCastException
	 *             if the class datatype does not match the column type
	 * 
	 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	// We assume that the user has picked the correct type to match the column
	// so getObject will return the appropriate type and the cast will succeed.
	@Override
	public T handle(ResultSet rs) throws SQLException {
		return this.defaultHandle(rs);
	}

}
