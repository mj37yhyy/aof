<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping
  PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
         "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd">
 
	<hibernate-mapping>
    	<class
        name="${entity.name}"
        table="${entity.tableName}"
	        dynamic-update="false"
	        dynamic-insert="false"
	        select-before-update="false"
	        optimistic-lock="version">
	        
	        <#if entity.pk?exists>
	         <id
	            name="${entity.pk.name}"
	            column="${entity.pk.columnName}"
	            type="${entity.pk.columnType}">
	            <generator class="assigned" />
	        </id>
	        <#else>
	        <id
	            name="id"
	            column="ID"
	            type="long"
	            unsaved-value="null">
	            <generator class="assigned" />
	        </id>
	        </#if>
	        <#if entity.columnAttrList?exists>
	            <#list entity.columnAttrList as attr>
	                <#if attr.name == "id">
	                <#elseif attr.columnType=="string">
	        <property
	            name="${attr.name}"
	            type="java.lang.String"
	            update="true"
	            insert="true"
	            access="property"
	            column="${attr.columnName}"
	            length="${attr.length}"
	            not-null="false"
	            unique="false"
	        />
	        <#else>
	        <property
	            name="${attr.name}"
	            type="${attr.columnType}"
	            update="true"
	            insert="true"
	            access="property"
	            column="`${attr.columnName}`"
	            not-null="false"
	            unique="false"
	        />
	 
	                </#if>
	            </#list>
	        </#if>
	    </class>
	</hibernate-mapping>