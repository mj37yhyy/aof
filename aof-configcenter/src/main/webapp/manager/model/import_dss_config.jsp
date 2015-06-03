<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>批量导入配置信息</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body>
     <div class="modal fade" id="importConfigInfo" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        <h4 class="modal-title" id="myModalLabel">请将开发中的配置JSON文件粘贴到输出框</h4>
      </div>
      <div class="modal-body">
       <textarea rows="15" cols="75" id="importContent"></textarea>
       <div class="input-group bizUniqueName">
           <span class="input-group-addon">
            <input type="checkbox" id="biz_split_val">多个自定义配置
           </span>
           <input type="text" class="form-control" id="importId" placeholder="自定义配置唯一名称" readonly="readonly">
       </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" id="stopImportConfig" data-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary " id="startImportConfig">导入</button>
      </div>
    </div>
  </div>
</div>
  </body>
</html>
