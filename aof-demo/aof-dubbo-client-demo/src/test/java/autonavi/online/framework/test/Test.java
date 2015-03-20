package autonavi.online.framework.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class Test {
	public static void main(String[] s) {
		URLClassLoader loader = null;
		try { // create a URLClassLoader
			URL[] urls = new URL[1];
			URLStreamHandler streamHandler = null;
			File classPath = new File(
					"D:/online.framework.core.api-2.0.0-SNAPSHOT.jar");
			// the forming of repository is taken from the
			// createClassLoader method in
			// org.apache.catalina.startup.ClassLoaderFactory
			String repository = (new URL("file", null,
					classPath.getCanonicalPath() + File.separator)).toString();
			// the code for forming the URL is taken from
			// the addRepository method in
			// org.apache.catalina.loader.StandardClassLoader.
			urls[0] = new URL("file:/D:/online.framework.core.api-2.0.0-SNAPSHOT.jar");
			loader = new URLClassLoader(urls);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		Class myClass = null;
		try {
			myClass = loader
					.loadClass("autonavi.online.framework.cc.CcBaseEntity");
			System.out.println(myClass.getName());
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}
	}

}
