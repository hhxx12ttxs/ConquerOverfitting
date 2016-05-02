/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */

package org.jikesrvm.dsu;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.ApplicationClassLoader;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.RVMType;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.dsu.DsuSafePoint.JvolveSafeState;
import org.jikesrvm.runtime.Reflection;
import org.jikesrvm.runtime.RuntimeEntrypoints;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.util.HashSetRVM;
import org.jikesrvm.util.LinkedListRVM;

public class Dsu {

  public static final Atom JVOLVE_CLASS  = Atom.findOrCreateAsciiAtom("jvolveClass");
  public static final Atom JVOLVE_OBJECT = Atom.findOrCreateAsciiAtom("jvolveObject");

  protected static ApplicationClassLoader application_classloader = null;

  /**
   * The parsed file. Each line is an element in the list. Each element is
   * represented by a string array.
   */
  private LinkedListRVM<String[]> parsed_file;

  /**
   * The list of methods to be invalidated. These are methods that refer to
   * fields and methods in classes that have been updated. These methods
   * cannot be on stack.
   */
  private HashSetRVM<RVMMethod> methodsInUpdatedClasses;
  private HashSetRVM<RVMMethod> methodBodyChanges;
  private HashSetRVM<RVMMethod> indirectUpdates;
  private HashSetRVM<RVMMethod> unmodifiedMethodsInUpdatedClasses;

  private HashSetRVM<RVMClass> groupBClasses;

  private LinkedListRVM<RVMMethod> classUpdaterMethods;

  /**
   * Class that contains all transformer methods for this update.
   */
  private RVMClass transformerType;

  /**
   * Whatever should be added to the beginning of classPath.
   */
  private LinkedListRVM<String> newPath;

  private static int revision = 0;

  public static boolean dsuInProgress = false;

  private HashSetRVM<DsuSafePoint> dsps;

  protected Dsu() throws IOException {
    parsed_file = new LinkedListRVM<String[]>();
    methodsInUpdatedClasses = new HashSetRVM<RVMMethod>();
    methodBodyChanges = new HashSetRVM<RVMMethod>();
    indirectUpdates = new HashSetRVM<RVMMethod>();
    unmodifiedMethodsInUpdatedClasses = new HashSetRVM<RVMMethod>();
    groupBClasses = new HashSetRVM<RVMClass>();
    classUpdaterMethods = new LinkedListRVM<RVMMethod>();
    newPath = new LinkedListRVM<String>();
    if (VM.VerifyAssertions) VM._assert(application_classloader != null);
    parseDSUSpecFile();
    preProcessDSUSpecFile();
  }

  protected void doAll() {
    try {
      if (VM.VerifyAssertions) VM._assert(!VM.DsuSuspendAndThenDie, "Die after stopping threads");
      revision++;
      processDSUSpecFile();
    } catch (Throwable t) {
      t.printStackTrace();
      VM.sysExit(-1);
    }
  }

  private void loadTransformerClass(String className) throws ClassNotFoundException {
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("Dsu.loadTransformerClass: ", className);
    if (className.startsWith("L") && className.endsWith(";")) {
      className = className.substring(1, className.length() - 1);
    }
    Class<?> c = application_classloader.loadClass(className);
    RVMClass type = JikesRVMSupport.getTypeForClass(c).asClass();
    RuntimeEntrypoints.initializeClassForDynamicLink(type);
    transformerType = type;
  }

