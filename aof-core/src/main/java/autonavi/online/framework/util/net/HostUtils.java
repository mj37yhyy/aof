package autonavi.online.framework.util.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class HostUtils {

	/**
	 * 获取本机的Host，linux适用
	 * 
	 * @return 所有网卡的ipv4地址，但不包含localhost和127.0.0.1
	 * @throws SocketException
	 */
	public static List<String> getLocalHosts() throws SocketException {
		List<String> hosts = new ArrayList<String>();
		Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress inetAddress = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
					.nextElement();
			Enumeration addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				inetAddress = (InetAddress) addresses.nextElement();
				if (inetAddress != null && inetAddress instanceof Inet4Address) {
					String _host = inetAddress.getHostAddress();
					if (!netInterface.getName().equals("lo"))
						hosts.add(_host);
				}
			}
		}
		return hosts;
	}

	/**
	 * 获取本机的Host，linux适用
	 * 
	 * @param netInterfaceName
	 *            接口名，如eth0、lo等
	 * @return 指定网卡的ipv4地址
	 * @throws SocketException
	 */
	public static String getLocalHost(String netInterfaceName)
			throws SocketException {
		Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress inetAddress = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
					.nextElement();
			Enumeration addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				inetAddress = (InetAddress) addresses.nextElement();
				if (inetAddress != null && inetAddress instanceof Inet4Address) {
					String _host = inetAddress.getHostAddress();
					if (netInterface.getName().equals(netInterfaceName))
						return _host;
				}
			}
		}
		return null;
	}

	public static String getFristLocalHost() throws SocketException {
		return HostUtils.getLocalHosts().get(0);
	}
}
