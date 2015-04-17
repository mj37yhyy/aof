package autonavi.online.framework.sharding.dao;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import autonavi.online.framework.metadata.DialectUtils;
import autonavi.online.framework.metadata.xml.CreateHibernateCfg;
import autonavi.online.framework.metadata.xml.SessionFactoryEntity;
import autonavi.online.framework.sharding.dao.constant.RegularExpressions;

public class HbmHelper {
//	private DataSourceRoute dataSourceRoute;
//	
//	public void setDataSourceRoute(DataSourceRoute dataSourceRoute) {
//		this.dataSourceRoute = dataSourceRoute;
//	}
	protected void executeHbm(int dsKey,String author,String hbm,
			Map<String, Object> parameterMap)throws Exception{
		Connection conn=null;
		try {
			conn=dataSourceRoute.getConnection(dsKey, false);
			SessionFactoryEntity sfe = new SessionFactoryEntity();
			sfe.setDialect(DialectUtils.getDialect4Hibernate(conn.getMetaData()));
			org.hibernate.cfg.Configuration hbcfg = new org.hibernate.cfg.Configuration();
			CreateHibernateCfg createHibernateCfg = new CreateHibernateCfg();
			hbcfg.configure(createHibernateCfg.createSessionFactory(sfe));
			Properties extraProp = new Properties();
			extraProp.put("hibernate.hbm2ddl.auto", "update");
			hbcfg.addProperties(extraProp);
			hbcfg.addXML(parse2(hbm,parameterMap));
			SchemaExport schemaExport;
			schemaExport = new SchemaExport(hbcfg,conn);
			schemaExport.execute(true, true, false, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		} finally{
			if(conn!=null&&!conn.isClosed()){
				conn.close();
			}
		}
		
	}
	/**
	 * 解析${}串
	 * 
	 * @param query
	 * @param parameterMap
	 * @param indexMap
	 * @return
	 * @throws Exception
	 */
	final String parse2(String query, Map<String,Object> parameterMap)
			throws Exception {
		// 替换所有${...}的字符串
		Pattern pattern = Pattern
				.compile(RegularExpressions.PARAM_RIGHT_STRING);
		Matcher matcher = pattern.matcher(query);
		while (matcher.find()) {
			String param = matcher.group();
			Object obj=PropertyUtils
					.getProperty(parameterMap, param.replace("${", "")
							.replace("}", ""));
			if(obj==null){
				throw new RuntimeException("要替换的参数["+param+"] 在入参中没有找到");
			}
			query = query.replace(param, (String)obj );
		}


		return query;
	}
	private AbstractDataSourceRoute dataSourceRoute = null;

	public void setDataSourceRoute(AbstractDataSourceRoute dataSourceRoute) {
		this.dataSourceRoute = dataSourceRoute;
	}

}
