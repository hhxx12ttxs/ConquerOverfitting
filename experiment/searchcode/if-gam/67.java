package edu.utexas.cs.speedway.jvolve;

import edu.utexas.cs.speedway.jvolve.JvolveClass.GroupStatus;
import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.NotImplementedException;

public class UpdatePreparationTool {
  
  /*
   * These will be set by command line options.
   */
  private static String FILENAME0;
  private static String FILENAME1;
  private static String NEW_CLASSPATH;
  private static String OLD_PATH_PREFIX;
  
  /**
   * 
   * @param filename0
   * @param filename1
   * @throws Exception
   */
  public static void compareClassFiles(String filename0, String filename1) throws Exception {
    Hierarchy hierarchy0 = new Hierarchy(filename0);
    hierarchy0.addClass(filename0);
    Hierarchy hierarchy1 = new Hierarchy(filename1);
    hierarchy1.addClass(filename1);
    StringBuffer sb = new StringBuffer();
    sb.append(displayHierarchies(filename0, filename1, hierarchy0, hierarchy1));
    sb.append(compareHierarchies(hierarchy0, hierarchy1));
    System.out.print(sb);
  }

  private static void compareJarFiles(String filename0, String filename1) throws Exception {
    Hierarchy hierarchy0 = ByteCodeToAscii.readJarfile(filename0);
    Hierarchy hierarchy1 = ByteCodeToAscii.readJarfile(filename1);
    StringBuffer sb = new StringBuffer();
    // sb.append(displayHierarchies(filename0, filename1, hierarchy0, hierarchy1));
    // sb.append("\n");
    sb.append(compareHierarchies(hierarchy0, hierarchy1));
    System.out.print(sb);
  }
  
  private static String compareHierarchies(Hierarchy hierarchy0, Hierarchy hierarchy1) throws InvalidByteCodeException, IOException, NotImplementedException {
    computeUpdateInformation(hierarchy0, hierarchy1);
    checkUpdateInformationReady(hierarchy0);
    StringBuffer sb = new StringBuffer();
    String outputForVM = outputForVM(hierarchy0, hierarchy1);
    String stubs = classUpdateStubs(hierarchy0, hierarchy1);
    sb.append(outputForVM);
    sb.append(stubs);
    return sb.toString();
  }

  private static String displayHierarchies(String filename0, String filename1,
      Hierarchy hierarchy0, Hierarchy hierarchy1)
      throws InvalidByteCodeException, IOException, NotImplementedException {
    StringBuffer sb = new StringBuffer();
    sb.append(String.format("Hierarchy %s\n%s", filename0, hierarchy0.display()));
    sb.append(String.format("Hierarchy %s\n%s", filename1, hierarchy1.display()));
    return sb.toString();
  }

  /**
   * Computes information regarding the update when given two hierarchies. This
   * information is stored in fields of JvolveClass instances of the
   * hierarchies.
   * 
   * @param hierarchy0
   * @param hierarchy1
   * @throws InvalidByteCodeException
   * @throws IOException
   * @throws NotImplementedException
   */
  public static void computeUpdateInformation(Hierarchy hierarchy0,
      Hierarchy hierarchy1) throws InvalidByteCodeException, IOException,
      NotImplementedException {
    if (Logger.LOG_DEBUG) System.err.println("computeUpdateInformation");
    Set<String> updatedClasses = new HashSet<String>();
    List<JvolveClass> hierarchy0classes = hierarchy0.classesInTopologicalOrder();
    // One pass through all classes to identify classes that need to be udpated
    for (JvolveClass jc0 : hierarchy0classes) {
      String className = jc0.getClassName();
      JvolveClass jc1 = hierarchy1.get(className);
      if (ClassUpdate.isThisAnUpdatedClass(jc0, jc1)) {
        if (Logger.LOG_DEBUG) System.err.println("YES");
        updatedClasses.add(className);
        List<MethodInfo> unChangedMethods = MethodImplementationChanges.unChangedMethodsinUpdatedClass(jc0, jc1);
        jc0.setUnModifiedMethods(unChangedMethods);
      } else {
        if (Logger.LOG_DEBUG) System.err.println("NO");
        MethodImplementationChanges.shouldLoadSomeMethodBodies(jc0, jc1);
      }
    }
    // Now, identify methods that refer to updated classes.
    for (JvolveClass jc0 : hierarchy0classes) {
      IndirectUpdates.hasIndirectUpdates(updatedClasses, jc0);
    }
  }
  
  /**
   * Check that update information is computed.
   * 
   * @param hierarchy0
   */
  public static void checkUpdateInformationReady(Hierarchy hierarchy0) {
    for (JvolveClass jc0 : hierarchy0.classes()) {
      switch (jc0.getStatus()) {
      case LoadEntireClass:
        assert (jc0.getUnModifiedMethods() != null);
        assert (jc0.getModifiedMethodBodies() == null);
        assert (jc0.getIndirectMethodUpdates() == null);
        break;
      case LoadSomeMethodBodies:
        assert (jc0.getUnModifiedMethods() == null);
        assert (jc0.getModifiedMethodBodies() != null);
        assert (jc0.getIndirectMethodUpdates() != null);
        break;
      case Clean:
        assert (jc0.getUnModifiedMethods() == null);
        assert (jc0.getModifiedMethodBodies() == null);
        if (jc0.getClassFile() != null) {
          assert (jc0.getIndirectMethodUpdates() != null);
        } else {
          assert (jc0.getIndirectMethodUpdates() == null);
        }
        break;
      case Unknown:
        assert false;
        break;
      }
    }
  }

