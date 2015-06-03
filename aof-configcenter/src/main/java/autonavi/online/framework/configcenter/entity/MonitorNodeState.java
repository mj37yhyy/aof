package autonavi.online.framework.configcenter.entity;

/**
 * 监控节点状态信息
 * @author chunsheng.zhang
 *
 */
public class MonitorNodeState {
	
	/**
	 * 监控节点名称
	 */
	private String monitorNodeName;

	/**
	 * 更新时间
	 */
	private Long uTime;
	
	
	/**
	 * 创建时间
	 */
	private Long cTime;

	public String getMonitorNodeName() {
		return monitorNodeName;
	}

	public void setMonitorNodeName(String monitorNodeName) {
		this.monitorNodeName = monitorNodeName;
	}

	public Long getuTime() {
		return uTime;
	}

	public void setuTime(Long uTime) {
		this.uTime = uTime;
	}

	public Long getcTime() {
		return cTime;
	}

	public void setcTime(Long cTime) {
		this.cTime = cTime;
	}
	
}
