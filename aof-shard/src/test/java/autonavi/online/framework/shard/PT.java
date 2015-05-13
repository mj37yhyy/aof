package autonavi.online.framework.shard;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class PT {
	boolean ok;
	boolean mTime;
	String eName;

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public boolean ismTime() {
		return mTime;
	}

	public void setmTime(boolean mTime) {
		this.mTime = mTime;
	}
	
	public static void main(String[] args) throws IntrospectionException{
		String name="eName";
		String getterMethodName = "is" + name;
		String setterMethodName = "set" + name;
		Method getterMethod = new PropertyDescriptor(name,
				PT.class, getterMethodName,
				setterMethodName).getReadMethod();
		System.out.println(getterMethod.getName());
	}

	public String geteName() {
		return eName;
	}

	public void seteName(String eName) {
		this.eName = eName;
	}
}
