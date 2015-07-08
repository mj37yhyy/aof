package autonavi.online.framework.cc.config;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import autonavi.online.framework.constant.Miscellaneous;

public class ConfigMyidFile {
	private Log log = LogFactory.getLog(this.getClass());

	public void readMyId() {
		int myid = 0;
		InputStream is = getClass().getResourceAsStream("/myid");
		if (is != null) {
			try {
				myid = Integer.valueOf(IOUtils.toString(is).trim());
			} catch (IOException e) {
				log.error("读取myid文件错误，启动失败");
				System.exit(0);
			} catch (NumberFormatException e) {
				log.error("读取myid的内容不是数字，启动失败");
				System.exit(0);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			log.error("myid文件不存在，启动失败");
			System.exit(0);
		}
		if (!Miscellaneous.setMyid(myid)) {
			log.error(String.format("myid的内容必须在%d-%d之间，启动失败", Miscellaneous.minNodeKey,
					Miscellaneous.maxNodeKey));
			System.exit(0);
		}
	}
}
