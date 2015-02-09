package autonavi.online.framework.configcenter.controller;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import autonavi.online.framework.cc.CcBaseEntity;
import autonavi.online.framework.cc.CcBizEntity;
import autonavi.online.framework.cc.CcDataSource;
import autonavi.online.framework.cc.Miscellaneous;
import autonavi.online.framework.configcenter.commons.AppNode;
import autonavi.online.framework.configcenter.entity.ResultEntity;
import autonavi.online.framework.configcenter.exception.AofException;
import autonavi.online.framework.configcenter.service.ZookeeperService;
import autonavi.online.framework.configcenter.util.AofCcProps;
import autonavi.online.framework.util.json.JsonBinder;
import autonavi.online.framework.zookeeper.SysProps;

@Controller
public class ConfigCenterForRunController {
	 static final int BUFFER = 8192; 
	static{
		Miscellaneous.setMyid(32);
	}
	
	private Logger logger = LogManager.getLogger(getClass());
	
	@Resource private ZookeeperService zooKeeperService;
	/**
	 * 初始化运行模式 读出所有的配置中的临时配置
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/index_run")
	public @ResponseBody Object initIndexRun(HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("初始化运行模式编辑中的临时配置  应用名称["+app+"]");
		ResultEntity entity=new ResultEntity();
		Map<String,List<Map<String,String>>> object=new HashMap<String,List<Map<String,String>>>();
		try {
			//获取临时路径下所有子节点
			List<String> list=zooKeeperService.getAppNodeTree(SysProps.AOF_TEMP_ROOT+"/"+app, zk);
			List<Map<String,String>> listBase=new ArrayList<Map<String,String>>();
			List<Map<String,String>> listBiz=new ArrayList<Map<String,String>>();
			for(String node:list){
				//base
				if(("/"+node).startsWith(SysProps.AOF_APP_BASE)){
					Map<String,String> map=new HashMap<String,String>();
					map.put("baseName", node);
					map.put("basePath",node);
					map.put("lastModify",DateFormatUtils.format(new Date(zooKeeperService.getAppNodeStat(SysProps.AOF_TEMP_ROOT+"/"+app+"/"+node, zk).getCtime()), "yyyy-MM-dd HH:mm:ss"));
					listBase.add(map);
				}else if(("/"+node).startsWith(SysProps.AOF_APP_BIZ)){
					Map<String,String> map=new HashMap<String,String>();
					map.put("bizName", node);
					map.put("bizPath", node);
					map.put("lastModify",DateFormatUtils.format(new Date(zooKeeperService.getAppNodeStat(SysProps.AOF_TEMP_ROOT+"/"+app+"/"+node, zk).getCtime()), "yyyy-MM-dd HH:mm:ss"));
					listBiz.add(map);
				}
			}
			object.put("base", listBase);
			object.put("biz", listBiz);
			
			
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(object);
			
			
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	/**
	 * 初始化base信息编辑
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/manager/base_edit_run")
	public @ResponseBody Object baseEditRun(@RequestParam String fileName,HttpServletRequest request, HttpServletRequest response){
		logger.info("获取base数据源列表");
		String app=(String)request.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)request.getSession().getAttribute(AofCcProps.SESSION_ZK);
		ResultEntity entity = new ResultEntity();
		try {
			CcBaseEntity ccBase = zooKeeperService.getCcBaseEntity(zk, app, fileName);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(ccBase);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		return entity;
	}
	/**
	 * 初始化sharding信息
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/manager/base_edit_run_shard")
	public @ResponseBody Object baseEditRunShard(HttpServletRequest request, HttpServletRequest response){
		logger.info("获取Sharding数据源列表");
		String app=(String)request.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)request.getSession().getAttribute(AofCcProps.SESSION_ZK);
		ResultEntity entity = new ResultEntity();
		try {
			CcBaseEntity ccBase = zooKeeperService.getCcShardingEntity(zk, app);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(ccBase);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		return entity;
	}
	/**
	 * 导出数据源配置
	 * @param response
	 */
	@RequestMapping("/manager/export_dss_config")
	public void exportDssConfig(HttpServletRequest res,HttpServletResponse response)throws Exception {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		// 获取数据源配置
		CcBaseEntity ccDss=zooKeeperService.getCcBaseEntity(zk, app, null);
		CcBaseEntity ccShard=zooKeeperService.getCcShardingEntity(zk, app);
		CcBaseEntity ccBaseEntity=new CcBaseEntity();
		ccBaseEntity.setDataSources(ccDss.getDataSources());
		Map<String,CcDataSource> _m=ccDss.getRealDataSources();
		for(String key:_m.keySet()){
			CcDataSource _cc=_m.get(key);
			_cc.setAcitve(null);
		}
		
		ccBaseEntity.setRealDataSources(ccDss.getRealDataSources());
		
		ccBaseEntity.setShardIndex(ccDss.getShardIndex());
		ccBaseEntity.setIndexTableMap(ccShard.getIndexTableMap());
		ccBaseEntity.setSegmentTables(ccShard.getSegmentTables());
		String json=JsonBinder.buildNonDefaultBinder(false).toJson(ccBaseEntity);
        // 设置response的Header
		
        response.addHeader("Content-Disposition", "attachment;filename=" + "export_dss_config.json");
        response.addHeader("Content-Length", "" + json.length());
        OutputStream toClient=null;
        try {
			toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/octet-stream");
			toClient.write(json.getBytes(SysProps.CHARSET));
			toClient.flush();
		} catch (Exception e) {
			throw e;
		}finally{
			if(toClient!=null)
			toClient.close();
		}
	}
	/**
	 * 导出biz配置
	 * @param response
	 */
	@RequestMapping("/manager/export_biz_config")
	public void exportBizConfig(HttpServletRequest res,HttpServletResponse response)throws Exception {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		// 获取数据源配置
		Map<String,Map<String,CcBizEntity>> result=new HashMap<String,Map<String,CcBizEntity>>();
		result.putAll(zooKeeperService.getBizInfoComments(zk, app,null));
		ZipOutputStream out=null;
        try {
        	out=new ZipOutputStream(response.getOutputStream());
        	for(String key:result.keySet()){
    			String name="";
    			if(key.equals(SysProps.DEDAULT_BIZ_UNIQUE_NAME))name="DEFAULT_BIZ.json";
    			else
    			   name="BIZ_"+key+".json";
    			Map<String,CcBizEntity> _m=result.get(key);
    			String json=JsonBinder.buildNonDefaultBinder(false).toJson(_m);
    			this.compressFile(name, json, out);
    		}
			response.addHeader("Content-Disposition", "attachment;filename=" + "export_biz_config.zip");
			response.setContentType("application/octet-stream");
			out.flush();
		} catch (Exception e) {
			throw e;
		}finally{
			if(out!=null)
				out.close();
		}
	}
	/**
	 * 导入BASE信息
	 * @param imports
	 * @param res
	 * @return
	 */
	
