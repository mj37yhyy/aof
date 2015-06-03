<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>C3P0数据源模板</title>
    
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
     <div id="dss_c3p0_m" style="display:none" class="dssC3p0Info dssInfo info">
<h4 class="sub-header dssTitle" style="border-bottom:0px;"><span class="glyphicon glyphicon-trash trashDss"></span>&nbsp;&nbsp;数据源配置 &nbsp;&nbsp;&nbsp;
<span class="glyphicon glyphicon-chevron-up" style="display:none"></span>
<span class="glyphicon glyphicon-chevron-down"></span> 
</h4>
        <h5 class="sub-header" style="border-bottom:0px">基本信息 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<!--         <span class="shardIndexTitle">索引库</span><input type="checkBox" name="shardIndex">&nbsp;&nbsp;&nbsp;&nbsp; -->
        </h5>
        <input type="hidden" name="beanClass">
        <input type="hidden" name="dsKey">
        <input type="hidden" name="name">
		<table class="table table-striped dssBasic">
		<tr>
		<td width="12%" style="line-height:35px">驱动名称</td>
		<td width="88%"><input type="text" name="driverClass" class="form-control"></td>
		</tr>
		<tr>
		<td style="line-height:35px">数据库连接</td>
		<td><input type="text" name="jdbcUrl" class="form-control"></td>
		</tr>
		<tr>
		<td style="line-height:35px">用户名</td>
		<td><input type="text" name="user" class="form-control"></td>
		</tr>
		<tr>
		<td style="line-height:35px">密码</td>
		<td><input type="password" name="password" class="form-control"></td>
		</tr>
		</table>
		<h5 class="sub-header" style="border-bottom:0px">其他属性 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;      
        <span class="glyphicon glyphicon-plus dss"></span>
        </h5>
		<table class="table table-striped dssProps" >
		<tr>
		<td width="20%" style="line-height:35px">acquireRetryAttempts</td>
		<td width="75%"><input type="text" class="form-control" name="acquireRetryAttempts" value="30"></td>
		<td width="5%"  style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">acquireRetryDelay</td>
		<td width="75%"><input type="text" name="acquireRetryDelay" class="form-control" value="100"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">maxIdleTime</td>
		<td width="75%"><input type="text" name="maxIdleTime" class="form-control" value="60"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">testConnectionOnCheckout</td>
		<td width="75%"><input type="text" name="testConnectionOnCheckout" class="form-control" value="false"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">breakAfterAcquireFailure</td>
		<td width="75%"><input type="text" name="breakAfterAcquireFailure" class="form-control" value="false"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">acquireIncrement</td>
		<td width="75%"><input type="text" name="acquireIncrement" class="form-control" value="100"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">idleConnectionTestPeriod</td>
		<td width="75%"><input type="text" name="idleConnectionTestPeriod" class="form-control" value="60"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">maxPoolSize</td>
		<td width="75%"><input type="text" name="maxPoolSize" class="form-control" value="20000"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">maxStatements</td>
		<td width="75%"><input type="text" name="maxStatements" class="form-control" value="100"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">minPoolSize</td>
		<td width="75%"><input type="text" name="minPoolSize" class="form-control" value="100"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		<tr>
		<td width="20%" style="line-height:35px">initialPoolSize</td>
		<td width="75%"><input type="text" name="initialPoolSize" class="form-control" value="100"></td>
		<td width="5%" style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td>
		</tr>
		
		
		
		</table>
</div>

  </body>
</html>
