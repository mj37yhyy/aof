

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import autonavi.online.framework.configcenter.service.ZookeeperService;
import autonavi.online.framework.configcenter.util.ZookeeperInit;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:conf/applicationContext.xml")
public class DemoTest {
	@Resource
    private ZookeeperService zookeeperService;
	@Resource
    private ZookeeperInit zookeeperInit;
	@Test
	public void test()throws Exception {
//		zookeeperService.addAppRoot("gds", "123456");
//		zookeeperService.addAppRoot("demo", "123456");
		//zookeeperService.addAppRoot("demo_wzy", "123456");
//		ZooKeeper zk=zookeeperInit.generateAppZoo("demo", "123456");
//		zookeeperService.copyAppNode("/aof/demo/base", "/aof_temp/demo/base_test", zk, "demo", "123456");
        //zookeeperService.initAppProp(new File("e://zookperinit.xml"));
		//zookeeperService.addAppRoot("text", "123456");
//		ZooKeeper zk=zookeeperInit.getZoo();
//		List<ACL> acls = new ArrayList<ACL>();
//		Id id2 = new Id("digest",
//				DigestAuthenticationProvider.generateDigest("demo" + ":"
//						+ "123456"));
//		ACL acl2 = new ACL(ZooDefs.Perms.ALL, id2);
//		acls.add(zookeeperInit.getAcl());
//		acls.add(acl2);
//		
//		zk.create("/aof/gds/base/shard/index/tables/gds_mesh_index/mesh/name", "mesh".getBytes("utf-8"), acls, CreateMode.PERSISTENT);
//		zk.close();
	}

}