	@RequestMapping("/manager/import_dss_config")
	public @ResponseBody Object importDssConfig(@RequestParam String imports, HttpServletRequest res) {
		ResultEntity entity = new ResultEntity();
		CcBaseEntity ccBaseEntity=null;
		try {
			ccBaseEntity = JsonBinder.buildNonDefaultBinder(false).fromJson(imports, CcBaseEntity.class);
			
		} catch (Exception e1) {
			entity.setCode("1");
			entity.setMsg("JSON格式错误");
			return entity;
		}
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("导入BASE到正式配置 应用名称["+app+"]");
		
		try {
			zooKeeperService.importDssConfig(ccBaseEntity, zk, app, pass);
			entity.setCode("0");
			entity.setMsg("success");
			zooKeeperService.notifyDssMonitor(zk, app, pass);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBaseEntity);
		return entity;
	}
	/**
	 * 导入BIZ信息
	 * @param ccBase
	 * @param res
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/manager/import_biz_config")
	public @ResponseBody Object importBizConfig(@RequestParam String imports, HttpServletRequest res) {
		ResultEntity entity = new ResultEntity();
		Map<String,CcBizEntity> ccBase=new HashMap<String,CcBizEntity>();
		try{
			JsonBinder builder=JsonBinder.buildNonDefaultBinder(false);
			ccBase=builder.fromJson(imports, HashMap.class, builder.getCollectionType(HashMap.class, String.class,CcBizEntity.class));
		}catch(Exception e){
			entity.setCode("1");
			entity.setMsg("JSON格式错误");
			return entity;
		}
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("导入BIZ配置 应用名称["+app+"]");
		
		try {
			zooKeeperService.importBizConfig(ccBase, zk, app, pass,null);
			entity.setCode("0");
			entity.setMsg("success");
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBase);
		return entity;
	}
	
	/**
	 * 导入BIZ信息
	 * @param ccBase
	 * @param res
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/manager/import_biz_config/{fileName}")
	public @ResponseBody Object importBizConfigSplit(@RequestParam String imports,@PathVariable("fileName") String fileName, HttpServletRequest res) {
		ResultEntity entity = new ResultEntity();
		Map<String,CcBizEntity> ccBase=new HashMap<String,CcBizEntity>();
		try{
			JsonBinder builder=JsonBinder.buildNonDefaultBinder(false);
			ccBase=builder.fromJson(imports, HashMap.class, builder.getCollectionType(HashMap.class, String.class,CcBizEntity.class));
		}catch(Exception e){
			entity.setCode("1");
			entity.setMsg("JSON格式错误");
			return entity;
		}
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("导入BIZ配置 应用名称["+app+"]");
		
		try {
			zooKeeperService.importBizConfig(ccBase, zk, app, pass,fileName);
			entity.setCode("0");
			entity.setMsg("success");
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBase);
		return entity;
	}
	
	/**
	 * 把页面上的内容保存到zooKeeper
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/base_edit_run_dss_save")
	public @ResponseBody Object baseEditRunDssSave(@RequestBody CcBaseEntity ccBaseEntity, HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("存储新的正式配置 应用名称["+app+"]");
		ResultEntity entity = new ResultEntity();
		try {
			zooKeeperService.saveBaseConfig(ccBaseEntity, zk, app, pass);
			entity.setCode("0");
			entity.setMsg("success");
			zooKeeperService.notifyDssMonitor(zk, app, pass);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBaseEntity);
		return entity;
	}
	/**
	 * 把页面上的内容保存到zooKeeper
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/base_edit_run_shard_save")
	public @ResponseBody Object baseEditRunShardSave(@RequestBody CcBaseEntity ccBaseEntity, HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("存储新的正式分区分表配置 应用名称["+app+"]");
		ResultEntity entity = new ResultEntity();
		try {
			zooKeeperService.saveShardingConfig(ccBaseEntity, zk, app, pass);
			entity.setCode("0");
			entity.setMsg("success");
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBaseEntity);
		return entity;
	}
	/**
	 * 把页面上的内容保存到zooKeeper 临时目录
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/base_edit_run_save/{fileName}")
	public @ResponseBody Object baseEditRunSave(@RequestBody CcBaseEntity ccBaseEntity, @PathVariable("fileName") String fileName,HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		//String fileName=(String)res.getSession().getAttribute(AofCcProps.TEMPNAME);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("存储新的临时配置 应用名称["+app+"] 配置名称["+fileName+"]");
		ResultEntity entity = new ResultEntity();
		try {
			zooKeeperService.saveBaseConfigToTemp(ccBaseEntity, SysProps.AOF_APP_BASE.replaceAll("/", "")+"_"+fileName, zk, app, pass);
			entity.setCode("0");
			entity.setMsg("success");
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBaseEntity);
		return entity;
	}
	/**
	 * 校验文件是否存在
	 * @param fileName
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/edit_run_check_file")
	public @ResponseBody Object editDevCheckFile(@RequestParam String fileName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("校验新的临时配置 应用名称["+app+"] 配置名称["+fileName+"]");
		ResultEntity entity=new ResultEntity();
		if(zooKeeperService.getAppNodeStat(SysProps.AOF_TEMP_ROOT+"/"+app+SysProps.AOF_APP_BASE+"_"+fileName, zk)!=null){
			entity.setCode("1");
			entity.setMsg("配置已经存在");
		}else{
			//res.getSession().setAttribute(AofCcProps.TEMPNAME, fileName);
			entity.setCode("0");
			entity.setMsg("success");
		}
		return entity;
	}
	/**
	 * 初始化检测服务器状态
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/init_check_appNode")
	public @ResponseBody Object initAppNodeStat(HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("检查各个节点心跳状态 应用名称["+app+"]");
		ResultEntity entity=new ResultEntity();
		try {
			List<AppNode> l=zooKeeperService.checkAppNodesStats(zk, app);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(l);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	@RequestMapping("/manager/del_app_node")
	public @ResponseBody Object deleteAppNode(@RequestParam String nodeName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("删除节点 应用名称["+app+"] 节点名称["+nodeName+"]");
		ResultEntity entity=new ResultEntity();
		try {
			zooKeeperService.deleteServerNode(zk, app, nodeName);
			entity.setCode("0");
			entity.setMsg("success");
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	@RequestMapping("/manager/del_temp_config")
	public @ResponseBody Object deleteTempConfig(@RequestParam String nodeName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("删除临时配置 应用名称["+app+"] 配置名称["+nodeName+"]");
		ResultEntity entity=new ResultEntity();
		try {
			zooKeeperService.deleteTempConfig(zk, app, nodeName);
			entity.setCode("0");
			entity.setMsg("success");
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	/**
	 * 预激活数据源
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/pre_active_dss")
	public @ResponseBody Object preActiveDss(@RequestBody List<AppNode> nodeList , HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("预激活数据源配置 应用名称["+app+"] ");
		ResultEntity entity = new ResultEntity();
		try {
			String version=zooKeeperService.preBaseActive(zk, app, nodeList);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(version);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
		
	}
	/**
	 * 预激活数据源
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/pre_active_biz")
	public @ResponseBody Object preActiveBiz(@RequestBody List<AppNode> nodeList ,HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("预激活数据源配置 应用名称["+app+"] ");
		ResultEntity entity = new ResultEntity();
		try {
			String version=zooKeeperService.preBizActive(zk, app, nodeList);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(version);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
		
	}
	/**
	 * 初始化检测服务器状态
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/init_commit_appNode")
	public @ResponseBody Object initCommitAppNode(@RequestBody List<AppNode> nodeList,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("检查各个节点预提交状态 应用名称["+app+"]");
		ResultEntity entity=new ResultEntity();
		try {
			List<AppNode> l=zooKeeperService.checkPreBaseActive(zk, app, nodeList);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(l);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	/**
	 * 激活数据源
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/commit_active_dss/{tempName}")
	public @ResponseBody Object commitActiveDss(@RequestBody List<AppNode> nodeList ,@PathVariable("tempName") String tempName, HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		logger.info("激活数据源配置 应用名称["+app+"] ");
		ResultEntity entity = new ResultEntity();
		try {
			String version=zooKeeperService.commitBaseActive(zk, app, nodeList,tempName,pass);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(version);
			zooKeeperService.notifyDssMonitor(zk, app, pass);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
		
	}
	/**
	 * 激活Biz
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/commit_active_biz/{tempName}")
	public @ResponseBody Object commitActiveBiz(@RequestBody List<AppNode> nodeList, @PathVariable("tempName") String tempName , HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("激活数据源配置 应用名称["+app+"] ");
		ResultEntity entity = new ResultEntity();
		try {
			//zooKeeperService.copyAppNode(SysProps.AOF_TEMP_ROOT+"/"+app+"/"+tempName, SysProps.AOF_ROOT+"/"+app+SysProps.AOF_APP_BIZ, zk, app, pass);
			String version=zooKeeperService.commitBizActive(zk, app, nodeList,tempName,pass);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(version);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
		
	}
	/**
	 * 初始化检测服务器状态
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/check_commit_appNode")
	public @ResponseBody Object checkCommitAppNode(@RequestBody List<AppNode> nodeList,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("检查各个节点的提交状态 应用名称["+app+"]");
		ResultEntity entity=new ResultEntity();
		try {
			List<AppNode> l=zooKeeperService.checkCommitBaseActive(zk, app, nodeList);
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(l);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	/**
	 * 初始化配置信息
	 * @param fileName
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/biz_edit_run")
	public @ResponseBody Object bizEditRun(@RequestParam String fileName,HttpServletRequest request){
		logger.info("获取自定义配置信息-热部署模式");
		String app=(String)request.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)request.getSession().getAttribute(AofCcProps.SESSION_ZK);
		ResultEntity entity = new ResultEntity();
		try {
			//BIZ改动为两部分 一部分来源于biz目录 一部分来源于biz_split目录 biz目录默认给一个名字为空字符串
			Map<String,Map<String,CcBizEntity>> result=new HashMap<String,Map<String,CcBizEntity>>();
			result.putAll(zooKeeperService.getBizInfoComments(zk, app,fileName));
			entity.setCode("0");
			entity.setMsg("success");
			entity.setResult(result);
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		return entity;
	}
	/**
	 * 校验BIZ名称
	 * @param fileName
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/edit_run_check_biz_file")
	public @ResponseBody Object editDevCheckBizFile(@RequestParam String fileName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("校验新的临时配置 应用名称["+app+"] 配置名称["+fileName+"]");
		ResultEntity entity=new ResultEntity();
		if(zooKeeperService.getAppNodeStat(SysProps.AOF_TEMP_ROOT+"/"+app+SysProps.AOF_APP_BIZ+"_"+fileName, zk)!=null){
			entity.setCode("1");
			entity.setMsg("配置已经存在");
		}else{
			//res.getSession().setAttribute(AofCcProps.TEMPNAME, fileName);
			entity.setCode("0");
			entity.setMsg("success");
		}
		return entity;
	}
	/**
	 * 把页面上的内容保存到zooKeeper 临时目录
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/biz_edit_run_save/{fileName}")
	public @ResponseBody Object bizEditRunSave(@RequestBody Map<String,Map<String,CcBizEntity>> ccBase,@PathVariable("fileName") String fileName, HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
//		String fileName=(String)res.getSession().getAttribute(AofCcProps.TEMPNAME);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("存储新的临时配置 应用名称["+app+"] 配置名称["+fileName+"]");
		ResultEntity entity = new ResultEntity();
		try {
			zooKeeperService.saveBizEntityToTemp(ccBase, SysProps.AOF_APP_BIZ.replaceAll("/", "")+"_"+fileName, zk, app, pass);
			entity.setCode("0");
			entity.setMsg("success");
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBase);
		return entity;
	}
	@RequestMapping("/manager/biz_edit_run_cold_save")
	public @ResponseBody Object bizEditRunColdSave(@RequestBody Map<String,Map<String,CcBizEntity>> ccBase, HttpServletRequest res) {
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		String pass=(String)res.getSession().getAttribute(AofCcProps.SESSION_PASS);
		ZooKeeper zk=(ZooKeeper)res.getSession().getAttribute(AofCcProps.SESSION_ZK);
		logger.info("存储新的配置 应用名称["+app+"]");
		ResultEntity entity = new ResultEntity();
		if(ccBase.size()==0){
			entity.setCode("1");
			entity.setMsg("没有配置项目填入");
			return entity;
		}
		try {
			zooKeeperService.saveBizEntity(ccBase, zk, app, pass);
			entity.setCode("0");
			entity.setMsg("success");
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBase);
		return entity;
	}
	 /** 压缩一个流 */  
    private void compressFile(String fileName,String json, ZipOutputStream out)throws Exception {  
    	 ZipEntry entry = new ZipEntry(fileName);  
         out.putNextEntry(entry);  
         out.write(json.getBytes(SysProps.CHARSET));
    }

}
