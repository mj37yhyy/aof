package autonavi.online.framework.sharding.dao.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;



/**
 * <code>ResultSetHandler</code> implementation that converts one
 * <code>ResultSet</code> column into a <code>List</code> of
 * <code>Object</code>s. This class is thread safe.
 *
 * @param <T> The type of the column.
 * @see org.apache.commons.dbutils.ResultSetHandler
 * @since DbUtils 1.1
 */
public class ColumnListHandler<T> extends AbstractListHandler<T> {

    /**
     * Creates a new instance of ColumnListHandler.  The first column of each
     * row will be returned from <code>handle()</code>.
     */
    public ColumnListHandler(Class<T> resultType) {
        this(resultType,1, null);
    }

    /**
     * Creates a new instance of ColumnListHandler.
     *
     * @param columnIndex The index of the column to retrieve from the
     * <code>ResultSet</code>.
     */
    public ColumnListHandler(Class<T> resultType,int columnIndex) {
        this(resultType,columnIndex, null);
    }

    /**
     * Creates a new instance of ColumnListHandler.
     *
     * @param columnName The name of the column to retrieve from the
     * <code>ResultSet</code>.
     */
    public ColumnListHandler(Class<T> resultType,String columnName) {
        this(resultType,1, columnName);
    }

    /** Private Helper
     * @param columnIndex The index of the column to retrieve from the
     * <code>ResultSet</code>.
     * @param columnName The name of the column to retrieve from the
     * <code>ResultSet</code>.
     */
    private ColumnListHandler(Class<T> resultType,int columnIndex, String columnName) {
        super();
        this.columnIndex = columnIndex;
        this.columnName = columnName;
        this.resultType=resultType;
    }

    /**
     * Returns one <code>ResultSet</code> column value as <code>Object</code>.
     * @param rs <code>ResultSet</code> to process.
     * @return <code>Object</code>, never <code>null</code>.
     *
     * @throws SQLException if a database access error occurs
     * @throws ClassCastException if the class datatype does not match the column type
     *
     * @see org.apache.commons.dbutils.handlers.AbstractListHandler#handle(ResultSet)
     */
    // We assume that the user has picked the correct type to match the column
    // so getObject will return the appropriate type and the cast will succeed.
    @Override
    protected T handleRow(ResultSet rs) throws SQLException {
       return this.defaultHandleWithOutNext(rs);
   }

}
