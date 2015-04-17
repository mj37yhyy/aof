<!DOCTYPE html>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
%>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon" href="favicon.ico">

<title>主界面</title>

<!-- Bootstrap core CSS -->
<link href="<%=path%>/resources/css/bootstrap.min.css" rel="stylesheet">

<!-- Custom styles for this template -->
<link href="<%=path%>/resources/css/dashboard.css" rel="stylesheet">
<link href="<%=path%>/resources/css/aof.css" rel="stylesheet">

<!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
<script src="<%=path%>/resources/js/ie-emulation-modes-warning.js"></script>

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
<script src="<%=path%>/resources/js/jquery-1.11.1.min.js"></script>
<script src="<%=path%>/resources/js/aof-active.js"></script>
<script src="<%=path%>/resources/js/aof-date.js"></script>
<script src="<%=path%>/resources/js/md5.js"></script>

<script type="text/javascript">


	function initJsonFileTable(msg) {
		var r = msg.result;
		var html = "";
		for (var i = 0; i < r.length; i++) {
			monitornode = r[i];
			html += " <tr> " 
					+ "	<td width='20%' style='line-height:35px'>" + monitornode.dbname + "</td> "
					+ "	<td width='75%'><input type='text' id='" + md5(monitornode.dbname) + "' class='form-control' "
					+	"	 value='" + monitornode.max_conn + "'></td> "
					+	"<td width='5%' style='text-align:right'><span "
					+	"	class='glyphicon glyphicon-ok' onclick='saveMaxConn(\"" + monitornode.dbname + "\")'  > "
					+ "</span></td> "
					+ "</tr>  ";

		}
		
		if(0 == r.length) {
			html += "	<tr> "
				+ " <td width='100%' style='line-height:35px'>没有数据库信息</td> "
				+ " </tr>";
		}

		$("#contentId").append(html);
		
		
		
	}



	
	function saveMaxConn(dbname) {
		dbid = md5(dbname);
		max_conn = $("#" + dbid).val();
		$.ajax({
			type : "POST",
			url : "<%=path%>/monitor/savedatabaseconfiginfo",
			dataType : "json",
			data : {
				dbname : dbname,
				maxConn : max_conn
			},
			success : function(msg) {
				if (msg.code == '0') {
					window.parent.$("#main").attr("src","<%=path%>/monitor/db_edit.jsp");
				} else {
					alert(msg.msg);
				}
			}
		});
	}



	function init() {
		$.ajax({
			type : "POST",
			url : "<%=path%>/monitor/listdatabaseconfiginfo",
			dataType : "json",
			data : "",
			success : function(msg) {
				if (msg.code == '0') {
					initJsonFileTable(msg);
				} else {
					alert(msg.msg);
				}
			}
		});
	}

	$(document).ready(function() {
		init();
	});



</script>




</head>

<body>
	

	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-12 col-md-12 main" >

				<ul class="nav nav-pills">
					<li role="presentation" ><a href="<%=path%>/monitor/dbmonitor_node.jsp">监控节点</a></li>
					<li role="presentation" class="active" ><a href="#">数据库设置</a></li>
					<li role="presentation"><a
						href="<%=path%>/monitor/dbmonitor_result_node.jsp">监控结果</a></li>
					
					<li role="presentation"><a
						href="<%=path%>/monitor/db_ud.jsp">数据库上下线</a></li>
				</ul>



				<table class='table table-striped dssProps' id="contentId">
					<tr>
						<td width='20%' style='line-height:35px'>数据源名称</td>
						<td width='75%'>最大连接数</td>
						<td width='5%' style='text-align:right'>操作</td>
					</tr>
			<!-- 	<tr>
						<td width='20%' style='line-height:35px'>acquireRetryAttempts</td>
						<td width='75%'><input type='text' class='form-control'
							name='acquireRetryAttempts' value='30'></td>
						<td width='5%' style='text-align:right'><span
							class='glyphicon glyphicon-ok'></span></td>
					</tr> -->


				</table>

			</div>
			
		</div>
</div>
		<!-- Bootstrap core JavaScript
    ================================================== -->
		<!-- Placed at the end of the document so the pages load faster -->
		<script src="<%=path%>/resources/js/bootstrap.min.js"></script>
		<script src="<%=path%>/resources/js/docs.min.js"></script>
		<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
		<script src="<%=path%>/resources/js/ie10-viewport-bug-workaround.js"></script>

</body>
</html>
