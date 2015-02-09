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
<script src="<%=path%>/resources/js/aof-import.js"></script>
<script type="text/javascript">
function initJsonFileTable(msg){
	for(var i=0;i<msg.result.base.length;i++){
		var html="<tr><td>"+msg.result.base[i].baseName+"</td><td>"+msg.result.base[i].lastModify+"</td><td><a href='javascript:editRunBase(\""+msg.result.base[i].baseName+"\")'>查看</a>&nbsp;&nbsp;<a href='javascript:initCheckAppNodes(\"activeDssSb\",\"activeDssPre\",\"activeDssCom\",\"base\",\""+msg.result.base[i].baseName+"\")'>激活</a>&nbsp;&nbsp;<a href='javascript:deletes(\""+msg.result.base[i].baseName+"\")'>删除</a></td></tr>";
		$("#base").find("tbody").append(html);
	}
	for(var i=0;i<msg.result.biz.length;i++){
		var html="<tr><td>"+msg.result.biz[i].bizName+"</td><td>"+msg.result.biz[i].lastModify+"</td><td><a href='javascript:editRunBiz(\""+msg.result.biz[i].bizName+"\")'>查看</a>&nbsp;&nbsp;<a href='javascript:initCheckAppNodes(\"activeDssSb\",\"activeDssPre\",\"activeDssCom\",\"biz\",\""+msg.result.biz[i].bizName+"\")'>激活</a>&nbsp;&nbsp;<a href='javascript:deletes(\""+msg.result.biz[i].bizName+"\")'>删除</a></td></tr>";
		$("#biz").find("tbody").append(html);
	}
}
function deletes(name){
	if(confirm("确定要删除吗?")){
		$.ajax({
			   type: "POST",
			   url: "../manager/del_temp_config",
			   dataType:"json",
			   data:{nodeName:name} ,
			   success: function(msg){
			     if(msg.code==0){
			    	 init();
			     }else{
			    	
			     }
			   },
			   error:function(){
				   location.href="session_timeout.jsp";
			   }
		});
	}
}
function editRunBase(name){
	window.parent.$("#main").attr("src","base_edit_run.jsp?fileName="+name);
}
function editRunBiz(name){
	window.parent.$("#main").attr("src","biz_edit_run.jsp?fileName="+name);
}
function editRunDss(){
	window.parent.$("#main").attr("src","base_edit_run_dss.jsp");
}
function editRunShard(){
	window.parent.$("#main").attr("src","base_edit_run_shard.jsp");
}
function editRunBizCold(){
	window.parent.$("#main").attr("src","biz_edit_run_cold.jsp");
}
$(document).ready(function(){
	$("span.glyphicon-chevron-up").click(function(){
		$(this).hide();
		$(this).siblings("span.glyphicon-chevron-down").show();
		$(this).parents("table").find("thead").hide();
		$(this).parents("table").find("tbody").hide();
	});
	$("span.glyphicon-chevron-down").click(function(){
		$(this).hide();
		$(this).siblings("span.glyphicon-chevron-up").show();
		$(this).parents("table").find("thead").show();
		$(this).parents("table").find("tbody").show();
	});
	$("span.glyphicon-plus").click(function(){
		if($(this).attr("id")=="addCore"){
			window.parent.$("#main").attr("src","base_edit_run.jsp");
		}else if($(this).attr("id")=="addBiz"){
			window.parent.$("#main").attr("src","biz_edit_run.jsp");
		}
	});
	init();
});
function init(){
	$("#base").find("tbody").children().remove();
	$("#biz").find("tbody").children().remove();
	$.ajax({
		   type: "POST",
		   url: "../manager/index_run",
		   dataType:"json",
		   data:"" ,
		   success: function(msg){
		     if(msg.code==0){
		    	 initJsonFileTable(msg);
		     }else{
		    	
		     }
		   },
		   error:function(){
			   location.href="session_timeout.jsp";
		   }
	});
}
</script>
</head>

<body>
<jsp:include page="model/active_dss_model_standby.jsp"></jsp:include>
<jsp:include page="model/import_dss_config.jsp"></jsp:include>
<jsp:include page="model/active_dss_model_pre.jsp"></jsp:include>
<jsp:include page="model/active_dss_model_complete.jsp"></jsp:include>
	<div class="container-fluid">
		<div class="row">
			<div class="col-sm-12 col-md-12 main">
				<table class="table" id="base">
					<caption>生产模式--未发布的热部署数据源  
					<span class="glyphicon glyphicon-chevron-down" style="display:none" title="展开"></span>
					<span class="glyphicon glyphicon-chevron-up"   title="收起"></span>
					<span class="glyphicon glyphicon-plus" title="添加" id="addCore"></span>
					</caption>
					<thead>
						<tr>
							<th width="33%">唯一名称</th>
							<th width="34%">修改时间</th>
							<th width="33%">操作</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
				
				<table class="table" id="biz">
					<caption>生产模式--未发布的热部署自定义配置
					<span class="glyphicon glyphicon-chevron-down" style="display:none" title="展开"></span>
					<span class="glyphicon glyphicon-chevron-up" title="收起"></span>
					<span class="glyphicon glyphicon-plus" title="添加" id="addBiz"></span>
					</caption>
					<thead>
						<tr>
							<th width="33%">唯一名称</th>
							<th width="34%">修改时间</th>
							<th width="33%">操作</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
				<table class="table" id="init">
					<caption>生产模式--初始化或者不使用热部署方式
					</caption>
					<thead>
						<tr>
							<th width="33%">类别</th>
							<th width="34%">说明</th>
							<th width="33%">操作</th>
						</tr>
						<tr>
							<td width="33%">数据源配置(配置此项目后分库分表需重新设置)</td>
							<td width="34%">本模式不支持热部署，配置完成后需要重启各个节点，才能生效</td>
							<td width="33%"><a href="javascript:editRunDss()">配置</a></td>
						</tr>
						<tr>
							<td width="33%">分库分表配置</td>
							<td width="34%">本模式不支持热部署，配置完成后需要重启各个节点，才能生效</td>
							<td width="33%"><a href="javascript:editRunShard()">配置</a></td>
						</tr>
						<tr>
							<td width="33%">自定义属性配置</td>
							<td width="34%">本模式不支持热部署，配置完成后需要重启各个节点，才能生效</td>
							<td width="33%"><a href="javascript:editRunBizCold()">配置</a></td>
						</tr>
						
					</thead>
					<tbody>
					</tbody>
				</table>
				<table class="table" id="init">
					<caption>生产模式--批量初始化导入/导出配置
					</caption>
					<thead>
						<tr>
							<th width="33%">类别</th>
							<th width="34%">说明</th>
							<th width="33%">操作</th>
						</tr>
						<tr>
							<td width="33%">数据源配置批量导入</td>
							<td width="34%">注意!导入将直接替换已经存在的配置</td>
							<td width="33%"><a href="javascript:importConfig('base')">导入</a>&nbsp;<a href="javascript:exportConfig('base')" target="_blank">导出</a></td>
						</tr>
						<tr>
							<td width="33%">自定义属性配置批量导入</td>
							<td width="34%">1.未分类的自定义配置将会直接覆盖已经存在的配置<br>2.提供了唯一标示名称的自定义配置不会覆盖
							<br>3.替换已存在唯一标示的配置，请先使用<a href="javascript:editRunBizCold()">冷部署</a>删除</td>
							<td width="33%"><a href="javascript:importConfig('biz')">导入</a>&nbsp;<a href="javascript:exportConfig('biz')">导出</a></td>
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
