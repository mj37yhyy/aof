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

<title>分区信息添加模式对话框</title>

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
	<div class="modal fade" id="shardModal" tabindex="-1" role="dialog"
		aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">
						<span aria-hidden="true">&times;</span><span class="sr-only">Close</span>
					</button>
					<h4 class="modal-title" id="myModalLabel">请配置分片信息</h4>
				</div>
				<div class="modal-body">
					<table class="table table-striped shardBasic">
						<tr>
						<td colspan="3">选择分片中的数据源</td>
						</tr>
						<tr>
						<td width="47%">
						<select size="8" multiple="multiple" style="width:98%;font-size:12pt;" id="realDssSource">
						</select>
						</td>
						<td  style=" vertical-align:middle;">
						<span class="glyphicon glyphicon-arrow-right" id="dss_select" ></span>
						<br>
						<span class="glyphicon glyphicon-arrow-left" id="dss_cancle"></span>
						</td>
						<td width="47%">
						<select size="8" multiple="multiple" style="width:98%;font-size:12pt;" id="realDss">
						</select>
						</td>
						</tr>
					</table>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" id="stopShard"
						data-dismiss="modal">取消</button>
					<button type="button" class="btn btn-primary" name="addShard">添加</button>
					<button type="button" class="btn btn-primary" name="saveShard" style="display:none">保存</button>
					<input type="hidden" id="shardId">
				</div>
			</div>
		</div>
	</div>
</body>
</html>
