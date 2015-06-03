<%@page import="autonavi.online.framework.zookeeper.SysProps"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String fileName=request.getParameter("fileName");
if(fileName==null)fileName="";
String defaultName=SysProps.DEDAULT_BIZ_UNIQUE_NAME;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>自定义配置编辑-运行模式</title>
<link href="../resources/css/bootstrap.min.css" rel="stylesheet">
<script src="../resources/js/jquery-1.11.1.min.js"></script>
<script src="../resources/js/bootstrap.min.js"></script>
<script src="../resources/js/aof.js"></script>
<script src="../resources/js/jquery.pin.js"></script>
<script type="text/javascript">

function save(btn,fileName){
	$.ajax({
		   type: "POST",
		   url: "../manager/biz_edit_run_save/"+fileName,
		   dataType:"json",
		   contentType:"application/json",
		   data:JSON.stringify(jsons) ,
		   success: function(msg){
		     if(msg.code==0){
		    	 window.parent.$("#main").attr("src","index_run.jsp");
		     }else{
		    	 alert(msg.msg);
		    	 btn.siblings("button").trigger("click");
		     }
		   },
		   error:function(){
			   location.href="session_timeout.jsp";
		   }
	});
}

function checkFile(fileName,btn){
	$.ajax({
		   type: "POST",
		   url: "../manager/edit_run_check_biz_file",
		   dataType:"json",
		   data: {fileName:fileName},
		   success: function(msg){
		     if(msg.code==0){
		    	 if(saveBizInfoForDev()){
		    		 save(btn,fileName);
		    	 }else{
		    		 btn.siblings("button").trigger("click");
		    	 }
		    	
		     }else{
		    	alert(msg.msg);
		    	btn.siblings("button").trigger("click");
		     }
		   },
		   error:function(){
			   location.href="session_timeout.jsp";
		   }
	});
	
}

$(document).ready(function(){
	$.ajax({
		   type: "POST",
		   url: "../manager/biz_edit_run",
		   dataType:"json",
		   data:{fileName:"<%=fileName%>"} ,
		   success: function(msg){
		     if(msg.code==0){
		    	 paresJson2Biz(msg.result["<%=defaultName%>"],"<%=defaultName%>");
		    	 paresJson2BizSplit(msg.result,"<%=defaultName%>");
		     }else{
		    	alert(msg.msg);
		     }
		   },
		   error:function(){
			   location.href="session_timeout.jsp";
		   }
	});
	
	$("button[name='addFile']").click(function(){
		checkFile($(this).parent().siblings("div.modal-body").find("input:text").val(),$(this));
	});
	$("button[name='addBiz']").click(function(){
		addNewBiz(this);		
	});
	
	
});
function back(){
	location.href="index_run.jsp";
}
</script>

<style type="text/css">
    /* Custom Styles */
    ul.nav-tabs{
        width: 140px;
        margin-top: 20px;
        border-radius: 4px;
        border: 1px solid #ddd;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.067);
    }
    ul.nav-tabs li{
        margin: 0;
        border-top: 1px solid #ddd;
    }
    ul.nav-tabs li:first-child{
        border-top: none;
    }
    ul.nav-tabs li a{
        margin: 0;
        padding: 8px 16px;
        border-radius: 0;
    }
    ul.nav-tabs li.active a, ul.nav-tabs li.active a:hover{
        color: #fff;
        background: #0088cc;
        border: 1px solid #0088cc;
    }
    ul.nav-tabs li:first-child a{
        border-radius: 4px 4px 0 0;
    }
    ul.nav-tabs li:last-child a{
        border-radius: 0 0 4px 4px;
    }
    ul.nav-tabs.affix{
        top: 30px; /* Set the top position of pinned element */
    }
    li.leftMenu{
        font-size:14pt;
        font-weight:bold;
        text-align:center;
    }
     li.leftMenu_1{
        font-size:5pt;
        font-weight:bold;
        text-align:center;
    }
    li.topMenu{
        font-size:14pt;
        font-weight:bold;
        text-align:center;
        margin-bottom:3px;
    }
    span.glyphicon{
       cursor:pointer;
    }
    body { 
    padding-top: 45px; 
    overflow: scroll; 
    }
</style>
</head>
<body data-spy="scroll" data-target="#myScrollspy">
<jsp:include page="model/biz_model.jsp"></jsp:include>
<jsp:include page="model/config_save_run_model.jsp"></jsp:include>
<jsp:include page="model/biz_add_model.jsp"></jsp:include>
<nav class="navbar navbar-default navbar-fixed-top" role="navigation" style="min-height:25px;">
        <ul class="nav">
            <li id="saveMenu" class="topMenu">
            <button type="button" class="btn btn-default btn-lg" data-toggle="modal" data-target="#saveModal"><span class="glyphicon glyphicon-floppy-saved"></span>保存</button>&nbsp;&nbsp;
            <button type="button" class="btn btn-default btn-lg" id="backBtn" onclick="back()"><span class="glyphicon glyphicon-floppy-remove"></span>取消</button>
            </li>    
       </ul>
</nav>
<div class="container" style="margin-left:30px;margin-right:30px;">
    <div class="row">
         <div class="col-xs-3" id="myScrollspy">
            <ul class="nav nav-tabs nav-stacked " data-spy="affix" data-offset-top="125" style="border:0px">
			    <li id="bizMenu" class="leftMenu">唯一名称 <span class="glyphicon glyphicon-plus addDss" data-toggle="modal" data-target="#bizModal"></span></li>
			    <li id="bizMenuEnd" class="leftMenu" style="display:none"></li>
            </ul>
			
        </div>
        <div class="col-xs-9" style="overflow-x:auto;overflow-y:auto">
         <div id="biz"></div>
        </div>
    </div>
</div>
</body>
</html> 

