package semanticAnalyzer.signatures;

import semanticAnalyzer.types.TypeVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parseTree.nodeTypes.LetStatementNode;
import parseTree.nodeTypes.GlobalDefTypes.FunctionInvocationNode;
import parseTree.nodeTypes.OperatorNodeTypes.ArrayOperatorNode;
import parseTree.nodeTypes.OperatorNodeTypes.FreshOperatorNode;
import asmCodeGenerator.codeStorage.ASMOpcode;
import lexicalAnalyzer.Punctuator;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.PrimitiveType;;

public class FunctionSignatures extends ArrayList<FunctionSignature> {
	private static final long serialVersionUID = -4907792488209670697L;
	private static Map<Object, FunctionSignatures> signaturesForKey = new HashMap<Object, FunctionSignatures>();
	
	static TypeVariable tV;
	static TypeVariable[] tVs;
	
	Object key;
	
	public FunctionSignatures(Object key, FunctionSignature ...functionSignatures) {
		this.key = key;
		for(FunctionSignature functionSignature: functionSignatures) {
			add(functionSignature);
		}
		signaturesForKey.put(key, this);
	}
	
	public Object getKey() {
		return key;
	}
	public boolean hasKey(Object key) {
		return this.key.equals(key);
	}
	
	public FunctionSignature acceptingSignature(List<Type> types) {
		for(FunctionSignature functionSignature: this) {
			if(functionSignature.accepts(types)) {
				return functionSignature;
			}
		}
		return FunctionSignature.nullInstance();
	}
	public boolean accepts(List<Type> types) {
		return !acceptingSignature(types).isNull();
	}

	
	/////////////////////////////////////////////////////////////////////////////////
	// access to FunctionSignatures by key object.
	
	public static FunctionSignatures nullSignatures = new FunctionSignatures(0, FunctionSignature.nullInstance());

	public static FunctionSignatures signaturesOf(Object key) {
		if(signaturesForKey.containsKey(key)) {
			return signaturesForKey.get(key);
		}
		return nullSignatures;
	}
	public static FunctionSignature signature(Object key, List<Type> types) {

		FunctionSignatures signatures = FunctionSignatures.signaturesOf(key);
		return signatures.acceptingSignature(types);
	}

