package autonavi.online.framework.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import autonavi.online.framework.sharding.dao.ResultSetCallback;
import autonavi.online.framework.test.crud.TestDao;
import autonavi.online.framework.test.entity.Demo;
import autonavi.online.framework.test.service.TestService;

@Service
public class TestServiceImpl implements TestService {
	Logger logger = LogManager.getLogger(getClass());

	@Autowired
	private TestDao testDao = null;

	/**
	 * 用户自定义返回结果
	 * 
	 * @param m
	 * @param others
	 * @param callback
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Long> userCallbackSelect(String name) {
		Demo demo = new Demo();
		demo.setName(name);
		List<Long> ids = (List<Long>) testDao.userCallbackSelect(demo,
				new ResultSetCallback<List<Long>>() {

					@Override
					public List<Long> process(final ResultSet rs)
							throws SQLException {
						final List<Long> ids = new ArrayList<Long>();
						while (rs.next())
							ids.add(rs.getLong(1));
						return ids;
					}

				});
		logger.debug(ids);
		return ids;
	}

	/**
	 * 使用系统定的返回方式(单字段)
	 * 
	 * @param name
	 * @param dskey
	 * @return
	 */
	public Long singleFieldSelect(String name, int dskey) {
		Demo demo = new Demo();
		demo.setName(name);
		Long id = (Long) testDao.singleFieldSelect(demo, dskey);
		logger.debug(id);
		return id;
	}

	/**
	 * 使用系统定的返回方式(beanList)，并使用分页
	 * 
	 * @param name
	 * @param skip
	 * @param size
	 * @return
	 */
	public List<Demo> pagingSelect(String name, int skip, int size) {
		Demo demo = new Demo();
		demo.setName(name);
		List<Demo> list = (List<Demo>) testDao.pagingSelect(demo, skip, size);
		logger.debug(list);
		return list;
	}

	/**
	 * 批量查询(union all)
	 * 
	 * @param names
	 * @return
	 */
	public List<Map> batchSelect(final String... names) {
		List<Demo> list = new ArrayList<Demo>() {
			{
				for (String name : names) {
					Demo demo = new Demo();
					demo.setName(name);
					add(demo);
				}
			}
		};
		List<Map> newList = (List<Map>) testDao.batchSelect(list);
		logger.debug(newList);
		return newList;
	}

	/**
	 * 另一种批量查询（自己组织SQL）
	 * 
	 * @param id
	 * @return
	 */
	public List<Map> batchSelect2(String name, long... id) {
		List<Map> newList = (List<Map>) testDao.batchSelect2(name, id);
		logger.debug(newList);
		return newList;
	}

	/**
	 * 插入
	 * 
	 * @param name
	 */
	public void insert(String name) {
		Demo demo = new Demo();
		demo.setName(name);
		testDao.insert(demo);
	}

	/**
	 * 批量插入
	 * 
	 * @param names
	 */
	public void batchInsert(final String... names) {

		List<Demo> list = new ArrayList<Demo>() {
			{
				for (String name : names) {
					Demo demo = new Demo();
					demo.setName(name);
					add(demo);
				}
			}
		};
		testDao.batchInsert(list);
	}

	/**
	 * 更新
	 * 
	 * @param name
	 */
	public void update(String name) {
		Demo demo = new Demo();
		demo.setName(name);
		testDao.update(demo);
	}

	/**
	 * 批量更新
	 * 
	 * @param names
	 */
	public void batchUpdate(final String... names) {

		List<Demo> list = new ArrayList<Demo>() {
			{
				for (String name : names) {
					Demo demo = new Demo();
					demo.setName(name);
					add(demo);
				}
			}
		};
		testDao.batchUpdate(list);
	}

	/**
	 * 删除
	 * 
	 * @param name
	 */
	public void delete(String name) {
		Demo demo = new Demo();
		demo.setName(name);
		testDao.delete(demo);
	}

	/**
	 * 批量删除
	 * 
	 * @param names
	 */
	public void batchDelete(final String... names) {

		List<Demo> list = new ArrayList<Demo>() {
			{
				for (String name : names) {
					Demo demo = new Demo();
					demo.setName(name);
					add(demo);
				}
			}
		};
		testDao.batchDelete(list);
	}
}
