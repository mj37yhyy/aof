package autonavi.online.framework.support.zookeeper.entity;

/**
 * 返回最基本的属于源信息 链接 驱动 用户名 密码 是否为索引数据源 是否启用
 * 
 * @author Xuyaming-iMac
 * 
 */
public class DataSourceEntity {
	private String driver;
	private String url;
	private String user;
	private String password;
	private boolean index;
	private boolean active;

	public boolean isIndex() {
		return index;
	}

	public void setIndex(boolean index) {
		this.index = index;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
