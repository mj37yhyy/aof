package autonavi.online.framework.sharding.transaction;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

public class TransactionAttribute {
	String name = null;
	Propagation propagation = Propagation.REQUIRED;
	Isolation isolation = Isolation.DEFAULT;
	int timeout = TransactionDefinition.TIMEOUT_DEFAULT;
	boolean readOnly = false;
	Class[] rollbackFor = {};
	String[] rollbackForClassName = {};
	Class[] noRollbackFor = {};
	String[] noRollbackForClassName = {};

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Propagation getPropagation() {
		return propagation;
	}

	public void setPropagation(String propagation) {
		if (propagation.equals("REQUIRED")) {
			this.propagation = Propagation.REQUIRED;
		} else if (propagation.equals("MANDATORY")) {
			this.propagation = Propagation.MANDATORY;
		} else if (propagation.equals("NESTED")) {
			this.propagation = Propagation.NESTED;
		} else if (propagation.equals("NEVER")) {
			this.propagation = Propagation.NEVER;
		} else if (propagation.equals("NOT_SUPPORTED")) {
			this.propagation = Propagation.NOT_SUPPORTED;
		} else if (propagation.equals("REQUIRES_NEW")) {
			this.propagation = Propagation.REQUIRES_NEW;
		} else if (propagation.equals("SUPPORTS")) {
			this.propagation = Propagation.SUPPORTS;
		}
	}

	public void setPropagation(Propagation propagation) {
		this.propagation = propagation;
	}

	public Isolation getIsolation() {
		return isolation;
	}

	public void setIsolation(String isolation) {
		if (isolation.equals("DEFAULT")) {
			this.isolation = Isolation.DEFAULT;
		} else if (isolation.equals("READ_COMMITTED")) {
			this.isolation = Isolation.READ_COMMITTED;
		} else if (isolation.equals("READ_UNCOMMITTED")) {
			this.isolation = Isolation.READ_UNCOMMITTED;
		} else if (isolation.equals("REPEATABLE_READ")) {
			this.isolation = Isolation.REPEATABLE_READ;
		} else if (isolation.equals("SERIALIZABLE")) {
			this.isolation = Isolation.SERIALIZABLE;
		}
	}

	public void setIsolation(Isolation isolation) {
		this.isolation = isolation;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Class[] getRollbackFor() {
		return rollbackFor;
	}

	public void setRollbackFor(Class[] rollbackFor) {
		this.rollbackFor = rollbackFor;
	}

	public void setRollbackFor(String rollbackFor) {
		this.rollbackForClassName = rollbackFor.split(",");
	}

	public String[] getRollbackForClassName() {
		return rollbackForClassName;
	}

	public void setRollbackForClassName(String[] rollbackForClassName) {
		this.rollbackForClassName = rollbackForClassName;
	}

	public Class[] getNoRollbackFor() {
		return noRollbackFor;
	}

	public void setNoRollbackFor(Class[] noRollbackFor) {
		this.noRollbackFor = noRollbackFor;
	}

	public void setNoRollbackFor(String noRollbackFor) {
		this.noRollbackForClassName = noRollbackFor.split(",");
	}

	public String[] getNoRollbackForClassName() {
		return noRollbackForClassName;
	}

	public void setNoRollbackForClassName(String[] noRollbackForClassName) {
		this.noRollbackForClassName = noRollbackForClassName;
	}
	/**
	 * Return the depth of the superclass matching.
	 * <p>{@code 0} means {@code ex} matches exactly. Returns
	 * {@code -1} if there is no match. Otherwise, returns depth with the
	 * lowest depth winning.
	 */
	public int getRollBackDepth(Throwable ex) {
		return getRollBackDepth(ex.getClass(), 0);
	}


	private int getRollBackDepth(Class<?> exceptionClass, int depth) {
		if(this.getNoRollbackForClassName().length==0&&this.getNoRollbackFor().length==0){
			return -2;
		}
		for(String clazz:this.getRollbackForClassName()){
			if (exceptionClass.getName().indexOf(clazz) != -1) {
				// Found it!
				return depth;
			}
			// If we've gone as far as we can go and haven't found it...
			if (exceptionClass.equals(Throwable.class)) {
				return -1;
			}
			
		}
		for(Class<?> clazz:this.getRollbackFor()){
			if (exceptionClass.getName().indexOf(clazz.getName()) != -1) {
				// Found it!
				return depth;
			}
			// If we've gone as far as we can go and haven't found it...
			if (exceptionClass.equals(Throwable.class)) {
				return -1;
			}
		}
		return getRollBackDepth(exceptionClass.getSuperclass(), depth + 1);
		
	}
	/**
	 * Return the depth of the superclass matching.
	 * <p>{@code 0} means {@code ex} matches exactly. Returns
	 * {@code -1} if there is no match. Otherwise, returns depth with the
	 * lowest depth winning.
	 */
	public int getNoRollBackDepth(Throwable ex) {
		return getNoRollBackDepth(ex.getClass(), 0);
	}


	private int getNoRollBackDepth(Class<?> exceptionClass, int depth) {
		if(this.getNoRollbackForClassName().length==0&&this.getNoRollbackFor().length==0){
			return -2;
		}
		for(String clazz:this.getNoRollbackForClassName()){
			if (exceptionClass.getName().indexOf(clazz) != -1) {
				// Found it!
				return depth;
			}
			// If we've gone as far as we can go and haven't found it...
			if (exceptionClass.equals(Throwable.class)) {
				return -1;
			}
		}
		for(Class<?> clazz:this.getNoRollbackFor()){
			if (exceptionClass.getName().indexOf(clazz.getName()) != -1) {
				// Found it!
				return depth;
			}
			// If we've gone as far as we can go and haven't found it...
			if (exceptionClass.equals(Throwable.class)) {
				return -1;
			}
		}
		return getNoRollBackDepth(exceptionClass.getSuperclass(), depth + 1);
		
	}
	public boolean rollbackOn(Throwable ex){
		TransactionAttribute ta=null;
		int deepest = Integer.MAX_VALUE;
		int depth = this.getNoRollBackDepth(ex);
		//判断no-rollback
		if (depth >= 0 && depth < deepest) {
			deepest = depth;
			ta = this;
		}
		if(ta!=null){
			return false;
		}else{
			depth = this.getRollBackDepth(ex);
			//判断rollback
			if (depth >= 0 && depth < deepest) {
				deepest = depth;
				ta = this;
			}
			if(ta==null){
				return (ex instanceof java.lang.RuntimeException||ex instanceof java.lang.Error);
			}
			return true;
		}
	}

}
