package kafka.ir.types;

import kafka.ir.instructions.Instruction;
import kafka.ir.instructions.InstructionVisitor;
import kafka.ir.types.Type;
import kafka.ir.types.MethodReference;
import kafka.ir.types.Class;
import kafka.ir.instances.Primitives;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMethodBuilder extends MethodVisitor {
  protected static interface HalfBuiltInstruction extends Instruction {
    Instruction finish(AbstractMethodBuilder builder);
  }
  public AbstractMethodBuilder() {
    super(Opcodes.ASM4);
  }
  private static int hash(Object operand, int hash) { return operand.hashCode() ^ (hash << 1); };
  private Nop cacheNop = new Nop();
  private Nop newNop() {
    return cacheNop;
  }
  /**
   * perform no operation
   */
  public static class Nop implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Nop(" + ")";
    }
  }
  private AConstNull cacheAConstNull = new AConstNull();
  private AConstNull newAConstNull() {
    return cacheAConstNull;
  }
  /**
   * push a ''null'' reference onto the stack
   */
  public static class AConstNull implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "AConstNull(" + ")";
    }
  }
  private static class IConstArgs {
    private int value;
    public IConstArgs(int value) {
      this.value = value;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.value, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IConstArgs other = (IConstArgs)obj;
      if (this.value != other.value)
        return false;
      return true;
    }
  }
  private Map<IConstArgs, IConst> cacheIConst = new HashMap<IConstArgs, IConst>();
  public IConst newIConst(int value) {
    IConstArgs key = new IConstArgs(value);
    IConst obj = cacheIConst.get(key);
    if (obj != null)
      return obj;
    obj = new IConst(buildPrimitive(value));
    cacheIConst.put(key, obj);
    return obj;
  }
  /**
   * load the int value -1 onto the stack
   */
  public static class IConst implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Primitives.Int value;

    public IConst(Primitives.Int value) {
      this.value = value;
    }
    public Primitives.Int value() { return value; }
    @Override
    public String toString() {
      return "IConst(" + "value=" + value + ")";
    }
  }
  private static class LConstArgs {
    private long value;
    public LConstArgs(long value) {
      this.value = value;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.value, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     LConstArgs other = (LConstArgs)obj;
      if (this.value != other.value)
        return false;
      return true;
    }
  }
  private Map<LConstArgs, LConst> cacheLConst = new HashMap<LConstArgs, LConst>();
  public LConst newLConst(long value) {
    LConstArgs key = new LConstArgs(value);
    LConst obj = cacheLConst.get(key);
    if (obj != null)
      return obj;
    obj = new LConst(buildPrimitive(value));
    cacheLConst.put(key, obj);
    return obj;
  }
  /**
   * push the long 0 onto the stack
   */
  public static class LConst implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Primitives.Long value;

    public LConst(Primitives.Long value) {
      this.value = value;
    }
    public Primitives.Long value() { return value; }
    @Override
    public String toString() {
      return "LConst(" + "value=" + value + ")";
    }
  }
  private static class FConstArgs {
    private float value;
    public FConstArgs(float value) {
      this.value = value;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.value, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     FConstArgs other = (FConstArgs)obj;
      if (this.value != other.value)
        return false;
      return true;
    }
  }
  private Map<FConstArgs, FConst> cacheFConst = new HashMap<FConstArgs, FConst>();
  public FConst newFConst(float value) {
    FConstArgs key = new FConstArgs(value);
    FConst obj = cacheFConst.get(key);
    if (obj != null)
      return obj;
    obj = new FConst(buildPrimitive(value));
    cacheFConst.put(key, obj);
    return obj;
  }
  /**
   * push ''0.0f'' on the stack
   */
  public static class FConst implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Primitives.Float value;

    public FConst(Primitives.Float value) {
      this.value = value;
    }
    public Primitives.Float value() { return value; }
    @Override
    public String toString() {
      return "FConst(" + "value=" + value + ")";
    }
  }
  private static class DConstArgs {
    private double value;
    public DConstArgs(double value) {
      this.value = value;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.value, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     DConstArgs other = (DConstArgs)obj;
      if (this.value != other.value)
        return false;
      return true;
    }
  }
  private Map<DConstArgs, DConst> cacheDConst = new HashMap<DConstArgs, DConst>();
  public DConst newDConst(double value) {
    DConstArgs key = new DConstArgs(value);
    DConst obj = cacheDConst.get(key);
    if (obj != null)
      return obj;
    obj = new DConst(buildPrimitive(value));
    cacheDConst.put(key, obj);
    return obj;
  }
  /**
   * push the constant ''0.0'' onto the stack
   */
  public static class DConst implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Primitives.Double value;

    public DConst(Primitives.Double value) {
      this.value = value;
    }
    public Primitives.Double value() { return value; }
    @Override
    public String toString() {
      return "DConst(" + "value=" + value + ")";
    }
  }
  private Iaload cacheIaload = new Iaload();
  private Iaload newIaload() {
    return cacheIaload;
  }
  /**
   * load an int from an array
   */
  public static class Iaload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Iaload(" + ")";
    }
  }
  private Laload cacheLaload = new Laload();
  private Laload newLaload() {
    return cacheLaload;
  }
  /**
   * load a long from an array
   */
  public static class Laload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Laload(" + ")";
    }
  }
  private Faload cacheFaload = new Faload();
  private Faload newFaload() {
    return cacheFaload;
  }
  /**
   * load a float from an array
   */
  public static class Faload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Faload(" + ")";
    }
  }
  private Daload cacheDaload = new Daload();
  private Daload newDaload() {
    return cacheDaload;
  }
  /**
   * load a double from an array
   */
  public static class Daload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Daload(" + ")";
    }
  }
  private Aaload cacheAaload = new Aaload();
  private Aaload newAaload() {
    return cacheAaload;
  }
  /**
   * load onto the stack a reference from an array
   */
  public static class Aaload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Aaload(" + ")";
    }
  }
  private Baload cacheBaload = new Baload();
  private Baload newBaload() {
    return cacheBaload;
  }
  /**
   * load a byte or Boolean value from an array
   */
  public static class Baload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Baload(" + ")";
    }
  }
  private Caload cacheCaload = new Caload();
  private Caload newCaload() {
    return cacheCaload;
  }
  /**
   * load a char from an array
   */
  public static class Caload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Caload(" + ")";
    }
  }
  private Saload cacheSaload = new Saload();
  private Saload newSaload() {
    return cacheSaload;
  }
  /**
   * load short from array
   */
  public static class Saload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Saload(" + ")";
    }
  }
  private Iastore cacheIastore = new Iastore();
  private Iastore newIastore() {
    return cacheIastore;
  }
  /**
   * store an int into an array
   */
  public static class Iastore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Iastore(" + ")";
    }
  }
  private Lastore cacheLastore = new Lastore();
  private Lastore newLastore() {
    return cacheLastore;
  }
  /**
   * store a long to an array
   */
  public static class Lastore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Lastore(" + ")";
    }
  }
  private Fastore cacheFastore = new Fastore();
  private Fastore newFastore() {
    return cacheFastore;
  }
  /**
   * store a float in an array
   */
  public static class Fastore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Fastore(" + ")";
    }
  }
  private Dastore cacheDastore = new Dastore();
  private Dastore newDastore() {
    return cacheDastore;
  }
  /**
   * store a double into an array
   */
  public static class Dastore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dastore(" + ")";
    }
  }
  private Aastore cacheAastore = new Aastore();
  private Aastore newAastore() {
    return cacheAastore;
  }
  /**
   * store into a reference in an array
   */
  public static class Aastore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Aastore(" + ")";
    }
  }
  private Bastore cacheBastore = new Bastore();
  private Bastore newBastore() {
    return cacheBastore;
  }
  /**
   * store a byte or Boolean value into an array
   */
  public static class Bastore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Bastore(" + ")";
    }
  }
  private Castore cacheCastore = new Castore();
  private Castore newCastore() {
    return cacheCastore;
  }
  /**
   * store a char into an array
   */
  public static class Castore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Castore(" + ")";
    }
  }
  private Sastore cacheSastore = new Sastore();
  private Sastore newSastore() {
    return cacheSastore;
  }
  /**
   * store short to array
   */
  public static class Sastore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Sastore(" + ")";
    }
  }
  private Pop cachePop = new Pop();
  private Pop newPop() {
    return cachePop;
  }
  /**
   * discard the top value on the stack
   */
  public static class Pop implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Pop(" + ")";
    }
  }
  private Pop2 cachePop2 = new Pop2();
  private Pop2 newPop2() {
    return cachePop2;
  }
  /**
   * discard the top two values on the stack (or one value, if it is a double or long)
   */
  public static class Pop2 implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Pop2(" + ")";
    }
  }
  private Dup cacheDup = new Dup();
  private Dup newDup() {
    return cacheDup;
  }
  /**
   * duplicate the value on top of the stack
   */
  public static class Dup implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dup(" + ")";
    }
  }
  private Dup_x1 cacheDup_x1 = new Dup_x1();
  private Dup_x1 newDup_x1() {
    return cacheDup_x1;
  }
  /**
   * insert a copy of the top value into the stack two values from the top. value1 and value2 must not be of the type double or long.
   */
  public static class Dup_x1 implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dup_x1(" + ")";
    }
  }
  private Dup_x2 cacheDup_x2 = new Dup_x2();
  private Dup_x2 newDup_x2() {
    return cacheDup_x2;
  }
  /**
   * insert a copy of the top value into the stack two (if value2 is double or long it takes up the entry of value3, too) or three values (if value2 is neither double nor long) from the top
   */
  public static class Dup_x2 implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dup_x2(" + ")";
    }
  }
  private Dup2 cacheDup2 = new Dup2();
  private Dup2 newDup2() {
    return cacheDup2;
  }
  /**
   * duplicate top two stack words (two values, if value1 is not double nor long; a single value, if value1 is double or long)
   */
  public static class Dup2 implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dup2(" + ")";
    }
  }
  private Dup2_x1 cacheDup2_x1 = new Dup2_x1();
  private Dup2_x1 newDup2_x1() {
    return cacheDup2_x1;
  }
  /**
   * duplicate two words and insert beneath third word (see explanation above)
   */
  public static class Dup2_x1 implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dup2_x1(" + ")";
    }
  }
  private Dup2_x2 cacheDup2_x2 = new Dup2_x2();
  private Dup2_x2 newDup2_x2() {
    return cacheDup2_x2;
  }
  /**
   * duplicate two words and insert beneath fourth word
   */
  public static class Dup2_x2 implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dup2_x2(" + ")";
    }
  }
  private Swap cacheSwap = new Swap();
  private Swap newSwap() {
    return cacheSwap;
  }
  /**
   * swaps two top words on the stack (note that value1 and value2 must not be double or long)
   */
  public static class Swap implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Swap(" + ")";
    }
  }
  private Add cacheAdd = new Add();
  private Add newAdd() {
    return cacheAdd;
  }
  /**
   * add two ints
   */
  public static class Add implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Add(" + ")";
    }
  }
  private Sub cacheSub = new Sub();
  private Sub newSub() {
    return cacheSub;
  }
  /**
   * Pops two values off the operand stack, subtracts the top one from the second, and pushes the int result back onto the stack.
   */
  public static class Sub implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Sub(" + ")";
    }
  }
  private Mul cacheMul = new Mul();
  private Mul newMul() {
    return cacheMul;
  }
  /**
   * multiply two integers
   */
  public static class Mul implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Mul(" + ")";
    }
  }
  private Div cacheDiv = new Div();
  private Div newDiv() {
    return cacheDiv;
  }
  /**
   * divide two integers
   */
  public static class Div implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Div(" + ")";
    }
  }
  private Rem cacheRem = new Rem();
  private Rem newRem() {
    return cacheRem;
  }
  /**
   * logical int remainder
   */
  public static class Rem implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Rem(" + ")";
    }
  }
  private Ineg cacheIneg = new Ineg();
  private Ineg newIneg() {
    return cacheIneg;
  }
  /**
   * negate int
   */
  public static class Ineg implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Ineg(" + ")";
    }
  }
  private Lneg cacheLneg = new Lneg();
  private Lneg newLneg() {
    return cacheLneg;
  }
  /**
   * negate a long
   */
  public static class Lneg implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Lneg(" + ")";
    }
  }
  private Fneg cacheFneg = new Fneg();
  private Fneg newFneg() {
    return cacheFneg;
  }
  /**
   * negate a float
   */
  public static class Fneg implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Fneg(" + ")";
    }
  }
  private Dneg cacheDneg = new Dneg();
  private Dneg newDneg() {
    return cacheDneg;
  }
  /**
   * negate a double
   */
  public static class Dneg implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dneg(" + ")";
    }
  }
  private Shl cacheShl = new Shl();
  private Shl newShl() {
    return cacheShl;
  }
  /**
   * int shift left
   */
  public static class Shl implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Shl(" + ")";
    }
  }
  private Shr cacheShr = new Shr();
  private Shr newShr() {
    return cacheShr;
  }
  /**
   * int arithmetic shift right
   */
  public static class Shr implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Shr(" + ")";
    }
  }
  private Ushr cacheUshr = new Ushr();
  private Ushr newUshr() {
    return cacheUshr;
  }
  /**
   * int logical shift right
   */
  public static class Ushr implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Ushr(" + ")";
    }
  }
  private And cacheAnd = new And();
  private And newAnd() {
    return cacheAnd;
  }
  /**
   * perform a bitwise and on two integers
   */
  public static class And implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "And(" + ")";
    }
  }
  private Or cacheOr = new Or();
  private Or newOr() {
    return cacheOr;
  }
  /**
   * bitwise int or
   */
  public static class Or implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Or(" + ")";
    }
  }
  private Xor cacheXor = new Xor();
  private Xor newXor() {
    return cacheXor;
  }
  /**
   * int xor
   */
  public static class Xor implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Xor(" + ")";
    }
  }
  private I2l cacheI2l = new I2l();
  private I2l newI2l() {
    return cacheI2l;
  }
  /**
   * convert an int into a long
   */
  public static class I2l implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "I2l(" + ")";
    }
  }
  private I2f cacheI2f = new I2f();
  private I2f newI2f() {
    return cacheI2f;
  }
  /**
   * convert an int into a float
   */
  public static class I2f implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "I2f(" + ")";
    }
  }
  private I2d cacheI2d = new I2d();
  private I2d newI2d() {
    return cacheI2d;
  }
  /**
   * convert an int into a double
   */
  public static class I2d implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "I2d(" + ")";
    }
  }
  private L2i cacheL2i = new L2i();
  private L2i newL2i() {
    return cacheL2i;
  }
  /**
   * convert a long to a int
   */
  public static class L2i implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "L2i(" + ")";
    }
  }
  private L2f cacheL2f = new L2f();
  private L2f newL2f() {
    return cacheL2f;
  }
  /**
   * convert a long to a float
   */
  public static class L2f implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "L2f(" + ")";
    }
  }
  private L2d cacheL2d = new L2d();
  private L2d newL2d() {
    return cacheL2d;
  }
  /**
   * convert a long to a double
   */
  public static class L2d implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "L2d(" + ")";
    }
  }
  private F2i cacheF2i = new F2i();
  private F2i newF2i() {
    return cacheF2i;
  }
  /**
   * convert a float to an int
   */
  public static class F2i implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "F2i(" + ")";
    }
  }
  private F2l cacheF2l = new F2l();
  private F2l newF2l() {
    return cacheF2l;
  }
  /**
   * convert a float to a long
   */
  public static class F2l implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "F2l(" + ")";
    }
  }
  private F2d cacheF2d = new F2d();
  private F2d newF2d() {
    return cacheF2d;
  }
  /**
   * convert a float to a double
   */
  public static class F2d implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "F2d(" + ")";
    }
  }
  private D2i cacheD2i = new D2i();
  private D2i newD2i() {
    return cacheD2i;
  }
  /**
   * convert a double to an int
   */
  public static class D2i implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "D2i(" + ")";
    }
  }
  private D2l cacheD2l = new D2l();
  private D2l newD2l() {
    return cacheD2l;
  }
  /**
   * convert a double to a long
   */
  public static class D2l implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "D2l(" + ")";
    }
  }
  private D2f cacheD2f = new D2f();
  private D2f newD2f() {
    return cacheD2f;
  }
  /**
   * convert a double to a float
   */
  public static class D2f implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "D2f(" + ")";
    }
  }
  private I2b cacheI2b = new I2b();
  private I2b newI2b() {
    return cacheI2b;
  }
  /**
   * convert an int into a byte
   */
  public static class I2b implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "I2b(" + ")";
    }
  }
  private I2c cacheI2c = new I2c();
  private I2c newI2c() {
    return cacheI2c;
  }
  /**
   * convert an int into a character
   */
  public static class I2c implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "I2c(" + ")";
    }
  }
  private I2s cacheI2s = new I2s();
  private I2s newI2s() {
    return cacheI2s;
  }
  /**
   * convert an int into a short
   */
  public static class I2s implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "I2s(" + ")";
    }
  }
  private Lcmp cacheLcmp = new Lcmp();
  private Lcmp newLcmp() {
    return cacheLcmp;
  }
  /**
   * compare two longs values
   */
  public static class Lcmp implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Lcmp(" + ")";
    }
  }
  private Fcmpl cacheFcmpl = new Fcmpl();
  private Fcmpl newFcmpl() {
    return cacheFcmpl;
  }
  /**
   * compare two floats
   */
  public static class Fcmpl implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Fcmpl(" + ")";
    }
  }
  private Fcmpg cacheFcmpg = new Fcmpg();
  private Fcmpg newFcmpg() {
    return cacheFcmpg;
  }
  /**
   * compare two floats
   */
  public static class Fcmpg implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Fcmpg(" + ")";
    }
  }
  private Dcmpl cacheDcmpl = new Dcmpl();
  private Dcmpl newDcmpl() {
    return cacheDcmpl;
  }
  /**
   * compare two doubles
   */
  public static class Dcmpl implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dcmpl(" + ")";
    }
  }
  private Dcmpg cacheDcmpg = new Dcmpg();
  private Dcmpg newDcmpg() {
    return cacheDcmpg;
  }
  /**
   * compare two doubles
   */
  public static class Dcmpg implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dcmpg(" + ")";
    }
  }
  private Ireturn cacheIreturn = new Ireturn();
  private Ireturn newIreturn() {
    return cacheIreturn;
  }
  /**
   * return an integer from a method
   */
  public static class Ireturn implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Ireturn(" + ")";
    }
  }
  private Lreturn cacheLreturn = new Lreturn();
  private Lreturn newLreturn() {
    return cacheLreturn;
  }
  /**
   * return a long value
   */
  public static class Lreturn implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Lreturn(" + ")";
    }
  }
  private Freturn cacheFreturn = new Freturn();
  private Freturn newFreturn() {
    return cacheFreturn;
  }
  /**
   * return a float
   */
  public static class Freturn implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Freturn(" + ")";
    }
  }
  private Dreturn cacheDreturn = new Dreturn();
  private Dreturn newDreturn() {
    return cacheDreturn;
  }
  /**
   * return a double from a method
   */
  public static class Dreturn implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Dreturn(" + ")";
    }
  }
  private Areturn cacheAreturn = new Areturn();
  private Areturn newAreturn() {
    return cacheAreturn;
  }
  /**
   * return a reference from a method
   */
  public static class Areturn implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Areturn(" + ")";
    }
  }
  private Returnv cacheReturnv = new Returnv();
  private Returnv newReturnv() {
    return cacheReturnv;
  }
  /**
   * return void from method
   */
  public static class Returnv implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Returnv(" + ")";
    }
  }
  private Arraylength cacheArraylength = new Arraylength();
  private Arraylength newArraylength() {
    return cacheArraylength;
  }
  /**
   * get the length of an array
   */
  public static class Arraylength implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Arraylength(" + ")";
    }
  }
  private Athrow cacheAthrow = new Athrow();
  private Athrow newAthrow() {
    return cacheAthrow;
  }
  /**
   * throws an error or exception (notice that the rest of the stack is cleared, leaving only a reference to the Throwable)
   */
  public static class Athrow implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Athrow(" + ")";
    }
  }
  private Monitorenter cacheMonitorenter = new Monitorenter();
  private Monitorenter newMonitorenter() {
    return cacheMonitorenter;
  }
  /**
   * enter monitor for object ("grab the lock" - start of synchronized() section)
   */
  public static class Monitorenter implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Monitorenter(" + ")";
    }
  }
  private Monitorexit cacheMonitorexit = new Monitorexit();
  private Monitorexit newMonitorexit() {
    return cacheMonitorexit;
  }
  /**
   * exit monitor for object ("release the lock" - end of synchronized() section)
   */
  public static class Monitorexit implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Monitorexit(" + ")";
    }
  }
  private Icmp cacheIcmp = new Icmp();
  private Icmp newIcmp() {
    return cacheIcmp;
  }
  /**
   * compare two int values
   */
  public static class Icmp implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Icmp(" + ")";
    }
  }
  private Acmp cacheAcmp = new Acmp();
  private Acmp newAcmp() {
    return cacheAcmp;
  }
  /**
   * compare two references
   */
  public static class Acmp implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
      return "Acmp(" + ")";
    }
  }
  private static class StrconstArgs {
    private String string;
    public StrconstArgs(String string) {
      this.string = string;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.string, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     StrconstArgs other = (StrconstArgs)obj;
      if (this.string != other.string)
        return false;
      return true;
    }
  }
  private Map<StrconstArgs, Strconst> cacheStrconst = new HashMap<StrconstArgs, Strconst>();
  public Strconst newStrconst(String string) {
    StrconstArgs key = new StrconstArgs(string);
    Strconst obj = cacheStrconst.get(key);
    if (obj != null)
      return obj;
    obj = new Strconst(string);
    cacheStrconst.put(key, obj);
    return obj;
  }
  /**
   * push a string on the stack
   */
  public static class Strconst implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private String string;

    public Strconst(String string) {
      this.string = string;
    }
    public String string() { return string; }
    @Override
    public String toString() {
      return "Strconst(" + "string=" + string + ")";
    }
  }
  private static class TypeconstArgs {
    private String type;
    public TypeconstArgs(String type) {
      this.type = type;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.type, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     TypeconstArgs other = (TypeconstArgs)obj;
      if (this.type != other.type)
        return false;
      return true;
    }
  }
  private Map<TypeconstArgs, Typeconst> cacheTypeconst = new HashMap<TypeconstArgs, Typeconst>();
  public Typeconst newTypeconst(String type) {
    TypeconstArgs key = new TypeconstArgs(type);
    Typeconst obj = cacheTypeconst.get(key);
    if (obj != null)
      return obj;
    obj = new Typeconst(buildType(type));
    cacheTypeconst.put(key, obj);
    return obj;
  }
  /**
   * push a class object on the stack
   */
  public static class Typeconst implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Type type;

    public Typeconst(Type type) {
      this.type = type;
    }
    public Type type() { return type; }
    @Override
    public String toString() {
      return "Typeconst(" + "type=" + type.name() + ")";
    }
  }
  private static class BipushArgs {
    private int operand;
    public BipushArgs(int operand) {
      this.operand = operand;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.operand, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     BipushArgs other = (BipushArgs)obj;
      if (this.operand != other.operand)
        return false;
      return true;
    }
  }
  private Map<BipushArgs, Bipush> cacheBipush = new HashMap<BipushArgs, Bipush>();
  public Bipush newBipush(int operand) {
    BipushArgs key = new BipushArgs(operand);
    Bipush obj = cacheBipush.get(key);
    if (obj != null)
      return obj;
    obj = new Bipush(buildPrimitive(operand));
    cacheBipush.put(key, obj);
    return obj;
  }
  /**
   * push a ''byte'' onto the stack as an integer ''value''
   */
  public static class Bipush implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Primitives.Int operand;

    public Bipush(Primitives.Int operand) {
      this.operand = operand;
    }
    public Primitives.Int operand() { return operand; }
    @Override
    public String toString() {
      return "Bipush(" + "operand=" + operand + ")";
    }
  }
  private static class SipushArgs {
    private int operand;
    public SipushArgs(int operand) {
      this.operand = operand;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.operand, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     SipushArgs other = (SipushArgs)obj;
      if (this.operand != other.operand)
        return false;
      return true;
    }
  }
  private Map<SipushArgs, Sipush> cacheSipush = new HashMap<SipushArgs, Sipush>();
  public Sipush newSipush(int operand) {
    SipushArgs key = new SipushArgs(operand);
    Sipush obj = cacheSipush.get(key);
    if (obj != null)
      return obj;
    obj = new Sipush(buildPrimitive(operand));
    cacheSipush.put(key, obj);
    return obj;
  }
  /**
   * push a short onto the stack
   */
  public static class Sipush implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Primitives.Int operand;

    public Sipush(Primitives.Int operand) {
      this.operand = operand;
    }
    public Primitives.Int operand() { return operand; }
    @Override
    public String toString() {
      return "Sipush(" + "operand=" + operand + ")";
    }
  }
  private static class NewarrayArgs {
    private int operand;
    public NewarrayArgs(int operand) {
      this.operand = operand;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.operand, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     NewarrayArgs other = (NewarrayArgs)obj;
      if (this.operand != other.operand)
        return false;
      return true;
    }
  }
  private Map<NewarrayArgs, Newarray> cacheNewarray = new HashMap<NewarrayArgs, Newarray>();
  public Newarray newNewarray(int operand) {
    NewarrayArgs key = new NewarrayArgs(operand);
    Newarray obj = cacheNewarray.get(key);
    if (obj != null)
      return obj;
    obj = new Newarray(int2Primitive(operand));
    cacheNewarray.put(key, obj);
    return obj;
  }
  /**
   * create new array with ''count'' elements of primitive type identified by ''atype''
   */
  public static class Newarray implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Type type;

    public Newarray(Type type) {
      this.type = type;
    }
    public Type type() { return type; }
    @Override
    public String toString() {
      return "Newarray(" + "type=" + type.name() + ")";
    }
  }
  private static class IloadArgs {
    private int var;
    public IloadArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IloadArgs other = (IloadArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<IloadArgs, Iload> cacheIload = new HashMap<IloadArgs, Iload>();
  public Iload newIload(int var) {
    IloadArgs key = new IloadArgs(var);
    Iload obj = cacheIload.get(key);
    if (obj != null)
      return obj;
    obj = new Iload(var);
    cacheIload.put(key, obj);
    return obj;
  }
  /**
   * load an int ''value'' from a local variable ''#index''
   */
  public static class Iload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Iload(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Iload(" + "var=" + var + ")";
    }
  }
  private static class LloadArgs {
    private int var;
    public LloadArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     LloadArgs other = (LloadArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<LloadArgs, Lload> cacheLload = new HashMap<LloadArgs, Lload>();
  public Lload newLload(int var) {
    LloadArgs key = new LloadArgs(var);
    Lload obj = cacheLload.get(key);
    if (obj != null)
      return obj;
    obj = new Lload(var);
    cacheLload.put(key, obj);
    return obj;
  }
  /**
   * load a long value from a local variable ''#index''
   */
  public static class Lload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Lload(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Lload(" + "var=" + var + ")";
    }
  }
  private static class FloadArgs {
    private int var;
    public FloadArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     FloadArgs other = (FloadArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<FloadArgs, Fload> cacheFload = new HashMap<FloadArgs, Fload>();
  public Fload newFload(int var) {
    FloadArgs key = new FloadArgs(var);
    Fload obj = cacheFload.get(key);
    if (obj != null)
      return obj;
    obj = new Fload(var);
    cacheFload.put(key, obj);
    return obj;
  }
  /**
   * load a float ''value'' from a local variable ''#index''
   */
  public static class Fload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Fload(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Fload(" + "var=" + var + ")";
    }
  }
  private static class DloadArgs {
    private int var;
    public DloadArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     DloadArgs other = (DloadArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<DloadArgs, Dload> cacheDload = new HashMap<DloadArgs, Dload>();
  public Dload newDload(int var) {
    DloadArgs key = new DloadArgs(var);
    Dload obj = cacheDload.get(key);
    if (obj != null)
      return obj;
    obj = new Dload(var);
    cacheDload.put(key, obj);
    return obj;
  }
  /**
   * load a double ''value'' from a local variable ''#index''
   */
  public static class Dload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Dload(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Dload(" + "var=" + var + ")";
    }
  }
  private static class AloadArgs {
    private int var;
    public AloadArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     AloadArgs other = (AloadArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<AloadArgs, Aload> cacheAload = new HashMap<AloadArgs, Aload>();
  public Aload newAload(int var) {
    AloadArgs key = new AloadArgs(var);
    Aload obj = cacheAload.get(key);
    if (obj != null)
      return obj;
    obj = new Aload(var);
    cacheAload.put(key, obj);
    return obj;
  }
  /**
   * load a reference onto the stack from a local variable ''#index''
   */
  public static class Aload implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Aload(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Aload(" + "var=" + var + ")";
    }
  }
  private static class IstoreArgs {
    private int var;
    public IstoreArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IstoreArgs other = (IstoreArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<IstoreArgs, Istore> cacheIstore = new HashMap<IstoreArgs, Istore>();
  public Istore newIstore(int var) {
    IstoreArgs key = new IstoreArgs(var);
    Istore obj = cacheIstore.get(key);
    if (obj != null)
      return obj;
    obj = new Istore(var);
    cacheIstore.put(key, obj);
    return obj;
  }
  /**
   * store int ''value'' into variable ''#index''
   */
  public static class Istore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Istore(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Istore(" + "var=" + var + ")";
    }
  }
  private static class LstoreArgs {
    private int var;
    public LstoreArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     LstoreArgs other = (LstoreArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<LstoreArgs, Lstore> cacheLstore = new HashMap<LstoreArgs, Lstore>();
  public Lstore newLstore(int var) {
    LstoreArgs key = new LstoreArgs(var);
    Lstore obj = cacheLstore.get(key);
    if (obj != null)
      return obj;
    obj = new Lstore(var);
    cacheLstore.put(key, obj);
    return obj;
  }
  /**
   * store a long ''value'' in a local variable ''#index''
   */
  public static class Lstore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Lstore(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Lstore(" + "var=" + var + ")";
    }
  }
  private static class FstoreArgs {
    private int var;
    public FstoreArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     FstoreArgs other = (FstoreArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<FstoreArgs, Fstore> cacheFstore = new HashMap<FstoreArgs, Fstore>();
  public Fstore newFstore(int var) {
    FstoreArgs key = new FstoreArgs(var);
    Fstore obj = cacheFstore.get(key);
    if (obj != null)
      return obj;
    obj = new Fstore(var);
    cacheFstore.put(key, obj);
    return obj;
  }
  /**
   * store a float ''value'' into a local variable ''#index''
   */
  public static class Fstore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Fstore(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Fstore(" + "var=" + var + ")";
    }
  }
  private static class DstoreArgs {
    private int var;
    public DstoreArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     DstoreArgs other = (DstoreArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<DstoreArgs, Dstore> cacheDstore = new HashMap<DstoreArgs, Dstore>();
  public Dstore newDstore(int var) {
    DstoreArgs key = new DstoreArgs(var);
    Dstore obj = cacheDstore.get(key);
    if (obj != null)
      return obj;
    obj = new Dstore(var);
    cacheDstore.put(key, obj);
    return obj;
  }
  /**
   * store a double ''value'' into a local variable ''#index''
   */
  public static class Dstore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Dstore(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Dstore(" + "var=" + var + ")";
    }
  }
  private static class AstoreArgs {
    private int var;
    public AstoreArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     AstoreArgs other = (AstoreArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<AstoreArgs, Astore> cacheAstore = new HashMap<AstoreArgs, Astore>();
  public Astore newAstore(int var) {
    AstoreArgs key = new AstoreArgs(var);
    Astore obj = cacheAstore.get(key);
    if (obj != null)
      return obj;
    obj = new Astore(var);
    cacheAstore.put(key, obj);
    return obj;
  }
  /**
   * store a reference into a local variable ''#index''
   */
  public static class Astore implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Astore(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Astore(" + "var=" + var + ")";
    }
  }
  private static class RetArgs {
    private int var;
    public RetArgs(int var) {
      this.var = var;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     RetArgs other = (RetArgs)obj;
      if (this.var != other.var)
        return false;
      return true;
    }
  }
  private Map<RetArgs, Ret> cacheRet = new HashMap<RetArgs, Ret>();
  public Ret newRet(int var) {
    RetArgs key = new RetArgs(var);
    Ret obj = cacheRet.get(key);
    if (obj != null)
      return obj;
    obj = new Ret(var);
    cacheRet.put(key, obj);
    return obj;
  }
  /**
   * continue execution from address taken from a local variable ''#index'' (the asymmetry with jsr is intentional)
   */
  public static class Ret implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;

    public Ret(int var) {
      this.var = var;
    }
    public int var() { return var; }
    @Override
    public String toString() {
      return "Ret(" + "var=" + var + ")";
    }
  }
  private static class NewArgs {
    private String type;
    public NewArgs(String type) {
      this.type = type;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.type, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     NewArgs other = (NewArgs)obj;
      if (this.type != other.type)
        return false;
      return true;
    }
  }
  private Map<NewArgs, New> cacheNew = new HashMap<NewArgs, New>();
  public New newNew(String type) {
    NewArgs key = new NewArgs(type);
    New obj = cacheNew.get(key);
    if (obj != null)
      return obj;
    obj = new New(buildClass(type));
    cacheNew.put(key, obj);
    return obj;
  }
  /**
   * create new object of type identified by class reference in constant pool ''index'' (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class New implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Class type;

    public New(Class type) {
      this.type = type;
    }
    public Class type() { return type; }
    @Override
    public String toString() {
      return "New(" + "type=" + type.name() + ")";
    }
  }
  private static class AnewarrayArgs {
    private String type;
    public AnewarrayArgs(String type) {
      this.type = type;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.type, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     AnewarrayArgs other = (AnewarrayArgs)obj;
      if (this.type != other.type)
        return false;
      return true;
    }
  }
  private Map<AnewarrayArgs, Anewarray> cacheAnewarray = new HashMap<AnewarrayArgs, Anewarray>();
  public Anewarray newAnewarray(String type) {
    AnewarrayArgs key = new AnewarrayArgs(type);
    Anewarray obj = cacheAnewarray.get(key);
    if (obj != null)
      return obj;
    obj = new Anewarray(buildType(type));
    cacheAnewarray.put(key, obj);
    return obj;
  }
  /**
   * create a new array of references of length ''count'' and component type identified by the class reference ''index'' (''indexbyte1 &lt;&lt; 8 + indexbyte2'') in the constant pool
   */
  public static class Anewarray implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Type type;

    public Anewarray(Type type) {
      this.type = type;
    }
    public Type type() { return type; }
    @Override
    public String toString() {
      return "Anewarray(" + "type=" + type.name() + ")";
    }
  }
  private static class CheckcastArgs {
    private String type;
    public CheckcastArgs(String type) {
      this.type = type;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.type, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     CheckcastArgs other = (CheckcastArgs)obj;
      if (this.type != other.type)
        return false;
      return true;
    }
  }
  private Map<CheckcastArgs, Checkcast> cacheCheckcast = new HashMap<CheckcastArgs, Checkcast>();
  public Checkcast newCheckcast(String type) {
    CheckcastArgs key = new CheckcastArgs(type);
    Checkcast obj = cacheCheckcast.get(key);
    if (obj != null)
      return obj;
    obj = new Checkcast(buildType(type));
    cacheCheckcast.put(key, obj);
    return obj;
  }
  /**
   * checks whether an ''objectref'' is of a certain type, the class reference of which is in the constant pool at ''index'' (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class Checkcast implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Type type;

    public Checkcast(Type type) {
      this.type = type;
    }
    public Type type() { return type; }
    @Override
    public String toString() {
      return "Checkcast(" + "type=" + type.name() + ")";
    }
  }
  private static class InstanceofArgs {
    private String type;
    public InstanceofArgs(String type) {
      this.type = type;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.type, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     InstanceofArgs other = (InstanceofArgs)obj;
      if (this.type != other.type)
        return false;
      return true;
    }
  }
  private Map<InstanceofArgs, Instanceof> cacheInstanceof = new HashMap<InstanceofArgs, Instanceof>();
  public Instanceof newInstanceof(String type) {
    InstanceofArgs key = new InstanceofArgs(type);
    Instanceof obj = cacheInstanceof.get(key);
    if (obj != null)
      return obj;
    obj = new Instanceof(buildType(type));
    cacheInstanceof.put(key, obj);
    return obj;
  }
  /**
   * determines if an object ''objectref'' is of a given type, identified by class reference ''index'' in constant pool (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class Instanceof implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Type type;

    public Instanceof(Type type) {
      this.type = type;
    }
    public Type type() { return type; }
    @Override
    public String toString() {
      return "Instanceof(" + "type=" + type.name() + ")";
    }
  }
  private static class GetstaticArgs {
    private String owner;
    private String name;
    private String desc;
    public GetstaticArgs(String owner,String name,String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.owner, hash);
      hash = AbstractMethodBuilder.hash(this.name, hash);
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     GetstaticArgs other = (GetstaticArgs)obj;
      if (this.owner != other.owner)
        return false;
      if (this.name != other.name)
        return false;
      if (this.desc != other.desc)
        return false;
      return true;
    }
  }
  private Map<GetstaticArgs, Getstatic> cacheGetstatic = new HashMap<GetstaticArgs, Getstatic>();
  public Getstatic newGetstatic(String owner,String name,String desc) {
    GetstaticArgs key = new GetstaticArgs(owner,name,desc);
    Getstatic obj = cacheGetstatic.get(key);
    if (obj != null)
      return obj;
    obj = new Getstatic(buildClass(owner), name, buildType(desc));
    cacheGetstatic.put(key, obj);
    return obj;
  }
  /**
   * get a static field ''value'' of a class, where the field is identified by field reference in the constant pool ''index'' (''index1 &lt;&lt; 8 + index2'')
   */
  public static class Getstatic implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Class owner;
    private String name;
    private Type desc;

    public Getstatic(Class owner,String name,Type desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    public Class owner() { return owner; }
    public String name() { return name; }
    public Type desc() { return desc; }
    @Override
    public String toString() {
      return "Getstatic(" + "owner=" + owner.name()+ ", " + "name=" + name+ ", " + "desc=" + desc.name() + ")";
    }
  }
  private static class PutstaticArgs {
    private String owner;
    private String name;
    private String desc;
    public PutstaticArgs(String owner,String name,String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.owner, hash);
      hash = AbstractMethodBuilder.hash(this.name, hash);
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     PutstaticArgs other = (PutstaticArgs)obj;
      if (this.owner != other.owner)
        return false;
      if (this.name != other.name)
        return false;
      if (this.desc != other.desc)
        return false;
      return true;
    }
  }
  private Map<PutstaticArgs, Putstatic> cachePutstatic = new HashMap<PutstaticArgs, Putstatic>();
  public Putstatic newPutstatic(String owner,String name,String desc) {
    PutstaticArgs key = new PutstaticArgs(owner,name,desc);
    Putstatic obj = cachePutstatic.get(key);
    if (obj != null)
      return obj;
    obj = new Putstatic(buildClass(owner), name, buildType(desc));
    cachePutstatic.put(key, obj);
    return obj;
  }
  /**
   * set static field to ''value'' in a class, where the field is identified by a field reference ''index'' in constant pool (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class Putstatic implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Class owner;
    private String name;
    private Type desc;

    public Putstatic(Class owner,String name,Type desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    public Class owner() { return owner; }
    public String name() { return name; }
    public Type desc() { return desc; }
    @Override
    public String toString() {
      return "Putstatic(" + "owner=" + owner.name()+ ", " + "name=" + name+ ", " + "desc=" + desc.name() + ")";
    }
  }
  private static class GetfieldArgs {
    private String owner;
    private String name;
    private String desc;
    public GetfieldArgs(String owner,String name,String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.owner, hash);
      hash = AbstractMethodBuilder.hash(this.name, hash);
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     GetfieldArgs other = (GetfieldArgs)obj;
      if (this.owner != other.owner)
        return false;
      if (this.name != other.name)
        return false;
      if (this.desc != other.desc)
        return false;
      return true;
    }
  }
  private Map<GetfieldArgs, Getfield> cacheGetfield = new HashMap<GetfieldArgs, Getfield>();
  public Getfield newGetfield(String owner,String name,String desc) {
    GetfieldArgs key = new GetfieldArgs(owner,name,desc);
    Getfield obj = cacheGetfield.get(key);
    if (obj != null)
      return obj;
    obj = new Getfield(buildClass(owner), name, buildType(desc));
    cacheGetfield.put(key, obj);
    return obj;
  }
  /**
   * get a field ''value'' of an object ''objectref'', where the field is identified by field reference in the constant pool ''index'' (''index1 &lt;&lt; 8 + index2'')
   */
  public static class Getfield implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Class owner;
    private String name;
    private Type desc;

    public Getfield(Class owner,String name,Type desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    public Class owner() { return owner; }
    public String name() { return name; }
    public Type desc() { return desc; }
    @Override
    public String toString() {
      return "Getfield(" + "owner=" + owner.name()+ ", " + "name=" + name+ ", " + "desc=" + desc.name() + ")";
    }
  }
  private static class PutfieldArgs {
    private String owner;
    private String name;
    private String desc;
    public PutfieldArgs(String owner,String name,String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.owner, hash);
      hash = AbstractMethodBuilder.hash(this.name, hash);
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     PutfieldArgs other = (PutfieldArgs)obj;
      if (this.owner != other.owner)
        return false;
      if (this.name != other.name)
        return false;
      if (this.desc != other.desc)
        return false;
      return true;
    }
  }
  private Map<PutfieldArgs, Putfield> cachePutfield = new HashMap<PutfieldArgs, Putfield>();
  public Putfield newPutfield(String owner,String name,String desc) {
    PutfieldArgs key = new PutfieldArgs(owner,name,desc);
    Putfield obj = cachePutfield.get(key);
    if (obj != null)
      return obj;
    obj = new Putfield(buildClass(owner), name, buildType(desc));
    cachePutfield.put(key, obj);
    return obj;
  }
  /**
   * set field to ''value'' in an object ''objectref'', where the field is identified by a field reference ''index'' in constant pool (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class Putfield implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Class owner;
    private String name;
    private Type desc;

    public Putfield(Class owner,String name,Type desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    public Class owner() { return owner; }
    public String name() { return name; }
    public Type desc() { return desc; }
    @Override
    public String toString() {
      return "Putfield(" + "owner=" + owner.name()+ ", " + "name=" + name+ ", " + "desc=" + desc.name() + ")";
    }
  }
  private static class InvokevirtualArgs {
    private String owner;
    private String name;
    private String desc;
    public InvokevirtualArgs(String owner,String name,String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.owner, hash);
      hash = AbstractMethodBuilder.hash(this.name, hash);
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     InvokevirtualArgs other = (InvokevirtualArgs)obj;
      if (this.owner != other.owner)
        return false;
      if (this.name != other.name)
        return false;
      if (this.desc != other.desc)
        return false;
      return true;
    }
  }
  private Map<InvokevirtualArgs, Invokevirtual> cacheInvokevirtual = new HashMap<InvokevirtualArgs, Invokevirtual>();
  public Invokevirtual newInvokevirtual(String owner,String name,String desc) {
    InvokevirtualArgs key = new InvokevirtualArgs(owner,name,desc);
    Invokevirtual obj = cacheInvokevirtual.get(key);
    if (obj != null)
      return obj;
    obj = new Invokevirtual(buildType(owner), buildMethodReference(name, desc));
    cacheInvokevirtual.put(key, obj);
    return obj;
  }
  /**
   * invoke virtual method on object ''objectref'', where the method is identified by method reference ''index'' in constant pool (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class Invokevirtual implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Type owner;
    private MethodReference method;

    public Invokevirtual(Type owner,MethodReference method) {
      this.owner = owner;
      this.method = method;
    }
    public Type owner() { return owner; }
    public MethodReference method() { return method; }
    @Override
    public String toString() {
      return "Invokevirtual(" + "owner=" + owner.name()+ ", " + "method=" + method + ")";
    }
  }
  private static class InvokespecialArgs {
    private String owner;
    private String name;
    private String desc;
    public InvokespecialArgs(String owner,String name,String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.owner, hash);
      hash = AbstractMethodBuilder.hash(this.name, hash);
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     InvokespecialArgs other = (InvokespecialArgs)obj;
      if (this.owner != other.owner)
        return false;
      if (this.name != other.name)
        return false;
      if (this.desc != other.desc)
        return false;
      return true;
    }
  }
  private Map<InvokespecialArgs, Invokespecial> cacheInvokespecial = new HashMap<InvokespecialArgs, Invokespecial>();
  public Invokespecial newInvokespecial(String owner,String name,String desc) {
    InvokespecialArgs key = new InvokespecialArgs(owner,name,desc);
    Invokespecial obj = cacheInvokespecial.get(key);
    if (obj != null)
      return obj;
    obj = new Invokespecial(buildClass(owner), buildMethodReference(name, desc));
    cacheInvokespecial.put(key, obj);
    return obj;
  }
  /**
   * invoke instance method on object ''objectref'', where the method is identified by method reference ''index'' in constant pool (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class Invokespecial implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Class owner;
    private MethodReference method;

    public Invokespecial(Class owner,MethodReference method) {
      this.owner = owner;
      this.method = method;
    }
    public Class owner() { return owner; }
    public MethodReference method() { return method; }
    @Override
    public String toString() {
      return "Invokespecial(" + "owner=" + owner.name()+ ", " + "method=" + method + ")";
    }
  }
  private static class InvokestaticArgs {
    private String owner;
    private String name;
    private String desc;
    public InvokestaticArgs(String owner,String name,String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.owner, hash);
      hash = AbstractMethodBuilder.hash(this.name, hash);
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     InvokestaticArgs other = (InvokestaticArgs)obj;
      if (this.owner != other.owner)
        return false;
      if (this.name != other.name)
        return false;
      if (this.desc != other.desc)
        return false;
      return true;
    }
  }
  private Map<InvokestaticArgs, Invokestatic> cacheInvokestatic = new HashMap<InvokestaticArgs, Invokestatic>();
  public Invokestatic newInvokestatic(String owner,String name,String desc) {
    InvokestaticArgs key = new InvokestaticArgs(owner,name,desc);
    Invokestatic obj = cacheInvokestatic.get(key);
    if (obj != null)
      return obj;
    obj = new Invokestatic(buildClass(owner), buildMethodReference(name, desc));
    cacheInvokestatic.put(key, obj);
    return obj;
  }
  /**
   * invoke a static method, where the method is identified by method reference ''index'' in constant pool (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class Invokestatic implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Class owner;
    private MethodReference method;

    public Invokestatic(Class owner,MethodReference method) {
      this.owner = owner;
      this.method = method;
    }
    public Class owner() { return owner; }
    public MethodReference method() { return method; }
    @Override
    public String toString() {
      return "Invokestatic(" + "owner=" + owner.name()+ ", " + "method=" + method + ")";
    }
  }
  private static class InvokeinterfaceArgs {
    private String owner;
    private String name;
    private String desc;
    public InvokeinterfaceArgs(String owner,String name,String desc) {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.owner, hash);
      hash = AbstractMethodBuilder.hash(this.name, hash);
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     InvokeinterfaceArgs other = (InvokeinterfaceArgs)obj;
      if (this.owner != other.owner)
        return false;
      if (this.name != other.name)
        return false;
      if (this.desc != other.desc)
        return false;
      return true;
    }
  }
  private Map<InvokeinterfaceArgs, Invokeinterface> cacheInvokeinterface = new HashMap<InvokeinterfaceArgs, Invokeinterface>();
  public Invokeinterface newInvokeinterface(String owner,String name,String desc) {
    InvokeinterfaceArgs key = new InvokeinterfaceArgs(owner,name,desc);
    Invokeinterface obj = cacheInvokeinterface.get(key);
    if (obj != null)
      return obj;
    obj = new Invokeinterface(buildClass(owner), buildMethodReference(name, desc));
    cacheInvokeinterface.put(key, obj);
    return obj;
  }
  /**
   * invokes an interface method on object ''objectref'', where the interface method is identified by method reference ''index'' in constant pool (''indexbyte1 &lt;&lt; 8 + indexbyte2'')
   */
  public static class Invokeinterface implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Class owner;
    private MethodReference method;

    public Invokeinterface(Class owner,MethodReference method) {
      this.owner = owner;
      this.method = method;
    }
    public Class owner() { return owner; }
    public MethodReference method() { return method; }
    @Override
    public String toString() {
      return "Invokeinterface(" + "owner=" + owner.name()+ ", " + "method=" + method + ")";
    }
  }
  private static class IfeqArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public IfeqArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IfeqArgs other = (IfeqArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<IfeqArgs, Ifeq> cacheIfeq = new HashMap<IfeqArgs, Ifeq>();
  public Instruction finish(IfeqArgs key) {
    return newIfeq(key.label);
  }
  public Ifeq newIfeq(Label label) {
    IfeqArgs key = new IfeqArgs(label);
    Ifeq obj = cacheIfeq.get(key);
    if (obj != null)
      return obj;
    obj = new Ifeq(buildLabel(label));
    cacheIfeq.put(key, obj);
    return obj;
  }
  /**
   * if ''value'' is 0, branch to instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Ifeq implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Ifeq(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Ifeq(" + "label=" + label + ")";
    }
  }
  private static class IfneArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public IfneArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IfneArgs other = (IfneArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<IfneArgs, Ifne> cacheIfne = new HashMap<IfneArgs, Ifne>();
  public Instruction finish(IfneArgs key) {
    return newIfne(key.label);
  }
  public Ifne newIfne(Label label) {
    IfneArgs key = new IfneArgs(label);
    Ifne obj = cacheIfne.get(key);
    if (obj != null)
      return obj;
    obj = new Ifne(buildLabel(label));
    cacheIfne.put(key, obj);
    return obj;
  }
  /**
   * if ''value'' is not 0, branch to instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Ifne implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Ifne(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Ifne(" + "label=" + label + ")";
    }
  }
  private static class IfltArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public IfltArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IfltArgs other = (IfltArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<IfltArgs, Iflt> cacheIflt = new HashMap<IfltArgs, Iflt>();
  public Instruction finish(IfltArgs key) {
    return newIflt(key.label);
  }
  public Iflt newIflt(Label label) {
    IfltArgs key = new IfltArgs(label);
    Iflt obj = cacheIflt.get(key);
    if (obj != null)
      return obj;
    obj = new Iflt(buildLabel(label));
    cacheIflt.put(key, obj);
    return obj;
  }
  /**
   * if ''value'' is less than 0, branch to instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Iflt implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Iflt(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Iflt(" + "label=" + label + ")";
    }
  }
  private static class IfgeArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public IfgeArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IfgeArgs other = (IfgeArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<IfgeArgs, Ifge> cacheIfge = new HashMap<IfgeArgs, Ifge>();
  public Instruction finish(IfgeArgs key) {
    return newIfge(key.label);
  }
  public Ifge newIfge(Label label) {
    IfgeArgs key = new IfgeArgs(label);
    Ifge obj = cacheIfge.get(key);
    if (obj != null)
      return obj;
    obj = new Ifge(buildLabel(label));
    cacheIfge.put(key, obj);
    return obj;
  }
  /**
   * if ''value'' is greater than or equal to 0, branch to instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Ifge implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Ifge(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Ifge(" + "label=" + label + ")";
    }
  }
  private static class IfgtArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public IfgtArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IfgtArgs other = (IfgtArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<IfgtArgs, Ifgt> cacheIfgt = new HashMap<IfgtArgs, Ifgt>();
  public Instruction finish(IfgtArgs key) {
    return newIfgt(key.label);
  }
  public Ifgt newIfgt(Label label) {
    IfgtArgs key = new IfgtArgs(label);
    Ifgt obj = cacheIfgt.get(key);
    if (obj != null)
      return obj;
    obj = new Ifgt(buildLabel(label));
    cacheIfgt.put(key, obj);
    return obj;
  }
  /**
   * if ''value'' is greater than 0, branch to instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Ifgt implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Ifgt(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Ifgt(" + "label=" + label + ")";
    }
  }
  private static class IfleArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public IfleArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IfleArgs other = (IfleArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<IfleArgs, Ifle> cacheIfle = new HashMap<IfleArgs, Ifle>();
  public Instruction finish(IfleArgs key) {
    return newIfle(key.label);
  }
  public Ifle newIfle(Label label) {
    IfleArgs key = new IfleArgs(label);
    Ifle obj = cacheIfle.get(key);
    if (obj != null)
      return obj;
    obj = new Ifle(buildLabel(label));
    cacheIfle.put(key, obj);
    return obj;
  }
  /**
   * if ''value'' is less than or equal to 0, branch to instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Ifle implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Ifle(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Ifle(" + "label=" + label + ")";
    }
  }
  private static class GotoArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public GotoArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     GotoArgs other = (GotoArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<GotoArgs, Goto> cacheGoto = new HashMap<GotoArgs, Goto>();
  public Instruction finish(GotoArgs key) {
    return newGoto(key.label);
  }
  public Goto newGoto(Label label) {
    GotoArgs key = new GotoArgs(label);
    Goto obj = cacheGoto.get(key);
    if (obj != null)
      return obj;
    obj = new Goto(buildLabel(label));
    cacheGoto.put(key, obj);
    return obj;
  }
  /**
   * goes to another instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Goto implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Goto(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Goto(" + "label=" + label + ")";
    }
  }
  private static class JsrArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public JsrArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     JsrArgs other = (JsrArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<JsrArgs, Jsr> cacheJsr = new HashMap<JsrArgs, Jsr>();
  public Instruction finish(JsrArgs key) {
    return newJsr(key.label);
  }
  public Jsr newJsr(Label label) {
    JsrArgs key = new JsrArgs(label);
    Jsr obj = cacheJsr.get(key);
    if (obj != null)
      return obj;
    obj = new Jsr(buildLabel(label));
    cacheJsr.put(key, obj);
    return obj;
  }
  /**
   * jump to subroutine at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'') and place the return address on the stack
   */
  public static class Jsr implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Jsr(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Jsr(" + "label=" + label + ")";
    }
  }
  private static class IfnullArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public IfnullArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IfnullArgs other = (IfnullArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<IfnullArgs, Ifnull> cacheIfnull = new HashMap<IfnullArgs, Ifnull>();
  public Instruction finish(IfnullArgs key) {
    return newIfnull(key.label);
  }
  public Ifnull newIfnull(Label label) {
    IfnullArgs key = new IfnullArgs(label);
    Ifnull obj = cacheIfnull.get(key);
    if (obj != null)
      return obj;
    obj = new Ifnull(buildLabel(label));
    cacheIfnull.put(key, obj);
    return obj;
  }
  /**
   * if ''value'' is null, branch to instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Ifnull implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Ifnull(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Ifnull(" + "label=" + label + ")";
    }
  }
  private static class IfnonnullArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label label;
    public IfnonnullArgs(Label label) {
      this.label = label;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.label, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IfnonnullArgs other = (IfnonnullArgs)obj;
      if (this.label != other.label)
        return false;
      return true;
    }
  }
  private Map<IfnonnullArgs, Ifnonnull> cacheIfnonnull = new HashMap<IfnonnullArgs, Ifnonnull>();
  public Instruction finish(IfnonnullArgs key) {
    return newIfnonnull(key.label);
  }
  public Ifnonnull newIfnonnull(Label label) {
    IfnonnullArgs key = new IfnonnullArgs(label);
    Ifnonnull obj = cacheIfnonnull.get(key);
    if (obj != null)
      return obj;
    obj = new Ifnonnull(buildLabel(label));
    cacheIfnonnull.put(key, obj);
    return obj;
  }
  /**
   * if ''value'' is not null, branch to instruction at ''branchoffset'' (signed short constructed from unsigned bytes ''branchbyte1 &lt;&lt; 8 + branchbyte2'')
   */
  public static class Ifnonnull implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int label;

    public Ifnonnull(int label) {
      this.label = label;
    }
    public int label() { return label; }
    @Override
    public String toString() {
      return "Ifnonnull(" + "label=" + label + ")";
    }
  }
  private static class LdcArgs {
    private Object cst;
    public LdcArgs(Object cst) {
      this.cst = cst;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.cst, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     LdcArgs other = (LdcArgs)obj;
      if (this.cst != other.cst)
        return false;
      return true;
    }
  }
  private Map<LdcArgs, Ldc> cacheLdc = new HashMap<LdcArgs, Ldc>();
  public Ldc newLdc(Object cst) {
    LdcArgs key = new LdcArgs(cst);
    Ldc obj = cacheLdc.get(key);
    if (obj != null)
      return obj;
    obj = new Ldc(cst);
    cacheLdc.put(key, obj);
    return obj;
  }
  /**
   * push a constant ''#index'' from a constant pool (String, int or float) onto the stack
   */
  public static class Ldc implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Object cst;

    public Ldc(Object cst) {
      this.cst = cst;
    }
    public Object cst() { return cst; }
    @Override
    public String toString() {
      return "Ldc(" + "cst=" + cst + ")";
    }
  }
  private static class IincArgs {
    private int var;
    private int increment;
    public IincArgs(int var,int increment) {
      this.var = var;
      this.increment = increment;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.var, hash);
      hash = AbstractMethodBuilder.hash(this.increment, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     IincArgs other = (IincArgs)obj;
      if (this.var != other.var)
        return false;
      if (this.increment != other.increment)
        return false;
      return true;
    }
  }
  private Map<IincArgs, Iinc> cacheIinc = new HashMap<IincArgs, Iinc>();
  public Iinc newIinc(int var,int increment) {
    IincArgs key = new IincArgs(var,increment);
    Iinc obj = cacheIinc.get(key);
    if (obj != null)
      return obj;
    obj = new Iinc(var, increment);
    cacheIinc.put(key, obj);
    return obj;
  }
  /**
   * increment local variable ''#index'' by signed byte ''const''
   */
  public static class Iinc implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int var;
    private int increment;

    public Iinc(int var,int increment) {
      this.var = var;
      this.increment = increment;
    }
    public int var() { return var; }
    public int increment() { return increment; }
    @Override
    public String toString() {
      return "Iinc(" + "var=" + var+ ", " + "increment=" + increment + ")";
    }
  }
  private static class TableswitchArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private int min;
    private int max;
    private Label dflt;
    private Label[] labels;
    public TableswitchArgs(int min,int max,Label dflt,Label[] labels) {
      this.min = min;
      this.max = max;
      this.dflt = dflt;
      this.labels = labels;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.min, hash);
      hash = AbstractMethodBuilder.hash(this.max, hash);
      hash = AbstractMethodBuilder.hash(this.dflt, hash);
      hash = AbstractMethodBuilder.hash(this.labels, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     TableswitchArgs other = (TableswitchArgs)obj;
      if (this.min != other.min)
        return false;
      if (this.max != other.max)
        return false;
      if (this.dflt != other.dflt)
        return false;
      if (this.labels != other.labels)
        return false;
      return true;
    }
  }
  private Map<TableswitchArgs, Tableswitch> cacheTableswitch = new HashMap<TableswitchArgs, Tableswitch>();
  public Instruction finish(TableswitchArgs key) {
    return newTableswitch(key.min, key.max, key.dflt, key.labels);
  }
  public Tableswitch newTableswitch(int min,int max,Label dflt,Label[] labels) {
    TableswitchArgs key = new TableswitchArgs(min,max,dflt,labels);
    Tableswitch obj = cacheTableswitch.get(key);
    if (obj != null)
      return obj;
    obj = new Tableswitch(min, max, buildLabel(dflt), buildLabels(labels));
    cacheTableswitch.put(key, obj);
    return obj;
  }
  /**
   * continue execution from an address in the table at offset ''index''
   */
  public static class Tableswitch implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int min;
    private int max;
    private int dflt;
    private int[] labels;

    public Tableswitch(int min,int max,int dflt,int[] labels) {
      this.min = min;
      this.max = max;
      this.dflt = dflt;
      this.labels = labels;
    }
    public int min() { return min; }
    public int max() { return max; }
    public int dflt() { return dflt; }
    public int[] labels() { return labels; }
    @Override
    public String toString() {
      return "Tableswitch(" + "min=" + min+ ", " + "max=" + max+ ", " + "dflt=" + dflt+ ", " + "labels=" + labels + ")";
    }
  }
  private static class LookupswitchArgs implements HalfBuiltInstruction {
    @Override
    public Instruction finish(AbstractMethodBuilder builder) {
      return builder.finish(this);
    }
    @Override
    public void visit(InstructionVisitor v) {
      assert false;
    }
    private Label dflt;
    private int[] keys;
    private Label[] labels;
    public LookupswitchArgs(Label dflt,int[] keys,Label[] labels) {
      this.dflt = dflt;
      this.keys = keys;
      this.labels = labels;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.dflt, hash);
      hash = AbstractMethodBuilder.hash(this.keys, hash);
      hash = AbstractMethodBuilder.hash(this.labels, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     LookupswitchArgs other = (LookupswitchArgs)obj;
      if (this.dflt != other.dflt)
        return false;
      if (this.keys != other.keys)
        return false;
      if (this.labels != other.labels)
        return false;
      return true;
    }
  }
  private Map<LookupswitchArgs, Lookupswitch> cacheLookupswitch = new HashMap<LookupswitchArgs, Lookupswitch>();
  public Instruction finish(LookupswitchArgs key) {
    return newLookupswitch(key.dflt, key.keys, key.labels);
  }
  public Lookupswitch newLookupswitch(Label dflt,int[] keys,Label[] labels) {
    LookupswitchArgs key = new LookupswitchArgs(dflt,keys,labels);
    Lookupswitch obj = cacheLookupswitch.get(key);
    if (obj != null)
      return obj;
    obj = new Lookupswitch(buildLabel(dflt), keys, buildLabels(labels));
    cacheLookupswitch.put(key, obj);
    return obj;
  }
  /**
   * a target address is looked up from a table using a key and execution continues from the instruction at that address
   */
  public static class Lookupswitch implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private int dflt;
    private int[] keys;
    private int[] labels;

    public Lookupswitch(int dflt,int[] keys,int[] labels) {
      this.dflt = dflt;
      this.keys = keys;
      this.labels = labels;
    }
    public int dflt() { return dflt; }
    public int[] keys() { return keys; }
    public int[] labels() { return labels; }
    @Override
    public String toString() {
      return "Lookupswitch(" + "dflt=" + dflt+ ", " + "keys=" + keys+ ", " + "labels=" + labels + ")";
    }
  }
  private static class MultianewarrayArgs {
    private String desc;
    private int dims;
    public MultianewarrayArgs(String desc,int dims) {
      this.desc = desc;
      this.dims = dims;
    }
    @Override
    public int hashCode() {
      int hash = 33;
      hash = AbstractMethodBuilder.hash(this.desc, hash);
      hash = AbstractMethodBuilder.hash(this.dims, hash);
      return hash;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
     MultianewarrayArgs other = (MultianewarrayArgs)obj;
      if (this.desc != other.desc)
        return false;
      if (this.dims != other.dims)
        return false;
      return true;
    }
  }
  private Map<MultianewarrayArgs, Multianewarray> cacheMultianewarray = new HashMap<MultianewarrayArgs, Multianewarray>();
  public Multianewarray newMultianewarray(String desc,int dims) {
    MultianewarrayArgs key = new MultianewarrayArgs(desc,dims);
    Multianewarray obj = cacheMultianewarray.get(key);
    if (obj != null)
      return obj;
    obj = new Multianewarray(buildType(desc), dims);
    cacheMultianewarray.put(key, obj);
    return obj;
  }
  /**
   * create a new array of ''dimensions'' dimensions with elements of type identified by class reference in constant pool ''index'' (''indexbyte1 &lt;&lt; 8 + indexbyte2''); the sizes of each dimension is identified by ''count1'', [''count2'', etc.]
   */
  public static class Multianewarray implements Instruction {
    @Override
    public void visit(InstructionVisitor visitor) { visitor.visit(this); }
    private Type desc;
    private int dims;

    public Multianewarray(Type desc,int dims) {
      this.desc = desc;
      this.dims = dims;
    }
    public Type desc() { return desc; }
    public int dims() { return dims; }
    @Override
    public String toString() {
      return "Multianewarray(" + "desc=" + desc.name()+ ", " + "dims=" + dims + ")";
    }
  }

  @Override
  public void visitInsn(int opcode) {
    switch (opcode) {
      case 0: addInstruction(newNop()); break;
      case 1: addInstruction(newAConstNull()); break;
      case 2: addInstruction(newIConst(-1)); break;
      case 3: addInstruction(newIConst(0)); break;
      case 4: addInstruction(newIConst(1)); break;
      case 5: addInstruction(newIConst(2)); break;
      case 6: addInstruction(newIConst(3)); break;
      case 7: addInstruction(newIConst(4)); break;
      case 8: addInstruction(newIConst(5)); break;
      case 9: addInstruction(newLConst(0)); break;
      case 10: addInstruction(newLConst(1)); break;
      case 11: addInstruction(newFConst(0)); break;
      case 12: addInstruction(newFConst(1)); break;
      case 13: addInstruction(newFConst(2)); break;
      case 14: addInstruction(newDConst(0)); break;
      case 15: addInstruction(newDConst(1)); break;
      case 46: addInstruction(newIaload()); break;
      case 47: addInstruction(newLaload()); break;
      case 48: addInstruction(newFaload()); break;
      case 49: addInstruction(newDaload()); break;
      case 50: addInstruction(newAaload()); break;
      case 51: addInstruction(newBaload()); break;
      case 52: addInstruction(newCaload()); break;
      case 53: addInstruction(newSaload()); break;
      case 79: addInstruction(newIastore()); break;
      case 80: addInstruction(newLastore()); break;
      case 81: addInstruction(newFastore()); break;
      case 82: addInstruction(newDastore()); break;
      case 83: addInstruction(newAastore()); break;
      case 84: addInstruction(newBastore()); break;
      case 85: addInstruction(newCastore()); break;
      case 86: addInstruction(newSastore()); break;
      case 87: addInstruction(newPop()); break;
      case 88: addInstruction(newPop2()); break;
      case 89: addInstruction(newDup()); break;
      case 90: addInstruction(newDup_x1()); break;
      case 91: addInstruction(newDup_x2()); break;
      case 92: addInstruction(newDup2()); break;
      case 93: addInstruction(newDup2_x1()); break;
      case 94: addInstruction(newDup2_x2()); break;
      case 95: addInstruction(newSwap()); break;
      case 96: addInstruction(newAdd()); break;
      case 97: addInstruction(newAdd()); break;
      case 98: addInstruction(newAdd()); break;
      case 99: addInstruction(newAdd()); break;
      case 100: addInstruction(newSub()); break;
      case 101: addInstruction(newSub()); break;
      case 102: addInstruction(newSub()); break;
      case 103: addInstruction(newSub()); break;
      case 104: addInstruction(newMul()); break;
      case 105: addInstruction(newMul()); break;
      case 106: addInstruction(newMul()); break;
      case 107: addInstruction(newMul()); break;
      case 108: addInstruction(newDiv()); break;
      case 109: addInstruction(newDiv()); break;
      case 110: addInstruction(newDiv()); break;
      case 111: addInstruction(newDiv()); break;
      case 112: addInstruction(newRem()); break;
      case 113: addInstruction(newRem()); break;
      case 114: addInstruction(newRem()); break;
      case 115: addInstruction(newRem()); break;
      case 116: addInstruction(newIneg()); break;
      case 117: addInstruction(newLneg()); break;
      case 118: addInstruction(newFneg()); break;
      case 119: addInstruction(newDneg()); break;
      case 120: addInstruction(newShl()); break;
      case 121: addInstruction(newShl()); break;
      case 122: addInstruction(newShr()); break;
      case 123: addInstruction(newShr()); break;
      case 124: addInstruction(newUshr()); break;
      case 125: addInstruction(newUshr()); break;
      case 126: addInstruction(newAnd()); break;
      case 127: addInstruction(newAnd()); break;
      case 128: addInstruction(newOr()); break;
      case 129: addInstruction(newOr()); break;
      case 130: addInstruction(newXor()); break;
      case 131: addInstruction(newXor()); break;
      case 133: addInstruction(newI2l()); break;
      case 134: addInstruction(newI2f()); break;
      case 135: addInstruction(newI2d()); break;
      case 136: addInstruction(newL2i()); break;
      case 137: addInstruction(newL2f()); break;
      case 138: addInstruction(newL2d()); break;
      case 139: addInstruction(newF2i()); break;
      case 140: addInstruction(newF2l()); break;
      case 141: addInstruction(newF2d()); break;
      case 142: addInstruction(newD2i()); break;
      case 143: addInstruction(newD2l()); break;
      case 144: addInstruction(newD2f()); break;
      case 145: addInstruction(newI2b()); break;
      case 146: addInstruction(newI2c()); break;
      case 147: addInstruction(newI2s()); break;
      case 148: addInstruction(newLcmp()); break;
      case 149: addInstruction(newFcmpl()); break;
      case 150: addInstruction(newFcmpg()); break;
      case 151: addInstruction(newDcmpl()); break;
      case 152: addInstruction(newDcmpg()); break;
      case 172: addInstruction(newIreturn()); break;
      case 173: addInstruction(newLreturn()); break;
      case 174: addInstruction(newFreturn()); break;
      case 175: addInstruction(newDreturn()); break;
      case 176: addInstruction(newAreturn()); break;
      case 177: addInstruction(newReturnv()); break;
      case 190: addInstruction(newArraylength()); break;
      case 191: addInstruction(newAthrow()); break;
      case 194: addInstruction(newMonitorenter()); break;
      case 195: addInstruction(newMonitorexit()); break;
    }
  }

  @Override
  public void visitIntInsn(int opcode, int operand) {
    switch (opcode) {
      case 16: addInstruction(newBipush(operand)); break;
      case 17: addInstruction(newSipush(operand)); break;
      case 188: addInstruction(newNewarray(operand)); break;
    }
  }

  @Override
  public void visitVarInsn(int opcode, int var) {
    switch (opcode) {
      case 21: addInstruction(newIload(var)); break;
      case 22: addInstruction(newLload(var)); break;
      case 23: addInstruction(newFload(var)); break;
      case 24: addInstruction(newDload(var)); break;
      case 25: addInstruction(newAload(var)); break;
      case 54: addInstruction(newIstore(var)); break;
      case 55: addInstruction(newLstore(var)); break;
      case 56: addInstruction(newFstore(var)); break;
      case 57: addInstruction(newDstore(var)); break;
      case 58: addInstruction(newAstore(var)); break;
      case 169: addInstruction(newRet(var)); break;
    }
  }

  @Override
  public void visitTypeInsn(int opcode, String type) {
    switch (opcode) {
      case 187: addInstruction(newNew(type)); break;
      case 189: addInstruction(newAnewarray(type)); break;
      case 192: addInstruction(newCheckcast(type)); break;
      case 193: addInstruction(newInstanceof(type)); break;
    }
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    switch (opcode) {
      case 178: addInstruction(newGetstatic(owner, name, desc)); break;
      case 179: addInstruction(newPutstatic(owner, name, desc)); break;
      case 180: addInstruction(newGetfield(owner, name, desc)); break;
      case 181: addInstruction(newPutfield(owner, name, desc)); break;
    }
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String desc) {
    switch (opcode) {
      case 182: addInstruction(newInvokevirtual(owner, name, desc)); break;
      case 183: addInstruction(newInvokespecial(owner, name, desc)); break;
      case 184: addInstruction(newInvokestatic(owner, name, desc)); break;
      case 185: addInstruction(newInvokeinterface(owner, name, desc)); break;
    }
  }

  @Override
  public void visitJumpInsn(int opcode, Label label) {
    switch (opcode) {
      case 153: addHalfBuiltInstruction(new IfeqArgs(label)); break;
      case 154: addHalfBuiltInstruction(new IfneArgs(label)); break;
      case 155: addHalfBuiltInstruction(new IfltArgs(label)); break;
      case 156: addHalfBuiltInstruction(new IfgeArgs(label)); break;
      case 157: addHalfBuiltInstruction(new IfgtArgs(label)); break;
      case 158: addHalfBuiltInstruction(new IfleArgs(label)); break;
      case 159: addInstruction(newIcmp()); addHalfBuiltInstruction(new IfeqArgs(label)); break;
      case 160: addInstruction(newIcmp()); addHalfBuiltInstruction(new IfneArgs(label)); break;
      case 161: addInstruction(newIcmp()); addHalfBuiltInstruction(new IfltArgs(label)); break;
      case 162: addInstruction(newIcmp()); addHalfBuiltInstruction(new IfgeArgs(label)); break;
      case 163: addInstruction(newIcmp()); addHalfBuiltInstruction(new IfgtArgs(label)); break;
      case 164: addInstruction(newIcmp()); addHalfBuiltInstruction(new IfleArgs(label)); break;
      case 165: addInstruction(newAcmp()); addHalfBuiltInstruction(new IfeqArgs(label)); break;
      case 166: addInstruction(newAcmp()); addHalfBuiltInstruction(new IfneArgs(label)); break;
      case 167: addHalfBuiltInstruction(new GotoArgs(label)); break;
      case 168: addHalfBuiltInstruction(new JsrArgs(label)); break;
      case 198: addHalfBuiltInstruction(new IfnullArgs(label)); break;
      case 199: addHalfBuiltInstruction(new IfnonnullArgs(label)); break;
    }
  }

  @Override
  public void visitLdcInsn(Object cst) {
    Instruction i = null;
    if (cst instanceof Integer)
      i = newIConst((Integer)cst);
    else if (cst instanceof Long)
      i = newLConst((Long)cst);
    else if (cst instanceof Float)
      i = newFConst((Float)cst);
    else if (cst instanceof Double)
      i = newDConst((Double)cst);
    else if (cst instanceof String)
      i = newStrconst((String)cst);
    else if (cst instanceof org.objectweb.asm.Type)
      i = newTypeconst(((org.objectweb.asm.Type)cst).getInternalName());
    else
      addInstruction(newLdc(cst));
    if (i != null)
      addInstruction(i);
  }

  @Override
  public void visitIincInsn(int var, int increment) {
    addInstruction(newIinc(var, increment));
  }

  @Override
  public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
    addHalfBuiltInstruction(new TableswitchArgs(min, max, dflt, labels));
  }

  @Override
  public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    addHalfBuiltInstruction(new LookupswitchArgs(dflt, keys, labels));
  }

  @Override
  public void visitMultiANewArrayInsn(String desc, int dims) {
    addInstruction(newMultianewarray(desc, dims));
  }

  private Type int2Primitive(int code) {
    if (code == Opcodes.T_BOOLEAN)
      return buildType("[Z");
    else if (code == Opcodes.T_CHAR)
      return buildType("[C");
    else if (code == Opcodes.T_CHAR)
      return buildType("[C");
    else if (code == Opcodes.T_FLOAT)
      return buildType("[F");
    else if (code == Opcodes.T_DOUBLE)
      return buildType("[D");
    else if (code == Opcodes.T_BYTE)
      return buildType("[B");
    else if (code == Opcodes.T_SHORT)
      return buildType("[S");
    else if (code == Opcodes.T_INT)
      return buildType("[I");
    else if (code == Opcodes.T_LONG)
      return buildType("[J");
    throw new AssertionError("Unknown code: " + code);
  }


  protected abstract void addHalfBuiltInstruction(HalfBuiltInstruction i);
  protected abstract void addInstruction(Instruction i);
  protected abstract Type buildType(String typeName);
  protected abstract MethodReference buildMethodReference(String methodName, String descriptor);
  protected abstract Class buildClass(String className);
  protected abstract Primitives.Int buildPrimitive(int value);
  protected abstract Primitives.Long buildPrimitive(long value);
  protected abstract Primitives.Double buildPrimitive(double value);
  protected abstract Primitives.Float buildPrimitive(float value);
  protected abstract int buildLabel(Label label);
  private int[] buildLabels(Label[] labels) { 
    int[] newLabels = new int[labels.length];
    for (int i = 0; i < labels.length; i++) {
      newLabels[i] = buildLabel(labels[i]);
    }
    return newLabels;
  }
}