  private static String outputForVM(Hierarchy hierarchy0, Hierarchy hierarchy1) throws InvalidByteCodeException {
    StringBuffer sb = new StringBuffer();
    sb.append(String.format("Nop Comparison of files %s %s\n", (new File(hierarchy0.getFileName())).getAbsolutePath(), (new File(hierarchy1.getFileName())).getAbsolutePath()));
    sb.append(String.format("classpath %s\n", NEW_CLASSPATH));
    // Class updates -- class signature changes
    for (JvolveClass jc0 : hierarchy0.classes()) {
      switch (jc0.getStatus()) {
        case LoadEntireClass: {
          sb.append(String.format("gAc %s %s\n", Utils.classNameToDescriptor(jc0.getClassName()),
            Utils.classNameToDescriptor(Utils.addPrefixToClassName(jc0.getClassName(), OLD_PATH_PREFIX))));
          String classDescriptor = Utils.classNameToDescriptor(jc0.getClassName());
          List<MethodInfo> unModifiedMethods = jc0.getUnModifiedMethods();
          ClassFile cf0 = jc0.getClassFile();
          for (MethodInfo m : cf0.getMethods()) {
            boolean unModified = unModifiedMethods.contains(m);
            sb.append(String.format("gAm-%s %s %s %s\n",
                unModified ? "unmodified" : "modified",
                classDescriptor,
                m.getName(),
                m.getDescriptor()));
          }
          break;
        }
        case LoadSomeMethodBodies: {
          sb.append(String.format("gBc %s\n", Utils.classNameToDescriptor(jc0.getClassName())));
          String classDescriptor = Utils.classNameToDescriptor(jc0.getClassName());
          for (MethodInfo m : jc0.getModifiedMethodBodies()) {
            sb.append(String.format("gBm %s %s %s\n", classDescriptor, m.getName(), m.getDescriptor()));
          }
          break;
        }
      }
      // Indirectly updated methods
      ClassFile cf0 = jc0.getClassFile();
      String classDescriptor = Utils.classNameToDescriptor(jc0.getClassName());
      if ((jc0.getStatus() != GroupStatus.LoadEntireClass) && (cf0 != null)) {
        for (MethodInfo m : jc0.getIndirectMethodUpdates()) {
          sb.append(String.format("gCm %s %s %s\n", classDescriptor, m.getName(), m.getDescriptor()));
        }
      }
    }
    sb.append("EOF\n");
    return sb.toString();
  }

  private static String classUpdateStubs(Hierarchy hierarchy0, Hierarchy hierarchy1)
    throws InvalidByteCodeException, NotImplementedException
  {
    StringBuffer sb = new StringBuffer();
    for (JvolveClass jc0 : hierarchy0.classes()) {
      if (jc0.getStatus() == GroupStatus.LoadEntireClass) {
        String className = jc0.getClassName();
        JvolveClass jc1 = hierarchy1.get(className);
        sb.append(ClassUpdate.generateStub(jc0, jc1, OLD_PATH_PREFIX));
      }
    }
    return sb.toString();
  }

  /**
   * Process commandline arguments. The current arguments are:
   *  -a old class file or jar file
   *  -b new class file or jar file
   *  -p prefix to prepend the old class names to generate stubs
   *  -d directory to create these stub classes
   *  -c create stub classes (otherwise, just write them to stdout)
   * @param args
   */
  private static void processCommandLine(String args[]) {
    Getopt g = new Getopt("UpdatePreparationTool", args, "a:b:c:p:");
    int c;
    while ((c = g.getopt()) != -1) {
      switch (c) {
      case 'a':
        FILENAME0 = g.getOptarg();
        break;
      case 'b':
        FILENAME1 = g.getOptarg();
        break;
      case 'c':
        NEW_CLASSPATH = g.getOptarg();
        break;
      case 'p':
        OLD_PATH_PREFIX = g.getOptarg();
        break;
      }
    }
    if ((FILENAME0 == null) || (FILENAME1 == null) || (OLD_PATH_PREFIX == null)) {
      System.err.println("Usage: UpdatePreparationTool -a old-file -b new-file -c new-class-path -p prefix");
      System.exit(1);
    }
  }
  
  /**
   * The main function
   * @param args
   * @throws Exception
   * @see processCommandLine
   */
  public static void main(String args[]) throws Exception {
    processCommandLine(args);
    if (FILENAME0.endsWith(".jar") && FILENAME1.endsWith(".jar")) {
      compareJarFiles(FILENAME0, FILENAME1);
    } else if (FILENAME0.endsWith(".class") && FILENAME1.endsWith(".class")) {
      compareClassFiles(FILENAME0, FILENAME1);
    } else {
      System.err.println("Possibly invalid command line arguments.");
      System.exit(-1);
    }
  }
}

