<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>添加数据源模式对话框</title>
    
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
     <div class="modal fade" id="activeDssCom" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        <h4 class="modal-title" id="myModalLabel">等待各个服务器节点激活</h4>
      </div>
      <div class="modal-body">
       <table class="table table-striped dssBasic">
        <tr class="header">
		<th width="70%">服务器节点</th>
		<th width="30%">数据源部署状态</th>
		</tr>
       </table>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" id="stopChoiceDss" data-dismiss="modal">关闭</button>
        <button type="button" class="btn btn-primary refreshNode" name="refreshNode">刷新</button>
      </div>
    </div>
  </div>
</div>
  </body>
</html>