  /**
   * Rename classname to old_namespace_classname. Get a new version of
   * classname.
   */
  private void processUpdatedClass(String className, String renamedClassName) {
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("Dsu.processUpdatedClass: ", className, " -> ", renamedClassName);
    Atom atom = Atom.findAsciiAtom(className);
    if (atom == null) {
      if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("atom is null for: ", className);
      return;
    }
    Atom renamedAtom = Atom.findOrCreateAsciiAtom(renamedClassName);
    TypeReference typeRef = TypeReference.findOrCreate(application_classloader, atom);
    if (!typeRef.isLoaded()) {
      if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("This type was not loaded: ", typeRef.toString());
      return;
    }
    RVMClass type = typeRef.peekType().asClass();
    TypeReference renamedTypeRef = TypeReference.findOrCreate(application_classloader, renamedAtom);
    if (VM.VerifyAssertions) {
      VM._assert(type != null);
      VM._assert(!renamedTypeRef.isLoaded());
    }
    if (Logger.LOG_DEBUG && Logger.LOG_TIB) {
      VM.sysWriteln("Dumping TIB of original class before it is renamed");
      type.getTypeInformationBlock().dump();
    }
    /* rename the class */
    type.dsuRename(renamedTypeRef);
    Class<?> cls = VMClassLoader.findLoadedClass(application_classloader, atom.classNameFromDescriptor());
    if (VM.VerifyAssertions) VM._assert(cls != null);
    if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("cls ", cls.toString());
    /* remove the previously loaded class */
    String classNameFromDesc = atom.classNameFromDescriptor();
    if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("unFindLoadedClass: ", classNameFromDesc);
    VMClassLoader.unFindLoadedClass(application_classloader, classNameFromDesc);
    cls = VMClassLoader.findLoadedClass(application_classloader, classNameFromDesc);
    if (VM.VerifyAssertions) VM._assert(cls == null);
    /* load the new class */
    RVMClass updatedType = typeRef.resolve().asClass();
    RuntimeEntrypoints.DSU_initializeClassForDynamicLink(updatedType);
    updatedType.pointToOldVersion(type);
    updatedType.pointMethodsToOldVersion(type);
    if (Logger.LOG_DEBUG && Logger.LOG_TIB) {
      VM.sysWriteln("Dumping TIB of updated class after it is loaded");
      updatedType.getTypeInformationBlock().dump();
    }
    type.disableMethodSlotsInTIB();
    if (Logger.LOG_DEBUG && Logger.LOG_TIB) {
      VM.sysWriteln("Dumping TIB of original class after it is prefixed with r0_");
      type.getTypeInformationBlock().dump();
    }
    setObjectAndClassTransformers(updatedType, className, renamedClassName);
  }

  /**
   * @param updatedType
   */
  private void setObjectAndClassTransformers(RVMClass updatedType, String className, String renamedClassName) {
    /*
     * Class transformers
     */
    String jvolveClassDesc = "(" + className + ")V";
    RVMMethod jvolveClass = transformerType.findDeclaredMethod(JVOLVE_CLASS, Atom.findOrCreateAsciiAtom(jvolveClassDesc));
    if (VM.VerifyAssertions) {
      VM._assert(jvolveClass != null);
      VM._assert(jvolveClass.isStatic());
    }
    updatedType.setDsuClassUpdaterMethod(jvolveClass);
    classUpdaterMethods.add(jvolveClass);
    /*
     * Object transformers
     */
    String jvolveObjectDesc = "(" + className + renamedClassName + ")V";
    RVMMethod jvolveObject = transformerType.findDeclaredMethod(JVOLVE_OBJECT, Atom.findAsciiAtom(jvolveObjectDesc));
    if (VM.VerifyAssertions) {
      VM._assert(jvolveObject != null);
      VM._assert(jvolveClass.isStatic());
    }
    updatedType.setDsuObjectUpdaterMethod(jvolveObject);
  }

  /*
   * Only methods bodies change. Replace the classfile with a new classfile.
   * Nothing changes. Only the methods change and they have to be recompiled.
   */
  private void processChangedMethodBodies(String descriptor) throws ClassNotFoundException, ClassFormatError, IOException {
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("Dsu.processChangedMethodBodies: ", descriptor);
    Atom atom = Atom.findAsciiAtom(descriptor);
    if (atom == null) {
      if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("Dsu.processChangedMethodBodies: atom is null. ", descriptor);
      return;
    }
    TypeReference typeRef = TypeReference.findOrCreate(application_classloader, atom);
    if (!typeRef.isLoaded()) {
      if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("Dsu.processChangedMethodBodies: type is not loaded ", typeRef.toString());
      return;
    }
    RVMType type = typeRef.peekType();
    if (VM.VerifyAssertions) VM._assert(type != null);
    InputStream is = application_classloader.findClassStream(atom.classNameFromDescriptor());
    DataInputStream input = new DataInputStream(is);
    type.asClass().dsu_groupBReplace(typeRef, input);
    groupBClasses.add(type.asClass());
  }

  /**
   * Add a method to methods_to_be_recompiled.
   */
  private void addMethodToGroup(HashSetRVM<RVMMethod> group, String className, String methodName, String methodDescriptor) {
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWrite("Dsu.addMethodToGroup: ", className, " ", methodName, " ");
    if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln(methodDescriptor);
    Atom classNameAtom = Atom.findAsciiAtom(className);
    Atom methodNameAtom = Atom.findAsciiAtom(methodName);
    Atom methodDescriptorAtom = Atom.findAsciiAtom(methodDescriptor);
    if ((classNameAtom == null) || (methodNameAtom == null) || (methodDescriptorAtom == null)) {
      if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("The method was not found");
      return;
    }
    TypeReference typeRef = TypeReference.findOrCreate(application_classloader, classNameAtom);
    if (!typeRef.isLoaded()) {
      if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("This type is not loaded: ", typeRef.toString());
      return;
    }
    RVMType type = typeRef.peekType();
    if (!type.isInstantiated()) {
      if (Logger.LOG_DEBUG && Logger.LOG_DSU) VM.sysWriteln("This type is not instantiated: ", typeRef.toString());
      return;
    }
    RVMMethod method = type.asClass().findDeclaredMethod(methodNameAtom, methodDescriptorAtom);
    if (VM.VerifyAssertions) VM._assert(method != null);
    group.add(method);
  }

