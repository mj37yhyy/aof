package autonavi.online.framework.cc.entity;

public class CcDaoEntity {
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	private String id;
	private String className;

	@Override
	public String toString() {
		return "DaoEntity [id=" + id + ", className=" + className + "]";
	}
}
