var setting = {	};

		var zNodes =[
			{ name:"demo - 应用名称", open:true,
				children: [
					{ name:"base - 新框架核心配置，如数据源，索引库，索引表，分库，分表配置 (固定节点名称)", open:true,
						children: [
							{ name:"dss - 数据源配置信息 (固定节点名称)",open:true,
							   children : [
							    { name:"c3p0DataSource1 - 数据源唯一名称",open:true,
								   children : [
								     { name:"jdbcUrl - 数据源详细属性"},
									 { name:"...... - 数据源详细属性"}
								]},
							    { name:"c3p0DataSource2 - 数据源唯一名称",open:true,
								   children : [
								     { name:"jdbcUrl - 数据源详细属性"},
									 { name:"...... - 数据源详细属性"}
								]}
							]},
							{ name:"shard --分库分表配置信息 (固定节点名称)" ,open:true,
							  children :[
							    { name:"index - 索引表和索引库信息 (固定节点名称)",open:true,
								   children : [
								     {name:"tables - 索引表信息 (固定节点名称)",open:true,

									    children : [
										   {name:"demo_index - 索引表的名字",open:true,
										      children:[
											    {name:"name - 索引字段名字",open:true,
												   children:[
												       { name:"type - 字段类型 (固定节点名称)"},
													   { name:"length - 字段长度 (固定节点名称)"}
												]}
										   ]}
									 ]},
									 {name:"ds - 索引库的数据源名称 (固定节点名称)"}
								]},
							    { name:"dss - 数据源编号信息（1-32）(固定节点名称)",open:true,
								   children: [
								        { name:"1  - 数据源编号"},
										{ name:"2  - 数据源编号"}
								]},
							    { name:"segment-tables  - 分表信息 (固定节点名称)",open:true,
								   children:[
								      {name:"demo - 表名称"}
								]}
							]}
						]},
					{ name:"biz - Properties信息(固定节点名称)", open:true,
						children: [
							{ name:"Prop1 - 自定义配置"},
							{ name:"Prop2 - 自定义配置"},
							{ name:"Prop3 - 自定义配置"},
							{ name:"Prop4 - 自定义配置"}
						]}
				]}

		];

		$(document).ready(function(){
			$.fn.zTree.init($("#treeDemo"), setting, zNodes);
		});