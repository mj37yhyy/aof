package autonavi.online.framework.support.zookeeper;

import autonavi.online.framework.support.zookeeper.des.Cryptor;

public class ZooKeeperProp {
	private String address;
	private int sessionTimeout;
	private String projectName;
	private String password;
	
	static{
		Cryptor.RC4Init("4cea79d4-3406-463f-9593-09cc9264cb82");
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
//		this.projectName = DesUtils.desDecode(projectName);
		this.projectName = projectName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
//		this.password =  DesUtils.desDecode(password);
		this.password = password;
	}
	
	

}
