function exportConfig(type){
	if(type=="base"){
		url="../manager/export_dss_config";
		location.href=url;
	}else if(type=="biz"){
		url="../manager/export_biz_config";
		location.href=url;
	}
}
function importConfig(type){
	var url="";
	if(type=="base"){
		url="../manager/import_dss_config";
		$("#importConfigInfo").find("div.bizUniqueName").hide();
	}else if(type=="biz"){
		url="../manager/import_biz_config";
		$("#importConfigInfo").find("div.bizUniqueName").show();
	}
	$("#importConfigInfo").find("div.bizUniqueName").find("input").val("");
	//清理
	$("#importConfigInfo").find("#biz_split_val").prop("checked",false);
	$("#importConfigInfo").find("#importId").val("");
	$("#importConfigInfo").find("#importContent").val("");
	$("#importConfigInfo").find("#importId").attr("readonly",true);
	//显示
	$("#importConfigInfo").modal("show");
	//解绑定
	$("#importConfigInfo").find("#startImportConfig").unbind();
	$("#importConfigInfo").find("#biz_split_val").unbind();
	//多个配置文件开关
	$("#importConfigInfo").find("#biz_split_val").bind("click",function(){
		if($(this).prop("checked")){
			$("#importConfigInfo").find("#importId").removeAttr("readonly");
			
		}else{
			$("#importConfigInfo").find("#importId").val("");
			$("#importConfigInfo").find("#importId").attr("readonly",true);
		}
	});
	
	$("#importConfigInfo").find("#startImportConfig").bind("click",function(){
		$("#importConfigInfo").find("#startImportConfig").attr("disabled",true);
		var submitJson=$("#importContent").val().replace(/(\r\n|\n|\r)/gm, '');
		var splitName=$("#importConfigInfo").find("#importId").val().replace(/(\r\n|\n|\r)/gm, '');
		var isSplit=$("#importConfigInfo").find("#biz_split_val").prop("checked");
		if(submitJson==""){
			alert("配置信息输入不能为空");
			$("#importConfigInfo").find("#startImportConfig").removeAttr("disabled");
			return;
		}
		if(isSplit&&splitName==""){
			alert("配置唯一名称不能为空");
			$("#importConfigInfo").find("#startImportConfig").removeAttr("disabled");
			return;
		}
		if(isSplit&&splitName.indexOf(" ")!=-1){
			alert("名称不能含有空格");
			$("#importConfigInfo").find("#startImportConfig").removeAttr("disabled");
			return;
		}
		if(isSplit){
			splitName="/"+$.trim(splitName);
		}
		$.ajax({
			   type: "POST",
			   url: url+splitName,
			   dataType:"json",
			   data:{imports:submitJson},
			   success: function(msg){
				   if(msg.code=="0"){
					   alert("导入成功")
					   $("#importConfigInfo").find("#stopImportConfig").trigger("click");
					   $("#importConfigInfo").find("#startImportConfig").removeAttr("disabled");
				   }else{
					   alert(msg.msg)
					   $("#importConfigInfo").find("#startImportConfig").removeAttr("disabled");
				   }
			   },
			   error:function(){
				   location.href="session_timeout.jsp";
			   }
		});
	});
	
	
}