  /**
   * Invalidate all methods of a class.
   */
  private static void recompileMethods(String classname) {
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("Dsu.recompileMethods: ", classname);
    Atom atom = Atom.findAsciiAtom(classname);
    TypeReference typeRef = TypeReference.findOrCreate(application_classloader, atom);
    RVMType type = typeRef.peekType();
    if (VM.VerifyAssertions) VM._assert(type != null);
    type.asClass().invalidateMethods();
  }

  /**
   * Invalidate a particular method.
   */
  private static void recompileMethod(RVMMethod method) {
    method.invalidateCompiledMethod();
  }

  /**
   * Parse the DSU specification file
   * @throws IOException
   */
  private void parseDSUSpecFile() throws IOException {
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("Dsu.parseDSUSpecFile");
    if (VM.VerifyAssertions) VM._assert(application_classloader != null);
    parsed_file = new LinkedListRVM<String[]>();
    /* Read the file and build the list of classes to update */
    BufferedReader in = new BufferedReader(new FileReader(VM.DsuSpecificationFile));
    while (true) {
      String line = in.readLine();
      if (line == null) break;
      String[] tokens = line.split(" ");
      parsed_file.add(tokens);
      if (tokens[0].equals("EOF")) break;
    }
  }

  /**
   * Do some processing of the DSU specification file. All that we do is group
   * methods into different categories and use them to see if we are in a safe
   * point.
   */
  private void preProcessDSUSpecFile() {
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("Dsu.preProcessDSUSpecFile");
    for (String[] tokens : parsed_file) {
      String command = tokens[0];
      if (command.equals("Nop")) {
        ;
      } else if (command.equals("transformers")) {
        ;
      } else if (command.equals("gAc") ||
                 command.equals("gBc")) {
        // Nothing to be done now
      } else if (command.equals("gAm-unmodified") ||
                 command.equals("gAm-modified") ||
                 command.equals("gBm") ||
                 command.equals("gCm")) {
        // This methods should not be on stack
        String className = tokens[1];
        String methodName = tokens[2];
        String methodDescriptor = tokens[3];
        if (command.equals("gAm-unmodified")) {
          addMethodToGroup(methodsInUpdatedClasses, className, methodName, methodDescriptor);
          addMethodToGroup(unmodifiedMethodsInUpdatedClasses, className, methodName, methodDescriptor);
        } else if (command.equals("gAm-modified")) {
          addMethodToGroup(methodsInUpdatedClasses, className, methodName, methodDescriptor);
        } else if (command.equals("gBm")) {
          addMethodToGroup(methodBodyChanges, className, methodName, methodDescriptor);
        } else if (command.equals("gCm")) {
          addMethodToGroup(indirectUpdates, className, methodName, methodDescriptor);
        }
      } else if (command.equals("classpath")) {
        newPath.add(tokens[1]);
      } else if (command.equals("EOF")) {
        break;
      } else VM.notReached("Unknown command");
    }
  }

  /**
   * Do all that has to be done to perform DSU
   * @throws ClassFormatError
   * @throws ClassNotFoundException
   * @throws IOException
   */
  private void processDSUSpecFile() throws ClassFormatError, ClassNotFoundException, IOException {
    /* Add newPath to the beginning of Classpath */
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("Dsu.processDSUSpecFile");
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("application_classloader: ", application_classloader.toString());
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("application_classloader: ", application_classloader.toStringSuper());
    for (String p : newPath) {
      application_classloader.addURLInFront(p);
    }
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("application_classloader: ", application_classloader.toString());
    if (Logger.LOG_INFO && Logger.LOG_DSU) VM.sysWriteln("application_classloader: ", application_classloader.toStringSuper());
    for (String[] tokens : parsed_file) {
      String command = tokens[0];
      if (command.equals("Nop")) {
        ;
      } else if (command.equals("transformers")) {
        String className = tokens[1];
        loadTransformerClass(className);
      } else if (command.equals("gAc")) {
        String className = tokens[1];
        String renamedClassName = tokens[2];
        processUpdatedClass(className, renamedClassName);
      } else if (command.equals("gBc")) {
        String classname = tokens[1];
        processChangedMethodBodies(classname);
      } else if (command.equals("gAm-modified") ||
                 command.equals("gAm-unmodified") ||
                 command.equals("gBm") ||
                 command.equals("gCm")) {
        // Already handled in pre_process
      } else if (command.equals("classpath")) {
        // Already handled in pre_process
      } else if (command.equals("EOF")) {
        break;
      } else if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
    }
    for (RVMMethod method : indirectUpdates) {
      recompileMethod(method);
    }
  }

