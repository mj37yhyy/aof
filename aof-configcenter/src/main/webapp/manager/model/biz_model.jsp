<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>BIZ信息模板</title>
    
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
     <div id="biz_m" style="display:none" class="bizInfo info">
<h4 class="sub-header bizTitle" style="border-bottom:0px;"></span>&nbsp;&nbsp;用户自定义属性(唯一名称:<span id="uniqueBizName"></span>) &nbsp;&nbsp;&nbsp;
<span class="glyphicon glyphicon-trash trashBiz" style="display:none"></span>
<span class="glyphicon glyphicon-chevron-up" style="display:none"></span>
<span class="glyphicon glyphicon-chevron-down" style="display:none"></span>
<input type="hidden" name="name" >
<input type="hidden" id="bizUniqueId">
</h4>
		<h5 class="sub-header" style="border-bottom:0px">添加用户自定义属性 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;      
        <span class="glyphicon glyphicon-plus bizTable"></span>
        </h5>
		<table class="table table-striped bizProps" >
		</table>
</div>

  </body>
</html>
