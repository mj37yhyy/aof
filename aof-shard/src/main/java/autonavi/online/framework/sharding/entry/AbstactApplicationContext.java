package autonavi.online.framework.sharding.entry;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import autonavi.online.framework.cc.CcBaseEntity;
import autonavi.online.framework.cc.CcDaoEntity;
import autonavi.online.framework.sharding.dao.AbstractDaoSupport;
import autonavi.online.framework.sharding.entry.aspect.DaoAspectProxyCreator;
import autonavi.online.framework.sharding.entry.xml.builder.ClassGeneration;
import autonavi.online.framework.sharding.entry.xml.builder.support.session.SqlSession;
import autonavi.online.framework.sharding.holder.XmlBuilderHolder;
import autonavi.online.framework.util.BeanUtils;
import autonavi.online.framework.util.json.JsonBinder;

/**
 * 应用上下文的抽象类
 * 
 * @author jia.miao
 * 
 */
public abstract class AbstactApplicationContext extends DaoFactory {

	/**
	 * 构造函数
	 * 
	 * @param path
	 * @throws Exception
	 * @throws ClassNotFoundException
	 */
	public AbstactApplicationContext(String path) throws Exception {
		this.init(path);
	}

	/**
	 * 获取json流
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	protected abstract InputStream getJson(String path) throws Exception;

	/**
	 * 初始化
	 * 
	 * @param path
	 * @throws Exception
	 */
	private void init(String path) throws Exception {
		InputStream is = this.getJson(path);// 得到流
		if (is != null) {
			try {
				// 读取json内容
				StringBuffer sb = new StringBuffer();
				List<String> stringList = IOUtils.readLines(is);
				for (String str : stringList) {
					sb.append(str);
				}
				// 映射CcBaseEntity
				CcBaseEntity daoSupportEnitiy=JsonBinder.buildNonDefaultBinder(false)
						.fromJson(sb.toString(),CcBaseEntity.class);
				
				// 生成daoSupport对象
				AbstractDaoSupport daoSupport = new DaoSupportFactory()
						.getDaoSupport(daoSupportEnitiy);
				// 代理dao类
				this.proxyClass(daoSupportEnitiy.getDaos(), daoSupport);
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
	}

	/**
	 * 代理对象
	 * 
	 * @throws Exception
	 */
	private void proxyClass(List<CcDaoEntity> ccDaoEntitys,
			AbstractDaoSupport daoSupport) throws Exception {
		Map<String, Object> propertyMap = null;// 属性Map
		if(ccDaoEntitys!=null){
			for (CcDaoEntity ccDaoEntity : ccDaoEntitys) {
				Class<?> targetClass = this.getClass().getClassLoader()
						.loadClass(ccDaoEntity.getClassName());
				Object proxyObject = null;
				// 生成代理对象
				if (targetClass.isInterface()) {// 如果是接口，采用接口+xml方案
					propertyMap = new HashMap<String, Object>();// 属性Map
					// 通过xml生成接口实现类
					targetClass = new ClassGeneration()
							.createImplClassFromInterface(targetClass);
					sqlSession.setClasses(XmlBuilderHolder.classesHolder.get());// 将生成好的SqlSource插入sqlSession对象
					propertyMap.put("sqlSession", sqlSession);// 注入sqlSession
					propertyMap.put("daoSupport", daoSupport);// 注入daoSupport
					proxyObject = BeanUtils.generateObject(
							targetClass.getName(), propertyMap);
				} else {// 如果不是，采用实体类+注解方案
					DaoAspectProxyCreator daoAspectProxyCreator = new DaoAspectProxyCreator();
					daoAspectProxyCreator.setDaoSupport(daoSupport);// 注入daoSupport对象到代理生成器中
					proxyObject = daoAspectProxyCreator.getProxy(targetClass);// 生成代理对象
				}
				// 生成好的代理对象放入DaoFactory中待用
				if (proxyObject != null)
					DaoFactory.put(ccDaoEntity.getId(), proxyObject);
			}
		}
		
	}

	private final static SqlSession sqlSession = new SqlSession();

}
