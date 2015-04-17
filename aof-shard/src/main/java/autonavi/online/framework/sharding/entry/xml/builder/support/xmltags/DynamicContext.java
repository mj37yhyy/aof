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

import java.util.HashMap;
import java.util.Map;

import ognl.OgnlException;

public class DynamicContext {

	public static final String PARAMETER_OBJECT_KEY = "_parameter";

	private final Map bindings = new HashMap();
	private final StringBuilder sqlBuilder = new StringBuilder();
	private int uniqueNumber = 0;

	public DynamicContext(Object parameterObject) {
		bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
	}

	public Map<String, Object> getBindings() {
		return bindings;
	}

	public void bind(String name, Object value) {
		bindings.put(name, value);
	}

	public void appendSql(String sql) {
		sqlBuilder.append(sql);
		sqlBuilder.append(" ");
	}

	public String getSql() {
		return sqlBuilder.toString().trim();
	}

	public int getUniqueNumber() {
		return uniqueNumber++;
	}

	public void setProperty(Map context, Object target, Object name,
			Object value) throws OgnlException {
		Map map = (Map) target;
		map.put(name, value);

	}
}