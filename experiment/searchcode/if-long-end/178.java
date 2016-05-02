/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.asdec.abc.avm2;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.ABCInputStream;
import com.jpexs.asdec.abc.CopyOutputStream;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.IfTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.arithmetic.*;
import com.jpexs.asdec.abc.avm2.instructions.bitwise.*;
import com.jpexs.asdec.abc.avm2.instructions.comparsion.*;
import com.jpexs.asdec.abc.avm2.instructions.construction.*;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugFileIns;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugIns;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugLineIns;
import com.jpexs.asdec.abc.avm2.instructions.executing.*;
import com.jpexs.asdec.abc.avm2.instructions.jumps.*;
import com.jpexs.asdec.abc.avm2.instructions.localregs.*;
import com.jpexs.asdec.abc.avm2.instructions.other.*;
import com.jpexs.asdec.abc.avm2.instructions.stack.*;
import com.jpexs.asdec.abc.avm2.instructions.types.*;
import com.jpexs.asdec.abc.avm2.instructions.xml.*;
import com.jpexs.asdec.abc.avm2.treemodel.*;
import com.jpexs.asdec.abc.avm2.treemodel.clauses.*;
import com.jpexs.asdec.abc.avm2.treemodel.operations.AndTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.OrTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.PreDecrementTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.PreIncrementTreeItem;
import com.jpexs.asdec.abc.types.ABCException;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.helpers.Highlighting;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AVM2Code {

    public ArrayList<AVM2Instruction> code = new ArrayList<AVM2Instruction>();
    public static boolean DEBUG_REWRITE=false;
    public static final int OPT_U30 = 0x100;
    public static final int OPT_U8 = 0x200;
    public static final int OPT_S24 = 0x300;
    public static final int OPT_CASE_OFFSETS = 0x400;
    public static final int OPT_BYTE = 0x500;
    public static final int DAT_MULTINAME_INDEX = OPT_U30 + 0x01;
    public static final int DAT_ARG_COUNT = OPT_U30 + 0x02;
    public static final int DAT_METHOD_INDEX = OPT_U30 + 0x03;
    public static final int DAT_STRING_INDEX = OPT_U30 + 0x04;
    public static final int DAT_DEBUG_TYPE = OPT_U8 + 0x05;
    public static final int DAT_REGISTER_INDEX = OPT_U8 + 0x06;
    public static final int DAT_LINENUM = OPT_U30 + 0x07;
    public static final int DAT_LOCAL_REG_INDEX = OPT_U30 + 0x08;
    public static final int DAT_SLOT_INDEX = OPT_U30 + 0x09;
    public static final int DAT_SLOT_SCOPE_INDEX = OPT_U30 + 0x0A;
    public static final int DAT_OFFSET = OPT_S24 + 0x0B;
    public static final int DAT_EXCEPTION_INDEX = OPT_U30 + 0x0C;
    public static final int DAT_CLASS_INDEX = OPT_U30 + 0x0D;
    public static final int DAT_INT_INDEX = OPT_U30 + 0x0E;
    public static final int DAT_UINT_INDEX = OPT_U30 + 0x0F;
    public static final int DAT_DOUBLE_INDEX = OPT_U30 + 0x10;
    public static final int DAT_DECIMAL_INDEX = OPT_U30 + 0x11;
    public static final int DAT_CASE_BASEOFFSET = OPT_S24 + 0x12;
    public static InstructionDefinition instructionSet[] = new InstructionDefinition[]{
            new AddIns(),
            new InstructionDefinition(0x9b,"add_d",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  return -2+1; //?
               }

            },
            new AddIIns(),
            new InstructionDefinition(0xb5,"add_p",new int[]{AVM2Code.OPT_U30}),
            new ApplyTypeIns(),
            new AsTypeIns(),
            new AsTypeLateIns(),
            new BitAndIns(),
            new BitNotIns(),
            new BitOrIns(),
            new BitXorIns(),
            new InstructionDefinition(0x01,"bkpt",new int[]{}),
            new InstructionDefinition(0xf2,"bkptline",new int[]{AVM2Code.OPT_U30}),
            new CallIns(),
            new InstructionDefinition(0x4d,"callinterface",new int[]{AVM2Code.OPT_U30}),
            new CallMethodIns(),
            new CallPropertyIns(),
            new CallPropLexIns(),
            new CallPropVoidIns(),
            new CallStaticIns(),
            new CallSuperIns(),
            new InstructionDefinition(0x4b,"callsuperid",new int[]{}),
            new CallSuperVoidIns(),
            new CheckFilterIns(),
            new CoerceIns(),
            new CoerceAIns(),
            new InstructionDefinition(0x81,"coerce_b",new int[]{}), //stack:-1+1
            new InstructionDefinition(0x84,"coerce_d",new int[]{}), //stack:-1+1
            new InstructionDefinition(0x83,"coerce_i",new int[]{}), //stack:-1+1
            new InstructionDefinition(0x89,"coerce_o",new int[]{}), //stack:-1+1
            new CoerceSIns(),
            new InstructionDefinition(0x88,"coerce_u",new int[]{}), //stack:-1+1
            new InstructionDefinition(0x9a,"concat",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  return -2+1; //?
               }

            },
            new ConstructIns(),
            new ConstructPropIns(),
            new ConstructSuperIns(),
            new ConvertBIns(),
            new ConvertIIns(),
            new ConvertDIns(),
            new ConvertOIns(),
            new ConvertUIns(),
            new ConvertSIns(),
            new InstructionDefinition(0x79,"convert_m",new int[]{}), //-1 +1
            new InstructionDefinition(0x7a,"convert_m_p",new int[]{AVM2Code.OPT_U30 /*param (?)*/}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new DebugIns(),
            new DebugFileIns(),
            new DebugLineIns(),
            new DecLocalIns(),
            new DecLocalIIns(),
            new DecrementIns(),
            new DecrementIIns(),
             new InstructionDefinition(0x5b,"deldescendants",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new DeletePropertyIns(),
            new InstructionDefinition(0x6b,"deletepropertylate",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new DivideIns(),
            new InstructionDefinition(0xb8,"divide_p",new int[]{AVM2Code.OPT_U30}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  return -2+1; //?
               }

            },
            new DupIns(),
            new DXNSIns(),
            new DXNSLateIns(),
            new EqualsIns(),
            new EscXAttrIns(),
            new EscXElemIns(),
            new InstructionDefinition(0x5f,"finddef",new int[]{AVM2Code.DAT_MULTINAME_INDEX}),
            /* //Duplicate OPCODE with deldescendants. Prefering deldescendants (found in FLEX compiler)
             new InstructionDefinition(0x5b,"findpropglobalstrict",new int[]{AVM2Code.DAT_MULTINAME_INDEX}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },*/
            new InstructionDefinition(0x5c,"findpropglobal",new int[]{AVM2Code.DAT_MULTINAME_INDEX}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new FindPropertyIns(),
            new FindPropertyStrictIns(),
            new GetDescendantsIns(),
            new GetGlobalScopeIns(),
            new GetGlobalSlotIns(),
            new GetLexIns(),
            new GetLocalIns(),
            new GetLocal0Ins(),
            new GetLocal1Ins(),
            new GetLocal2Ins(),
            new GetLocal3Ins(),
            new InstructionDefinition(0x67,"getouterscope",new int[]{AVM2Code.DAT_MULTINAME_INDEX}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new GetPropertyIns(),
            new GetScopeObjectIns(),
            new GetSlotIns(),
            new GetSuperIns(),
            new GreaterEqualsIns(),
            new GreaterThanIns(),
            new HasNextIns(),
            new HasNext2Ins(),
            new IfEqIns(),
            new IfFalseIns(),
            new IfGeIns(),
            new IfGtIns(),
            new IfLeIns(),
            new IfLtIns(),
            new IfNGeIns(),
            new IfNGtIns(),
            new IfNLeIns(),
            new IfNLtIns(),
            new IfNeIns(),
            new IfStrictEqIns(),
            new IfStrictNeIns(),
            new IfTrueIns(),
            new InIns(),
            new IncLocalIns(),
            new IncLocalIIns(),
            new IncrementIns(),
            new IncrementIIns(),
            new InstructionDefinition(0x9c,"increment_p",new int[]{AVM2Code.OPT_U30 /*param*/}),
            new InstructionDefinition(0x9d,"inclocal_p",new int[]{AVM2Code.OPT_U30 /*param*/,AVM2Code.DAT_REGISTER_INDEX}),
            new InstructionDefinition(0x9e,"decrement_p",new int[]{AVM2Code.OPT_U30 /*param*/}),
            new InstructionDefinition(0x9f,"declocal_p",new int[]{AVM2Code.OPT_U30 /*param*/,AVM2Code.DAT_REGISTER_INDEX}),
            new InitPropertyIns(),
            new InstanceOfIns(),
            new IsTypeIns(),
            new IsTypeLateIns(),
            new JumpIns(),
            new KillIns(),
            new LabelIns(),
            new LessEqualsIns(),
            new LessThanIns(),
            new LookupSwitchIns(),
            new LShiftIns(),
            new ModuloIns(),
            new InstructionDefinition(0xb9,"modulo_p",new int[]{AVM2Code.OPT_U30}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  return -2+1; //?
               }

            },
            new MultiplyIns(),
            new MultiplyIIns(),
            new InstructionDefinition(0xb7,"multiply_p",new int[]{AVM2Code.OPT_U30}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  return -2+1; //?
               }

            },
            new NegateIns(),
            new NegateIIns(),
            new InstructionDefinition(0x8f,"negate_p",new int[]{AVM2Code.OPT_U30 /* param */}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new NewActivationIns(),
            new NewArrayIns(),
            new NewCatchIns(),
            new NewClassIns(),
            new NewFunctionIns(),
            new NewObjectIns(),
            new NextNameIns(),
            new NextValueIns(),
            new NopIns(),
            new NotIns(),
            new PopIns(),
            new PopScopeIns(),
            new PushByteIns(),
            new InstructionDefinition(0x22,"pushconstant",new int[]{AVM2Code.DAT_STRING_INDEX}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  return 1; //?
               }

            },
            new InstructionDefinition(0x33,"pushdecimal",new int[]{AVM2Code.DAT_DECIMAL_INDEX}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  return 1; //?
               }

            },
            new InstructionDefinition(0x34,"pushdnan",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  return 1; //?
               }

            },
            new PushDoubleIns(),
            new PushFalseIns(),
            new PushIntIns(),
            new PushNamespaceIns(),
            new PushNanIns(),
            new PushNullIns(),
            new PushScopeIns(),
            new PushShortIns(),
            new PushStringIns(),
            new PushTrueIns(),
            new PushUIntIns(),
            new PushUndefinedIns(),
            new PushWithIns(),
            new ReturnValueIns(),
            new ReturnVoidIns(),
            new RShiftIns(),
            new SetLocalIns(),
            new SetLocal0Ins(),
            new SetLocal1Ins(),
            new SetLocal2Ins(),
            new SetLocal3Ins(),
            new SetGlobalSlotIns(),
            new SetPropertyIns(),
            new InstructionDefinition(0x69,"setpropertylate",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new SetSlotIns(),
            new SetSuperIns(),
            new StrictEqualsIns(),
            new SubtractIns(),
            new SubtractIIns(),
            new InstructionDefinition(0xb6,"subtract_p",new int[]{AVM2Code.OPT_U30}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new SwapIns(),
            new ThrowIns(),
            new InstructionDefinition(0xf3,"timestamp",new int[]{}),
            new TypeOfIns(),
            new URShiftIns(),
            new InstructionDefinition(0x35,"li8",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x36,"li16",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x37,"li32",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x38,"lf32",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x39,"lf64",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x3A,"si8",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x3B,"si16",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x3C,"si32",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x3D,"sf32",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x3E,"sf64",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x50,"sxi1",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x51,"sxi8",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            },
            new InstructionDefinition(0x52,"sxi16",new int[]{}){

               @Override
               public int getStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

               @Override
               public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                  throw new UnsupportedOperationException();
               }

            }
            
    };
    //endoflist
    public static InstructionDefinition instructionSetByCode[] = buildInstructionSetByCode();

	private static InstructionDefinition[] buildInstructionSetByCode() {
		InstructionDefinition result[] = new InstructionDefinition[256];
		for (InstructionDefinition id : instructionSet) {
			if (result[id.instructionCode] != null) {
				System.out.println("Warning: Duplicate OPCODE for instruction "+result[id.instructionCode]+" "+id);
			}
			result[id.instructionCode] = id;
		}
		return result;
	}

    public static final String IDENTOPEN = "/*IDENTOPEN*/";
    public static final String IDENTCLOSE = "/*IDENTCLOSE*/";

    private class ConvertOutput {

        public Stack<TreeItem> stack;
        public List<TreeItem> output;

        public ConvertOutput(Stack<TreeItem> stack, List<TreeItem> output) {
            this.stack = stack;
            this.output = output;
        }
    }

    public AVM2Code() {
    }

	public Object execute(HashMap arguments,ConstantPool constants){
        int pos=0;
        LocalDataArea lda=new LocalDataArea();
        lda.localRegisters=arguments;
        try{
        while(true){
            AVM2Instruction ins=code.get(pos);
            if(ins.definition instanceof JumpIns){
                pos=adr2pos((Long)ins.getParamsAsList(constants).get(0));
                continue;
            }
            if(ins.definition instanceof IfFalseIns){
                Boolean b=(Boolean)lda.operandStack.pop();
                if(b==false){
                    pos=adr2pos((Long)ins.getParamsAsList(constants).get(0));
                }else{
                    pos++;
                }
                continue;
            }
            if(ins.definition instanceof IfTrueIns){
                Boolean b=(Boolean)lda.operandStack.pop();
                if(b==true){
                    pos=adr2pos((Long)ins.getParamsAsList(constants).get(0));
                }else{
                    pos++;
                }
                continue;
            }
            if(ins.definition instanceof ReturnValueIns){
                return lda.operandStack.pop();
            }
            if(ins.definition instanceof ReturnVoidIns){
                return null;
            }
            ins.definition.execute(lda, constants, ins.getParamsAsList(constants));
            pos++;
        }
        }catch(ConvertException e){

        }
        return null;
    }

    public AVM2Code(InputStream is) throws IOException {
        ABCInputStream ais = new ABCInputStream(is);
        while (ais.available() > 0) {
            long startOffset = ais.getPosition();
            ais.startBuffer();
            int instructionCode = ais.read();
            InstructionDefinition instr = instructionSetByCode[instructionCode];
            if (instr != null) {
                int actualOperands[];
                if (instructionCode == 0x1b) { //switch
                    int firstOperand = ais.readS24();
                    int case_count = ais.readU30();
                    actualOperands = new int[case_count + 3];
                    actualOperands[0] = firstOperand;
                    actualOperands[1] = case_count;
                    for (int c = 0; c < case_count + 1; c++) {
                        actualOperands[2 + c] = ais.readS24();
                    }
                } else {
                    actualOperands = new int[instr.operands.length];
                    for (int op = 0; op < instr.operands.length; op++) {
                        switch (instr.operands[op] & 0xff00) {
                            case OPT_U30:
                                actualOperands[op] = ais.readU30();
                                break;
                            case OPT_U8:
                                actualOperands[op] = ais.read();
                                break;
                            case OPT_BYTE:
                                actualOperands[op] = (byte) ais.read();
                                break;
                            case OPT_S24:
                                actualOperands[op] = ais.readS24();
                                break;
                        }
                    }
                }

                code.add(new AVM2Instruction(startOffset, instr, actualOperands, ais.stopBuffer()));
            } else {
                throw new UnknownInstructionCode(instructionCode);
            }
        }
    }

	public void compact() {
		code.trimToSize();
	}

    public byte[] getBytes() {
       return getBytes(null);
    }
    public byte[] getBytes(byte origBytes[]) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
       
        OutputStream cos;
        if((origBytes!=null)&&(DEBUG_REWRITE))
        {
        ByteArrayInputStream origis=new ByteArrayInputStream(origBytes);
        cos=new CopyOutputStream(bos,origis);
       }else{
           cos=bos;
       }
        try {
            for (AVM2Instruction instruction : code) {
                cos.write(instruction.getBytes());
            }
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    @Override
    public String toString() {
        String s = "";
        for (AVM2Instruction instruction : code) {
            s += instruction.toString() + "\r\n";
        }
        return s;
    }

    public String toString(ConstantPool constants) {
        String s = "";
        int i = 0;
        for (AVM2Instruction instruction : code) {
            s += Helper.formatAddress(i) + " " + instruction.toString(constants) + "\r\n";
            i++;
        }
        return s;
    }

    private static String popStack(Stack<String> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            String s = stack.get(i);
            if (!s.startsWith("//")) {
                stack.remove(i);
                return s;
            }
        }
        return null;
    }

    public String toASMSource(ConstantPool constants,MethodBody body) {
        String ret = "";
        for(int e=0;e<body.exceptions.length;e++){
            ret+="exception "+e+" m["+body.exceptions[e].name_index+"]\""+Helper.escapeString(body.exceptions[e].getVarName(constants))+"\" "+
                            "m["+body.exceptions[e].type_index+"]\""+Helper.escapeString(body.exceptions[e].getTypeName(constants))+"\"\n";
        }
        List<Long> offsets = new ArrayList<Long>();
        for (AVM2Instruction ins : code) {
            offsets.addAll(ins.getOffsets());
        }
        long ofs = 0;
        for (AVM2Instruction ins : code) {
            if (offsets.contains(ofs)) {
                ret += "ofs" + Helper.formatAddress(ofs) + ":";
            }
            for(int e=0;e<body.exceptions.length;e++){
                if(body.exceptions[e].start==ofs){
                    ret+="exceptionstart "+e+":";
                }
                if(body.exceptions[e].end==ofs){
                    ret+="exceptionend "+e+":";
                }
                if(body.exceptions[e].target==ofs){
                    ret+="exceptiontarget "+e+":";
                }
            }
            ret += ins.toStringNoAddress(constants) + "\n";
            ofs += ins.getBytes().length;
        }

        return ret;
    }

    public int adr2pos(long address) throws ConvertException {
        int a = 0;
        for (int i = 0; i < code.size(); i++) {
            if (a == address) {
                return i;
            }
            a += code.get(i).getBytes().length;
        }
        if (a == address) {
            return code.size();
        }
        throw new ConvertException("Bad jump", -1);
    }

    public int pos2adr(int pos) {
        int a = 0;
        for (int i = 0; i < pos; i++) {
            a += code.get(i).getBytes().length;
        }

        return a;
    }

    private static String listToString(List<TreeItem> stack, ConstantPool constants) {
        String ret = "";
        for (int d = 0; d < stack.size(); d++) {
            TreeItem o = stack.get(d);
            ret += o.toString(constants) + "\r\n";
        }
        return ret;
    }

    private static String innerStackToString(List stack) {
        String ret = "";
        for (int d = 0; d < stack.size(); d++) {
            Object o = stack.get(d);
            ret += o.toString();
            if (d < stack.size() - 1) {
                if (!ret.endsWith("\r\n")) {
                    ret += "\r\n";
                }
            }
        }
        return ret;
    }

    private class Loop {

        public int loopContinue;
        public int loopBreak;
        public int continueCount = 0;
        public int breakCount = 0;

        public Loop(int loopContinue, int loopBreak) {
            this.loopContinue = loopContinue;
            this.loopBreak = loopBreak;
        }
    }

    private List<Loop> loopList;
    private List<Integer> unknownJumps;
    private List<Integer> finallyJumps;
    private List<ABCException> parsedExceptions;

    private String stripBrackets(String s) {
        if (s.startsWith("(") && (s.endsWith(")"))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private int checkCatches(ABC abc, ConstantPool constants, MethodInfo method_info[], Stack<TreeItem> stack, Stack<TreeItem> scopeStack, List<TreeItem> output, MethodBody body, int ip) throws ConvertException {
        /*int newip = ip;
        loope:
        for (int e = 0; e < body.exceptions.length; e++) {
        if (pos2adr(ip) == body.exceptions[e].end) {
        for (int f = 0; f < e; f++) {
        if (body.exceptions[e].startServer == body.exceptions[f].startServer) {
        if (body.exceptions[e].end == body.exceptions[f].end) {
        continue loope;
        }
        }
        }
        output.add("}");
        if (!(code.get(ip).definition instanceof JumpIns)) {
        throw new ConvertException("No jump to skip catches");
        }
        int addrAfterCatches = pos2adr(ip + 1) + code.get(ip).operands[0];
        int posAfterCatches = adr2pos(addrAfterCatches);
        for (int g = 0; g < body.exceptions.length; g++) {
        if (body.exceptions[e].startServer == body.exceptions[g].startServer) {
        if (body.exceptions[e].end == body.exceptions[g].end) {
        if (body.exceptions[g].isFinally()) {
        output.add("finally");
        } else {
        output.add("catch(" + body.exceptions[g].getVarName(constants) + ":" + body.exceptions[g].getTypeName(constants) + ")");
        }
        output.add("{");

        if (body.exceptions[g].isFinally()) {
        int jumppos = adr2pos(body.exceptions[g].target) - 1;
        AVM2Instruction jumpIns = code.get(jumppos);
        if (!(jumpIns.definition instanceof JumpIns)) {
        throw new ConvertException("No jump in finally block");
        }
        int nextAddr = pos2adr(jumppos + 1) + jumpIns.operands[0];
        int nextins = adr2pos(nextAddr);
        int pos = nextins;
        Integer uj = new Integer(nextins);
        if (unknownJumps.contains(uj)) {
        unknownJumps.remove(uj);
        }
        int endpos = 0;
        do {
        if (code.get(pos).definition instanceof LookupSwitchIns) {
        if (code.get(pos).operands[0] == 0) {
        if (adr2pos(pos2adr(pos) + code.get(pos).operands[2]) < pos) {
        endpos = pos - 1;
        newip = endpos + 1;
        break;
        }
        }
        }
        pos++;
        } while (pos < code.size());
        output.addAll(toSource(stack, scopeStack, abc, constants, method_info, body, nextins, endpos).output);
        } else {

        int pos = adr2pos(body.exceptions[g].target);
        int endpos = posAfterCatches - 1;
        for (int p = pos; p < posAfterCatches; p++) {
        if (code.get(p).definition instanceof JumpIns) {
        int nextAddr = pos2adr(p + 1) + code.get(p).operands[0];
        int nextPos = adr2pos(nextAddr);
        if (nextPos == posAfterCatches) {
        endpos = p - 1;
        break;
        }
        }
        }
        Stack cstack = new Stack<TreeItem>();
        cstack.push("catched " + body.exceptions[g].getVarName(constants));
        List outcatch = toSource(cstack, new Stack<TreeItem>(), abc, constants, method_info, body, pos, endpos).output;
        output.addAll(outcatch);
        newip = endpos + 1;
        }
        output.add("}");
        }
        }
        }
        }
        }
        return newip;*/
        return ip;
    }

    boolean isCatched = false;

    private boolean isKilled(int regName, int start, int end) {
        for (int k = start; k <= end; k++) {
            if (code.get(k).definition instanceof KillIns) {
                if (code.get(k).operands[0] == regName) {
                    return true;
                }
            }
        }
        return false;
    }

    private int toSourceCount = 0;


private int ipOfType(int from,boolean up,Class search,Class skipped,int start,int end)
{
   if(up)
   {
      for(int i=from;i>=start;i--){
         if(search.isInstance(code.get(i).definition)){
            return i;
         }else if((skipped!=null)&&skipped.isInstance(code.get(i).definition))
         {
            //skipped
         }else{
            return -1;
         }
      }
   }else
   {
      for(int i=from;i<=end;i++){
         if(search.isInstance(code.get(i).definition)){
            return i;
         }else if((skipped!=null)&&skipped.isInstance(code.get(i).definition))
         {
            //skipped
         }else{
            return -1;
         }
      }

   }
   return -1;
}

public HashMap<Integer,String> getLocalRegNamesFromDebug(ABC abc){
       HashMap<Integer,String> localRegNames= new HashMap<Integer,String>();
       for(AVM2Instruction ins:code)
       {
          if (ins.definition instanceof DebugIns) {
                   if(ins.operands[0]==1){
                      localRegNames.put(ins.operands[2]+1, abc.constants.constant_string[ins.operands[1]]);
                   }
                }
       }
       return localRegNames;
    }

    private void clearKilledAssigments(List<TreeItem> output){
       for(int i=0;i<output.size();i++){
          if(output.get(i) instanceof SetLocalTreeItem)
          {
             if(isKilled(((SetLocalTreeItem)output.get(i)).regIndex, 0, code.size()-1))
             {
                output.remove(i);
                i--;
             }
          }
       }
    }

    private int fixIPAfterDebugLine(int ip){
       if(ip>=code.size()) return code.size()-1;
       if(code.get(ip).definition instanceof DebugLineIns){
          return ip+1;
       }
       return ip;
    }

    private int fixAddrAfterDebugLine(int addr) throws ConvertException{
       return pos2adr(fixIPAfterDebugLine(adr2pos(addr)));
    }

    private ConvertOutput toSource(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, Stack<TreeItem> scopeStack, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, int start, int end) throws ConvertException {
        boolean debugMode = false;
        if (debugMode)
            System.out.println("OPEN SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
        //if(true) return "";
        toSourceCount++;
        if (toSourceCount > 255) {
            throw new ConvertException("StackOverflow", start);
        }
        List<TreeItem> output = new ArrayList();
        String ret = "";
        int ip = start;
        try {
            int addr;
            iploop:
            while (ip <= end) {

                addr = pos2adr(ip);
                int ipfix=fixIPAfterDebugLine(ip);
                int addrfix=pos2adr(ipfix);
                int maxend = -1;
                List<ABCException> catchedExceptions = new ArrayList<ABCException>();
                for (int e = 0; e < body.exceptions.length; e++) {
                    if (addrfix == fixAddrAfterDebugLine(body.exceptions[e].start)) {
                        if (!body.exceptions[e].isFinally()) {
                            if ((fixAddrAfterDebugLine(body.exceptions[e].end) > maxend) && (!parsedExceptions.contains(body.exceptions[e]))) {
                                catchedExceptions.clear();
                                maxend = fixAddrAfterDebugLine(body.exceptions[e].end);
                                catchedExceptions.add(body.exceptions[e]);
                            } else if (fixAddrAfterDebugLine(body.exceptions[e].end) == maxend) {
                                catchedExceptions.add(body.exceptions[e]);
                            }
                        }
                    }
                }
                if (catchedExceptions.size() > 0) {
                    ip=ipfix;
                    addr=addrfix;
                    parsedExceptions.addAll(catchedExceptions);
                    int endpos = adr2pos(fixAddrAfterDebugLine(catchedExceptions.get(0).end));


                    List<List<TreeItem>> catchedCommands = new ArrayList<List<TreeItem>>();
                    if (code.get(endpos).definition instanceof JumpIns) {
                        int afterCatchAddr = pos2adr(endpos + 1) + code.get(endpos).operands[0];
                        int afterCatchPos = adr2pos(afterCatchAddr);
                        Collections.sort(catchedExceptions, new Comparator<ABCException>() {

                            public int compare(ABCException o1, ABCException o2) {
                              try {
                                 return fixAddrAfterDebugLine(o1.target) - fixAddrAfterDebugLine(o2.target);
                              } catch (ConvertException ex) {
                                 return 0;
                              }
                            }
                        });


                        List<TreeItem> finallyCommands = new ArrayList<TreeItem>();
                        int returnPos = afterCatchPos;
                        for (int e = 0; e < body.exceptions.length; e++) {
                            if (body.exceptions[e].isFinally()) {
                                if (addr == fixAddrAfterDebugLine(body.exceptions[e].start)) {
                                    if (afterCatchPos + 1 == adr2pos(fixAddrAfterDebugLine(body.exceptions[e].end))) {
                                        AVM2Instruction jmpIns = code.get(adr2pos(fixAddrAfterDebugLine(body.exceptions[e].end)));
                                        if (jmpIns.definition instanceof JumpIns) {
                                            int finStart = adr2pos(fixAddrAfterDebugLine(body.exceptions[e].end) + jmpIns.getBytes().length + jmpIns.operands[0]);
                                            finallyJumps.add(finStart);
                                            if (unknownJumps.contains(finStart)) {
                                                unknownJumps.remove((Integer) finStart);
                                            }
                                            for (int f = finStart; f <= end; f++) {
                                                if (code.get(f).definition instanceof LookupSwitchIns) {
                                                    AVM2Instruction swins = code.get(f);
                                                    if (swins.operands.length >= 3) {
                                                        if (swins.operands[0] == swins.getBytes().length) {
                                                            if (adr2pos(pos2adr(f) + swins.operands[2]) < finStart) {
                                                                finallyCommands = toSource(isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, finStart, f - 1).output;
                                                                returnPos = f + 1;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        for (int e = 0; e < catchedExceptions.size(); e++) {
                            int eendpos = 0;
                            if (e < catchedExceptions.size() - 1) {
                                eendpos = adr2pos(fixAddrAfterDebugLine(catchedExceptions.get(e + 1).target)) - 2;
                            } else {
                                eendpos = afterCatchPos - 1;
                            }
                            Stack<TreeItem> substack = new Stack<TreeItem>();
                            substack.add(new ExceptionTreeItem(catchedExceptions.get(e)));
                            catchedCommands.add(toSource(isStatic, classIndex, localRegs, substack, new Stack<TreeItem>(), abc, constants, method_info, body, adr2pos(fixAddrAfterDebugLine(catchedExceptions.get(e).target)), eendpos).output);
                        }

                        List<TreeItem> tryCommands = toSource(isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, ip, endpos - 1).output;


                        output.add(new TryTreeItem(tryCommands, catchedExceptions, catchedCommands, finallyCommands));
                        ip = returnPos;
                        addr = pos2adr(ip);
                    }

                }

                if (ip > end)
                    break;

                if (unknownJumps.contains(ip)) {
                    unknownJumps.remove(new Integer(ip));
                    throw new UnknownJumpException(stack, ip, output);
                }
                AVM2Instruction ins = code.get(ip);
                //Ify s vice podminkama
                if (ins.definition instanceof JumpIns) {
                    if (ins.operands[0] == 0) {
                        ip++;
                        addr = pos2adr(ip);
                    } else if (ins.operands[0] > 0) {
                        int secondAddr = addr + ins.getBytes().length;
                        int jumpAddr = secondAddr + ins.operands[0];
                        int jumpPos = adr2pos(jumpAddr);//

                        if (finallyJumps.contains(jumpPos)) {
                            if (code.get(ip + 1).definition instanceof LabelIns) {
                                if (code.get(ip + 2).definition instanceof PopIns) {
                                    if (code.get(ip + 3).definition instanceof LabelIns) {
                                        if (code.get(ip + 4).definition instanceof GetLocalTypeIns) {
                                            if (code.get(ip - 1).definition instanceof PushByteIns) {
                                                if (code.get(ip - 2).definition instanceof SetLocalTypeIns) {
                                                    if (((SetLocalTypeIns) code.get(ip - 2).definition).getRegisterId(code.get(ip - 2)) == ((GetLocalTypeIns) code.get(ip + 4).definition).getRegisterId(code.get(ip + 4))) {
                                                        SetLocalTreeItem ti = (SetLocalTreeItem) output.remove(output.size() - 1);
                                                        stack.add(ti.value);
                                                        ip = ip + 5;
                                                        continue;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            //continue;
                            ip++;
                            continue;
                        }
                        for (Loop l : loopList) {
                            if (l.loopBreak == jumpPos) {
                                output.add(new BreakTreeItem(ins, l.loopBreak));
                                addr = secondAddr;
                                ip = ip + 1;
                                continue iploop;
                            }
                            if (l.loopContinue == jumpPos) {
                                l.continueCount++;
                                output.add(new ContinueTreeItem(ins, l.loopBreak));
                                addr = secondAddr;
                                ip = ip + 1;
                                continue iploop;
                            }
                        }


                        boolean backJumpFound = false;
                        int afterBackJumpAddr = 0;
                        AVM2Instruction backJumpIns = null;
                        boolean isSwitch = false;
                        int switchPos = 0;
                        loopj:
                        for (int j = jumpPos; j <= end; j++) {
                            if (code.get(j).definition instanceof IfTypeIns) {
                                afterBackJumpAddr = pos2adr(j + 1);

                                if (afterBackJumpAddr + code.get(j).operands[0] == secondAddr) {
                                    backJumpFound = true;
                                    backJumpIns = code.get(j);
                                    break;
                                }
                            }
                            if (code.get(j).definition instanceof LookupSwitchIns) {
                                for (int h = 2; h < code.get(j).operands.length; h++) {
                                    int ofs = code.get(j).operands[h] + pos2adr(j);
                                    if (ofs == secondAddr) {
                                        isSwitch = true;
                                        switchPos = j;
                                        break loopj;
                                    }
                                }
                            }
                        }
                        if (isSwitch) {
                            AVM2Instruction killIns = code.get(switchPos - 1);
                            if (!(killIns.definition instanceof KillIns)) {
                                throw new ConvertException("Unknown pattern: no kill before lookupswitch", switchPos - 1);
                            }
                            int userReg = killIns.operands[0];
                            int evalTo = -1;
                            for (int g = jumpPos; g < switchPos; g++) {
                                if ((code.get(g).definition instanceof SetLocal0Ins) && (userReg == 0)) {
                                    evalTo = g;
                                    break;
                                } else if ((code.get(g).definition instanceof SetLocal1Ins) && (userReg == 1)) {
                                    evalTo = g;
                                    break;
                                } else if ((code.get(g).definition instanceof SetLocal2Ins) && (userReg == 2)) {
                                    evalTo = g;
                                    break;
                                } else if ((code.get(g).definition instanceof SetLocal3Ins) && (userReg == 3)) {
                                    evalTo = g;
                                    break;
                                }
                                if ((code.get(g).definition instanceof SetLocalIns) && (userReg == code.get(g).operands[0])) {
                                    evalTo = g;
                                    break;
                                }
                            }
                            if (evalTo == -1) {
                                throw new ConvertException("Unknown pattern: no setlocal before lookupswitch", switchPos);
                            }
                            loopList.add(new Loop(ip, switchPos + 1));
                            Stack<TreeItem> substack = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, jumpPos, evalTo - 1).stack;
                            TreeItem switchedValue = substack.pop();
                            //output.add("loop" + (switchPos + 1) + ":");
                            int switchBreak = switchPos + 1;
                            List<TreeItem> casesList = new ArrayList<TreeItem>();
                            List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
                            List<TreeItem> defaultCommands = new ArrayList<TreeItem>();
                            //output.add("switch(" + switchedValue + ")");
                            //output.add("{");
                            int curPos = evalTo + 1;
                            int casePos = 0;
                            do {
                                evalTo = -1;
                                for (int g = curPos; g < switchPos; g++) {
                                    if ((code.get(g).definition instanceof GetLocal0Ins) && (userReg == 0)) {
                                        evalTo = g;
                                        break;
                                    } else if ((code.get(g).definition instanceof GetLocal1Ins) && (userReg == 1)) {
                                        evalTo = g;
                                        break;
                                    } else if ((code.get(g).definition instanceof GetLocal2Ins) && (userReg == 2)) {
                                        evalTo = g;
                                        break;
                                    } else if ((code.get(g).definition instanceof GetLocal3Ins) && (userReg == 3)) {
                                        evalTo = g;
                                        break;
                                    }
                                    if ((code.get(g).definition instanceof GetLocalIns) && (userReg == code.get(g).operands[0])) {
                                        evalTo = g;
                                        break;
                                    }
                                }


                                if (evalTo > -1) {
                                    substack = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, curPos, evalTo - 1).stack;
                                    casesList.add(substack.pop());
                                }
                                int substart = adr2pos(code.get(switchPos).operands[2 + casePos] + pos2adr(switchPos));
                                int subend = jumpPos - 1;
                                if (casePos + 1 < code.get(switchPos).operands.length - 2) {
                                    subend = adr2pos(code.get(switchPos).operands[2 + casePos + 1] + pos2adr(switchPos)) - 1;
                                }

                                if (evalTo == -1)
                                    subend--;
                                List commands = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, substart, subend).output;
                                if ((evalTo == -1) && (casePos < code.get(switchPos).operands.length - 2)) {
                                    if (commands.size() == 1) {
                                        commands.remove(0);
                                    }
                                    if (commands.size() > 0) {
                                        //hasDefault=true;
                                    }
                                }
                                List<TreeItem> caseCommandPart = new ArrayList<TreeItem>();
                                if (evalTo == -1) {
                                    defaultCommands.addAll(commands);
                                } else {
                                    caseCommandPart.addAll(commands);
                                    caseCommands.add(caseCommandPart);
                                }
                                curPos = evalTo + 4;
                                casePos++;
                                if (evalTo == -1) {
                                    break;
                                }
                            } while (true);
                            output.add(new SwitchTreeItem(code.get(switchPos), switchBreak, switchedValue, casesList, caseCommands, defaultCommands));
                            ip = switchPos + 1;
                            addr = pos2adr(ip);
                            continue;
                        }

                        if (!backJumpFound) {
                            if (jumpPos <= end + 1) { //probably skipping catch
                                ip = jumpPos;
                                addr = pos2adr(ip);
                                continue;
                            }
                            output.add(new ContinueTreeItem(ins, jumpPos, false));
                            addr = secondAddr;
                            ip = ip + 1;
                            if (!unknownJumps.contains(jumpPos)) {
                                unknownJumps.add(jumpPos);
                            }
                            continue;
                            //throw new ConvertException("Unknown pattern: forjump with no backjump");
                        }
                        Loop currentLoop = new Loop(jumpPos, adr2pos(afterBackJumpAddr));
                        loopList.add(currentLoop);


                        ConvertOutput co = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, jumpPos, adr2pos(afterBackJumpAddr) - 2);
                        Stack<TreeItem> substack = co.stack;
                        backJumpIns.definition.translate(isStatic, classIndex, localRegs, substack, scopeStack, constants, backJumpIns, method_info, output, body, abc);

                        TreeItem expression = substack.pop();
                        List<TreeItem> subins = new ArrayList<TreeItem>();
                        boolean isFor = false;
                        List<TreeItem> finalExpression = new ArrayList<TreeItem>();
                        try {
                            subins = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, adr2pos(secondAddr) + 1/*label*/, jumpPos - 1).output;
                        } catch (UnknownJumpException uje) {
                            if ((uje.ip >= start) && (uje.ip <= end)) {
                                currentLoop.loopContinue = uje.ip;
                                subins = uje.output;

                                List<ContinueTreeItem> contList = new ArrayList<ContinueTreeItem>();
                                for (TreeItem ti : subins) {
                                    if (ti instanceof ContinueTreeItem) {
                                        contList.add((ContinueTreeItem) ti);
                                    }
                                    if (ti instanceof Block) {
                                        contList.addAll(((Block) ti).getContinues());
                                    }
                                }
                                for (int u = 0; u < contList.size(); u++) {
                                    if (contList.get(u) instanceof ContinueTreeItem) {
                                        if (((ContinueTreeItem) contList.get(u)).loopPos == uje.ip) {
                                            if (!((ContinueTreeItem) contList.get(u)).isKnown) {
                                                ((ContinueTreeItem) contList.get(u)).isKnown = true;
                                                ((ContinueTreeItem) contList.get(u)).loopPos = currentLoop.loopBreak;
                                            }
                                        }
                                    }
                                }
                                finalExpression = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, uje.ip, jumpPos - 1).output;
                                isFor = true;
                            } else {
                                throw new ConvertException("Unknown pattern: jump to nowhere", ip);
                            }
                        }
                        boolean isDoWhile = false;

                        if (jumpPos == ip + 2) {
                            if (code.get(ip + 1).definition instanceof LabelIns) {
                                isDoWhile = true;
                            }
                        }
                        if (!isDoWhile) {
                            if (!isFor) {
                                for (Loop l : loopList) {
                                    if (l.loopContinue == jumpPos) {
                                        if (l.continueCount == 0) {
                                            //isFor = true;
                                            //finalExpression = subins.remove(subins.size() - 1).toString();
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        String firstIns = "";
                        if (isFor) {
                            if (output.size() > 0) {
                                //firstIns = output.remove(output.size() - 1).toString();
                            }
                        }

                        List<TreeItem> loopBody = new ArrayList<TreeItem>();
                        loopBody.addAll(co.output);
                        loopBody.addAll(subins);

                        if (isFor) {
                            output.add(new ForTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, new ArrayList<TreeItem>(), expression, finalExpression, loopBody));
                        } else if (isDoWhile) {
                            output.add(new DoWhileTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, loopBody, expression));
                        } else {
                            if (expression instanceof EachTreeItem) {
                                output.add(new ForEachTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, (EachTreeItem) expression, loopBody));
                            } else {
                                output.add(new WhileTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, expression, loopBody));
                            }
                        }
                        addr = afterBackJumpAddr;
                        ip = adr2pos(addr);
                    } else {
                        throw new ConvertException("Unknown pattern: back jump ", ip);
                    }
                } else if (ins.definition instanceof DupIns) {
                    int nextPos = 0;
                    do {
                        AVM2Instruction insAfter = code.get(ip + 1);
                        AVM2Instruction insBefore = ins;
                        if (ip - 1 >= start) {
                            insBefore = code.get(ip - 1);
                        }
                        boolean isAnd = false;
                        if (insAfter.definition instanceof IfFalseIns) {
                            //stack.add("(" + stack.pop() + ")&&");
                            isAnd = true;
                        } else if (insAfter.definition instanceof IfTrueIns) {
                            //stack.add("(" + stack.pop() + ")||");
                            isAnd = false;
                        } else if ((insAfter.definition instanceof IncrementIIns) || ((insAfter.definition instanceof IncrementIns))) {
                            int np=-1;
                            if(((np=ipOfType(ip+2, false, SetLocalTypeIns.class, CoerceOrConvertTypeIns.class, start, end))>-1)
                             &&(ipOfType(ip-1, true, GetLocalTypeIns.class, CoerceOrConvertTypeIns.class, start, end)>-1))
                            {
                                 stack.add(new PostIncrementTreeItem(insAfter, stack.pop()));
                                 ip = np+1;
                                 addr = pos2adr(ip);
                                 break;
                            }
                            if (((ip - 1 >= start) && (ip + 2 <= end))
                                    && (code.get(ip + 2).definition instanceof SetLocalTypeIns)
                                    && (isKilled(((SetLocalTypeIns) code.get(ip + 2).definition).getRegisterId(code.get(ip + 2)), ip + 3, end))) {
                                int pos = -1;
                                for (int d = ip + 3; d <= end; d++) {
                                    if (!((code.get(d).definition instanceof GetLocalTypeIns)
                                            && (isKilled(((GetLocalTypeIns) code.get(d).definition).getRegisterId(code.get(d)), d + 1, end)))) {
                                        pos = d;
                                        break;
                                    }
                                }
                                if (pos > -1) {
                                    if (code.get(pos).definition instanceof SetTypeIns) {
                                        stack.push(new PostIncrementTreeItem(insAfter, stack.pop()));
                                        ip = pos + 1;
                                        addr = pos2adr(ip);
                                        break;
                                    }
                                }

                            }
                            ins.definition.translate(isStatic, classIndex, localRegs, st
