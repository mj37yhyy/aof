var maxSplitTime=60;
var startP=false;
var startC=true;
var startPIndex=0;
var submitAppNodes=[];
var preVersion="";
var comVersion="";

var modalStpOne="";
var modalStpTwo="";
var modalStpThree="";
var optype="";

var temp_name="";

function initModal(one,two,three,type){
	modalStpOne=one;
	modalStpTwo=two;
	modalStpThree=three;
	optype=type
}
function initSubmitAppNode(name,version1,version2){
	return {nodeName:name,updateTime:0,nodeVersion:version1,systemTime:0,nodeBizVersion:version2};
}
function deleteAppNode(name){
	var _index;
	$.each(submitAppNodes,function(i,n){
		if(n.nodeName==name){
			_index=i;
			return;
		}
	});
	return _index;
}
function cleanSubmitAppNode(){
	var _json=[];
	$.each(submitAppNodes,function(i,n){
		if(n!=null){
			_json.push(n);
		}
	});
	submitAppNodes=_json;
}
function setAppNodeVersion(version){
	$.each(submitAppNodes,function(i,n){
		n.nodeVersion=version;
	});
}
function cleanAppNodes(){
	//清理存在的节点内容
	$("#"+modalStpOne).find("table tr.content").remove();
	startP=false;
	startPIndex=0;
	submitAppNodes=[];
}
function generateAppNodes(fn){
	$.ajax({
		   type: "POST",
		   url: "../manager/init_check_appNode",
		   dataType:"json",
		   data:"" ,
		   success: function(msg){
		     if(msg.code==0){
		    	 processAppNode(msg);
		    	 fn();
		     }else{
		    	alert(msg.msg);
		     }
		   }
	});
}
function initCheckAppNodes(one,two,three,type,_temp){
	temp_name=_temp;
	initModal(one,two,three,type);
	cleanAppNodes();
	generateAppNodes(function(){
		bindButtonEventSelectNode();
		$("#"+modalStpOne).modal("show");
	});
}
function refreshAppNodes(){
	cleanAppNodes();
	generateAppNodes(function(){
	});
}
function processAppNode(msg){
	$.each(msg.result,function(i,n){
		var split=parseInt((parseInt(n.systemTime)-parseInt(n.updateTime))/1000);
		var content="";
		if(split<=maxSplitTime){
			startP=true;
			startPIndex++;
			content="<tr class='content' style='color:green'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-ok'></span></td><td><input type='checkbox' class='selectAppNode' value='"+n.nodeName+"' checked='checked'></td></tr>";
			var _json=initSubmitAppNode(n.nodeName,"","");
			submitAppNodes.push(_json);
		}
		else
			content="<tr class='content' style='color:red'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-remove'></span></td><td><input type='checkbox' value='"+n.nodeName+"' disabled='disabled'></td></tr>";
		
		$("#"+modalStpOne).find("table").append(content);
	});
	if(!startP){
		$("#"+modalStpOne).find("button.startPNode").prop("disabled","disabled");
	}else{
		$("#"+modalStpOne).find("button.startPNode").prop("disabled","");
	}
	bindEvent();
}
function bindEvent(){
	$("#"+modalStpOne).find("input:checkbox.selectAppNode").click(function(){
		if($(this).prop("checked")){
			$("#"+modalStpOne).find("button.startPNode").prop("disabled","");
			var _json=initSubmitAppNode($(this).val(),"","");
			submitAppNodes.push(_json);
			startPIndex++;
		}else{
			delete submitAppNodes[deleteAppNode($(this).val())];
			cleanSubmitAppNode();
			startPIndex--;
		}
		if(startPIndex==0){
			$("#"+modalStpOne).find("button.startPNode").prop("disabled","disabled");
		}
	});
}
//选择部署节点事件绑定
function bindButtonEventSelectNode(){
	$("#"+modalStpOne).find("button.startPNode").unbind();
	$("#"+modalStpOne).find("button.refreshNode").unbind();
	$("#"+modalStpOne).find("button.startPNode").click(function(){
		if(optype=="base"){
			$.ajax({
				   type: "POST",
				   url: "../manager/pre_active_dss",
				   dataType:"json",
				   contentType:"application/json",
				   data:JSON.stringify(submitAppNodes) ,
				   success: function(msg){
				     if(msg.code==0){
				    	 $("#"+modalStpOne).modal("hide");
				    	 initCommitAppNode(msg.result,true);
				    	 bindButtonEventCheckPNode();
				    	
				     }else{
				    	 alert(msg.msg);
				     }
				   }
			});
		}else if(optype=="biz"){
			$.ajax({
				   type: "POST",
				   url: "../manager/pre_active_biz",
				   dataType:"json",
				   contentType:"application/json",
				   data:JSON.stringify(submitAppNodes) ,
				   success: function(msg){
				     if(msg.code==0){
				    	 $("#"+modalStpOne).modal("hide");
				    	 initCommitAppNode(msg.result,true);
				    	 bindButtonEventCheckPNode();
				    	
				     }else{
				    	 alert(msg.msg);
				     }
				   }
			});
		}
		
	});
	$("#"+modalStpOne).find("button.refreshNode").click(function(){
		refreshAppNodes();
	});
}
//选择查看预激活状态事件绑定
function bindButtonEventCheckPNode(){
	$("#"+modalStpTwo).find("button.startCNode").unbind();
	$("#"+modalStpTwo).find("button.refreshNode").unbind();
	$("#"+modalStpTwo).find("button.refreshNode").click(function(){
			initCommitAppNode(preVersion,false);
	});
	$("#"+modalStpTwo).find("button.startCNode").click(function(){
		if(optype=="base"){
			$.ajax({
				   type: "POST",
				   url: "../manager/commit_active_dss/"+temp_name,
				   dataType:"json",
				   contentType:"application/json",
				   data:JSON.stringify(submitAppNodes) ,
				   success: function(msg){
				     if(msg.code==0){
				    	 setTimeout(function(){
				    		 $("#"+modalStpTwo).modal("hide");
					    	 initCompleteAppNode(msg.result,true);
					    	 bindButtonEventCompleteNode();
				    	 },1000);
				    	 
				    	
				     }else{
				    	 alert(msg.msg);
				     }
				   }
			});
		}else if(optype=="biz"){
			$.ajax({
				   type: "POST",
				   url: "../manager/commit_active_biz/"+temp_name,
				   dataType:"json",
				   contentType:"application/json",
				   data:JSON.stringify(submitAppNodes) ,
				   success: function(msg){
				     if(msg.code==0){
				    	 setTimeout(function(){
				    		 $("#"+modalStpTwo).modal("hide");
					    	 initCompleteAppNode(msg.result,true);
					    	 bindButtonEventCompleteNode();
				    	 },1000);
				    	 
				    	
				     }else{
				    	 alert(msg.msg);
				     }
				   }
			});
		}
		
	});
	
}
//
function bindButtonEventCompleteNode(){
	$("#"+modalStpThree).find("button.refreshNode").unbind();
	$("#"+modalStpThree).find("button.refreshNode").click(function(){
		initCompleteAppNode(comVersion,false);
    });
}
function initCommitAppNode(msg,isShow){
	preVersion=msg;
	$.ajax({
		   type: "POST",
		   url: "../manager/init_commit_appNode",
		   dataType:"json",
		   contentType:"application/json",
		   data:JSON.stringify(submitAppNodes) ,
		   success: function(msg){
		     if(msg.code==0){
		    	 processCommitAppNode(msg);
		    	 if(isShow){
		    		 $("#"+modalStpTwo).modal("show");
		    	 }
		    	 
		     }else{
		    	 alert(msg.msg);
		     }
		   }
	});
}
function processCommitAppNode(msg){
	$("#"+modalStpTwo).find("table tr.content").remove();
	startC=true;
	$.each(msg.result,function(i,n){
		var content="";
		if(optype=="base"){
			if(preVersion==n.nodeVersion){
				content="<tr class='content' style='color:green'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-ok'></span></td></tr>";
			}
			else{
				startC=false;
				content="<tr class='content' style='color:red'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-remove'></span></td></tr>";
			}
		}else if(optype="biz"){
			if(preVersion==n.nodeBizVersion){
				content="<tr class='content' style='color:green'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-ok'></span></td></tr>";
			}
			else{
				startC=false;
				content="<tr class='content' style='color:red'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-remove'></span></td></tr>";
			}
		}
		
			
		
		$("#"+modalStpTwo).find("table").append(content);
		setAppNodeVersion(preVersion);
	});
	if(!startC){
		$("#"+modalStpTwo).find("button.startCNode").prop("disabled","disabled");
	}else{
		$("#"+modalStpTwo).find("button.startCNode").prop("disabled","");
	}
}
function initCompleteAppNode(msg,isShow){
	comVersion=msg;
	$.ajax({
		   type: "POST",
		   url: "../manager/check_commit_appNode",
		   dataType:"json",
		   contentType:"application/json",
		   data:JSON.stringify(submitAppNodes) ,
		   success: function(msg){
		     if(msg.code==0){
		    	 processCompleteAppNode(msg);
		    	 if(isShow){
		    		 $("#"+modalStpThree).modal("show");
		    	 }
		    	 
		     }else{
		    	 alert(msg.msg);
		     }
		   }
	});
	
}
function processCompleteAppNode(msg){
	$("#"+modalStpThree).find("table tr.content").remove();
	$.each(msg.result,function(i,n){
		var content="";
		if(optype=="base"){
			if(comVersion==n.nodeVersion){
				content="<tr class='content' style='color:green'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-ok'></span></td></tr>";
			}
			else{
				content="<tr class='content' style='color:red'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-remove'></span></td></tr>";
			}
		}else if(optype=="biz"){
			if(comVersion==n.nodeBizVersion){
				content="<tr class='content' style='color:green'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-ok'></span></td></tr>";
			}
			else{
				content="<tr class='content' style='color:red'><td>"+n.nodeName+"</td><td><span class='glyphicon glyphicon-remove'></span></td></tr>";
			}
		}
		
			
		
		$("#"+modalStpThree).find("table").append(content);
	});
}

