package autonavi.online.framework.util.javassist;

public class JavassistClassInfo {
	String[] importPackages = null;
	String superclass = null;
	String[] interfaces = null;
	String clazz = null;
	String constructor = null;
	String[] fields = null;
	String[] methods = null;

	public String[] getImportPackages() {
		return importPackages;
	}

	public void setImportPackages(String... importPackages) {
		this.importPackages = importPackages;
	}

	public String getSuperclass() {
		return superclass;
	}

	public void setSuperclass(String superclass) {
		this.superclass = superclass;
	}

	public String[] getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(String... interfaces) {
		this.interfaces = interfaces;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getConstructor() {
		return constructor;
	}

	public void setConstructor(String constructor) {
		this.constructor = constructor;
	}

	public String[] getFields() {
		return fields;
	}

	public void setFields(String... fields) {
		this.fields = fields;
	}

	public String[] getMethods() {
		return methods;
	}

	public void setMethods(String... methods) {
		this.methods = methods;
	}

}
