package autonavi.online.framework.util;



import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import autonavi.online.framework.util.StopWatch.TaskInfo;

public class StopWatchLogger {
	private Logger logger = LogManager.getLogger(getClass());
	private StopWatch clock;

	/**
	 * 任务名称
	 */
	private String taskName;

	/**
	 * 创建一个即时任务
	 */
	public StopWatchLogger(Class clazz) {
		this.logger = LogManager.getLogger(clazz);
		this.taskName = clazz.getName();
		clock = new StopWatch();
	}

	/**
	 * 开始一个子任务计时
	 * 
	 * @param taskName
	 *            ： 任务名
	 */
	public void start(String subTaskName) {
		if (!clock.isRunning())
			clock.start(subTaskName);
	}

	/**
	 * 停止计时
	 */
	public void stop() {
		if (clock.isRunning())
			clock.stop();
	}

	/**
	 * 输出执行时间
	 */
	public void writeLog() {
		for (TaskInfo task : clock.getTaskInfo()) {
			logger.info("方法[" + task.getTaskName() + "]耗费时间为："
					+ task.getTimeMillis() + "ms");
		}
	}

}
