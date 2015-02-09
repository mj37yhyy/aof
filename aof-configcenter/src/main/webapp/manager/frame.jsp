<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String type=request.getParameter("type");
if(type==null)type="dev";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
<script src="<%=path%>/resources/js/jquery-1.11.1.min.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	<%if(type.equals("dev")){%>
	$("#main").attr("src","index_dev.jsp");
	<%}else {%>
	$("#main").attr("src","index_run.jsp");
	<%}%>
});
</script>
</head>
<frameset rows="50,*" framespacing="0" >
<frame id="top" src="manager_top.jsp" scrolling="no" frameborder="0" noresize />
<frame id="main" scrolling="yes" frameborder="0" noresize />
</frameset>
</html>