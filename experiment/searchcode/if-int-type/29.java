package com.objectwave.persist.bcel;

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class PersistenceBaseCreator implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;
  /* my code */
  private String thisClassName = "";
  private String superclassName = "";

  public void createStaticFields() {
      Field [] fields = _cg.getFields();
      for(int i = 0; i < fields.length; i++)
      {
          if( ! (fields[i].isStatic() || fields[i].isTransient()))
          {
              FieldGen field;
              field = new FieldGen(ACC_STATIC | ACC_PUBLIC, new ObjectType("java.lang.reflect.Field"), "_" + fields[i].getName(), _cp);
              _cg.addField(field.getField());
          }
      }
  }
  public PersistenceBaseCreator( ClassGen source )
  {
      _cg = source;
      _cp = source.getConstantPool();
      _factory = new InstructionFactory( _cg, _cp );
      superclassName = source.getSuperclassName();
      thisClassName = source.getClassName();
  }

  public void addPersistentInterfaces()
  {
      _cg.addInterface("com.objectwave.persist.Persistence");
  }
  public PersistenceBaseCreator() {
    _cg = new ClassGen(thisClassName, superclassName, "PersistenceBase.java", ACC_PUBLIC | ACC_SUPER, new String[] { "com.objectwave.persist.Persistence", "javax.jdo.spi.PersistenceCapable" });

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);
  }

  public void addPersistenceSupport(){
      addPersistentInterfaces();
      createStaticFields();
      createFields();
/* end */
      createMethod_0();
    createMethod_2();
    createMethod_3();
    createMethod_4();
    createMethod_5();
    createMethod_6();
    createMethod_7();
    createMethod_8();
    createMethod_9();
    createMethod_10();
    createMethod_11();
    createMethod_12();
    createMethod_13();
    createMethod_14();
    createMethod_15();
    createMethod_16();
    createMethod_17();
    createMethod_18();
    createMethod_19();
    createMethod_20();
    createMethod_21();
    createMethod_22();
    createMethod_23();
    createMethod_24();
    createMethod_25();
    createMethod_26();
    createMethod_27();
    createMethod_28();
    createMethod_29();
    createMethod_30();
    createMethod_31();
    createMethod_32();
    createMethod_33();
    createMethod_34();
    createMethod_35();
    createMethod_36();
    createMethod_37();
    createMethod_38();
    createMethod_39();
    createMethod_40();
//    _cg.getJavaClass().dump(out);
  }

  private void createFields() {
    FieldGen field;

    field = new FieldGen(ACC_STATIC, new ObjectType("java.util.Vector"), "classDescriptor", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(ACC_STATIC, Type.STRING, "tableName", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(ACC_PUBLIC | ACC_STATIC, new ObjectType("java.lang.reflect.Field"), "_objectIdentifier", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(ACC_PUBLIC, new ObjectType("java.lang.Integer"), "objectIdentifier", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(ACC_PUBLIC | ACC_TRANSIENT, new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), "editor", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(ACC_PUBLIC | ACC_TRANSIENT, new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"), "adapt", _cp);
    _cg.addField(field.getField());

    field = new FieldGen(ACC_PROTECTED, Type.STRING, "xmlInitString", _cp);
    _cg.addField(field.getField());
  }

  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_STATIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<clinit>", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(new PUSH(_cp, thisClassName));
    il.append(_factory.createInvoke("java.lang.Class", "forName", new ObjectType("java.lang.Class"), new Type[] { Type.STRING }, Constants.INVOKESTATIC));
    il.append(_factory.createStore(Type.OBJECT, 0));
    InstructionHandle ih_6 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Class", "getDeclaredFields", new ArrayType(new ObjectType("java.lang.reflect.Field"), 1), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 1));
    InstructionHandle ih_11 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_13;
    BranchInstruction goto_13 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_13 = il.append(goto_13);
    InstructionHandle ih_16 = il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.AALOAD);
    il.append(_factory.createInvoke("com.objectwave.persist.bcel.Instrumentor", "isPersistentField", Type.BOOLEAN, new Type[] { new ObjectType("java.lang.reflect.Field") }, Constants.INVOKESTATIC));
        BranchInstruction ifeq_22 = _factory.createBranchInstruction(Constants.IFEQ, null);
    il.append(ifeq_22);
    InstructionHandle ih_25 = il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.AALOAD);
    il.append(new PUSH(_cp, 1));
    il.append(_factory.createInvoke("java.lang.reflect.Field", "setAccessible", Type.VOID, new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_32 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createNew("java.lang.StringBuffer"));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, "_"));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.AALOAD);
    il.append(_factory.createInvoke("java.lang.reflect.Field", "getName", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.Class", "getDeclaredField", new ObjectType("java.lang.reflect.Field"), new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_58 = il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.AALOAD);
    il.append(_factory.createInvoke("java.lang.reflect.Field", "set", Type.VOID, new Type[] { Type.OBJECT, Type.OBJECT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_66;
    BranchInstruction goto_66 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_66 = il.append(goto_66);
    InstructionHandle ih_69 = il.append(_factory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_70 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(new PUSH(_cp, "Failed to set static reference"));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke("com.objectwave.logging.MessageLog", "warn", Type.VOID, new Type[] { Type.OBJECT, Type.STRING, new ObjectType("java.lang.Throwable") }, Constants.INVOKESTATIC));
    InstructionHandle ih_77 = il.append(new IINC(2, 1));
    InstructionHandle ih_80 = il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(InstructionConstants.ARRAYLENGTH);
        BranchInstruction if_icmplt_83 = _factory.createBranchInstruction(Constants.IF_ICMPLT, ih_16);
    il.append(if_icmplt_83);
    InstructionHandle ih_86;
    BranchInstruction goto_86 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_86 = il.append(goto_86);
    InstructionHandle ih_89 = il.append(_factory.createStore(Type.OBJECT, 0));
    InstructionHandle ih_90 = il.append(_factory.createFieldAccess("java.lang.System", "err", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
    il.append(new PUSH(_cp, "Failed to init static fields in base class "));
    il.append(_factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_98 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Throwable", "printStackTrace", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_102 = il.append(_factory.createReturn(Type.VOID));
    goto_13.setTarget(ih_80);
    ifeq_22.setTarget(ih_77);
    goto_66.setTarget(ih_77);
    goto_86.setTarget(ih_102);
    method.addExceptionHandler(ih_32, ih_66, ih_69, new ObjectType("java.lang.Exception"));
    method.addExceptionHandler(ih_0, ih_86, ih_89, new ObjectType("java.lang.Throwable"));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_1() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(superclassName, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "__init", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_8 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_2() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PRIVATE, Type.VOID, Type.NO_ARGS, new String[] {  }, "__init", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "__setup", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_4;
    BranchInstruction goto_4 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_4 = il.append(goto_4);
    InstructionHandle ih_7 = il.append(_factory.createStore(Type.OBJECT, 1));
    InstructionHandle ih_8 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(new PUSH(_cp, "Failed to properly initialize the persistent object"));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("com.objectwave.logging.MessageLog", "error", Type.VOID, new Type[] { Type.OBJECT, Type.STRING, new ObjectType("java.lang.Throwable") }, Constants.INVOKESTATIC));
    InstructionHandle ih_15 = il.append(_factory.createNew("java.lang.IllegalStateException"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createNew("java.lang.StringBuffer"));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, "Persistent object "));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.OBJECT }, Constants.INVOKEVIRTUAL));
    il.append(new PUSH(_cp, " failed to initialize"));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.IllegalStateException", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    il.append(InstructionConstants.ATHROW);
    InstructionHandle ih_44 = il.append(_factory.createReturn(Type.VOID));
    goto_4.setTarget(ih_44);
    method.addExceptionHandler(ih_0, ih_4, ih_7, new ObjectType("java.lang.Throwable"));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_3() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, Type.NO_ARGS, new String[] {  }, "__setup", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createInvoke(thisClassName, "initializeObjectEditor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke(thisClassName, "setObjectEditor", Type.VOID, new Type[] { new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView") }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_4() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.STRING }, new String[] { "arg0" }, "setBrokerName", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "setBrokerName", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEINTERFACE));
    InstructionHandle ih_10 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_5() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.BOOLEAN }, new String[] { "arg0" }, "setAsTransient", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createCheckCast(new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter")));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke("com.objectwave.persist.mapping.RDBPersistentAdapter", "setAsTransient", Type.VOID, new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_6() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView") }, new String[] { "arg0" }, "setObjectEditor", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(new INSTANCEOF(_cp.addClass(new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"))));
        BranchInstruction ifeq_4 = _factory.createBranchInstruction(Constants.IFEQ, null);
    il.append(ifeq_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createCheckCast(new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter")));
    il.append(_factory.createFieldAccess(thisClassName, "adapt", new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"), Constants.PUTFIELD));
        BranchInstruction goto_15 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_15);
    InstructionHandle ih_18 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.GETFIELD));
    il.append(new INSTANCEOF(_cp.addClass(new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"))));
        BranchInstruction ifeq_25 = _factory.createBranchInstruction(Constants.IFEQ, null);
    il.append(ifeq_25);
    InstructionHandle ih_28 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.GETFIELD));
    il.append(_factory.createCheckCast(new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter")));
    il.append(_factory.createFieldAccess(thisClassName, "adapt", new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"), Constants.PUTFIELD));
    InstructionHandle ih_39 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.PUTFIELD));
    InstructionHandle ih_44 = il.append(_factory.createReturn(Type.VOID));
    ifeq_4.setTarget(ih_18);
    goto_15.setTarget(ih_39);
    ifeq_25.setTarget(ih_39);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_7() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { new ObjectType("java.lang.Integer") }, new String[] { "arg0" }, "setObjectIdentifier", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(thisClassName, "objectIdentifier", new ObjectType("java.lang.Integer"), Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_8() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.OBJECT }, new String[] { "arg0" }, "setPrimaryKeyField", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "setPrimaryKeyField", Type.VOID, new Type[] { Type.OBJECT }, Constants.INVOKEINTERFACE));
    InstructionHandle ih_10 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_9() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.BOOLEAN }, new String[] { "arg0" }, "setRetrievedFromDatabase", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "setRetrievedFromDatabase", Type.VOID, new Type[] { Type.BOOLEAN }, Constants.INVOKEINTERFACE));
    InstructionHandle ih_10 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_10() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { Type.STRING }, new String[] { "arg0" }, "setTableName", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(thisClassName, "tableName", Type.STRING, Constants.PUTSTATIC));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_11() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ObjectType("java.util.Vector") }, new String[] { "arg0" }, "setClassDescriptor", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(thisClassName, "classDescriptor", new ObjectType("java.util.Vector"), Constants.PUTSTATIC));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_12() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, new String[] {  }, "getAdapter", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "adapt", new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"), Constants.GETFIELD));
        BranchInstruction ifnull_4 = _factory.createBranchInstruction(Constants.IFNULL, null);
    il.append(ifnull_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "adapt", new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"), Constants.GETFIELD));
    il.append(_factory.createReturn(Type.OBJECT));
    InstructionHandle ih_12 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.GETFIELD));
    il.append(_factory.createCheckCast(new ObjectType("com.objectwave.persist.Persistence")));
    InstructionHandle ih_19 = il.append(_factory.createReturn(Type.OBJECT));
    ifnull_4.setTarget(ih_12);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_13() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.STRING, Type.NO_ARGS, new String[] {  }, "getBrokerName", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "getBrokerName", Type.STRING, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_14() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Type.NO_ARGS, new String[] {  }, "getObjectEditor", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_15() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ObjectType("java.lang.Integer"), Type.NO_ARGS, new String[] {  }, "getObjectIdentifier", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "objectIdentifier", new ObjectType("java.lang.Integer"), Constants.GETFIELD));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_16() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.OBJECT, Type.NO_ARGS, new String[] {  }, "getPrimaryKeyField", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "getPrimaryKeyField", Type.OBJECT, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_17() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ArrayType(Type.OBJECT, 1), Type.NO_ARGS, new String[] {  }, "getPrimaryKeyFields", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "getPrimaryKeyFields", new ArrayType(Type.OBJECT, 1), Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_18() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, Type.NO_ARGS, new String[] {  }, "isDirty", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "isDirty", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_19() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, Type.NO_ARGS, new String[] {  }, "isRetrievedFromDatabase", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "isRetrievedFromDatabase", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_20() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, Type.NO_ARGS, new String[] {  }, "isTransient", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createCheckCast(new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter")));
    il.append(_factory.createInvoke("com.objectwave.persist.mapping.RDBPersistentAdapter", "isTransient", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_10 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_21() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.STRING, Type.NO_ARGS, new String[] {  }, "getTableName", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createFieldAccess(thisClassName, "tableName", Type.STRING, Constants.GETSTATIC));
    InstructionHandle ih_3 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_22() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ObjectType("java.util.Vector"), Type.NO_ARGS, new String[] {  }, "getClassDescriptor", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createFieldAccess(thisClassName, "classDescriptor", new ObjectType("java.util.Vector"), Constants.GETSTATIC));
    InstructionHandle ih_3 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_23() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ObjectType("com.objectwave.persist.xml.XMLDecoder"), new Type[] { Type.STRING, Type.OBJECT }, new String[] { "arg0", "arg1" }, "getXmlDefinition", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke(thisClassName, "locateXmlMap", Type.STRING, new Type[] { Type.STRING, Type.OBJECT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_7 = il.append(_factory.createNew("com.objectwave.persist.xml.XMLDecoder"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke("java.lang.Object", "getClass", new ObjectType("java.lang.Class"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.xml.XMLDecoder", "<init>", Type.VOID, new Type[] { Type.STRING, new ObjectType("java.lang.Class") }, Constants.INVOKESPECIAL));
    il.append(_factory.createStore(Type.OBJECT, 4));
    InstructionHandle ih_21 = il.append(_factory.createLoad(Type.OBJECT, 4));
    InstructionHandle ih_23 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_24() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), new Type[] { Type.STRING }, new String[] { "arg0" }, "initializeObjectEditor", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createFieldAccess(thisClassName, "xmlInitString", Type.STRING, Constants.PUTFIELD));
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "createAdapter", new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"), new Type[] { new ObjectType("com.objectwave.persist.Persistence") }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 2));
    InstructionHandle ih_11 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createFieldAccess(thisClassName, "adapt", new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"), Constants.PUTFIELD));
    InstructionHandle ih_16 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke(thisClassName, "buildDescriptor", Type.VOID, new Type[] { Type.STRING, new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter") }, Constants.INVOKESPECIAL));
    InstructionHandle ih_22 = il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getClassDescriptor", new ObjectType("java.util.Vector"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.mapping.RDBPersistentAdapter", "setClassDescription", Type.VOID, new Type[] { new ObjectType("java.util.Vector") }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_30 = il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getTableName", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.mapping.RDBPersistentAdapter", "setTableName", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_38 = il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(new PUSH(_cp, 1));
    il.append(_factory.createInvoke("com.objectwave.persist.mapping.RDBPersistentAdapter", "setAtomicReads", Type.VOID, new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_43 = il.append(_factory.createLoad(Type.OBJECT, 2));
    InstructionHandle ih_44 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_25() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PRIVATE | ACC_FINAL | ACC_SYNCHRONIZED, Type.VOID, new Type[] { Type.STRING, new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter") }, new String[] { "arg0", "arg1" }, "buildDescriptor", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getClassDescriptor", new ObjectType("java.util.Vector"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        BranchInstruction ifnonnull_4 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke(thisClassName, "initDescriptor", Type.VOID, new Type[] { Type.STRING, new ObjectType("com.objectwave.persist.Persistence"), new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter") }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_14 = il.append(_factory.createReturn(Type.VOID));
    ifnonnull_4.setTarget(ih_14);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_26() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.STRING, new ObjectType("com.objectwave.persist.Persistence"), new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter") }, new String[] { "arg0", "arg1", "arg2" }, "initDescriptor", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Object", "getClass", new ObjectType("java.lang.Class"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createStore(Type.OBJECT, 4));
    il.append(InstructionConstants.MONITORENTER);
    InstructionHandle ih_8 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getClassDescriptor", new ObjectType("java.util.Vector"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        BranchInstruction ifnull_12 = _factory.createBranchInstruction(Constants.IFNULL, null);
    il.append(ifnull_12);
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.OBJECT, 4));
    InstructionHandle ih_17 = il.append(InstructionConstants.MONITOREXIT);
    il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_19 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke(thisClassName, "getXmlDefinition", new ObjectType("com.objectwave.persist.xml.XMLDecoder"), new Type[] { Type.STRING, Type.OBJECT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 5));
    InstructionHandle ih_27 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 5));
    il.append(_factory.createInvoke("com.objectwave.persist.xml.XMLDecoder", "getTableName", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke(thisClassName, "setTableName", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createLoad(Type.OBJECT, 5));
    il.append(_factory.createInvoke("com.objectwave.persist.xml.XMLDecoder", "getBrokerGeneratedPrimaryKeys", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.mapping.RDBPersistentAdapter", "setBrokerGeneratedPrimaryKeys", Type.VOID, new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_45 = il.append(_factory.createLoad(Type.OBJECT, 5));
    il.append(_factory.createInvoke("com.objectwave.persist.xml.XMLDecoder", "getMaps", new ObjectType("java.util.ArrayList"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 6));
    InstructionHandle ih_52 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 6));
    il.append(_factory.createInvoke(thisClassName, "addDefaultPrimaryAttribute", Type.VOID, new Type[] { new ObjectType("java.util.ArrayList") }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_58 = il.append(_factory.createNew("java.util.Vector"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.OBJECT, 6));
    il.append(_factory.createInvoke("java.util.Vector", "<init>", Type.VOID, new Type[] { new ObjectType("java.util.Collection") }, Constants.INVOKESPECIAL));
    il.append(_factory.createStore(Type.OBJECT, 7));
    InstructionHandle ih_69 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createLoad(Type.OBJECT, 7));
    il.append(_factory.createInvoke(thisClassName, "setClassDescriptor", Type.VOID, new Type[] { new ObjectType("java.util.Vector") }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_75 = il.append(_factory.createLoad(Type.OBJECT, 4));
    InstructionHandle ih_77 = il.append(InstructionConstants.MONITOREXIT);
        BranchInstruction goto_78 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_78);
    InstructionHandle ih_81 = il.append(_factory.createLoad(Type.OBJECT, 4));
    InstructionHandle ih_83 = il.append(InstructionConstants.MONITOREXIT);
    il.append(InstructionConstants.ATHROW);
    InstructionHandle ih_85 = il.append(_factory.createReturn(Type.VOID));
    ifnull_12.setTarget(ih_19);
    goto_78.setTarget(ih_85);
    method.addExceptionHandler(ih_8, ih_17, ih_81, null);
    method.addExceptionHandler(ih_19, ih_77, ih_81, null);
    method.addExceptionHandler(ih_81, ih_83, ih_81, null);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_27() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "delete", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "delete", Type.VOID, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_28() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "insert", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "insert", Type.VOID, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_29() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, new Type[] { Type.BOOLEAN }, new String[] { "arg0" }, "lock", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.GETFIELD));
        BranchInstruction ifnonnull_4 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_4);
    InstructionHandle ih_7 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke("com.objectwave.transactionalSupport.ObjectEditingView", "lock", Type.BOOLEAN, new Type[] { Type.BOOLEAN }, Constants.INVOKEINTERFACE));
    InstructionHandle ih_19 = il.append(_factory.createReturn(Type.INT));
    ifnonnull_4.setTarget(ih_9);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_30() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "markForDelete", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createCheckCast(new ObjectType("com.objectwave.persist.RDBPersistence")));
    il.append(_factory.createInvoke("com.objectwave.persist.RDBPersistence", "markForDelete", Type.VOID, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_12 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_31() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "save", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke(thisClassName, "getAdapter", new ObjectType("com.objectwave.persist.Persistence"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.persist.Persistence", "save", Type.VOID, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_9 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_32() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "unlock", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.GETFIELD));
        BranchInstruction ifnull_4 = _factory.createBranchInstruction(Constants.IFNULL, null);
    il.append(ifnull_4);
    InstructionHandle ih_7 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess(thisClassName, "editor", new ObjectType("com.objectwave.transactionalSupport.ObjectEditingView"), Constants.GETFIELD));
    il.append(_factory.createInvoke("com.objectwave.transactionalSupport.ObjectEditingView", "unlock", Type.VOID, Type.NO_ARGS, Constants.INVOKEINTERFACE));
    InstructionHandle ih_16 = il.append(_factory.createReturn(Type.VOID));
    ifnull_4.setTarget(ih_16);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_33() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, new Type[] { Type.BOOLEAN, new ArrayType(Type.OBJECT, 1), new ArrayType(new ObjectType("java.lang.reflect.Field"), 1) }, new String[] { "arg0", "arg1", "arg2" }, "update", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createNew("java.lang.UnsupportedOperationException"));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, "This method should never be called"));
    il.append(_factory.createInvoke("java.lang.UnsupportedOperationException", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    InstructionHandle ih_10 = il.append(InstructionConstants.ATHROW);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_34() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, Type.NO_ARGS, new String[] {  }, "usesAdapter", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(new PUSH(_cp, 1));
    InstructionHandle ih_1 = il.append(_factory.createReturn(Type.INT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_35() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.VOID, new Type[] { new ObjectType("java.util.ArrayList") }, new String[] { "arg0" }, "addDefaultPrimaryAttribute", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_2;
    BranchInstruction goto_2 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_2 = il.append(goto_2);
    InstructionHandle ih_5 = il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createInvoke("java.util.ArrayList", "get", Type.OBJECT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createCheckCast(new ObjectType("com.objectwave.persist.AttributeTypeColumn")));
    il.append(_factory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_14 = il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke("com.objectwave.persist.AttributeTypeColumn", "getType", new ObjectType("com.objectwave.persist.Type"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createFieldAccess("com.objectwave.persist.AttributeTypeColumn", "PRIMARYATT", new ObjectType("com.objectwave.persist.Type"), Constants.GETSTATIC));
        BranchInstruction if_acmpne_21 = _factory.createBranchInstruction(Constants.IF_ACMPNE, null);
    il.append(if_acmpne_21);
    InstructionHandle ih_24 = il.append(_factory.createReturn(Type.VOID));
    InstructionHandle ih_25 = il.append(new IINC(2, 1));
    InstructionHandle ih_28 = il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("java.util.ArrayList", "size", Type.INT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        BranchInstruction if_icmplt_33 = _factory.createBranchInstruction(Constants.IF_ICMPLT, ih_5);
    il.append(if_icmplt_33);
    InstructionHandle ih_36 = il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(new PUSH(_cp, 0));
    il.append(_factory.createNew("com.objectwave.persist.AttributeTypeColumn"));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, "databaseIdentifier"));
    il.append(_factory.createFieldAccess(thisClassName, "_objectIdentifier", new ObjectType("java.lang.reflect.Field"), Constants.GETSTATIC));
    il.append(_factory.createFieldAccess("com.objectwave.persist.AttributeTypeColumn", "PRIMARYATT", new ObjectType("com.objectwave.persist.Type"), Constants.GETSTATIC));
    il.append(_factory.createInvoke("com.objectwave.persist.AttributeTypeColumn", "<init>", Type.VOID, new Type[] { Type.STRING, new ObjectType("java.lang.reflect.Field"), new ObjectType("com.objectwave.persist.Type") }, Constants.INVOKESPECIAL));
    il.append(_factory.createInvoke("java.util.ArrayList", "add", Type.VOID, new Type[] { Type.INT, Type.OBJECT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_57 = il.append(_factory.createReturn(Type.VOID));
    goto_2.setTarget(ih_28);
    if_acmpne_21.setTarget(ih_25);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_36() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, new ObjectType("com.objectwave.persist.mapping.RDBPersistentAdapter"), new Type[] { new ObjectType("com.objectwave.persist.Persistence") }, new String[] { "arg0" }, "createAdapter", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createNew("com.objectwave.persist.mapping.RDBPersistentAdapter"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("com.objectwave.persist.mapping.RDBPersistentAdapter", "<init>", Type.VOID, new Type[] { new ObjectType("com.objectwave.persist.Persistence") }, Constants.INVOKESPECIAL));
    il.append(_factory.createStore(Type.OBJECT, 2));
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.OBJECT, 2));
    InstructionHandle ih_10 = il.append(_factory.createReturn(Type.OBJECT));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_37() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PROTECTED, Type.STRING, new Type[] { Type.STRING, Type.OBJECT }, new String[] { "arg0", "arg1" }, "locateXmlMap", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_2 = il.append(_factory.createLoad(Type.OBJECT, 1));
        BranchInstruction ifnull_3 = _factory.createBranchInstruction(Constants.IFNULL, null);
    il.append(ifnull_3);
    InstructionHandle ih_6 = il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("java.lang.System", "getProperty", Type.STRING, new Type[] { Type.STRING }, Constants.INVOKESTATIC));
    il.append(_factory.createStore(Type.OBJECT, 3));
    InstructionHandle ih_11 = il.append(InstructionConstants.ACONST_NULL);
    il.append(_factory.createStore(Type.OBJECT, 4));
    InstructionHandle ih_14 = il.append(_factory.createNew("com.objectwave.utility.FileFinder"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createInvoke("com.objectwave.utility.FileFinder", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    il.append(_factory.createStore(Type.OBJECT, 5));
    InstructionHandle ih_23 = il.append(_factory.createLoad(Type.OBJECT, 3));
        BranchInstruction ifnonnull_24 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_24);
    il.append(_factory.createLoad(Type.OBJECT, 1));
        BranchInstruction ifnull_28 = _factory.createBranchInstruction(Constants.IFNULL, null);
    il.append(ifnull_28);
    InstructionHandle ih_31 = il.append(_factory.createLoad(Type.OBJECT, 5));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke("java.lang.Object", "getClass", new ObjectType("java.lang.Class"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("com.objectwave.utility.FileFinder", "getUrl", new ObjectType("java.net.URL"), new Type[] { new ObjectType("java.lang.Class"), Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 4));
        BranchInstruction goto_43 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_43);
    InstructionHandle ih_46 = il.append(_factory.createLoad(Type.OBJECT, 3));
        BranchInstruction ifnull_47 = _factory.createBranchInstruction(Constants.IFNULL, null);
    il.append(ifnull_47);
    InstructionHandle ih_50 = il.append(_factory.createLoad(Type.OBJECT, 5));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke("java.lang.Object", "getClass", new ObjectType("java.lang.Class"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke("com.objectwave.utility.FileFinder", "getUrl", new ObjectType("java.net.URL"), new Type[] { new ObjectType("java.lang.Class"), Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 4));
    InstructionHandle ih_62 = il.append(_factory.createLoad(Type.OBJECT, 4));
        BranchInstruction ifnonnull_64 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_64);
    InstructionHandle ih_67 = il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke("java.lang.Object", "getClass", new ObjectType("java.lang.Class"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.Class", "getName", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 6));
    InstructionHandle ih_76 = il.append(_factory.createNew("java.lang.StringBuffer"));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, "/"));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    il.append(_factory.createLoad(Type.OBJECT, 6));
    il.append(new PUSH(_cp, 46));
    il.append(new PUSH(_cp, 47));
    il.append(_factory.createInvoke("java.lang.String", "replace", Type.STRING, new Type[] { Type.CHAR, Type.CHAR }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(new PUSH(_cp, ".xml"));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 6));
    InstructionHandle ih_109 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createNew("java.lang.StringBuffer"));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, "Looking for default xml resource of "));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    il.append(_factory.createLoad(Type.OBJECT, 6));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.logging.MessageLog", "debug", Type.VOID, new Type[] { Type.OBJECT, Type.STRING }, Constants.INVOKESTATIC));
    InstructionHandle ih_131 = il.append(_factory.createLoad(Type.OBJECT, 5));
    il.append(_factory.createLoad(Type.OBJECT, 2));
    il.append(_factory.createInvoke("java.lang.Object", "getClass", new ObjectType("java.lang.Class"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.OBJECT, 6));
    il.append(_factory.createInvoke("com.objectwave.utility.FileFinder", "getUrl", new ObjectType("java.net.URL"), new Type[] { new ObjectType("java.lang.Class"), Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.OBJECT, 4));
    InstructionHandle ih_144;
    BranchInstruction goto_144 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_144 = il.append(goto_144);
    InstructionHandle ih_147 = il.append(_factory.createStore(Type.OBJECT, 6));
    InstructionHandle ih_149 = il.append(_factory.createNew("java.lang.StringBuffer"));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, "Exception locating xml resource : "));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(new PUSH(_cp, " -> "));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("com.objectwave.exception.ExceptionBuilder", "configuration", new ObjectType("com.objectwave.exception.ConfigurationException"), new Type[] { Type.STRING }, Constants.INVOKESTATIC));
    il.append(InstructionConstants.ATHROW);
    InstructionHandle ih_180 = il.append(_factory.createLoad(Type.OBJECT, 4));
        BranchInstruction ifnonnull_182 = _factory.createBranchInstruction(Constants.IFNONNULL, null);
    il.append(ifnonnull_182);
    InstructionHandle ih_185 = il.append(_factory.createNew("java.io.FileNotFoundException"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createNew("java.lang.StringBuffer"));
    il.append(InstructionConstants.DUP);
    il.append(new PUSH(_cp, "Exception locating xml resource : "));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    il.append(_factory.createLoad(Type.OBJECT, 1));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(new PUSH(_cp, " -> "));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(Type.OBJECT, 3));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", Type.STRINGBUFFER, new Type[] { Type.STRING }, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.io.FileNotFoundException", "<init>", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESPECIAL));
    il.append(InstructionConstants.ATHROW);
    InstructionHandle ih_220 = il.append(_factory.createLoad(Type.OBJECT, 4));
    il.append(_factory.createInvoke("java.net.URL", "toString", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_225 = il.append(_factory.createReturn(Type.OBJECT));
    ifnull_3.setTarget(ih_11);
    ifnonnull_24.setTarget(ih_46);
    ifnull_28.setTarget(ih_46);
    goto_43.setTarget(ih_62);
    ifnull_47.setTarget(ih_62);
    ifnonnull_64.setTarget(ih_180);
    goto_144.setTarget(ih_180);
    ifnonnull_182.setTarget(ih_220);
    method.addExceptionHandler(ih_23, ih_144, ih_147, new ObjectType("java.net.MalformedURLException"));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_38() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(0, Type.VOID, Type.NO_ARGS, new String[] {  }, "installPropertyChangeListener", thisClassName, il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createNew("java.lang.StringBuffer"));
    il.append(InstructionConstants.DUP);
    il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Object", "getClass", new ObjectType("java.lang.Class"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.Class", "getName", Type.STRING, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createInvoke("java.lang.String", "valueOf", Type.STRING, new Type[] { Type.OBJECT }, Constants.INVOKESTATIC));
    il.append(_factory.createInvoke("java.lang.StringBuffer", "<init>", Type.VOID, new Type[] { Type.STRING }, Co
