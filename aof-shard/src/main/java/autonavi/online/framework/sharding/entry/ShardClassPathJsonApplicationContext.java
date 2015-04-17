package autonavi.online.framework.sharding.entry;

import java.io.InputStream;

public class ShardClassPathJsonApplicationContext extends
		AbstactApplicationContext {

	public ShardClassPathJsonApplicationContext(String path) throws Exception {
		super(path);
	}

	@Override
	protected InputStream getJson(String path) throws Exception {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}

}
