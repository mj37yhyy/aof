<!DOCTYPE html>
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
<link href="<%=path%>/resources/css/aof.css" rel="stylesheet">

<!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
<!--[if lt IE 9]><script src="../../assets/<%=path%>/resources/js/ie8-responsive-file-warning.js"></script><![endif]-->
<script src="<%=path%>/resources/js/ie-emulation-modes-warning.js"></script>

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
<script src="<%=path%>/resources/js/jquery-1.11.1.min.js"></script>
<script src="<%=path%>/resources/js/aof-active.js"></script>
<script src="<%=path%>/resources/js/aof-date.js"></script>
<script type="text/javascript">
var auto=null;
function initJsonFileTable(msg){
	$.each(msg.result,function(i,n){
		var sysTime=parseInt(n.systemTime);
		var updTime=parseInt(n.updateTime);
		if((sysTime-updTime)/1000>maxSplitTime){
			var html="<tr class='content' style='color:red'><td>"+n.nodeName+"</td><td>"+getSmpFormatDate(new Date(updTime), true)+"</td><td><span class='glyphicon glyphicon-remove'></span></td><td style='color:black'><span class=\"glyphicon glyphicon-trash\"><input type='hidden' value='"+n.nodeName+"'></span></td></tr>";
			$("#base").find("tbody").append(html);
		}else{
			var html="<tr class='content' style='color:green'><td>"+n.nodeName+"</td><td>"+getSmpFormatDate(new Date(updTime), true)+"</td><td><span class='glyphicon glyphicon-ok'></span></td><td style='color:black'>&nbsp;</td></tr>";
			$("#base").find("tbody").append(html);
		}
	});
}
function refresh(){
	if(auto!=null){
		clearInterval(auto);
		auto=null;
	}
	$("#base").find("tr.content").remove();
	$.ajax({
		   type: "POST",
		   url: "../manager/init_check_appNode",
		   dataType:"json",
		   data:"" ,
		   success: function(msg){
		     if(msg.code==0){
		    	 initJsonFileTable(msg);
		    	 monitiorBind();
		    	 autoRefresh();
		     }else{
		    	alert(msg.msg);
		     }
		   }
	});
}
function monitiorBind(){
	$("span.glyphicon-refresh").unbind("click");
	$("span.glyphicon-trash").unbind("click");
	$("span.glyphicon-refresh").click(function(){
		refresh();
	});
	$("span.glyphicon-trash").click(function(){
		 if(confirm("确定要删除这个节点吗?")){
			 delNode($(this).find("input:hidden").val());
		 }
	});
	
}
function autoRefresh(){
	auto=window.setInterval("refresh()", 6000);
}
function delNode(name){
	if(auto!=null){
		clearInterval(auto);
		auto=null;
	}
	$("#base").find("tr.content").remove();
	$.ajax({
		   type: "POST",
		   url: "../manager/del_app_node",
		   dataType:"json",
		   data:{nodeName:name} ,
		   success: function(msg){
		     if(msg.code==0){
		    	 refresh();
		     }else{
		    	alert(msg.msg);
		     }
		   }
	});
}
$(document).ready(function(){
	refresh();
});
</script>
</head>

<body>
	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-12 col-md-12 main">
				<table class="table" id="base">
					<caption>生产模式--节点监控  <span class="glyphicon glyphicon-refresh"></span> </caption>
					<thead>
						<tr>
							<th width="43%">节点名称(主机名_IP_节点MYID)</th>
							<th width="24%">上次心跳时间(ZK时间)</th>
							<th width="13%">状态诊断</th>
							<th width="20%">操作</th>
							
						</tr>
					</thead>
					<tbody>
					</tbody>
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
