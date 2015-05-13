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
function initJsonFileTable(msg){
	$.each(msg.result,function(i,n){
		var s = (1 == n.type ? '下线' : '在线');
		
		mns = (1 == n.type ? " style = 'color:red' " : " style = 'color:green' ");
		
		var html="<tr class='content' " + mns + " ><td> <input name='a' type='checkbox' value='" + n.dbname + "'> </td> <td>" + n.dbname + "</td><td>" + s + "</td></tr>";
		$("#base").find("tbody").append(html);
	});
}
function refresh(){
	$("#base").find("tr.content").remove();
	$.ajax({
		   type: "POST",
		   url: "<%=path%>/monitor/listdatabasestate",
		   dataType:"json",
		   data:"" ,
		   success: function(msg){
		     if(msg.code==0){
		    	 initJsonFileTable(msg);
		     }else{
		    	alert(msg.msg);
		     }
		   }
	});
}


function db_ud(ud){
	dbnames = '';

    $('input:checkbox[name=a]:checked').each(function(i){
       if(0 == i){
        dbnames = $(this).val();
       }else{
        dbnames += (","+$(this).val());
       }
      });

	if('' == dbnames) {
		alert('请选择要修改的数据库');
		return;
	}

	$.ajax({
		   type: "POST",
		   url: "<%=path%>/monitor/updateDatabaseState",
		   dataType:"json",
		   data:{dbnames:dbnames, ud:ud} ,
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

				<ul class="nav nav-pills">
					<li role="presentation"><a href="<%=path%>/monitor/dbmonitor_node.jsp">监控节点</a></li>
					<li role="presentation" ><a href="<%=path%>/monitor/db_edit.jsp">数据库设置</a></li>
					<li role="presentation" ><a
						href="<%=path%>/monitor/dbmonitor_result_node.jsp">监控结果</a></li>
					<li role="presentation" class="active"><a
						href="#">数据库上下线</a></li>
				</ul>


				<table class="table" id="base">
					<thead>
						<tr>
							<th width="10%">数据库节点名称</th>
							<th width="45%">数据库节点名称</th>
							<th width="45%">是否使用</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
			
				</table>
				
			<div style="text-align: center;">
					<button type="button" class="btn btn-default"
						aria-label="Left Align" onclick="db_ud(0)" >
						上线
					</button>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<button type="button" class="btn btn-default"
						aria-label="Left Align" onclick="db_ud(1)">
						下线
					</button>

				</div>	
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
