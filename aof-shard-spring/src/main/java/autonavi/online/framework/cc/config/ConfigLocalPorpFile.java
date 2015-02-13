package autonavi.online.framework.cc.config;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.constant.Miscellaneous;
import autonavi.online.framework.util.json.JsonBinder;

public class ConfigLocalPorpFile {
	private Logger log = LogManager.getLogger(getClass());

	/**
	 * 读取本地属性文件<br/>
	 * 这个方法主要是为那些需要不同配置的方法准备的，如比本机的地址
	 */
	public void readLocalPorpFile() {
		String json = null;
		InputStream is = getClass().getResourceAsStream("/aof.json");
		if (is != null) {
			try {
				json = IOUtils.toString(is);
			} catch (IOException e) {
				log.error("读取本地属性文件错误，启动失败");
				System.exit(0);
			} finally {
				IOUtils.closeQuietly(is);
			}
			JsonBinder binder = JsonBinder.buildNonDefaultBinder();
			binder.setDateFormat("yyyy-MM-dd HH:mm:ss");
			binder.fromJson(json, Miscellaneous.class);// 将json文件中的属性写入类中
		}
	}
}
