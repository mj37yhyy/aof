package autonavi.online.framework.configcenter.commons;

import java.io.Serializable;

public class AppNode implements Serializable {
	private static final long serialVersionUID = 1474857732420013550L;
	private String nodeName;
	private Long updateTime;
	private String nodeVersion;
	private String nodeBizVersion;
	private Long systemTime;
	
	
	public String getNodeBizVersion() {
		return nodeBizVersion;
	}
	public void setNodeBizVersion(String nodeBizVersion) {
		this.nodeBizVersion = nodeBizVersion;
	}
	public Long getSystemTime() {
		return systemTime;
	}
	public void setSystemTime(Long systemTime) {
		this.systemTime = systemTime;
	}
	public String getNodeVersion() {
		return nodeVersion;
	}
	public void setNodeVersion(String nodeVersion) {
		this.nodeVersion = nodeVersion;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public Long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}
	

}
