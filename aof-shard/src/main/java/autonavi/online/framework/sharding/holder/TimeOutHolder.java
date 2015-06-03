package autonavi.online.framework.sharding.holder;

import autonavi.online.framework.util.ConcurrentStack;

/**
 * 超时时间Holder
 * 
 * @author yaming.xu
 * 
 */
public class TimeOutHolder {
	private static final ThreadLocal<ConcurrentStack<Integer>> timeOutStackHolder = new ThreadLocal<ConcurrentStack<Integer>>();
	
	
	public static void cleanAllHolder(){
		timeOutStackHolder.remove();
	}

	public static Integer getTimeOut() {
		Integer time = -1;
		if (timeOutStackHolder.get() == null) {
			timeOutStackHolder.set(new ConcurrentStack<Integer>());
		}
		if (timeOutStackHolder.get().size() > 0) {
			time = timeOutStackHolder.get().pop();
			timeOutStackHolder.get().push(time);
		}
		return time;
	}

	public static void setTimeOut(Integer time) {
		if (timeOutStackHolder.get() == null) {
			timeOutStackHolder.set(new ConcurrentStack<Integer>());
		}
		timeOutStackHolder.get().push(time);
	}

	public static void refreshTimeOut() {
		if (timeOutStackHolder.get() == null) {
			timeOutStackHolder.set(new ConcurrentStack<Integer>());
		}
		if (timeOutStackHolder.get().size() > 0) {
			timeOutStackHolder.get().pop();
		}
		
	}

}
