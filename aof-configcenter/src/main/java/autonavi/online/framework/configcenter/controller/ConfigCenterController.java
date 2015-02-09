package autonavi.online.framework.configcenter.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import autonavi.online.framework.configcenter.entity.ResultEntity;
import autonavi.online.framework.configcenter.entity.TreeNode;
import autonavi.online.framework.configcenter.exception.AofException;
import autonavi.online.framework.configcenter.exception.AofExceptionEnum;
import autonavi.online.framework.configcenter.service.ZookeeperService;
import autonavi.online.framework.configcenter.util.AofCcProps;
import autonavi.online.framework.zookeeper.SysProps;

@Controller
public class ConfigCenterController {
	private Logger logger = LogManager.getLogger(this.getClass());
	@Resource
	ZookeeperService zookeeperService;
	/**
	 * 应用登录
	 * @param appName
	 * @param password
	 * @return
	 */
	@RequestMapping("/appLogin")
	public @ResponseBody Object appLogin(@RequestParam String appName,@RequestParam String password,@RequestParam Boolean isDev,HttpServletRequest res){
		logger.info("应用尝试登录配置服务器进行管理 应用名称["+appName+"]");
		ResultEntity entity=new ResultEntity();
		Map<String,Object> prop=new HashMap<String,Object>();
		try {
			ZooKeeper zk=zookeeperService.loginAppRoot(appName, password);
			entity.setCode("0");
			entity.setMsg("success");
			res.getSession().setAttribute(AofCcProps.SESSION_APP, appName);
			
			if(isDev){
				res.getSession().setAttribute(AofCcProps.SESSION_FLAG_RUN, false);
				prop.put("isDev", true);
				entity.setResult(prop);
				try {
					zk.close();
				} catch (InterruptedException e) {
					logger.error(e.getMessage(),e);
				}
			}else{
				res.getSession().setAttribute(AofCcProps.SESSION_ZK, zk);
				res.getSession().setAttribute(AofCcProps.SESSION_PASS, password);
				res.getSession().setAttribute(AofCcProps.SESSION_FLAG_RUN, true);
			}
			
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	/**
	 * 应用退出
	 * @param appName
	 * @param password
	 * @return
	 */
	@RequestMapping("/appLogout")
	public String appLogout(HttpServletRequest res){
		String appName=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		if(appName!=null){
			logger.info("应用退出 应用名称["+appName+"]");
		}
		res.getSession().invalidate();
		return "login";
	}
	@RequestMapping("/manager/index")
	public @ResponseBody Object index(HttpServletRequest res,@RequestParam String id){
		logger.info("初始化应用配置树");
//		ResultEntity entity=new ResultEntity();
		List<TreeNode> l1=new ArrayList<TreeNode>();
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String root=SysProps.AOF_ROOT+"/"+app;
		
		if(id==null){
			TreeNode treeNode=new TreeNode();
			treeNode.setErrorCode(AofExceptionEnum.MVC_TREE_ROOT_IS_NULL.getCode());
			treeNode.setErrorMessage(AofExceptionEnum.MVC_TREE_ROOT_IS_NULL.getMessage());
			l1.add(treeNode);
			return l1;
		}
		String pid=id;
		id="/"+id;
		if(id.equals(SysProps.AOF_APP_DSS)){
			root=root+SysProps.AOF_APP_BASE+id;
		}else if(id.equals(SysProps.AOF_APP_BIZ)){
			root=root+id;
		}else{
			TreeNode treeNode=new TreeNode();
			treeNode.setErrorCode(AofExceptionEnum.MVC_TREE_ROOT_IS_INVALID.getCode());
			treeNode.setErrorMessage(AofExceptionEnum.MVC_TREE_ROOT_IS_INVALID.getMessage());
			l1.add(treeNode);
			return l1;
		}
		try {
			ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
			
			List<String> l=zookeeperService.getAppNodeTree(root, zk);
			for(String s:l){
				TreeNode treeNode=new TreeNode();
				treeNode.setId((root+"/"+s).replace("/", "_"));
				treeNode.setpId(pid);
				treeNode.setName(s);
				treeNode.setErrorCode("0");
				l1.add(treeNode);
			}
			return l1;
			
		} catch (AofException e) {
			TreeNode treeNode=new TreeNode();
			treeNode.setErrorCode(AofExceptionEnum.MVC_TREE_ROOT_IS_INVALID.getCode());
			treeNode.setErrorMessage(AofExceptionEnum.MVC_TREE_ROOT_IS_INVALID.getMessage());
			l1.add(treeNode);
			return l1;
		}
	}
	@RequestMapping("/addAppUser")
	public @ResponseBody Object addAppUser(@RequestParam String adminName,@RequestParam String adminPass,@RequestParam String appName,@RequestParam String appPass,HttpServletRequest res){
		ResultEntity entity=new ResultEntity();
		try {
			if(zookeeperService.checkAdminUser(adminName, adminPass)){
				zookeeperService.addAppRoot(appName, appPass);
				entity.setCode("0");
				entity.setMsg("用户["+appName+"]添加成功");
			}else{
				entity.setCode("1");
				entity.setMsg("管理员账户信息错误");
			}
			return entity;
		} catch (Exception e) {
			entity.setCode("1");
			entity.setMsg(e.getMessage());
			return entity;
		}
	}
//	@RequestMapping("/manager/props")
//	public @ResponseBody Object getProps(HttpServletRequest res,@RequestParam String name,@RequestParam String type){
//		logger.info("初始化应用配置树");
//		ResultEntity entity=new ResultEntity();
//		List<TreeNode> l1=new ArrayList<TreeNode>();
//		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
//		String root=SysProps.AOF_ROOT+"/"+app;
//		
//		if(name==null){
//			TreeNode treeNode=new TreeNode();
//			treeNode.setErrorCode(AofExceptionEnum.MVC_TREE_ROOT_IS_NULL.getCode());
//			treeNode.setErrorMessage(AofExceptionEnum.MVC_TREE_ROOT_IS_NULL.getMessage());
//			l1.add(treeNode);
//			return l1;
//		}
//		String nodeName=name;
//		name="/"+name;
//		type="/"+type;
//		if(type.equals(SysProps.AOF_APP_DSS)){
//			root=root+SysProps.AOF_APP_BASE+SysProps.AOF_APP_DSS+name;
//		}else{
//			return null;
//		}
//		try {
//			ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
//			
//			Map<String,String> m=zookeeperService.getAppChildNodeMap(root, zk,nodeName);
//			entity.setCode("0");
//			entity.setMsg("success");
//			entity.setResult(m);
//			
//		} catch (AofException e) {
//			entity.setCode(e.getErrorCode()+"");
//			entity.setMsg(e.getMessage());
//		}
//		return entity;
//	}

}
