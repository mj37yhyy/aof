package autonavi.online.framework.zookeeper;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import autonavi.online.framework.support.zookeeper.exception.CommitZooKeeperNodeException;
import autonavi.online.framework.support.zookeeper.exception.CreateZooKeeperNodeException;
import autonavi.online.framework.support.zookeeper.exception.DeleteZooKeeperNodeException;
import autonavi.online.framework.support.zookeeper.exception.FindZooKeeperNodeException;
import autonavi.online.framework.support.zookeeper.exception.GetZooKeeperNodeException;
import autonavi.online.framework.support.zookeeper.exception.SetZooKeeperNodeException;
import autonavi.online.framework.support.zookeeper.holder.ZooKeeperHolder;

public class ZooKeeperUtils {
	private static Logger logger = LogManager.getLogger(ZooKeeperUtils.class);
	/**
	 * 检测某个路径是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkZKNodeIsExist(ZooKeeper zooKeeper, String path, boolean isListen) {
		try {
			if (zooKeeper.exists(path, isListen) != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new FindZooKeeperNodeException(e.getMessage(), e);
		}
	}
	public static boolean checkZKNodeIsExist(String path, boolean isListen) {
		try {
			if (ZooKeeperHolder.zooKeeper.get().exists(path, isListen) != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new FindZooKeeperNodeException(e.getMessage(), e);
		}
	}
	public static ZooKeeper getZK(){
		return ZooKeeperHolder.zooKeeper.get();
	}
 
	/**
	 * 自动创建父节点，如果不存在
	 * @param path
	 * @param bytes
	 */
	public static void autoSafeCreateParentZKNode(String path, byte[] bytes){
		logger.info("创建节点完整路径:"+path);
		//节点解析
		String[] paths=path.split("/");
		StringBuffer buffers=new StringBuffer("");
		for(String _path:paths){
			if(!StringUtils.isBlank(_path)){
				buffers.append("/"+_path);
				//检测是否存在
				if(!checkZKNodeIsExist(buffers.toString(),false)){
					createSafeZKNode(buffers.toString(),null);
				}
			}
			
		}
		setZkNode(path,bytes);
		
	}

