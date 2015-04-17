package autonavi.online.framework.configcenter.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import autonavi.online.framework.cc.entity.CcBaseEntity;
import autonavi.online.framework.cc.entity.CcBizEntity;
import autonavi.online.framework.configcenter.entity.ResultEntity;
import autonavi.online.framework.configcenter.exception.AofException;
import autonavi.online.framework.configcenter.exception.AofExceptionEnum;
import autonavi.online.framework.configcenter.util.AofCcProps;
import autonavi.online.framework.util.json.JsonBinder;
import autonavi.online.framework.zookeeper.SysProps;

@Controller
public class ConfigCenterForDevController {
	private Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * 初始化编辑模式
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/index_dev")
	public @ResponseBody Object initIndexDev(HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		logger.info("初始化开发模式JSON文件列表 应用名称["+app+"]");
		ResultEntity entity=new ResultEntity();
		Map<String,List<Map<String,String>>> object=new HashMap<String,List<Map<String,String>>>();
		try {
			String jsonPath=res.getSession().getServletContext().getRealPath(AofCcProps.JSON_PATH);
			File base=new File(jsonPath+SysProps.AOF_APP_BASE+"/"+app);
			List<Map<String,String>> listBase=new ArrayList<Map<String,String>>();
			if(base.exists()){
                List<File> orders=new ArrayList<File>();
				for(File f:base.listFiles()){
					if(f.isFile()){
						orders.add(f);
					}
				}
				Collections.sort(orders,new Comparator<File>(){
					@Override
					public int compare(File o1, File o2) {
						return new Long(o2.lastModified()).compareTo(new Long(o1.lastModified()));
					}
					
				});
				for(File f:orders){
					
					if(f.isFile()){
						Map<String,String> map=new HashMap<String,String>();
						map.put("baseName", f.getName());
						map.put("basePath", AofCcProps.JSON_PATH+SysProps.AOF_APP_BASE+"/"+app+"/"+f.getName());
						map.put("lastModify",DateFormatUtils.format(new Date(f.lastModified()), "yyyy-MM-dd HH:mm:ss"));
						listBase.add(map);
					}
					
				}
			}
			object.put("base", listBase);
			File biz=new File(jsonPath+SysProps.AOF_APP_BIZ+"/"+app);
			List<Map<String,String>> listBiz=new ArrayList<Map<String,String>>();
			if(biz.exists()){
				 List<File> orders=new ArrayList<File>();
					for(File f:biz.listFiles()){
						if(f.isFile()){
							orders.add(f);
						}
					}
					Collections.sort(orders,new Comparator<File>(){
						@Override
						public int compare(File o1, File o2) {
							return new Long(o2.lastModified()).compareTo(new Long(o1.lastModified()));
						}
						
					});
				for(File f:orders){
					if(f.isFile()){
						Map<String,String> map=new HashMap<String,String>();
						map.put("bizName", f.getName());
						map.put("bizPath", AofCcProps.JSON_PATH+SysProps.AOF_APP_BIZ+"/"+app+"/"+f.getName());
						map.put("lastModify",DateFormatUtils.format(new Date(f.lastModified()), "yyyy-MM-dd HH:mm:ss"));
						listBiz.add(map);
					}
					
				}
				
			}
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
	 * 初始化JSON信息
	 * @param fileName
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/base_edit_dev")
	public @ResponseBody Object baseEditDev(@RequestParam("fileName") String fileName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		logger.info("读取JSON配置文件 应用名称["+app+"] 文件名称["+fileName+"]");
		ResultEntity entity=new ResultEntity();
		try {
			String jsonPath=res.getSession().getServletContext().getRealPath(AofCcProps.JSON_PATH);
			File base=new File(jsonPath+SysProps.AOF_APP_BASE+"/"+app+"/"+fileName);
			if(!base.exists()||!base.isFile()){
				//文件没有找到
				AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
				throw aof;
			}
			FileInputStream file=null;
			InputStreamReader reader = null;
			BufferedReader buffer = null;
			try {
				file=new FileInputStream(base);
				reader = new InputStreamReader(file,SysProps.CHARSET);
				buffer = new BufferedReader(reader);
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line);
				}
				CcBaseEntity ccBase=null;
				try {
					ccBase = JsonBinder.buildNonDefaultBinder(false)
							.fromJson(sb.toString(), CcBaseEntity.class);
				} catch (Exception e) {
					entity.setCode("1");
					entity.setMsg("这个配置文件的版本过旧,已经不在兼容,请使用工具重新配置");
					return entity;
				}
				if(ccBase.getRealDataSources()==null||ccBase.getRealDataSources().size()==0){
					entity.setCode("1");
					entity.setMsg("这个配置文件的版本过旧,已经不在兼容,请使用工具重新配置");
					return entity;
						
				}
				
				entity.setCode("0");
				entity.setMsg("success");
				entity.setResult(ccBase);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
				throw aof;
			} finally{
				try {
					if (buffer != null)
						buffer.close();
					if (reader != null)
						reader.close();
					if (file != null)
						file.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
					throw aof;
				}
			}
			
			
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	/**
	 * 显示BIZ
	 * @param fileName
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/biz_show_dev/{fileName}")
	public @ResponseBody Object bizShowDev(@PathVariable("fileName") String fileName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		logger.info("读取JSON配置文件 应用名称["+app+"] 文件名称["+fileName+"]");
		ResultEntity entity=new ResultEntity();
		try {
			String jsonPath=res.getSession().getServletContext().getRealPath(AofCcProps.JSON_PATH);
			File base=new File(jsonPath+SysProps.AOF_APP_BIZ+"/"+app+"/"+fileName+".json");
			if(!base.exists()||!base.isFile()){
				//文件没有找到
				AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
				throw aof;
			}
			FileInputStream file=null;
			InputStreamReader reader = null;
			BufferedReader buffer = null;
			try {
				file=new FileInputStream(base);
				reader = new InputStreamReader(file,SysProps.CHARSET);
				buffer = new BufferedReader(reader);
				StringBuffer sb = new StringBuffer();
				String line = null; 
				while ((line = buffer.readLine()) != null) {
					sb.append(line);
				}
				return sb.toString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
				throw aof;
			} finally{
				try {
					if (buffer != null)
						buffer.close();
					if (reader != null)
						reader.close();
					if (file != null)
						file.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
					throw aof;
				}
			}
			
			
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
	@RequestMapping("/manager/biz_edit_dev")
	@SuppressWarnings("unchecked")
	public @ResponseBody Object bizEditDev(@RequestParam("fileName") String fileName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		logger.info("读取JSON配置文件 应用名称["+app+"] 文件名称["+fileName+"]");
		ResultEntity entity=new ResultEntity();
		try {
			String jsonPath=res.getSession().getServletContext().getRealPath(AofCcProps.JSON_PATH);
			File base=new File(jsonPath+SysProps.AOF_APP_BIZ+"/"+app+"/"+fileName);
			if(!base.exists()||!base.isFile()){
				//文件没有找到
				AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
				throw aof;
			}
			FileInputStream file=null;
			InputStreamReader reader = null;
			BufferedReader buffer = null;
			try {
				file=new FileInputStream(base);
				reader = new InputStreamReader(file,SysProps.CHARSET);
				buffer = new BufferedReader(reader);
				StringBuffer sb = new StringBuffer();
				String line = null; 
				while ((line = buffer.readLine()) != null) {
					sb.append(line);
				}
				JsonBinder builder=JsonBinder.buildNonDefaultBinder(false);
				
				Map<String, CcBizEntity> ccBase=null;
				try {
					ccBase = builder
							.fromJson(sb.toString(),HashMap.class ,builder.getCollectionType(HashMap.class, String.class,CcBizEntity.class));
				} catch (Exception e) {
					entity.setCode("1");
					entity.setMsg("JSON文件格式过旧,已经不在兼容");
					return entity;
				}
					
				
				entity.setCode("0");
				entity.setMsg("success");
				entity.setResult(ccBase);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
				throw aof;
			} finally{
				try {
					if (buffer != null)
						buffer.close();
					if (reader != null)
						reader.close();
					if (file != null)
						file.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					AofException aof=new AofException(AofExceptionEnum.JSON_IS_NOT_FOUND);
					throw aof;
				}
			}
			
			
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		return entity;
	}
	/**
	 * 保存文件
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/biz_edit_dev_save/{fileName}")
	public @ResponseBody Object bizEditDevSave(@RequestBody Map<String,Map<String,CcBizEntity>> ccBaseEntity,@PathVariable("fileName") String fileName,HttpServletRequest res){
		ResultEntity entity=new ResultEntity();
		String json="";
		if(ccBaseEntity.containsKey(SysProps.DEDAULT_BIZ_UNIQUE_NAME)){
			Map<String,CcBizEntity> cc=ccBaseEntity.get(SysProps.DEDAULT_BIZ_UNIQUE_NAME);
			json=JsonBinder.buildNonDefaultBinder(false).toJson(cc);	
		}else{
			entity.setCode("1");
			entity.setMsg("没有配置项目填入");
			return entity;
		}
		
		try {
			String jsonPath=res.getSession().getServletContext().getRealPath(AofCcProps.JSON_PATH);
			String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
			//创建目录
			File f=new File(jsonPath+SysProps.AOF_APP_BIZ+"/"+app);
			if(!f.exists()){
				f.mkdirs();
			}
			File file=new File(jsonPath+SysProps.AOF_APP_BIZ+"/"+app+"/"+fileName+".json");
			FileOutputStream writer=null;
			OutputStreamWriter os=null;
			BufferedWriter bw=null;
			try {
				writer=new FileOutputStream(file);
				os=new OutputStreamWriter(writer,SysProps.CHARSET);
				bw= new BufferedWriter(os); 
				bw.write(json);
				entity.setCode("0");
				entity.setMsg("success");
			} catch (Exception e) {
				e.printStackTrace();
				AofException aof=new AofException(AofExceptionEnum.JSON_IS_GE_ERROR);
				throw aof;
			}
			finally{
				try {
					if(bw!=null)bw.close();
					if(os!=null)os.close();
					if(writer!=null)writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					AofException aof=new AofException(AofExceptionEnum.JSON_IS_GE_ERROR);
					throw aof;
				}
			}
		} catch (AofException e) {
			entity.setCode(e.getErrorCode()+"");
			entity.setMsg(e.getMessage());
		}
		
		entity.setResult(ccBaseEntity);
		return entity;
	}
	/**
	 * 保存文件
	 * @param ccBaseEntity
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/base_edit_dev_save/{fileName}")
	public @ResponseBody Object baseEditDevSave(@RequestBody CcBaseEntity ccBaseEntity,@PathVariable("fileName") String fileName,HttpServletRequest res){
		String json=JsonBinder.buildNonDefaultBinder(false).toJson(ccBaseEntity);
		ResultEntity entity=new ResultEntity();
		try {
			String jsonPath=res.getSession().getServletContext().getRealPath(AofCcProps.JSON_PATH);
			String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
			
			//创建目录
			File f=new File(jsonPath+SysProps.AOF_APP_BASE+"/"+app);
			if(!f.exists()){
				f.mkdirs();
			}
			
			File file=new File(jsonPath+SysProps.AOF_APP_BASE+"/"+app+"/"+fileName+".json");
			FileOutputStream writer=null;
			OutputStreamWriter os=null;
			BufferedWriter bw=null;
			try {
				writer=new FileOutputStream(file);
				os=new OutputStreamWriter(writer,SysProps.CHARSET);
				bw= new BufferedWriter(os); 
				bw.write(json);
				entity.setCode("0");
				entity.setMsg("success");
			} catch (Exception e) {
				e.printStackTrace();
				AofException aof=new AofException(AofExceptionEnum.JSON_IS_GE_ERROR);
				throw aof;
			}
			finally{
				try {
					if(bw!=null)bw.close();
					if(os!=null)os.close();
					if(writer!=null)writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					AofException aof=new AofException(AofExceptionEnum.JSON_IS_GE_ERROR);
					throw aof;
				}
			}
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
	@RequestMapping("/manager/edit_dev_check_file")
	public @ResponseBody Object editDevCheckFile(@RequestParam("fileName") String fileName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		logger.info("校验新的配置文件 应用名称["+app+"] 文件名称["+fileName+"]");
		ResultEntity entity=new ResultEntity();
		
		String jsonPath=res.getSession().getServletContext().getRealPath(AofCcProps.JSON_PATH);
		File file=new File(jsonPath+SysProps.AOF_APP_BASE+"/"+app+"/"+fileName+".json");
		if(file.exists()){
			entity.setCode("1");
			entity.setMsg("文件已经存在");
		}else{
			//res.getSession().setAttribute(AofCcProps.FILENAME, fileName+".json");
			entity.setCode("0");
			entity.setMsg("success");
		}
		return entity;
	}
	/**
	 * 校验文件是否存在
	 * @param fileName
	 * @param res
	 * @return
	 */
	@RequestMapping("/manager/edit_dev_check_biz_file")
	public @ResponseBody Object editDevCheckBizFile(@RequestParam("fileName") String fileName,HttpServletRequest res){
		String app=(String)res.getSession().getAttribute(AofCcProps.SESSION_APP);
		logger.info("校验新的配置文件 应用名称["+app+"] 文件名称["+fileName+"]");
		ResultEntity entity=new ResultEntity();
		
		String jsonPath=res.getSession().getServletContext().getRealPath(AofCcProps.JSON_PATH);
		File file=new File(jsonPath+SysProps.AOF_APP_BIZ+"/"+app+"/"+fileName+".json");
		if(file.exists()){
			entity.setCode("1");
			entity.setMsg("文件已经存在");
		}else{
			//res.getSession().setAttribute(AofCcProps.FILENAME_BIZ, fileName+".json");
			entity.setCode("0");
			entity.setMsg("success");
		}
		return entity;
	}

}
