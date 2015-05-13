public ${returnType} ${id}(${parameterStrings}){
	DaoEntity daoEntity = new DaoEntity();
	${returnType} result = null;
	
	try {
		SqlSource sqlSource = sqlSession.getSqlSource(this.getClass().getInterfaces()[0],"${sqlid}");
		
		Map parameterMap = new HashMap();// 存放所有可能的参数
		<#list paramNames as paramName>
		parameterMap.put("${paramName}",${paramName});
		</#list>
		
		BoundSql boundSql = sqlSource.getBoundSql(parameterMap);
		String sql = boundSql.getSql();
		
		daoEntity.setParameterMap(parameterMap);
		daoEntity.setSql(sql);
		daoEntity.setAuthor("${author}");
		daoEntity.setIndexName("${indexName}");
		daoEntity.setIndexColumn("${indexColumn}".split(","));
		<#if dataSourceKeyName!="">
		daoEntity.setSingleDataSourceKey(PropertyUtils.getValue(parameterMap, ${dataSourceKeyName}));
		</#if>
		<#if dataSourceKey!="">
		daoEntity.setSingleDataSourceKey(${dataSourceKey});
		</#if>
		<#if resultCallBack!="">
		daoEntity.setCallback(${resultCallBack});
		</#if>
		<#if skip!="">
		daoEntity.setStartOrSkip("${skip}");
		</#if>
		<#if size!="">
		daoEntity.setEndOrRowSize("${size}");
		</#if>
		<#if collectionType!="">
		daoEntity.setCollectionType(CollectionType.${collectionType});
		</#if>
		<#if resultType!="">
		daoEntity.setResultType(${resultType}.class);
		</#if>
		daoEntity.setQueryCount(${queryCount?c});
		daoEntity.setShardingHandle(new ${shardingHandle}());
		
		result = (${returnType})this.daoSupport.execute(daoEntity, ${tableOperation});
	} catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	}
	return result;
}