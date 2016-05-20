package org.codehaus.groovy.aop.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.groovy.aop.parser.ClassTypeResolver;
import org.codehaus.groovy.aop.parser.codedom.AspectBlock;
import org.codehaus.groovy.aop.parser.codedom.GStatic;
import org.codehaus.groovy.aop.parser.codedom.ReplaceStmt;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodCallAdapter extends AnalyserAdapter implements Opcodes {

	final private boolean matchedWhere;
	private GStatic gstatic;
	private ClassTypeResolver resolver;
	@Deprecated
	private List<AspectBlock> matchedBlocks=new ArrayList<AspectBlock>();
	private ReplaceStmt[] replaceStmts;

	private static final Type BYTE_TYPE = Type.getObjectType("java/lang/Byte");
	private static final Type BOOLEAN_TYPE = Type.getObjectType("java/lang/Boolean");
	private static final Type SHORT_TYPE = Type.getObjectType("java/lang/Short");
	private static final Type CHARACTER_TYPE = Type.getObjectType("java/lang/Character");
	private static final Type INTEGER_TYPE = Type.getObjectType("java/lang/Integer");
	private static final Type FLOAT_TYPE = Type.getObjectType("java/lang/Float");
	private static final Type LONG_TYPE = Type.getObjectType("java/lang/Long");
	private static final Type DOUBLE_TYPE = Type.getObjectType("java/lang/Double");
	//private static final Type NUMBER_TYPE = Type.getObjectType("java/lang/Number");
	//private static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

	public MethodCallAdapter(String owner, int access, String name,
			String desc, MethodVisitor mv, GStatic gstatic) {
		super(owner, access, name, desc, mv);
		this.gstatic = gstatic;
		this.matchedWhere = matchWhere(owner, access, name, desc);
	}

	public Type box(final Type type) {
		if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
			return type;
		}
		if (type == Type.VOID_TYPE) {
			return type;
		} else {
			Type boxed = type;
			switch (type.getSort()) {
			case Type.BYTE:
				boxed = BYTE_TYPE;
				break;
			case Type.BOOLEAN:
				boxed = BOOLEAN_TYPE;
				break;
			case Type.SHORT:
				boxed = SHORT_TYPE;
				break;
			case Type.CHAR:
				boxed = CHARACTER_TYPE;
				break;
			case Type.INT:
				boxed = INTEGER_TYPE;
				break;
			case Type.FLOAT:
				boxed = FLOAT_TYPE;
				break;
			case Type.LONG:
				boxed = LONG_TYPE;
				break;
			case Type.DOUBLE:
				boxed = DOUBLE_TYPE;
				break;
			}
			return boxed;
		}
	}

