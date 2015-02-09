<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon" href="favicon.ico">

<title>在线框架配置工具</title>

<!-- Bootstrap core CSS -->
<link href="<%=path%>/resources/css/bootstrap.min.css" rel="stylesheet">

<!-- Custom styles for this template -->
<link href="<%=path%>/resources/css/dashboard.css" rel="stylesheet">
<link href="<%=path%>/resources/css/aof.css" rel="stylesheet">
<link href="<%=path%>/resources/css/signin.css" rel="stylesheet">

<!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
<!--[if lt IE 9]><script src="../../assets/<%=path%>/resources/js/ie8-responsive-file-warning.js"></script><![endif]-->
<script src="<%=path%>/resources/js/ie-emulation-modes-warning.js"></script>

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
<script src="<%=path%>/resources/js/jquery-1.11.1.min.js"></script>

</head>
<style>
body{
 background-color: #101010;
 color:white;
}
</style>
<script type="text/javascript">
	$(document)
			.ready(
					function() {
						$("#loginDiv")
								.css(
										"top",
										window.screen.height / 4);
						$("#login")
								.click(
										function() {
											var isDev = false;
											if ($("#isDev").prop("checked")) {
												isDev = true;
											}
											$
													.ajax({
														type : "POST",
														url : "appLogin",
														dataType : "json",
														data : {
															appName : $(
																	"#appName")
																	.val(),
															password : $(
																	"#password")
																	.val(),
															isDev : isDev
														},
														success : function(msg) {
															if (msg.code == 0) {
																if (msg.result
																		&& msg.result.isDev) {
																	location.href = "manager/frame.jsp?type=dev";
																} else {
																	location.href = "manager/frame.jsp?type=run";
																}
															} else {
																$("#errorInfo")
																		.text(
																				msg.msg);
															}
														}
													});
										});
					});
</script>

<body>

	<div class="container col-sm-4 col-md-4 col-md-offset-4 col-sm-offset-4" id="loginDiv">

			<h2 class="form-signin-heading" style="color:white" >在线框架配置工具</h2>
			<div class="input-group">
				<span class="input-group-addon">应用名称</span> <input type="text"
					class="form-control" placeholder="" name="appName" id="appName">
			</div>
			<div style="margin:auto;text-align: center;height:5px">&nbsp;</div>
			<div class="input-group">
				<span class="input-group-addon">应用密码</span> <input type="password"
					class="form-control" placeholder="" name="password" id="password">
			</div>
			<div style="margin:auto;text-align: center;height:5px">&nbsp;</div>
			<div style="margin:auto;text-align: center;">
			<button type="button" class="btn btn-default" id="login">
				<span class="glyphicon glyphicon-user"></span> 登&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;录
			</button>
			&nbsp;&nbsp;&nbsp;&nbsp; <input type="checkBox" id="isDev">开发模式
			</div>
			
	</div>

	</div>
	<!-- /container -->
	<!--  
  <div id="loginDiv">
  在线框架配置工具
  <div style="width:500px; height:40px;border:0px solid;margin:auto;color:red;" id="errorInfo"></div>
  <div style="width:500px; height:40px;border:0px solid;margin:auto">
  <div class="input-group">
  <span class="input-group-addon">应用名称</span>
  <input type="text" class="form-control" placeholder="" name="appName" id="appName">
  </div>
  </div>
  <div style="width:500px; height:10px;border:0px solid;margin:auto"></div>
  <div style="width:500px; height:40px;border:0px solid;margin:auto">
  <div class="input-group">
  <span class="input-group-addon">应用密码</span>
  <input type="password" class="form-control" placeholder="" name="password" id="password">
  </div>
  </div>
  <div style="width:500px; height:30px;border:0px solid;margin:auto">
  <button type="button" class="btn btn-default" id="login">
  <span class="glyphicon glyphicon-user"></span>
    登&nbsp;&nbsp;录
  </button>&nbsp;&nbsp;&nbsp;&nbsp;
  <input type="checkBox" id="isDev">开发模式</div>
  </div>
  -->
	<br>
	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="<%=path%>/resources/js/bootstrap.min.js"></script>
	<script src="<%=path%>/resources/js/docs.min.js"></script>
	<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
	<script src="<%=path%>/resources/js/ie10-viewport-bug-workaround.js"></script>
</body>
</html>