	/**
	 * 生成节点
	 * 
	 * @param path
	 * @param bytes
	 */
	public static void createZKNode(String path, byte[] bytes) {
		try {
			ZooKeeperHolder.transaction.set(ZooKeeperHolder.transaction.get()
					.create(path, bytes, Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT));
		} catch (Exception e) {
			throw new CreateZooKeeperNodeException(e.getMessage(), e);
		}
	}
	/**
	 * 生成节点-事务-权限
	 * 
	 * @param path
	 * @param bytes
	 */
	public static void createSafeZKNode(String path, byte[] bytes) {
		try {
			logger.info("创建节点:"+path);
			List<ACL> acls=null;
			if(ZooKeeperHolder.acls.get()==null||ZooKeeperHolder.acls.get().size()==0){
				acls=Ids.OPEN_ACL_UNSAFE;
			}else{
				acls=ZooKeeperHolder.acls.get();
			}
			ZooKeeperHolder.transaction.set(ZooKeeperHolder.transaction.get()
					.create(path, bytes, acls,
							CreateMode.PERSISTENT));
		} catch (Exception e) {
			throw new CreateZooKeeperNodeException(e.getMessage(), e);
		}
	}
	/**
	 * 生成节点-无事务-有权限
	 * @param path
	 * @param bytes
	 * @param acls
	 * @param zk
	 */
	public static void createSafeZKNode(String path, byte[] bytes,ZooKeeper zk) {
		try {
			List<ACL> acls=null;
			if(ZooKeeperHolder.acls.get()==null||ZooKeeperHolder.acls.get().size()==0){
				acls=Ids.OPEN_ACL_UNSAFE;
			}else{
				acls=ZooKeeperHolder.acls.get();
			}
			zk.create(path, bytes, acls,
					CreateMode.PERSISTENT);
		} catch (Exception e) {
			throw new CreateZooKeeperNodeException(e.getMessage(), e);
		}
	}
	/**
	 * 生成节点-无事务
	 * 
	 * @param path
	 * @param bytes
	 */
	public static void createZKNode(String path, byte[] bytes,ZooKeeper zk) {
		try {
			zk.create(path, bytes, Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
		} catch (Exception e) {
			throw new CreateZooKeeperNodeException(e.getMessage(), e);
		}
	}

	/**
	 * 查看节点值
	 * 
	 * @param path
	 */
	public static String getZkNode(ZooKeeper zooKeeper,String path) {
		try {
			return new String(zooKeeper.getData(path, false, null), SysProps.CHARSET);
		} catch (Exception e) {
			throw new GetZooKeeperNodeException(e.getMessage(), e);
		}
	}

	/**
	 * 设置节点值
	 * 
	 * @param path
	 * @param bytes
	 */
	public static void setZkNode(String path, byte[] bytes) {
		try {
			ZooKeeperHolder.transaction.set(ZooKeeperHolder.transaction.get()
					.setData(path, bytes, -1));
		} catch (Exception e) {
			throw new SetZooKeeperNodeException(e.getMessage(), e);
		}
	}
	/**
	 * 设置节点值-无事务
	 * 
	 * @param path
	 * @param bytes
	 */
	public static void setZkNode(String path, byte[] bytes,ZooKeeper zk) {
		try {
			zk.setData(path, bytes, -1);
		} catch (Exception e) {
			throw new SetZooKeeperNodeException(e.getMessage(), e);
		}
	}
	/**
	 * 删除节点-递归-事务
	 * @param path
	 * @param zooKeepter
	 * @param delSelf 是否删除自身
	 */
	public static void deleteNode(String path, ZooKeeper zooKeepter,boolean delSelf) {
		
		if(ZooKeeperHolder.deleteRoot.get()==null||ZooKeeperHolder.deleteRoot.get().equals("")){
			ZooKeeperHolder.deleteRoot.set(path);
		}
		try {
			List<String> l=zooKeepter.getChildren(path, false);
			if(l!=null){
				for(String s:l){
					String newPath=path+"/"+s;
					List<String> l1=zooKeepter.getChildren(newPath, false);
					if(l1!=null&&l1.size()>0){
						 deleteNode(newPath, zooKeepter,false);
					}
					logger.info("删除节点["+newPath+"]");
					ZooKeeperHolder.transaction.set(ZooKeeperHolder.transaction.get().delete(newPath, -1));
				}
			}
			if(delSelf&&path.equals(ZooKeeperHolder.deleteRoot.get())){
				logger.info("删除节点["+path+"]");
				ZooKeeperHolder.transaction.set(ZooKeeperHolder.transaction.get().delete(path, -1));
			}
			if(path.equals(ZooKeeperHolder.deleteRoot.get())){
				ZooKeeperHolder.deleteRoot.set(null);
			}
				
			
		} catch (Exception e) {
			throw new DeleteZooKeeperNodeException(e.getMessage(),e);
		}finally{
			if(path.equals(ZooKeeperHolder.deleteRoot.get())){
				ZooKeeperHolder.deleteRoot.set(null);
			}
		}
	}
	/**
	 * 删除节点，没有事务，需要嵌在其他地事务方代码里面
	 * @param path
	 * @param zk
	 * @param delSelf 是否删除自身
	 */
	public static void deleteNodeWithoutTx(String path, ZooKeeper zooKeepter,boolean delSelf) {
		if(ZooKeeperHolder.deleteRoot.get()==null||ZooKeeperHolder.deleteRoot.get().equals("")){
			ZooKeeperHolder.deleteRoot.set(path);
		}
		try {
			List<String> l = zooKeepter.getChildren(path, false);
			if(l!=null){
				for(String s: l){
					String newPath=path+"/"+s;
					List<String> l1=zooKeepter.getChildren(newPath, false);
					if(l1!=null&&l1.size()>0){
						deleteNodeWithoutTx(newPath, zooKeepter,false);
					}
					logger.info("删除节点["+newPath+"]");
					zooKeepter.delete(newPath, -1);
				}
			}
			if(delSelf&&path.equals(ZooKeeperHolder.deleteRoot.get())){
				logger.info("删除节点["+path+"]");
				zooKeepter.delete(path, -1);
			}
			if(path.equals(ZooKeeperHolder.deleteRoot.get())){
				ZooKeeperHolder.deleteRoot.set(null);
			}
				
			
		} catch (Exception e) {
			throw new DeleteZooKeeperNodeException(e.getMessage(),e);
		}finally{
			if(path.equals(ZooKeeperHolder.deleteRoot.get())){
				ZooKeeperHolder.deleteRoot.set(null);
			}
		}
	}

	/**
	 * 提交
	 */
	public static void commit() {
		try {
			ZooKeeperHolder.transaction.get().commit();
		} catch (Exception e) {
			throw new CommitZooKeeperNodeException(e.getMessage(), e);
		}
	}
	/**
	 * 回滚
	 */
	public static void close(){
		try {
			ZooKeeperHolder.clearHolder();
		} catch (Exception e) {
			throw new CommitZooKeeperNodeException(e.getMessage(), e);
		}
	}

	/**
	 * 开始事务
	 */
	public static void startTransaction(ZooKeeper zooKeeper) {
		ZooKeeperHolder.zooKeeper.set(zooKeeper);
		ZooKeeperHolder.transaction.set(zooKeeper.transaction());
		ZooKeeperHolder.commit.set(false);
	}
	/**
	 * 设置访问控制列表
	 * @param acl
	 */
	public static void setACL(List<ACL> acls){
		ZooKeeperHolder.acls.set(acls);
	}

}