//	/**
//	 * Generates the instructions to unbox the top stack value. This value is
//	 * replaced by its unboxed equivalent on top of the stack.
//	 * 
//	 * @param type
//	 *            the type of the top stack value.
//	 */
//	public void unbox(final Type type) {
//		Type t = NUMBER_TYPE;
//		Method sig = null;
//		switch (type.getSort()) {
//		case Type.VOID:
//			return;
//		case Type.CHAR:
//			t = CHARACTER_TYPE;
//			sig = CHAR_VALUE;
//			break;
//		case Type.BOOLEAN:
//			t = BOOLEAN_TYPE;
//			sig = BOOLEAN_VALUE;
//			break;
//		case Type.DOUBLE:
//			sig = DOUBLE_VALUE;
//			break;
//		case Type.FLOAT:
//			sig = FLOAT_VALUE;
//			break;
//		case Type.LONG:
//			sig = LONG_VALUE;
//			break;
//		case Type.INT:
//		case Type.SHORT:
//		case Type.BYTE:
//			sig = INT_VALUE;
//		}
//		if (sig == null) {
//			checkCast(type);
//		} else {
//			checkCast(t);
//			invokeVirtual(t, sig);
//		}
//	}

	// @Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		MatchInfo info = match(opcode, owner, name, desc);
		if (info.getSuccess()) {

			printInfo(owner, name, desc);

			String newInvoker = null;
			if (info.getName().equals("times")) {
				newInvoker = "org.codehaus.groovy.runtime.DefaultGroovyMethods#times((Number)$1, (Closure)$[0]);";
			} else if (info.getName().equals("currentTimeMillis")) {
				newInvoker = "java.lang.System#currentTimeMillis();";
			} else if(info.getName().equals("minus")) {
				newInvoker = "org.codehaus.groovy.runtime.DefaultGroovyMethods#minus((Number)$1, (Number)$[0]);";
			}
			ReplaceCall rc = new ReplaceCall(this.resolver, newInvoker);

			final String newReceiver = rc.getReceiver();
			final String newName = rc.getName();
			final String newDesc = rc.getDesc();

			// invoke*0 = special case
			if (info.hasArgs() == false && rc.getArgs().size() == 0) {
				super.visitInsn(POP); // [LObject;
				super.visitInsn(POP); // String
				super.visitInsn(POP); // Class
				super.visitMethodInsn(INVOKESTATIC, newReceiver, newName,
						newDesc);
			} else {
				// number of wrapped parameters using in the new invocation
				// DUP = (n-1)
				for (int i = 0; i < rc.getWrappedArgs() - 1; i++) {
					super.visitInsn(DUP);
				}
				// number of parameters
				// 0. special case
				// DUP = (n-1)
				super.visitInsn(ICONST_0);
				super.visitInsn(AALOAD);
				System.out.println(rc.getWrappedArg(0).getCastInternalName());
				super.visitTypeInsn(CHECKCAST, rc.getWrappedArg(0).getCastInternalName());
				super.visitInsn(SWAP); // swap
				super.visitInsn(POP);

				super.visitMethodInsn(INVOKESTATIC, newReceiver, newName, newDesc);
				if (newDesc.charAt(newDesc.length() - 1) != 'V') { // not void
					super.visitInsn(SWAP); // swap
					super.visitInsn(POP);
				}
			}
			char primType = newDesc.charAt(newDesc.length() - 1);
			if (primType != ';' && primType != 'V') {
				super.visitMethodInsn(
								INVOKESTATIC,
								"org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation",
								"box", "(" + primType + ")Ljava/lang/Object;");
				super.visitTypeInsn(CHECKCAST, box(Type.getType(""+primType)).getInternalName());
			}
		} else {
			super.visitMethodInsn(opcode, owner, name, desc);
		}
	}

	private void printInfo(String owner, String name, String desc) {
		System.out.println(">>> stack size: " + this.stack.size());
		Type[] types = Type.getArgumentTypes(desc);
		for (int i = stack.size() - 1; i >= stack.size() - types.length; i--) {
			System.out.print(this.stack.get(i).getType());
			System.out.println(" : " + this.stack.get(i).getValue());
			if (this.stack.get(i).isArray()) {
				Object[] objs = (Object[]) this.stack.get(i).getValue();
				for (int j = 0; j < objs.length; j++) {
					System.out.println(((Item) objs[j]).getInferredType());
					System.out.println(((Item) objs[j]).getValue());
				}
			}
		}
		System.out.println("owner :" + owner);
		System.out.println("name  :" + name);
		System.out.println("desc  :" + desc);
	}

	private MatchInfo match(int opcode, String owner, String name, String desc) {
		// TODO 1. where
		if (!matchedWhere) {
			return new MatchInfo(false);
		}
				
		// 2. wild card for package, class, method, arg types, matching
		if (opcode == INVOKESTATIC) {
			if (owner.equals("org/codehaus/groovy/runtime/ScriptBytecodeAdapter")) {				
				if (name.startsWith("invoke")) {
					final int methodNameLoc;
					boolean hasArgs ;
					boolean isStatic = false;
					if (name.charAt(name.length() - 1) == 'N') {
						hasArgs = true;
						methodNameLoc = 2;
					} else {
						hasArgs = false;					
						methodNameLoc = 1;						
					}
					if (name.startsWith("invokeStatic")) {
						isStatic = true;
					}
					try {
						String v = (String)this.stack.get(stack.size() - methodNameLoc).getValue();
						for(int i=0;i<this.replaceStmts.length;i++) {
							// match "name"
							if(v.equals(this.replaceStmts[i].getMethodCall().getName())) {
								// TODO how to match method sig?
								// TODO how to match pattern ?
								// TODO how to match class ?
								return new MatchInfo(true, hasArgs, isStatic, v);
							}
//							if (v.equals("times")
//									// || v.equals("println")
//										|| v.equals("minus")
//										|| v.equals("currentTimeMillis")) {
//										return new MatchInfo(true, hasArgs, isStatic, v);
//									}							
						}											
					} catch (Exception e) {
						return new MatchInfo(false);
					}
				}
			}
		}
		return new MatchInfo(false);
	}

	private boolean matchPattern(String pattern, String value) {
		pattern = pattern.replace('.', '/')
			.replace("+", ".+")
			.replace("*", ".*")
			.replace("$", "\\$");
		Pattern p = Pattern.compile(pattern);
		return p.matcher(value).matches();
	}
	
	private boolean matchWhere(String owner, int access, String name, String desc) {
		this.matchedBlocks.clear();
		this.replaceStmts = null;
		int replaceStmtCount = 0;
		for (AspectBlock ab : gstatic.getAspectBlocks()) {			
			if(matchPattern(ab.getWhereClassPattern(), owner)) {
				if(matchPattern(ab.getWhereMethodPattern(), name)) {
					if(matchPattern(ab.getWhereDescPattern(), desc)) {
						if (ab.isStatic() && ((access & ACC_STATIC) != 0)) {
							matchedBlocks.add(ab);
							replaceStmtCount += ab.getReplaceStmts().length;
						}
					}
				}
			}
		}
		if (matchedBlocks.size() > 0) {
			this.replaceStmts = new ReplaceStmt[replaceStmtCount];
			int j = 0;
			for(AspectBlock ab: matchedBlocks) {
				for(int i=0;i<ab.getReplaceStmts().length;i++) {
					this.replaceStmts[j] = ab.getReplaceStmts()[i];
					j++;
				}
			}
			return true;
		}
		return false;
	}

	public void setResolver(ClassTypeResolver resolver) {
		this.resolver = resolver;
	}

}

