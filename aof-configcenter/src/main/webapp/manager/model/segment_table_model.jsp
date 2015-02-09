<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>分表模板</title>
    
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
     <div id="segment_table_m" style="display:none" class="segmentTableInfo info">
<h4 class="sub-header segmentTitle" style="border-bottom:0px;"><span class="glyphicon glyphicon-trash trashSegment"></span>&nbsp;&nbsp;分表信息 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<span class="glyphicon glyphicon-chevron-up" style="display:none"></span>
<span class="glyphicon glyphicon-chevron-down"></span> 
</h4>
        <input type="hidden" name="name">
		<h5 class="sub-header" style="border-bottom:0px">此表的分表数量 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;      
        </h5>
		<table class="table table-striped" >
		<tr>
		<td><input type="text" name="count" class="form-control number number1"></td>
		</tr>
		</table>
</div>

  </body>
</html>
