package autonavi.online.framework.util;

import java.io.IOException;
import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class MethodUtils {
	/**
	 * <p>
	 * 获取方法参数名称
	 * </p>
	 * 
	 * @param cm
	 * 
	 * @return
	 * @throws Exception
	 */

	protected static String[] getMethodParamNames4Javassist(CtMethod cm)
			throws Exception {
		CtClass cc = cm.getDeclaringClass();
		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		if (attr == null) {
			throw new Exception(cc.getName());
		}
		String[] paramNames = null;
		try {
			paramNames = new String[cm.getParameterTypes().length];
		} catch (NotFoundException e) {
			throw e;
		}
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		for (int i = 0; i < paramNames.length; i++) {
			paramNames[i] = attr.variableName(i + pos);
		}
		return paramNames;
	}

	/**
	 * 
	 * 获取方法参数名称，按给定的参数类型匹配方法
	 * 
	 * @param clazz
	 * @param method
	 * @param paramTypes
	 * 
	 * @return
	 */

	public static String[] getMethodParamNames4Javassist(Class<?> clazz,
			String method, Class<?>... paramTypes) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = null;
		CtMethod cm = null;
		try {
			cc = pool.get(clazz.getName());
			String[] paramTypeNames = new String[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++)
				paramTypeNames[i] = paramTypes[i].getName();
			cm = cc.getDeclaredMethod(method, pool.get(paramTypeNames));
		} catch (NotFoundException e) {
			throw e;
		}
		return getMethodParamNames4Javassist(cm);
	}

	/**
	 * 
	 * 获取方法参数名称，匹配同名的某一个方法
	 * 
	 * @param clazz
	 * @param method
	 * @return
	 * 
	 * @throws NotFoundException
	 *             如果类或者方法不存在
	 * @throws MissingLVException
	 *             如果最终编译的class文件不包含局部变量表信息
	 */
	public static String[] getMethodParamNames4Javassist(Class<?> clazz,
			String method) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass cc;
		CtMethod cm = null;
		try {
			cc = pool.get(clazz.getName());
			cm = cc.getDeclaredMethod(method);
		} catch (NotFoundException e) {
			throw e;
		}
		return getMethodParamNames4Javassist(cm);
	}

	/**
	 * 
	 * 
	 * 
	 * <p>
	 * 比较参数类型是否一致
	 * </p>
	 * 
	 * 
	 * 
	 * @param types
	 *            asm的类型({@link Type})
	 * 
	 * @param clazzes
	 *            java 类型({@link Class})
	 * 
	 * @return
	 */

	private static boolean sameType(Type[] types, Class<?>[] clazzes) {

		// 个数不同

		if (types.length != clazzes.length) {

			return false;

		}

		for (int i = 0; i < types.length; i++) {

			if (!Type.getType(clazzes[i]).equals(types[i])) {

				return false;

			}

		}

		return true;

	}

	/**
	 * 
	 * 
	 * 
	 * <p>
	 * 获取方法的参数名
	 * </p>
	 * 
	 * 
	 * 
	 * @param m
	 * 
	 * @return
	 * @throws IOException
	 */

	public static String[] getMethodParamNames4Asm(final Method m)
			throws IOException {
		final String[] paramNames = new String[m.getParameterTypes().length];
		final String n = m.getDeclaringClass().getName();
		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassReader cr = null;
		try {
			cr = new ClassReader(n);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		cr.accept(new ClassVisitor(Opcodes.ASM4, cw) {
			@Override
			public MethodVisitor visitMethod(final int access,
					final String name, final String desc,
					final String signature, final String[] exceptions) {
				final Type[] args = Type.getArgumentTypes(desc);
				// 方法名相同并且参数个数相同
				if (!name.equals(m.getName())
						|| !sameType(args, m.getParameterTypes())) {
					return super.visitMethod(access, name, desc, signature,
							exceptions);
				}
				MethodVisitor v = cv.visitMethod(access, name, desc, signature,
						exceptions);
				return new MethodVisitor(Opcodes.ASM4, v) {
					@Override
					public void visitLocalVariable(String name, String desc,
							String signature, Label start, Label end, int index) {
						int i = index - 1;
						// 如果是静态方法，则第一就是参数
						// 如果不是静态方法，则第一个是"this"，然后才是方法的参数
						if (Modifier.isStatic(m.getModifiers())) {
							i = index;
						}
						if (i >= 0 && i < paramNames.length) {
							paramNames[i] = name;
						}
						super.visitLocalVariable(name, desc, signature, start,
								end, index);
					}
				};
			}
		}, 0);
		return paramNames;
	}

	/**
	 * 通过Paranamer实现
	 * @param method
	 * @return
	 */
	public static String[] getMethodParamNames4Paranamer(Method method) {
		Paranamer paranamer = new BytecodeReadingParanamer();
		return paranamer.lookupParameterNames(method);
	}

}
