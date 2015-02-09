package autonavi.online.framework.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 计数器
 * 
 * @author jia.miao
 * 
 */
public class AtomicCounter {
	private AtomicLong counter = new AtomicLong(0);

	public long getCount() {
		return counter.get();
	}

	public void counterIncrement() {
		for (;;) {
			long current = counter.get();
			long next = current + 1;
			if (counter.compareAndSet(current, next))
				return;
		}
	}

	public void counterDecrease() {
		for (;;) {
			long current = counter.get();
			long next = current - 1;
			if (counter.compareAndSet(current, next))
				return;
		}
	}

}
