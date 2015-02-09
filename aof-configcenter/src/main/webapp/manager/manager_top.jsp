<!DOCTYPE html>
<%@page import="autonavi.online.framework.configcenter.util.AofCcProps"%>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="favicon.ico">

    <title>Dashboard Template for Bootstrap</title>

    <!-- Bootstrap core CSS -->
    <link href="<%=path%>/resources/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="<%=path%>/resources/css/dashboard.css" rel="stylesheet">

    <!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
    <!--[if lt IE 9]><script src="../../assets/<%=path%>/resources/js/ie8-responsive-file-warning.js"></script><![endif]-->
    <script src="<%=path%>/resources/js/ie-emulation-modes-warning.js"></script>
    <script src="<%=path%>/resources/js/jquery-1.11.1.min.js"></script>

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <script type="text/javascript">
    function monitorNode(){
    	window.parent.$("#main").attr("src","monitor_node.jsp");
    }
    function exit(){
    	window.parent.location.href="../appLogout";
    }
    function main(){
    	<%if((Boolean)request.getSession().getAttribute(AofCcProps.SESSION_FLAG_RUN)){%>
    	window.parent.$("#main").attr("src","index_run.jsp");
    	<%}else{%>
    	window.parent.$("#main").attr("src","index_dev.jsp");
    	<%}%>
    }
    </script>
  </head>

  <body>

    <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
      <div class="container-fluid">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="javascript:main()">新框架在线配置工具</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav navbar-right">
          <%if((Boolean)request.getSession().getAttribute(AofCcProps.SESSION_FLAG_RUN)){%>
            <li><a href="javascript:monitorNode()">节点监控</a></li>
            <%} %>
            <li><a href="javascript:exit()">注销</a></li>
            <li><a href="http://10.19.2.9/publish/user.html" target="_blank">帮助</a></li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="<%=path%>/resources/js/jquery-1.11.1.min.js"></script>
    <script src="<%=path%>/resources/js/bootstrap.min.js"></script>
    <script src="<%=path%>/resources/js/docs.min.js"></script>
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <script src="<%=path%>/resources/js/ie10-viewport-bug-workaround.js"></script>
  </body>
</html>
