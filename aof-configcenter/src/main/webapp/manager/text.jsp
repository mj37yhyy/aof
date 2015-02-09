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

    <!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
    <!--[if lt IE 9]><script src="../../assets/<%=path%>/resources/js/ie8-responsive-file-warning.js"></script><![endif]-->
    <script src="<%=path%>/resources/js/ie-emulation-modes-warning.js"></script>

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <!-- Ztreee -->
    <link rel="stylesheet" href="../resources/zTreeStyle.css" type="text/css">
	<link rel="stylesheet" href="../resources/css/demo.css" type="text/css">
	<style type="text/css">
	.ztree li span.button.add {margin-left:2px; margin-right: -1px; background-position:-144px 0; vertical-align:top; *vertical-align:middle
	</style>
	<script src="<%=path%>/resources/js/jquery-1.11.1.min.js"></script>
	<script type="text/javascript" src="../resources/jquery.ztree.all-3.5.min.js"></script>
    <!-- end -->
    <script type="text/javascript">
    var dss="dss";
	var biz="biz";
		var setting = {
			async: {
				enable: true,
				url:"../manager/index",
				autoParam:["id"]
			},
			callback: {
				onClick:test,
				onAsyncSuccess: onAsyncSuccess,
				onClick:treeOnClick
			},
			edit: {
				enable: true,
				editNameSelectAll: true,
				showRenameBtn: false,
				showRemoveBtn: showRemoveBtn
			},
			view: {
				addHoverDom: addHoverDom,
				removeHoverDom: removeHoverDom,
				selectedMulti: false
			},
			data: {
				keep: {
					parent: true
				}
			}
			
		
		};
		function treeOnClick(event, treeId, treeNode,clickFlag){
			if(treeNode.pId==dss){
				 $.ajax({
					   type: "POST",
					   url: "../manager/props",
					   dataType:"json",
					   data: {name:treeNode.name,type:dss},
					   success: function(msg){
					     if(msg.code==0){
					    	 showDssDiv(msg,treeNode.name);
					     }else{
					    	
					     }
					   }
				});
				
			}else if(reeNode.pId==biz){
				
			}
		};
		function showDssDiv(msg,name){
			alert(msg.result);
			$("#dssName").val(name);
			$("#dssDriver").val(msg.result.driverClass);
			$("#dssUrl").val(msg.result.jdbcUrl);
			$("#dssUserName").val(msg.result.user);
			$("#dssEdit").show();
		}
		function test(event, treeId, treeNode){
			
		}
		function userPorps(){
			   var users=new Array();
			   var obj=$(".userProp");
			   for(var i=0;i<obj.length;i++){
				   users[i]=obj[i].value;
			   }
			   return users;
		}
		function test1(){
			$.ajax({
				   type: "POST",
				   url: "../appLogin",
				   dataType:"json",
				   data:{appName:"1",password:"2",userProp:userPorps()} ,
				   success: function(msg){
				     if(msg.code==0){
				    	 alert(1);
				     }else{
				    	$("#errorInfo").text(msg.msg); 
				     }
				   }
			});
		}
		function onAsyncSuccess(event, treeId, treeNode, msg) {
			var obj=$.parseJSON(msg);
			if(obj.length==1&&obj[0].errorCode!=0){
				alert(obj[0].errorMessage);
				var zTree = $.fn.zTree.getZTreeObj("treeDemo");
				zTree.removeChildNodes(treeNode);
			}
		}
		function addHoverDom(treeId, treeNode) {
			if(treeNode.pId=="0"&&treeNode.open){
				var sObj = $("#" + treeNode.tId + "_span");
				if (treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
				var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
					+ "' title='add node' onfocus='this.blur();'></span>";
				sObj.after(addStr);
				var btn = $("#addBtn_"+treeNode.tId);
				if (btn) btn.bind("click", function(){
					var zTree = $.fn.zTree.getZTreeObj("treeDemo");
					if(treeNode.id==dss){
						var index=treeNode.children.length+1;
						zTree.addNodes(treeNode, {id:100, pId:treeNode.id, name:"datasource"+index});
					}else if(treeNode.id==biz){
						
					}
					
					return false;
				});
			}
			
		};
		function removeHoverDom(treeId, treeNode) {
			$("#addBtn_"+treeNode.tId).unbind().remove();
		};
		function showRemoveBtn(treeId, treeNode){
			if(treeNode.pId==0){
				return false;
			}
			return true;
		};
    var zNodes =[
     			{ id:dss, pId:0, name:"数据源",isParent:true},
     			{ id:biz, pId:0, name:"自定义配置",isParent:true}
    ];

    $(document).ready(function(){
     			$.fn.zTree.init($("#treeDemo"), setting, zNodes);
     			$("#dssEdit").hide();
     			$("a.options").click(function(){
     				var key=$(this).text();
     				var value=$(this).siblings("input:hidden").val();     		
     				$(this).parents("div").siblings("input:text").val(key);
     				$(this).parents("div").siblings("input:hidden").val(value);

     			});
    });
    function addProps(){
		var obj='<tr><td width="10%"><input type="text" style="background-color:#C0C0C0;border:1px solid;width:99%" class="userProp"></td><td><input type="text" style="background-color:#C0C0C0;border:1px solid;width:99%" class="userProp"></td><td width="5%" onclick="removeProp(this)">删除</td></tr>';
		$("#props").append(obj);
	};
	function removeProp(obj){
		$(obj).parent().remove();
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
          <a class="navbar-brand" href="#">ZooKeeper配置工具</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav navbar-right">
            <li><a href="#">ZooKeeper配置</a></li>
            <li><a href="#">节点监控</a></li>
            <li><a href="#">帮助</a></li>
          </ul>
        </div>
      </div>
    </div>

    <div class="container-fluid">
      <div class="row">
        <div class="col-sm-4 col-md-3 sidebar">
        <div class="zTreeDemoBackground left">
		<ul id="treeDemo" class="ztree"></ul>
	    </div>
        </div>
        <div class="col-sm-8 col-sm-offset-4 col-md-9 col-md-offset-3 main">
        <div id="dssEdit">
        <h1 class="page-header" style="border-bottom:0px">数据源配置</h1>
        <h2 class="sub-header" style="border-bottom:0px">基本信息</h2>
		<table class="table table-striped">
		<tr>
		<td width="10%">数据源名称:</td>
		<td width="90%" colspan="5"><input type="text" id="dssName" class="form-control"></td>
		</tr>
		<tr>
		<td>数据源类型:</td>
		<td width="35%">
		 <div class="input-group">
      <div class="input-group-btn">
        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">数据源类型 <span class="caret"></span></button>
        <ul class="dropdown-menu" role="menu">
          <li><a href="#" class="options">C3P0</a><input type="hidden" value="com.mchange.v2.c3p0.ComboPooledDataSource"></li>
          <li><a href="#" class="options">Atomikos</a><input type="hidden" value="com.atomikos.jdbc.AtomikosDataSourceBean"></li>
        </ul>
      </div><!-- /btn-group -->
      <input type="text" class="form-control" id="dssTypeKey">
       <input type="hidden" id="dssType">
    </div>
		</td>
		<td width="10%" align="right">索引节点：
		</td>
		<td width="15%" align="left"><select id="dssIndex">
		<option value="0">是</option>
		<option value="1">否</option>
		</select>
		</td>
		<td width="10%" align="right">激活：
		</td>
		<td width="15%" align="left"><select id="dssActive">
		<option value="0">是</option>
		<option value="1">否</option>
		</select>
		</td>
		</tr>
		<tr>
		</table>
		<h2 class="sub-header" style="border-bottom:0px">连接信息</h2>
		<table class="table table-striped">
		<tr>
		<td width="10%">驱动名称</td>
		<td><input type="text" id="dssDriver" class="form-control"></td>
		</tr>
		<tr>
		<td width="10%">数据库连接</td>
		<td><input type="text" id="dssUrl" class="form-control"></td>
		</tr>
		<tr>
		<td width="10%">用户名</td>
		<td><input type="text" id="dssUserName" class="form-control"></td>
		</tr>
		<tr>
		<td width="10%">密码</td>
		<td><input type="text" id="dssPassword" class="form-control"></td>
		</tr>
		<tr style="display:none;">
		<td width="10%">数据库标记</td>
		<td><input type="text" id="dssUnique" class="form-control"></td>
		</tr>
		</table>
		<input type="button" value="增加其他属性" onclick="addProps()">
		<table id="props">
		</table>
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
