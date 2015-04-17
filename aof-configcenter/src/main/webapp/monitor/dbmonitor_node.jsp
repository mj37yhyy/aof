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
			ut = (monitornode.cTime - monitornode.uTime) / 1000;
			mns = ut > 3 ? " style = 'color:red' " : " style = 'color:green' ";
			html += "			<div class='panel panel-default'>"
					+ "<div  class='panel-heading' "
					+ mns
					+ ">  监控节点名称： "
					+ monitornode.monitorNodeName
					+ " &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "
					+ "上次更新时间: "
					+ getSmpFormatDate(new Date(monitornode.uTime), true)
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "
					+ "<span class='glyphicon glyphicon-trash'>"
					+ "<input type='hidden' value='" + monitornode.monitorNodeName + "'>"
					+ "</span> </div> ";
					
			html += "	</div>";

		}
		
		if(0 == r.length) {
			html += "			<div class='panel panel-default'>"
				+ "<div class='panel-heading'>没有监控节点信息</div></div>";
		}

		$("#contentId").append(html);
		
		
		
	}

	function init() {
		$.ajax({
			type : "POST",
			url : "<%=path%>/monitor/listmonitornode",
			dataType : "json",
			data : "",
			success : function(msg) {
				if (msg.code == '0') {
					initJsonFileTable(msg);
					monitiorBind();
					autoMonitorRefresh();//监控节点状态监控
				} else {
					alert(msg.msg);
				}
			}
		});
	}
	function monitiorBind() {
		$("span.glyphicon-trash").unbind("click");
		$("span.glyphicon-trash").click(function() {
			if (confirm("确定要删除这个节点吗?")) {
				delNode($(this).find("input:hidden").val());
			}
		});

	}
	function delNode(name) {
		
		if(null != autoMonitor) {
			clearInterval(autoMonitor);
			autoMonitor = null;
		}
		
		
		$("#base").find("tr.content").remove();
		$.ajax({
			type : "POST",
			url : "<%=path%>/monitor/delmonitornode",
			dataType : "json",
			data : {
				monitorName : name
			},
			success : function(msg) {
				if (msg.code == '0') {
					window.parent.$("#main").attr("src","<%=path%>/monitor/dbmonitor_node.jsp");
				} else {
					alert(msg.msg);
				}
			}
		});
	}
	$(document).ready(function() {
		init();
	});
	
	
	var autoMonitor = null;
	function autoMonitorRefresh() {
		autoMonitor = window.setInterval("refresh()", 5000);
	}
	
	function refresh() {
		window.parent.$("#main").attr("src","<%=path%>/monitor/dbmonitor_node.jsp");
	}
	
</script>




</head>

<body>
	

	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-12 col-md-12 main" id="contentId">

				<ul class="nav nav-pills">
					<li role="presentation" class="active"><a href="#">监控节点</a></li>
					<li role="presentation" ><a href="<%=path%>/monitor/db_edit.jsp">数据库设置</a></li>
					<li role="presentation"><a
						href="<%=path%>/monitor/dbmonitor_result_node.jsp">监控结果</a></li>
					<li role="presentation"><a
						href="<%=path%>/monitor/db_ud.jsp">数据库上下线</a></li>
				</ul>


<!-- 	 			<div class='panel panel-default'>
					<div class='panel-heading'>
						B监控 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<span class='glyphicon glyphicon-trash'>
							<input type='hidden' value='MININT-2FBK21E_10.61.34.29_32'>
						</span>
					</div>
				</div>  -->





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
