/*
 *    Copyright 2009-2012 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package autonavi.online.framework.sharding.entry.xml.builder.support.xmltags;

import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.BoundSql;
import autonavi.online.framework.sharding.entry.xml.builder.support.mapping.SqlSource;

public class DynamicSqlSource implements SqlSource {

	// private Configuration configuration;
	private SqlNode rootSqlNode;

	public DynamicSqlSource(SqlNode rootSqlNode) {
		// this.configuration = configuration;
		this.rootSqlNode = rootSqlNode;
	}

	public BoundSql getBoundSql(Object parameterObject) {
		DynamicContext context = new DynamicContext(parameterObject);
		rootSqlNode.apply(context);
		BoundSql boundSql = new BoundSql(context.getSql());
		return boundSql;
	}

}
