package autonavi.online.framework.util.classreading;

import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ClassMetadataVisitor extends ClassVisitor implements Opcodes,
		ClassMetadata {

	private String className;

	private boolean isInterface;

	private boolean isAbstract;

	private boolean isFinal;

	private String enclosingClassName;

	private boolean independentInnerClass;

	private String superClassName;

	private String[] interfaces;

	private Set<String> memberClassNames = new LinkedHashSet<String>();

	public ClassMetadataVisitor(int api) {
		super(api);
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.className = this.convertResourcePathToClassName(name);
		this.isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
		this.isAbstract = ((access & Opcodes.ACC_ABSTRACT) != 0);
		this.isFinal = ((access & Opcodes.ACC_FINAL) != 0);
		if (superName != null) {
			this.superClassName = this
					.convertResourcePathToClassName(superName);
		}
		this.interfaces = new String[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			this.interfaces[i] = this
					.convertResourcePathToClassName(interfaces[i]);
		}
	}

	public void visitOuterClass(String owner, String name, String desc) {
		this.enclosingClassName = this.convertResourcePathToClassName(owner);
	}

	public void visitInnerClass(String name, String outerName,
			String innerName, int access) {
		if (outerName != null) {
			String fqName = this.convertResourcePathToClassName(name);
			String fqOuterName = this.convertResourcePathToClassName(outerName);
			if (this.className.equals(fqName)) {
				this.enclosingClassName = fqOuterName;
				this.independentInnerClass = ((access & Opcodes.ACC_STATIC) != 0);
			} else if (this.className.equals(fqOuterName)) {
				this.memberClassNames.add(fqName);
			}
		}
	}

	public String convertResourcePathToClassName(String resourcePath) {
		return resourcePath.replace('/', '.');
	}

	@Override
	public String getClassName() {
		return this.className;
	}

	@Override
	public boolean isInterface() {
		return this.isInterface;
	}

	@Override
	public boolean isAbstract() {
		return this.isAbstract;
	}

	@Override
	public boolean isConcrete() {
		return !(this.isInterface || this.isAbstract);
	}

	@Override
	public boolean isFinal() {
		return this.isFinal;
	}

	@Override
	public boolean isIndependent() {
		return (this.enclosingClassName == null || this.independentInnerClass);
	}

	@Override
	public boolean hasEnclosingClass() {
		return (this.enclosingClassName != null);
	}

	@Override
	public String getEnclosingClassName() {
		return this.enclosingClassName;
	}

	@Override
	public boolean hasSuperClass() {
		return (this.superClassName != null);
	}

	@Override
	public String getSuperClassName() {
		return this.superClassName;
	}

	@Override
	public String[] getInterfaceNames() {
		return this.interfaces;
	}

	@Override
	public String[] getMemberClassNames() {
		return this.memberClassNames.toArray(new String[this.memberClassNames
				.size()]);
	}

}