<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>分区信息模板</title>
    
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
     <div id="shard_info_m" style="display:none" class="shardInfo info">
<h4 class="sub-header shardInfoTitle" style="border-bottom:0px;"><span class="glyphicon glyphicon-trash trashShard"></span>&nbsp;&nbsp;分区信息 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<span class="glyphicon glyphicon-chevron-up" style="display:none"></span>
<span class="glyphicon glyphicon-chevron-down"></span> 
</h4>
        
        
		<h5 class="sub-header" style="border-bottom:0px">分区详细信息 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;      
        </h5>
		<table class="table table-striped" >
		<tr>
		<td width="25%">负载策略</td>
		<td width="60%">包含数据源&nbsp;<span class="glyphicon glyphicon-pencil"></span></td>
		<td width="15%">设置为索引分区</td>
		</tr>
		<tr>
		<td class="strategy">
		
		<select style="width:98%;font-size:12pt;" id="strategyName">
						    <option value="none">请选择负载策略</option>
						    <option value="rr">轮询策略</option>
						    <option value="lc">最少连接策略</option>
						    <option value="wlc">加权最少连接策略</option>
						    <option value="wrr">加权轮询策略</option>
						    
		</select>
        </td>
		<td class="dss"></td>
		<td class="indexShard"><span><input type="checkBox" name="shardIndex">&nbsp;&nbsp;索引分区<input type="hidden" name="shardKey"></span></td>
		</tr>
		</table>
</div>

  </body>
</html>
