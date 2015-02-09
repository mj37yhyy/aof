package autonavi.online.framework.message;

import io.netty.channel.ChannelHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import autonavi.online.framework.message.utils.tcp.TcpServer;
import autonavi.online.framework.util.net.HostUtils;

/**
 * 消息监听者
 * 
 * @author jia.miao
 * 
 */
public class MessageListener {

	/**
	 * 启动服务器
	 */
	public void startListener() {
		if (handlers != null && handlers.length > 0) {
			String ip = null;
			try {
				if (localhost != null) {
					ip = localhost;// 用户定义的IP地址
				} else if (netInterfaceName != null) {
					ip = HostUtils.getLocalHost(netInterfaceName);// 得到对应网卡的地址
				} else {
					ip = HostUtils.getFristLocalHost();// 得到第一个网卡的地址
				}
				log.info("正在使用地址【" + ip + "】启动监听服务。");
				new TcpServer().run(ip, port, this.handlers);// 启动服务
				log.info("监听服务启动成功！");
			} catch (Exception e) {
				e.printStackTrace();
				log.error("监听服务启动失败！即将结束启动线程。", e);
				System.exit(0);// 停止启动线程
			}
		} else {// 如果没有handler就不启动服务了
			log.warn("监听服务未检测到任何handler，系统将无法接收任何消息。");
		}
	}

	public static int port = 14820;
	private String localhost = null;
	private String netInterfaceName = null;
	private ChannelHandler[] handlers = {};
	private Logger log = LogManager.getLogger(getClass());

	public void setPort(int port) {
		if (port > 0)
			MessageListener.port = port;
	}

	public void setLocalhost(String localhost) {
		if (localhost != null)
			this.localhost = localhost;
	}

	public void setNetInterfaceName(String netInterfaceName) {
		if (netInterfaceName != null)
			this.netInterfaceName = netInterfaceName;
	}

	public void setHandlers(ChannelHandler[] handlers) {
		if (handlers != null && handlers.length > 0)
			this.handlers = handlers;
	}

}
