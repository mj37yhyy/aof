<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>保存配置模式对话框</title>
    
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
     <div class="modal fade" id="saveModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        <h4 class="modal-title" id="myModalLabel">请输入临时配置的唯一名称（系统会自动添加base_或者biz_做为前缀）</h4>
      </div>
      <div class="modal-body">
       <table class="table table-striped dssBasic">
        <tr>
		<td width="20%" style="line-height:35px">临时配置唯一名称:</td>
		<td width="80%"><input type="text" name="name" class="form-control"></td>
		</tr>
       </table>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" id="stopDss" data-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary" name="addFile">添加</button>
      </div>
    </div>
  </div>
</div>
  </body>
</html>
