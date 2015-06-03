package autonavi.online.framework.sharding.index;
/**
 * 枚举支持的分区字段类型
 * @author yaming.xu
 *
 */
public enum ShardingSupportType {
	aof_string("string",java.lang.String.class),
	aof_int("int",java.lang.Integer.class),
	aof_integer("integer",java.lang.Integer.class),
	aof_long("long",java.lang.Long.class),
	aof_double("double",java.lang.Double.class),
	aof_flat("flat",java.lang.Float.class),
	aof_big_decimal("big_decimal",java.math.BigDecimal.class),
	aof_date("date",java.util.Date.class),
	aof_timestamp("timestamp",java.sql.Timestamp.class);
	private String type;  
	public String getType() {
		return type;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	private Class<?> clazz;  
	private ShardingSupportType(String type,Class<?> clazz){
		this.type=type;
		this.clazz=clazz;
	}
	public static ShardingSupportType getSupportByType(String type){
		for(ShardingSupportType types:ShardingSupportType.values()){
			if(types.getType().equals(type)){
				return types;
			}
		}
		return null;
	}

}