	public static void resetTV() {
		tV.reset();
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// Put the signatures for operators in the following static block.
	
	static {
		tV = new TypeVariable();
		
		
		// here's one example to get you started with FunctionSignatures: the signatures for addition.		
		// for this to work, you should statically import PrimitiveType.*
		FunctionSignature[] greater_lessorequal = new FunctionSignature[] {
				new FunctionSignature(ASMOpcode.JumpPos, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpFPos, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpPos, PrimitiveType.CHARACTER, PrimitiveType.CHARACTER, PrimitiveType.BOOLEAN)
		};
		FunctionSignature[] less_greaterorequal = new FunctionSignature[] {
				new FunctionSignature(ASMOpcode.JumpNeg, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpFNeg, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpNeg, PrimitiveType.CHARACTER, PrimitiveType.CHARACTER, PrimitiveType.BOOLEAN)
		};
		FunctionSignature[] equal_notequal = new FunctionSignature[] {
				new FunctionSignature(ASMOpcode.JumpFalse, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpFZero, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpFalse, PrimitiveType.CHARACTER, PrimitiveType.CHARACTER, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpFalse, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpFalse, PrimitiveType.STRING, PrimitiveType.STRING, PrimitiveType.BOOLEAN),
				new FunctionSignature(ASMOpcode.JumpFalse, tV, tV, PrimitiveType.BOOLEAN)
		};

		new FunctionSignatures(Punctuator.ADD,
		    new FunctionSignature(ASMOpcode.Add, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER),
		    new FunctionSignature(ASMOpcode.FAdd, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT),
		    new FunctionSignature(0, PrimitiveType.STRING, PrimitiveType.STRING, PrimitiveType.STRING)
		);
		new FunctionSignatures(Punctuator.SUBTRACT,
				new FunctionSignature(ASMOpcode.Subtract, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER),
			    new FunctionSignature(ASMOpcode.FSubtract, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT)
		);
		new FunctionSignatures(Punctuator.MULTIPLY,
				new FunctionSignature(ASMOpcode.Multiply, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER),
			    new FunctionSignature(ASMOpcode.FMultiply, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT)
		);
		new FunctionSignatures(Punctuator.DIVIDE,
				new FunctionSignature(ASMOpcode.Divide, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER),
				new FunctionSignature(ASMOpcode.FDivide, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT)
		);
		new FunctionSignatures(Punctuator.GREATER, greater_lessorequal);
		new FunctionSignatures(Punctuator.GREATER_OR_EQUAL, less_greaterorequal);
		new FunctionSignatures(Punctuator.LESS, less_greaterorequal);
		new FunctionSignatures(Punctuator.LESS_OR_EQUAL, greater_lessorequal);
		new FunctionSignatures(Punctuator.EQUAL, equal_notequal);
		new FunctionSignatures(Punctuator.NOT_EQUAL, equal_notequal);
		
		new FunctionSignatures(Punctuator.CAST,
				new FunctionSignature(0, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER),
				new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.CHARACTER, PrimitiveType.CHARACTER),
				new FunctionSignature(2, PrimitiveType.INTEGER, PrimitiveType.FLOAT, PrimitiveType.FLOAT),
				new FunctionSignature(4, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN),
				new FunctionSignature(0, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT),
				new FunctionSignature(3, PrimitiveType.FLOAT, PrimitiveType.INTEGER, PrimitiveType.INTEGER),
				new FunctionSignature(0, PrimitiveType.CHARACTER, PrimitiveType.CHARACTER, PrimitiveType.CHARACTER),
				new FunctionSignature(0, PrimitiveType.CHARACTER, PrimitiveType.INTEGER, PrimitiveType.INTEGER),
				new FunctionSignature(4, PrimitiveType.CHARACTER, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN),
				new FunctionSignature(0, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN),
				new FunctionSignature(0, PrimitiveType.STRING, PrimitiveType.STRING, PrimitiveType.STRING)
		);
		
		new FunctionSignatures(Punctuator.AND,	new FunctionSignature(ASMOpcode.JumpFalse, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN));
		new FunctionSignatures(Punctuator.OR,	new FunctionSignature(ASMOpcode.JumpTrue, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN));
		new FunctionSignatures(Punctuator.NOT,	new FunctionSignature(ASMOpcode.BNegate, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN));
		
		new FunctionSignatures(Punctuator.LENGTH,
				new FunctionSignature(0, PrimitiveType.STRING, PrimitiveType.INTEGER),
				new FunctionSignature(1, new ArrayType(tV), PrimitiveType.INTEGER),
				new FunctionSignature(2, tV, PrimitiveType.INTEGER)
		);
		
		new FunctionSignatures(FreshOperatorNode.ARRAY_FRESH_CREATION, 		new FunctionSignature(FreshOperatorNode.ARRAY_FRESH_CREATION, tV, PrimitiveType.INTEGER, tV));
		new FunctionSignatures(FreshOperatorNode.TUPLE_FRESH_CREATION, 		new FunctionSignature(FreshOperatorNode.TUPLE_FRESH_CREATION, tV, tV, tV));

		new FunctionSignatures(ArrayOperatorNode.POPULATED_CREATION,		new FunctionSignature(ArrayOperatorNode.POPULATED_CREATION, tV, tV));
		new FunctionSignatures(ArrayOperatorNode.INDEX_OPERATION,			new FunctionSignature(ArrayOperatorNode.INDEX_OPERATION, new ArrayType(tV), PrimitiveType.INTEGER, tV));
		new FunctionSignatures(ArrayOperatorNode.CONCATENATION_OPERATION,	new FunctionSignature(ArrayOperatorNode.POPULATED_CREATION, new ArrayType(tV), tV));
		
		new FunctionSignatures(FunctionInvocationNode.FUNCTION_INVOCATION,	new FunctionSignature(FunctionInvocationNode.FUNCTION_INVOCATION, tV, tV, tV));
		new FunctionSignatures(LetStatementNode.RE_ASSIGNMENT,				new FunctionSignature(LetStatementNode.RE_ASSIGNMENT, tV, tV, tV));
		
		// First, we use the operator itself (in this case the Punctuator ADD) as the key.
		// Then, we give that key two signatures: one an (INT x INT -> INT) and the other
		// a (FLOAT x FLOAT -> FLOAT).  Each signature has a "whichVariant" parameter where
		// I'm placing the instruction (ASMOpcode) that needs to be executed.
		//
		// I'll follow the convention that if a signature has an ASMOpcode for its whichVariant,
		// then to generate code for the operation, one only needs to generate the code for
		// the operands (in order) and then add to that the Opcode.  For instance, the code for
		// floating addition should look like:
		//
		//		(generate argument 1)	: may be many instructions
		//		(generate argument 2)   : ditto
		//		FAdd					: just one instruction
		//
		// If the code that an operator should generate is more complicated than this, then
		// I will not use an ASMOpcode for the whichVariant.  In these cases I typically use
		// a small object with one method (the "Command" design pattern) that generates the
		// required code.

	}

}