   private static void verifyTypeHierarchyIntegrity(RVMClass type) {
     if (Logger.LOG_DEBUG && Logger.LOG_RVMCLASS) VM.sysWriteln("Dsu.verifyTypeHierarchyIntegrity(type): ", type.toString(), " ", type.myHashCode());
     if (VM.VerifyAssertions) VM._assert(! type.isDead());
     for (RVMClass t : type.getSubClasses()) {
       verifyTypeHierarchyIntegrity(t);
     }
   }

   protected static void verifyTypeHierarchyIntegrity() {
     if (Logger.LOG_DEBUG && Logger.LOG_RVMCLASS) VM.sysWriteln("Begin: Dsu.verifyTypeHierarchyIntegrity");
     RVMClass JavaLangObject = TypeReference.JavaLangObject.peekType().asClass();
     verifyTypeHierarchyIntegrity(JavaLangObject);
     if (Logger.LOG_DEBUG && Logger.LOG_RVMCLASS) VM.sysWriteln("End: Dsu.verifyTypeHierarchyIntegrity");
   }

   /**
    * Check if all application threads are in a safe point.
    * @return true if threads are safe for updating, false otherwise.
    */
   protected JvolveSafeState safeForDSU() {
     dsps = new HashSetRVM<DsuSafePoint>();
     JvolveSafeState allThreadsSafe = JvolveSafeState.SAFE;
     for (RVMThread t : RVMThread.threads) {
       if ((t != null) && t.isApplicationThread()) {
         DsuSafePoint dsp = new DsuSafePoint(this, t, methodsInUpdatedClasses, methodBodyChanges, indirectUpdates, unmodifiedMethodsInUpdatedClasses);
         dsps.add(dsp);
         JvolveSafeState safe = dsp.isStackSafeForDsu();
         if (allThreadsSafe.value < safe.value) {
           allThreadsSafe = safe;
         }
         if (Logger.LOG_INFO) {
           VM.sysWrite("DSU Message: Thread: ");
           t.dump();
           VM.sysWriteln(" Safety: ", safe.toString());
         }
       }
     }
     if (allThreadsSafe == JvolveSafeState.RETURN_BARRIERS) {
       installReturnBarriers();
     }
     return allThreadsSafe;
   }

   protected void prettyPrintApplicationThreads(String s) {
     for (DsuSafePoint dsp : dsps) {
       PrettyPrintStack.prettyPrintStack(dsp.thread, s);
     }
   }

   protected void performOSR() {
     if (VM.EnableDsuOsr) {
       for (DsuSafePoint dsp : dsps) {
         dsp.performOSRIfNecessary();
       }
     }
   }

   protected void installReturnBarriers() {
     if (VM.EnableDsuReturnBarriers) {
       for (DsuSafePoint dsp : dsps) {
         dsp.installReturnBarrierIfNecessary();
       }
     }
   }

  public static void setApplicationClassLoader(ApplicationClassLoader application_classloader) {
    Dsu.application_classloader = application_classloader;
  }

  public static ApplicationClassLoader getApplicationClassLoader() {
    return application_classloader;
  }

  public static int getRevision() {
    return revision;
  }

  /**
   * When we perform DSU, we need bytecodes of both old and new method bodies.
   * We need the old bytecode to extract state for OSR and we need the new
   * bytecode to compile the machine code.
   *
   * As the last step of DSU, set the method bodies to be that of the new one.
   */
  protected void setRightByteCodesForModifiedMethodBodies() {
    for (RVMClass kls : groupBClasses) {
      for (RVMMethod method : kls.getDeclaredMethods()) {
        if (method instanceof NormalMethod) {
          ((NormalMethod) method).updateByteCodes();
        }
      }
    }
  }

  private static final Object[] UNUSED_ARGS = { null };
  /**
   * Run class updaters "jvolveClass" methods for the new classes.
   */
  protected void runClassUpdaters() {
    for (RVMMethod method : classUpdaterMethods) {
      if (Logger.LOG_INFO && Logger.LOG_CLASS_TRANSFORMERS) VM.sysWriteln("Going to invoke class updater: ", method.toString());
      try {
        Reflection.invoke(method, null, null, UNUSED_ARGS, false);
      } catch (InvocationTargetException ite) {
        VM.notReached();
      }
    }
    classUpdaterMethods = null;
  }
}

