var submitJson;
var regInt="^[1-9]{1}[0-9]*$";
var c3p0="com.mchange.v2.c3p0.ComboPooledDataSource";
var jta="com.atomikos.jdbc.AtomikosDataSourceBean";
var shardIndex;
var dsName=new Array();
var indexTableNames=new Array();
var segmentTableNames=new Array();
var maxKey=0;
var jsons;
var realDssSource=new Array();
var bizSource=null;
function bizSourceClean(){
	bizSource=new Array();
}
function initSubmitJson(){
	submitJson={"dataSources":{},"shardIndex":-1,"indexTableMap":{},"segmentTables":[],"realDataSources":{}};
	shardIndex=-1;
}
function shieldIndexSelect(){
	$("input:checkbox[name='shardIndex']").attr("disabled","disabled");
	$("span.trashShard:hidden").parentsUntil("div.shardInfo").parent().find("span.glyphicon-pencil").hide();
}
function parseJson2Shard(msg){
	var shard=msg.result.dataSources;
	shardIndex=msg.result.shardIndex;
	var _shard="";
	for(var i in shard){
		 var dataSource=shard[i];
		 var props=dataSource["props"];
		 var strategyName=props["strategyName"];
		 var realDss=props["realDss"];
		 var name=i;
    	 if(parseInt(i)>maxKey){
		   maxKey=parseInt(i);
	     }
		 var obj=$("#shard_info_m").clone();
	     obj.find("input:hidden[name='shardKey']").val(name);
		 //obj.find("input:hidden[name='name']").val("shard_"+name);
		 obj.find("h4.shardInfoTitle").append("(分区"+name+")");
		 obj.find("#strategyName").val(strategyName);
		 obj.attr("id","shard_"+name);
		 $.each(realDss,function(i,n){
			   if(strategyName.indexOf("w")!=0){
				   $("#dss_"+n).find("span.glyphicon-trash").hide();
					var _append='<input type="hidden" name="realDss" value="'+n+'">';
					obj.find("td.dss").append(_append);
					obj.find("td.dss").append(" ["+n+"] <span class='weight_class' style='display:none'>权重:<input name='"+n+"_weight' type='text' class='number'></span>");
					obj.find("td.dss").append("<br>");
			   }else{
				   var nn=n.substring(0,n.indexOf("?weight"));
				   var weight=n.substring(n.indexOf("=")+1);
				   $("#dss_"+nn).find("span.glyphicon-trash").hide();
				   var _append='<input type="hidden" name="realDss" value="'+nn+'">';
				   obj.find("td.dss").append(_append);
					obj.find("td.dss").append(" ["+nn+"] <span class='weight_class'>权重:<input name='"+nn+"_weight' type='text' class='number' value='"+weight+"'></span>");
					obj.find("td.dss").append("<br>");
			   }
				
		 });
		 
		 if(shardIndex==i){
    		 obj.find("input:checkbox[name='shardIndex']").prop("checked","checked");
    		 obj.find("span.glyphicon-trash").hide();
    	 }else{
    		 obj.find("input:checkbox[name='shardIndex']").parent().hide();
    	 }
		 bindEvent(obj);
		 $("#shard").append(obj);
    	 obj.show();
    	 obj.find("h4.shardInfoTitle").siblings().hide();
    	 _shard=_shard+"<li class='leftMenu_1 shardInfoMenu'><a href='#shard_"+name+"'>"+"分区"+name+"</a></li>";
	}
	$("#shardMenu").after(_shard);
	//初始化可用数据源
	$.each($("div[id^='dss_']").find("span.glyphicon-trash:visible"),function(i,n){
		var ds=$(this).parent().siblings("input:hidden[name='name']").val();
		realDssSource.push(ds);
	});
}
function parseJson2DS(msg){
	 submitJson=msg.result;
	 var ds=msg.result.realDataSources;
	 var dss="";
	 for(var i in ds){
    	 var dataSource=ds[i];
    	 var beanClass=dataSource["beanClass"];
    	 var name=i;
    	 var props=dataSource["props"];
    	 dsName.push(name);
    	 dss=dss+"<li class='leftMenu_1 dssMenu'><a href='#dss_"+name+"'>"+name+"</a></li>";
    	 var obj=null;
    	 //填入属性
    	 if(beanClass==c3p0){
    		 obj=$("#dss_c3p0_m").clone(); 
    		 obj.find("input:hidden[name='name']").val(name);
        	 obj.find("input:text[name='jdbcUrl']").val(props["jdbcUrl"]);
        	 delete props["jdbcUrl"];
        	 obj.find("input:text[name='driverClass']").val(props["driverClass"]);
        	 delete props["driverClass"];
        	 obj.find("input:text[name='user']").val(props["user"]);
        	 delete props["user"];
        	 obj.find("input:password[name='password']").val(props["password"]);
        	 delete props["password"];
    	 }else if(beanClass==jta){
    		 obj=$("#dss_jta_m").clone(); 
    		 obj.find("input:hidden[name='name']").val(name);
        	 obj.find("input:text[name='URL']").val(props["xaProperties"]['URL']);
        	 obj.find("input:text[name='xaDataSourceClassName']").val(props["xaDataSourceClassName"]);
        	 delete props["xaDataSourceClassName"];
        	 obj.find("input:text[name='user']").val(props["xaProperties"]['user']);
        	 obj.find("input:text[name='password']").val(props["xaProperties"]['password']);
        	 obj.find("input:text[name='uniqueResourceName']").val(props["uniqueResourceName"]);
        	 delete props["uniqueResourceName"];
        	 delete props["xaProperties"];
    	 }
    	 obj.find("table.dssProps").find("tr").remove();
    	 for(var j in props){
			 var p='<tr><td width="20%" style="line-height:35px">'+j+'</td><td width="75%"><input type="text" class="form-control" name="'+j+'" value="'+props[j]+'"></td><td width="5%"  style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td></tr>';
			 obj.find("table.dssProps").append(p);
    	 }
    	 
    	 obj.find("input:hidden[name='beanClass']").val(beanClass);
    	 obj.attr("id","dss_"+name);
    	 obj.find("h4.dssTitle").append("("+name+")");
    	 obj.find("h4.dssTitle").siblings().hide();
    	 bindEvent(obj);
    	 $("#dss").append(obj);
    	 obj.show();
     }
	 //生成菜单
	 $("#dssMenu").after(dss);
	 
	
}
function paresJson2Index(msg){
	 var index=msg.result.indexTableMap;
	 var shard="";
     for(var i in index){
    	 var obj=$("#index_table_m").clone(); 
    	 obj.find("input:hidden[name='name']").val(i);
    	 indexTableNames.push(i);
    	 for(j in index[i]){
    		 var name=index[i][j].name;
    		 var columnType=index[i][j].columnType;
    		 var columnName=index[i][j].columnName;
    		 var lengths=index[i][j]["length"];
    		 var p='<tr><td style="line-height:35px">'+name+'<input type="text" style="display:none" name="name" value="'+name+'"></td><td style="line-height:35px">'+columnName+'<input type="text" style="display:none" name="columnName" value="'+columnName+'"></td><td style="line-height:35px">'+columnType+'<input type="text" style="display:none" name="columnType" value="'+columnType+'"></td><td style="line-height:35px">'+lengths+'<input type="text" style="display:none" name="length" value="'+lengths+'"></td><td></td></tr>';
    		 obj.find("table.indexProps").append(p);
    		 
    		 
    	 }
    	 obj.find("h4.indexTitle").append("("+i+")");
    	 //默认隐藏
    	 obj.find("h4.indexTitle").siblings().hide();
    	 //隐藏添加
    	 obj.find("span.glyphicon-plus").hide();
    	 obj.attr("id","index_"+i);
    	 bindEvent(obj);
    	 $("#index").append(obj);
    	 obj.show();
		 shard=shard+"<li class='leftMenu_1 shardMenu'><a href='#index_"+i+"'>"+i+"</a></li>";
		 
	 }
     //生成菜单
	 $("#indexMenu").after(shard);
}
function paresJson2Segment(msg){
	 var segments=msg.result.segmentTables;
	 var seg="";
	 for(var i in segments){
		 var segment=segments[i];
		 var name=segment.name;
		 var counts=segment.count;
		 if( $.grep(segmentTableNames,function(n,j){
			 return n==name;
		 }).length==0){
			 var obj=$("#segment_table_m").clone();
			 obj.find("input:hidden[name='name']").val(name);
			 obj.find("input:text[name='count']").val(counts);
			 obj.find("input:text[name='count']").prop("readonly","readonly")
			 obj.find("h4.segmentTitle").append("("+name+")");
			//默认隐藏
	    	 obj.find("h4.segmentTitle").siblings().hide();
	    	 obj.attr("id","segment_"+name);
	    	 bindEvent(obj);
	    	 $("#segment").append(obj);
	    	 obj.show();
	    	 seg=seg+"<li class='leftMenu_1 segmentMenu'><a href='#segment_"+name+"'>"+name+"</a></li>";
	    	 segmentTableNames.push(name);
		 }
		 
	 }
	 //生成菜单
	 $("#segmentMenu").after(seg);
	 
}
function addNewBiz(a){
	if(checkNull(a)){
		var name=$(a).parent().siblings().find("input:text[name='name']").val();
		if($.grep(bizSource,function(n,j){
			 return n==name;
		}).length>0){
			alert("biz唯一名称已经存在");
			return false;
		}
		var obj=$("#biz_m").clone();
		obj.find("#uniqueBizName").append(name);
		obj.find("#bizUniqueId").val(name);
		obj.find("span.glyphicon-trash").show();
		obj.find("input:hidden[name='name']").val(name);
		bindEvent(obj);
		obj.attr("id",name+"_biz_model")
		$("#biz").append(obj);
		obj.find("span.glyphicon-chevron-down").trigger("click");
		var link="<li class=\"leftMenu_1 bizMenu\"><a href=\"#"+name+"_biz_model"+"\">"+name+"</a></li>"; 
		$("#bizMenuEnd").before(link);
		bizSource.push(name);
		$("#stopBiz").trigger("click");
		obj.show();
	}
}
function paresJson2BizSplit(msg,filter){
	bizSourceClean();
	
	if(msg!=null){
		for(var i in msg){
			var obj=$("#biz_m").clone();
			if(i!=filter){
				var j=msg[i];
				praseToBiz(obj,j);
				obj.find("#uniqueBizName").append(i);
				obj.find("#bizUniqueId").val(i);
				obj.find("span.glyphicon-trash").show();
				obj.find("input:hidden[name='name']").val(i);
				bindEvent(obj);
				obj.attr("id",i+"_biz_model")
				$("#biz").append(obj);
				obj.find("span.glyphicon-chevron-up").trigger("click");
				var link="<li class=\"leftMenu_1 bizMenu\"><a href=\"#"+i+"_biz_model"+"\">"+i+"</a></li>"; 
				$("#bizMenuEnd").before(link);
				bizSource.push(i);
				obj.show();
			}
			
		}
		
	}
}
function praseToBiz(obj,msg){
	for(var i in msg){
		var p=$('<tr><td width="20%" style="line-height:35px"><div class="input-group"><span class="input-group-addon">属性</span><input type="text" class="form-control bizPropsAddKey" value="'+i+'" readonly="readonly"></div></td>'
				 +'<td width="55%"><div class="input-group"><span class="input-group-addon">值</span><input type="text" class="form-control bizPropsAddValue" value="'+msg[i]["value"]+'"></div></td>'
				 +'<td width="20%"><div class="input-group"><span class="input-group-addon">描述</span><input type="text" class="form-control bizPropsComments uncheck" value="'+msg[i]["comments"]+'"></div></td>'
				 +'<td width="5%"  style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td></tr>');
		obj.find("table.bizProps").append(p);
		
	}
}
function paresJson2Biz(msg,filter){
	var obj=$("#biz_m").clone();
	if(msg!=null){
		praseToBiz(obj,msg);
	}
	obj.find("#uniqueBizName").append("默认未分类");
	obj.find("span.glyphicon-chevron-up").show();
	
	obj.find("#bizUniqueId").val(filter);
	obj.find("input:hidden[name='name']").val(filter);
	
	bindEvent(obj);
	obj.attr("id",filter+"_biz_model")
	obj.find("#bizUniqueId").val(filter);
	$("#biz").append(obj);
	var link="<li class=\"leftMenu_1 bizMenu\"><a href=\"#"+filter+"_biz_model"+"\">默认未分类</a></li>"; 
	$("#bizMenuEnd").before(link);
	obj.show();
}
function changeRealDssSource(dssName,isAdd){
	var _tempArray=new Array();
	if(!isAdd){
		$.each(realDssSource,function(i,n){
			if(n!=dssName){
				_tempArray.push(n);
			}
		});
	}else{
		realDssSource.push(dssName);
	}
	if(_tempArray.length>0){
		realDssSource=_tempArray;
	}
}
function replaceRealDssSource(dssNames){
	var _tempArray=new Array();
	$.each(dssNames,function(i,n){
		_tempArray.push(n);
	});
	realDssSource=_tempArray;
}
function initShardAdd(isNew,realDss,id){
	//初始化
	$("#shardModal").find("#realDssSource").empty();
	$("#shardModal").find("#realDss").empty();
	//填充选择
	$.each(realDssSource,function(i,n){
		$("#shardModal").find("#realDssSource").append("<option value='"+n+"'>"+n+"</option>");
	});
	if(!isNew){
		$("#shardModal").find("button[name='saveShard']").show();
		$("#shardModal").find("button[name='addShard']").hide();
		//修改是初始化已经选择的数据源
		$.each(realDss,function(i,n){
			$("#shardModal").find("#realDss").append("<option value='"+n+"'>"+n+"</option>");
		});
		$("#shardModal").find("#shardId").val(id);
	}else{
		$("#shardModal").find("#button[name='saveShard']").hide();
		$("#shardModal").find("#button[name='addShard']").show();
	}
	$("#shardModal").modal("show");	
}
function modifyShard(a){
	if(checkNull(a)){
		$.each($(a).parent().siblings().find("#realDssSource").children(),function(i,n){
			$(n).prop("selected",true);
		});
		$.each($(a).parent().siblings().find("#realDss").children(),function(i,n){
			$(n).prop("selected",true);
		});
		var _id=$(a).siblings("#shardId").val();
		//清理
		$("#"+_id).find("#strategyName").val("none");
		$("#"+_id).find("td.dss").html("")
		//重构
		bulidShardInfo(a,$("#"+_id));
		$("#stopShard").trigger("click");
  	     //清理模式对话框
  	    $(a).parent().siblings().find("#realDss").children().remove();
  	    $(a).parent().siblings().find("#realDssSource").children().remove();
  	    location.href="#"+_id;
	}
}
function bulidShardInfo(a,obj){
	var realDss=$(a).parent().siblings().find("#realDss").val();
	realDssSource=$(a).parent().siblings().find("#realDssSource").val();
	if(realDssSource==null){
		realDssSource=new Array();
	}
	$.each(realDss,function(i,n){
		$("#dss_"+n).find("span.glyphicon-trash").hide();
		var _append='<input type="hidden" name="realDss" value="'+n+'">';
		obj.find("td.dss").append(_append);
		obj.find("td.dss").append(" ["+n+"] <span class='weight_class' style='display:none'>权重:<input name='"+n+"_weight' type='text' class='number'></span>");
		obj.find("td.dss").append("<br>");
	});
	$.each(realDssSource,function(i,n){
		$("#dss_"+n).find("span.glyphicon-trash").show();
	});
}
function addNewShard(a){
	if(checkNull(a)){
		$.each($(a).parent().siblings().find("#realDssSource").children(),function(i,n){
			$(n).prop("selected",true);
		});
		$.each($(a).parent().siblings().find("#realDss").children(),function(i,n){
			$(n).prop("selected",true);
		});
		var name=""+(maxKey+1);
		var obj=$("#shard_info_m").clone();
		obj.find("input:hidden[name='shardKey']").val(name);
//		obj.find("input:hidden[name='name']").val("shard_"+name);
		obj.find("h4.shardInfoTitle").append("(分区"+name+")");
		bulidShardInfo(a,obj);
		
		if(shardIndex==maxKey+1){
   		 obj.find("input:checkbox[name='shardIndex']").prop("checked","checked");
   	    }else if(shardIndex==-1){
   		 obj.find("input:checkbox[name='shardIndex']").prop("checked","checked");
   		 shardIndex=maxKey+1;
   		 $("input:checkbox[name='shardIndex']").parent().hide();
   		 obj.find("input:checkbox[name='shardIndex']").parent().show();
   		 obj.find("span.glyphicon-trash").hide();
   	    }else{
   		 obj.find("input:checkbox[name='shardIndex']").parent().hide();
   		 //obj.find("input:checkbox[name='shardIndex']").siblings("span.shardIndexTitle").hide();
   	     }
		obj.attr("id","shard_"+name);
		obj.find("h4.shardInfoTitle").find("span.glyphicon-chevron-up").show();
   	    obj.find("h4.shardInfoTitle").find("span.glyphicon-chevron-down").hide();
   	    bindEvent(obj);
	    $("#shard").append(obj);
	    obj.show();
	    var shard="<li class='leftMenu_1 shardInfoMenu'><a href='#shard_"+name+"'>"+"分区"+name+"</a></li>";
	    $("#segmentMenu").parent().append(shard);
		$("#stopShard").trigger("click");
   	     //清理模式对话框
   	    $(a).parent().siblings().find("#realDss").children().remove();
   	    location.href="#shard_"+name;
   	    maxKey++;;
	}
}
function addNewDs(a){
	var dss='';
	if(checkNull(a)){
		var name=$(a).parent().siblings().find("input:text[name='name']").val();
		if($.grep(dsName,function(n,j){
			 return n==name;
		}).length>0){
			alert("数据源名称已经存在");
			return false;
		}
		var driver=$(a).parent().siblings().find("input:text[name='driver']").val();
		var url=$(a).parent().siblings().find("input:text[name='url']").val();
		var user=$(a).parent().siblings().find("input:text[name='user']").val();
		var password=$(a).parent().siblings().find("input:text[name='password']").val();
		var obj=null;
		if($(a).parent().siblings().find("input:radio[name='type']:checked").val()=='c3p0'){
		     obj=$("#dss_c3p0_m").clone(); 
   		     obj.find("input:hidden[name='name']").val(name);
       	     obj.find("input:text[name='jdbcUrl']").val(url);
       	     obj.find("input:text[name='driverClass']").val(driver);
       	     obj.find("input:text[name='user']").val(user);
       	     obj.find("input:text[name='password']").val(password);
       	     obj.find("input:hidden[name='beanClass']").val(c3p0);
		}else if($(a).parent().siblings().find("input:radio[name='type']:checked").val()=='jta'){
			 obj=$("#dss_jta_m").clone(); 
	   		 obj.find("input:hidden[name='name']").val(name);
	       	 obj.find("input:text[name='URL']").val(url);
	       	 obj.find("input:text[name='xaDataSourceClassName']").val(driver);
	       	 obj.find("input:text[name='user']").val(user);
	       	 obj.find("input:text[name='password']").val(password);
	       	 obj.find("input:hidden[name='beanClass']").val(jta);
		}
		 //maxKey=maxKey+1;
    	 //obj.find("input:hidden[name='dsKey']").val(maxKey);
//    	 if(shardIndex==maxKey){
//    		 obj.find("input:checkbox[name='shardIndex']").prop("checked","checked");
//    	 }else if(shardIndex==-1){
//    		 obj.find("input:checkbox[name='shardIndex']").prop("checked","checked");
//    		 shardIndex=maxKey;
//    		 obj.find("span.glyphicon-trash").hide();
//    	 }else{
//    		 obj.find("input:checkbox[name='shardIndex']").hide();
//    		 obj.find("input:checkbox[name='shardIndex']").siblings("span.shardIndexTitle").hide();
//    	 }
    	 obj.attr("id","dss_"+name);
    	 obj.find("h4.dssTitle").append("("+name+")");
    	//更新分区信息中可选择的数据源列表
    	 changeRealDssSource(name,true);
    	 bindEvent(obj);
    	 obj.find("h4.dssTitle").find("span.glyphicon-chevron-up").show();
    	 obj.find("h4.dssTitle").find("span.glyphicon-chevron-down").hide();
    	 $("#dss").append(obj);
    	 obj.show();
    	//生成菜单
    	 dss=dss+"<li  class='leftMenu_1 dssMenu'><a href='#dss_"+name+"'>"+name+"</a></li>";
    	 $("#indexMenu").before(dss);
    	 $("#stopDss").trigger("click");
    	 //清理模式对话框
    	 $(a).parent().siblings().find("input:text").val("");
    	 dsName.push(name);
    	 location.href="#dss_"+name;
    	 
	}
}
function addNewIndex(a){
	if(checkNull(a)){
		var name=$(a).parent().siblings().find("input:text[name='name']").val();
		if($.grep(indexTableNames,function(n,j){
			 return n==name;
		}).length>0){
			alert("索引表名称已经存在");
			return false;
		}
		 var obj=$("#index_table_m").clone();
		 obj.find("input:hidden[name='name']").val(name);
		 obj.find("h4.indexTitle").append("("+name+")");
		 var p=$('<tr><td><input type="text" name="name" class="form-control"></td><td><input type="text" name="columnName" class="form-control"></td><td><input type="text" name="columnType" class="form-control"></td><td><input type="text" name="length" class="form-control number"></td><td></td></tr>');
		 obj.find("table.indexProps").append(p);
		 obj.attr("id","index_"+name);
		 obj.find("h4.indexTitle").find("span.glyphicon-chevron-up").show();
    	 obj.find("h4.indexTitle").find("span.glyphicon-chevron-down").hide();
    	 bindEvent(obj);
    	 $("#index").append(obj);
    	 obj.show();
		 var shard="<li class='leftMenu_1 shardMenu'><a href='#index_"+name+"'>"+name+"</a></li>";
		 $("#segmentMenu").before(shard);
		 $("#stopIndex").trigger("click");
    	 //清理模式对话框
    	 $(a).parent().siblings().find("input:text").val("");
    	 indexTableNames.push(name);
    	 location.href="#index_"+name;
		
	}
}
function addNewSegment(a){
	if(checkNull(a)){
		var name=$(a).parent().siblings().find("input:text[name='name']").val();
		if($.grep(segmentTableNames,function(n,j){
			 return n==name;
		}).length>0){
			alert("分表信息已经存在");
			return false;
		}
		var counts=$(a).parent().siblings().find("input:text[name='count']").val();
		 var obj=$("#segment_table_m").clone();
		 obj.find("input:hidden[name='name']").val(name);
		 obj.find("input:text[name='count']").val(counts);
		 obj.find("h4.segmentTitle").append("("+name+")");
		 obj.attr("id","segment_"+name);
		 obj.find("h4.segmentTitle").find("span.glyphicon-chevron-up").show();
    	 obj.find("h4.segmentTitle").find("span.glyphicon-chevron-down").hide();
    	 bindEvent(obj);
    	 $("#segment").append(obj);
    	 obj.show();
		 var segment="<li class='leftMenu_1 segmentMenu'><a href='#segment_"+name+"'>"+name+"</a></li>";
		 $("#shardMenu").before(segment);
		 $("#stopSegment").trigger("click");
    	 //清理模式对话框
    	 $(a).parent().siblings().find("input:text").val("");
    	 segmentTableNames.push(name);
    	 location.href="#segment_"+name;
		
	}
}
function checkInt(str){
	if(str.match(regInt)==null){
		return false;
	}
	return true;
}
function generationTips(a){
	a.css("border","1px solid red");
	a.change(function(){
		if(a.val()!=""){
			a.css("border","");
			a.attr("class","form-control");
		}else{
			a.css("border","1px solid red");
		}
		
	});
	a.keydown(function(){
		if(a.val()!=""){
			a.css("border","");
			a.attr("class","form-control");
		}else{
			a.css("border","1px solid red");
		}
		
	});
}
function checkNull(a){
	if(a!=null&&a!="undefined"){
		var checked=true;
//		if($(a).parent().siblings().find("#strategyName").length>0){
//			if($(a).parent().siblings().find("#strategyName").val()=="none"){
//				alert("策略不能为空");
//				checked=false;
//				return false;
//			}
//		}
		if($(a).parent().siblings().find("#realDss").length>0){
			if($(a).parent().siblings().find("#realDss").children().size()==0){
				alert("数据源不能为空");
				checked=false;
				return false;
			}
		}
		$(a).parent().siblings().find("input:text").each(function(){
			if(!$(this).hasClass("uncheck")&&($(this).val()==null||$(this).val()=="")){
				alert("所有选项不能为空");
				checked=false;
				return false;
			}
		});
		$(a).parent().siblings().find("input:text").each(function(){
			if($(this).attr("class").indexOf("number")!=-1&&checked){
				if(!checkInt($(this).val())){
					alert("数量必须为数字");
					$(this).val("");
					checked=false;
					return false;
				}
				if($(this).attr("class").indexOf("number1")!=-1&&checked){
					if(parseInt($(this).val())<=1){
						alert("数量必须大于1");
						$(this).val("");
						checked=false;
						return false;
					}
				}
			}
		});
		if(!checked){
			return false;
		}
	}else{
		var isAlert=false;
		var locations=null;
		$("div.info:visible").each(function(){
			var checked=true;
			var obj=$(this);
			$(this).find("select:visible").each(function(){
				if($(this).val()==null||$(this).val()=="none"){
					if(!isAlert){//只弹出一次警告
						alert("下拉选项未选");
						isAlert=true;
						locations=obj;
					}
					generationTips($(this))
					checked=false;
				}
			});
			$(this).find("input:text:visible").each(function(){
				if(!$(this).hasClass("uncheck")&&($(this).val()==null||$(this).val()=="")){
					if(!isAlert){//只弹出一次警告
						alert("所有选项不能为空");
						isAlert=true;
						locations=obj;
					}
					generationTips($(this))
					checked=false;
				}
			});
			$(this).find("input:text:visible").each(function(){
				if($(this).attr("class")!=null&&$(this).attr("class").indexOf("number")!=-1&&checked){
					if(!checkInt($(this).val())){
						if(!isAlert){//只弹出一次警告
							alert("字段长度或者分表数量必须为数字");
							isAlert=true;
							locations=obj;
						}
						generationTips($(this));
						checked=false;
					}
					
				}
				if($(this).attr("class")!=null&&$(this).attr("class").indexOf("number1")!=-1&&checked){
					if(parseInt($(this).val())<=1){
						if(!isAlert){//只弹出一次警告
							alert("数量必须大于1");
							isAlert=true;
							locations=obj;
						}
						$(this).css("border","1px solid red");
						$(this).change(function(){
							if($(this).val()!=""){
								$(this).css("border","");
								$(this).attr("class","form-control");
							}else{
								$(this).css("border","1px solid red");
							}
							
						});
						$(this).keydown(function(){
							if($(this).val()!=""){
								$(this).css("border","");
								$(this).attr("class","form-control");
							}else{
								$(this).css("border","1px solid red");
							}
							
						});
						checked=false;
					}
					
				}
			});
			
			if(!checked){
				$(this).find("h4").siblings().show();
				$(this).find("h4").find("span.glyphicon-chevron-up").show();
				$(this).find("h4").find("span.glyphicon-chevron-down").hide();
				
			}
		});
		if(isAlert){
			location.href="#"+locations.attr("id");
			return false;
		}
		
	}
	
	return true;
}
function bindEvent(obj){
	//绑定数据源修改事件
	if(obj.attr("id").indexOf("shard_")==0){
		obj.find("span.glyphicon-pencil").click(function(){
			var _tempArray=new Array();
			$.each($(this).parentsUntil("table").find("input:hidden[name='realDss']"),function(i,n){
				_tempArray.push($(n).val());
			});
			var _id=$(this).parentsUntil("div.shardInfo").parent().attr("id");
			initShardAdd(false, _tempArray,_id);
		});
		obj.find("#strategyName").change(function(){
			if($(this).val().indexOf("w")==0){
				obj.find("span.weight_class").show();
			}else{
				obj.find("span.weight_class").hide();
			}
		});
	}
	//绑定数据源选择事件
	if(obj.attr("id").indexOf("dss_")==0){
		$("#shardModal").find("#dss_select").unbind();
		$("#shardModal").find("#dss_cancle").unbind();
		$("#shardModal").find("#dss_select").click(function(){
			if($("#shardModal").find("#realDssSource").val()!=null){
				$.each($("#shardModal").find("#realDssSource").val(),function(i,n){
					 $("#shardModal").find("#realDssSource").find("option[value='"+n+"']").remove();
					 $("#shardModal").find("#realDss").append("<option value='"+n+"'>"+n+"</option>");
					 
				});
			}
			
		});
		$("#shardModal").find("#dss_cancle").click(function(){
			if($("#shardModal").find("#realDss").val()!=null){
				$.each($("#shardModal").find("#realDss").val(),function(i,n){
					 $("#shardModal").find("#realDss").find("option[value='"+n+"']").remove();
					 $("#shardModal").find("#realDssSource").append("<option value='"+n+"'>"+n+"</option>");
				});
			}
			
		});
	}
	
	 //绑定删除事件
	 obj.find("h4").find("span.glyphicon-trash").click(function(){
		 if(confirm("确定要删除这个配置项目吗?")){
			 var name=obj.find("input:hidden[name='name']").val();
			 var clazz=$(this).attr("class");
			 //清理数组和菜单
			 if(clazz.indexOf("trashDss")!=-1){
				 $.grep(dsName,function(n,j){
					if(n==name){
						delete dsName[j];
						changeRealDssSource(name,false);
					} 
				 });
				 $("a[href='#dss_"+name+"']").parent().remove();
			 }else if(clazz.indexOf("trashIndexTable")!=-1){
				 $.grep(indexTableNames,function(n,j){
						if(n==name){
							delete indexTableNames[j];
						} 
				 });
				 $("a[href='#index_"+name+"']").parent().remove();
			 }else if(clazz.indexOf("trashSegment")!=-1){
				 $.grep(segmentTableNames,function(n,j){
						if(n==name){
							delete segmentTableNames[j];
						} 
				 });
				 $("a[href='#segment_"+name+"']").parent().remove();
			 }else if(clazz.indexOf("trashShard")!=-1){
				 $("a[href='#"+name+"']").parent().remove();
				 $.each(obj.find("input:hidden[name='realDss']"),function(n,j){
					 var _dss=$(j).val();
					 $("#dss_"+_dss).find("span.glyphicon-trash").show();
					 changeRealDssSource(_dss,true);
				 });
			 }else if(clazz.indexOf("trashBiz")!=-1){
				 $.grep(bizSource,function(n,j){
						if(n==name){
							delete bizSource[j];
						} 
				 });
				 $("a[href='#"+name+"_biz_model']").parent().remove();
			 } 
			 obj.remove();
		 }
		 
	 });
	 //绑定收起事件
	 obj.find("h4").find("span.glyphicon-chevron-up").click(function(){
		 $(this).hide();
		 $(this).siblings("span.glyphicon-chevron-down").show();
		 $(this).parent("h4").siblings().hide();
		 
	 });
	//绑定拉开事件
	 obj.find("h4").find("span.glyphicon-chevron-down").click(function(){
		 $(this).hide();
		 $(this).siblings("span.glyphicon-chevron-up").show();
		 $(this).parent("h4").siblings().show();
		 
	 });
	//减少属性
	 obj.find("span.glyphicon-minus").click(function(){
		$(this).parents("tr").remove(); 
	 });
	//增加属性
	 obj.find("span.glyphicon-plus").click(function(){
		var p=null;
		if($(this).attr("class").indexOf("dss")!=-1){
		  p=$('<tr><td width="20%" style="line-height:35px"><input type="text" class="form-control dssPropsAddKey"></td><td width="75%"><input type="text" class="form-control dssPropsAddValue"></td><td width="5%"  style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td></tr>');
		  $(this).parent().siblings("table.dssProps").prepend(p);
		}else if($(this).attr("class").indexOf("indexTable")!=-1){
		  p=$('<tr><td><input type="text" name="name" class="form-control"></td><td><input type="text" name="columnType" class="form-control"></td><td><input type="text" name="columnName" class="form-control"></td><td><input type="text" name="length" class="form-control"></td><td><span class="glyphicon glyphicon-minus"></span></td></tr>');
		  $(this).parent().siblings("table.indexProps").append(p);
		}else if($(this).attr("class").indexOf("bizTable")!=-1){
		  p=$('<tr><td width="20%" style="line-height:35px"><div class="input-group"><span class="input-group-addon">属性</span><input type="text" class="form-control bizPropsAddKey"></div></td>'
			 +'<td width="55%"><div class="input-group"><span class="input-group-addon">值</span><input type="text" class="form-control bizPropsAddValue"></div></td>'
			 +'<td width="20%"><div class="input-group"><span class="input-group-addon">描述</span><input type="text" class="form-control bizPropsComments uncheck"></div></td>'
			 +'<td width="5%"  style="text-align:right"><span class="glyphicon glyphicon-minus"></span></td></tr>');
		  $(this).parent().siblings("table.bizProps").append(p);
		}
		if(p!=null)
		p.find("span.glyphicon-minus").click(function(){
			$(this).parents("tr").remove(); 
		});
	 });
	 //索引表勾选逻辑
	 obj.find("input:checkbox[name='shardIndex']").click(function(){
		if($(this).prop("checked")){
			shardIndex=$(this).siblings("input:hidden[name='shardKey']").val();
			$("input:checkbox[name='shardIndex']").parent().hide();
			//$("input:checkbox[name='shardIndex']").siblings("span.shardIndexTitle").hide();
			$(this).parent().show();
			//$(this).siblings("span.shardIndexTitle").show();
			$("#shard_"+shardIndex).find("span.glyphicon-trash").hide();
		}else{
			var _index=$(this).siblings("input:hidden[name='shardKey']").val();
			$("input:checkbox[name='shardIndex']").parent().show();
			//$("input:checkbox[name='shardIndex']").siblings("span.shardIndexTitle").show();
			$("#shard_"+_index).find("span.glyphicon-trash").show();
			shardIndex=-1;
		}
	 });
}
function saveBizInfoForDev(){
	jsons={};
	var _jsons={};
	if(checkNull()){
		var flag=true;
		$("div.bizInfo:visible").each(function(){
			var obj=$(this);
//			//所有有名称的
//			obj.find("input:text[name]").each(function(){
//				jsons[$(this).attr("name")]=$(this).val();
//			});
			//所有用户自己添加的
			
			obj.find("input:text[class*='bizPropsAddKey']").each(function(){
				var splitName=$(this).parentsUntil("div.bizInfo").parent().find("input#bizUniqueId").val();
				if(_jsons[$(this).val()]!=null){
					alert("属性"+$(this).val()+"重复");
					$(this).css("border","1px solid red");
					flag=false;
					return;
				}
				
				var _obj={};
				_obj["value"]=$(this).parentsUntil("td").parent().siblings("td").find("input:text[class*='bizPropsAddValue']").val();
				_obj["comments"]=$(this).parentsUntil("td").parent().siblings("td").find("input:text[class*='bizPropsComments']").val();
				
				if(jsons[splitName]==null){
					var _split={};
					_split[$(this).val()]=_obj;
					jsons[splitName]=_split;
				}else{
					jsons[splitName][$(this).val()]=_obj;
				}
				
				
				_jsons[$(this).val()]=_obj;
				
			});
		});
		return flag;
	}else{
		return false;
	}
}
function saveConfiInfoForDev(){
	if(checkNull()){
		if(shardIndex<=0){
			alert("必须存在设置为索引分区的分区信息");
			return false;
		}
		submitJson.shardIndex=shardIndex;
		saveDssInfoForDev();
		saveIndexInfoForDev();
		saveSegmentInfoForDev();
		saveShardInfoForDev();
		return true;
	}else{
		return false;
	}
}
function saveShardInfoForDev(){
	//清理JSON
	for(var i in submitJson.dataSources){
		delete submitJson.dataSources[i];
	}
	$("div.shardInfo:visible").each(function(){
		var obj=$(this);
		var shardKey=obj.find("input:hidden[name='shardKey']").val();
		//var beanClass="autonavi.online.framework.jdbc.datasource.LoadBalancingDataSource";
		var strategyName=obj.find("#strategyName").val();
		submitJson.dataSources[shardKey]={};
		//submitJson.dataSources[shardKey]["beanClass"]=beanClass;
		submitJson.dataSources[shardKey]["props"]={};
		submitJson.dataSources[shardKey]["props"]["strategyName"]=strategyName;
		var _tempArray=new Array();
		if(strategyName.indexOf("w")==0){
			$.each(obj.find("input:hidden[name='realDss']"),function(i,n){
				var ds=$(n).val();
				var weight=$(n).siblings("span").find("input[name^='"+ds+"_']").val();
				_tempArray.push(ds+"?weight="+weight);
			});
		}else{
			$.each(obj.find("input:hidden[name='realDss']"),function(i,n){
				_tempArray.push($(n).val());
			});
		}
		submitJson.dataSources[shardKey]["props"]["realDss"]=_tempArray;
	});
}
function saveDssInfoForDev(){
	//清理JSON
	for(var i in submitJson.realDataSources){
		delete submitJson.realDataSources[i];
	}
	$("div.dssInfo:visible").each(function(){
		var obj=$(this);
		//拼接通用参数
		var name=obj.find("input:hidden[name='name']").val();
		var beanClass=obj.find("input:hidden[name='beanClass']").val();
		submitJson.realDataSources[name]={};
		submitJson.realDataSources[name]["beanClass"]=beanClass;
		submitJson.realDataSources[name]["props"]={};
		//根据数据源类型分别处理
		if(obj.attr("class").indexOf("C3p0")!=-1){
			processSaveC3p0ForDev(obj,name);
		}else if(obj.attr("class").indexOf("Jta")!=-1){
			processSaveJta0ForDev(obj,name);
		}
	});
}
function saveIndexInfoForDev(){
	//清理JSON
	delete submitJson.indexTableMap;
	//初始化
	submitJson.indexTableMap={};
	//查找索引表
	$("div.indexTableInfo:visible").each(function(){
		var obj=$(this);
		var name=obj.find("input:hidden[name='name']").val();
		submitJson.indexTableMap[name]=[];
		//查找每一行
		var i=0;
		obj.find("input:text[name='name']").each(function(){
			submitJson.indexTableMap[name][i]={};
			submitJson.indexTableMap[name][i]["name"]=$(this).val();
			submitJson.indexTableMap[name][i]["columnType"]=$(this).parent().siblings().find("input:text[name='columnType']").val();
			submitJson.indexTableMap[name][i]["columnName"]=$(this).parent().siblings().find("input:text[name='columnName']").val();
			submitJson.indexTableMap[name][i]["length"]=$(this).parent().siblings().find("input:text[name='length']").val();
			i++;
		});
		//alert(JSON.stringify(submitJson));
	});
}
function saveSegmentInfoForDev(){
	//清理JSON
	delete submitJson.segmentTables;
	//初始化
	submitJson.segmentTables=[];
	//查找分表
	var i=0;
	$("div.segmentTableInfo:visible").each(function(){
		var obj=$(this);
		var name=obj.find("input:hidden[name='name']").val();
		submitJson.segmentTables[i]={};
		submitJson.segmentTables[i]["name"]=name
		//查找每一行
		
		obj.find("input:text[name='count']").each(function(){
			submitJson.segmentTables[i]["count"]=$(this).val();
		});
		i++;
	});
	//alert(JSON.stringify(submitJson));
}
function processSaveC3p0ForDev(obj,dsKey){
	//所有有名称的
	obj.find("input:text[name]").each(function(){
		submitJson.realDataSources[dsKey]["props"][$(this).attr("name")]=$(this).val();
	});
	//密码
	obj.find("input:password[name]").each(function(){
		submitJson.realDataSources[dsKey]["props"][$(this).attr("name")]=$(this).val();
	});
	//所有用户自己添加的
	obj.find("input:text[class*='dssPropsAddKey']").each(function(){
		submitJson.realDataSources[dsKey]["props"][$(this).val()]=$(this).parent().siblings("td").find("input:text[class*='dssPropsAddValue']").val();
	});
}
function processSaveJta0ForDev(obj,dsKey){
	submitJson.realDataSources[dsKey]["props"]["xaProperties"]={};
	//所有有名称的
	obj.find("input:text[name]").each(function(){
		//特殊处理
		if($(this).attr("name")=="URL"||$(this).attr("name")=="user"||$(this).attr("name")=="password"){
			submitJson.realDataSources[dsKey]["props"]["xaProperties"][$(this).attr("name")]=$(this).val();
		}else{
			submitJson.realDataSources[dsKey]["props"][$(this).attr("name")]=$(this).val();
		}
		
	});
	//密码
	obj.find("input:password[name]").each(function(){
		submitJson.realDataSources[dsKey]["props"]["xaProperties"][$(this).attr("name")]=$(this).val();
	});
	//所有用户自己添加的
	obj.find("input:text[class*='dssPropsAddKey']").each(function(){
		submitJson.realDataSources[dsKey]["props"][$(this).val()]=$(this).parent().siblings("td").find("input:text[class*='dssPropsAddValue']").val();
	});
}
