package autonavi.online.framework.metadata;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import autonavi.online.framework.metadata.entity.FormTable;
import autonavi.online.framework.metadata.xml.CreateHibernateCfg;
import autonavi.online.framework.metadata.xml.SessionFactoryEntity;
import freemarker.template.Template;

public class TableGenerator {
	private SessionFactoryEntity sessionFactoryEntity;
	/**
	 * formTable
	 */
	private FormTable formTable;

	// /**
	// * 脚本文件
	// */
	// private String scriptFileName = "d:/table.sql";

	/**
	 * 构造函数
	 * 
	 * @param formTable
	 */
	public TableGenerator(SessionFactoryEntity sessionFactoryEntity,
			FormTable formTable) {
		this.sessionFactoryEntity = sessionFactoryEntity;
		this.formTable = formTable;
	}

	// /**
	// * 构造函数
	// *
	// * @param formTable
	// * @param scriptFileName
	// */
	// public TableGenerator(SessionFactoryEntity sessionFactoryEntity,
	// FormTable formTable, String scriptFileName) {
	// this.sessionFactoryEntity = sessionFactoryEntity;
	// this.formTable = formTable;
	// if (null != scriptFileName && !"".equals(scriptFileName)) {
	// this.scriptFileName = scriptFileName;
	// }
	// }
	
	/**
	 * 根据模板创建表
	 * @param dss
	 */
	public void generatorTable(DataSource... dss) {
		if (formTable.getColumnAttrList().isEmpty()) {
			System.out.println(" column attr list size==0 ");
			return;
		}

		Template tl;
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("entity", formTable);

			tl = getTemplateConfig(
					"/autonavi/online/framework/metadata/template")
					.getTemplate("template.hb.ftl");
			Writer out = new StringWriter();
			tl.process(paramMap, out);
			String hbxml = out.toString();
			System.out.println(hbxml);
			Configuration hbcfg = this.getHibernateCfg(hbxml);

			if (dss == null || dss.length == 0)
				createDbTableByCfg(hbcfg);
			else {
				createDbTableByConn(hbcfg, dss[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取freemarker的cfg
	 * 
	 * @param resource
	 * @return Configuration
	 */
	protected freemarker.template.Configuration getTemplateConfig(
			String resource) {

		freemarker.template.Configuration cfg = new freemarker.template.Configuration();
		cfg.setDefaultEncoding("UTF-8");
		cfg.setClassForTemplateLoading(this.getClass(), resource);
		return cfg;
	}

	/**
	 * 处理hibernate的配置文件
	 * 
	 * @param resource
	 * @throws ParserConfigurationException
	 * @throws HibernateException
	 */
	protected Configuration getHibernateCfg(String hbxml)
			throws HibernateException, ParserConfigurationException {
		org.hibernate.cfg.Configuration hbcfg = new org.hibernate.cfg.Configuration();
		CreateHibernateCfg createHibernateCfg = new CreateHibernateCfg();
		hbcfg.configure(createHibernateCfg
				.createSessionFactory(this.sessionFactoryEntity));
		Properties extraProp = new Properties();
		extraProp.put("hibernate.hbm2ddl.auto", "update");
		hbcfg.addProperties(extraProp);
		hbcfg.addXML(hbxml);
		return hbcfg;
	}

	/**
	 * 根据hibernate cfg配置文件动态建表
	 * 
	 * @param hbcfg
	 */
	public void createDbTableByCfg(Configuration hbcfg) {
		SchemaExport schemaExport;
		try {
			schemaExport = new SchemaExport(hbcfg);
			// 设置脚本文件
			// schemaExport.setOutputFile(scriptFileName);
			schemaExport.create(true, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据配置文件、Connection 来动态建表
	 * 
	 * @param conf
	 * @param ds
	 */
	public void createDbTableByConn(Configuration conf, DataSource ds) {
		SchemaExport schemaExport;
		try {

			schemaExport = new SchemaExport(conf, ds.getConnection());
			// schemaExport.setOutputFile(scriptFileName);
			schemaExport.create(true, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
