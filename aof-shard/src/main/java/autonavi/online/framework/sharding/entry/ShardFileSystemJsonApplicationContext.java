package autonavi.online.framework.sharding.entry;

import java.io.FileInputStream;
import java.io.InputStream;

public class ShardFileSystemJsonApplicationContext extends
		AbstactApplicationContext {

	public ShardFileSystemJsonApplicationContext(String path) throws Exception {
		super(path);
	}

	@Override
	protected InputStream getJson(String path) throws Exception {
		return new FileInputStream(path);
	}

}
