package semanticAnalyzer;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;

class FunctionSignature {
	private static final boolean ALL_TYPES_ACCEPT_ERROR_TYPES = true;
	private Type resultType;
	private Type[] paramTypes;
	Object whichVariant;

	public FunctionSignature(Object whichVariant, Type ...types) {
		assert(types.length >= 1);
		storeParamTypes(types);
		resultType = types[types.length-1];
		this.whichVariant = whichVariant;
	}
	private void storeParamTypes(Type[] types) {
		paramTypes = new Type[types.length-1];
		for(int i=0; i<types.length-1; i++) {
			paramTypes[i] = types[i];
		}
	}

	public Type resultType() {
		return resultType;
	}
	public boolean accepts(Type ...types) {

		if(types.length != paramTypes.length) {
			return false;
		}
		for(int i=0; i<paramTypes.length; i++) {
			if(!assignableTo(paramTypes[i], types[i])) {
				return false;
			}
		}
		return true;
	}

	private boolean assignableTo(Type variableType, Type valueType) {
		if(valueType == PrimitiveType.ERROR && ALL_TYPES_ACCEPT_ERROR_TYPES) {
			return true;
		}	
		return variableType.equals(valueType);
	}
	
	// signature definitions for integer add, subtract, multiply, divide
	private static FunctionSignature addSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	private static FunctionSignature subtractSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	private static FunctionSignature multiplySignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	private static FunctionSignature divideSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	private static FunctionSignature remainderSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	
	// signature definitions for fnumber integer add, subtract, multiply, divide
	private static FunctionSignature addFSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
	private static FunctionSignature subtractFSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
	private static FunctionSignature multiplyFSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
	private static FunctionSignature divideFSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
	
	// signature definitions for Integer Boolean Comparisons
	private static FunctionSignature lessSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
	private static FunctionSignature lessEqualSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
	private static FunctionSignature equalSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
	private static FunctionSignature notEqualSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
	private static FunctionSignature greaterSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
	private static FunctionSignature greaterEqualSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);
	
	// signature definitions for Float Boolean Comparisons
	private static FunctionSignature lessFSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN);
	private static FunctionSignature lessFEqualSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN);
	private static FunctionSignature equalFSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN);
	private static FunctionSignature notEqualFSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN);
	private static FunctionSignature greaterFSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN);
	private static FunctionSignature greaterFEqualSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.BOOLEAN);
	
	// signature definitions for <> == Boolean Boolean Comparisons
	private static FunctionSignature notEqualBooleanSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
	private static FunctionSignature equalBooleanSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
	
	// signature definitions for Conversion (int) (float)
	private static FunctionSignature convertFloatSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.INTEGER);
	private static FunctionSignature convertIntegerSignature = new FunctionSignature(1,PrimitiveType.INTEGER, PrimitiveType.FLOAT);
	
	
	// signature definitions for && || ! Boolean Boolean Comparisons
	private static FunctionSignature andSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
	private static FunctionSignature orSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
	private static FunctionSignature notSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
	
	// signature definitions for Uniary Negation
	private static FunctionSignature integerNegationSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	private static FunctionSignature floatNegationSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
	

	// signatures for the Cons Operators
	private static ListType listTypeInteger = new ListType(PrimitiveType.INTEGER);
	private static ListType listTypeFloat = new ListType(PrimitiveType.FLOAT);
	private static FunctionSignature integerAnyConsSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.ANY, listTypeInteger);
	private static FunctionSignature floatAnyConsSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.ANY, listTypeFloat);
	private static FunctionSignature integerConsSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, listTypeInteger);
	private static FunctionSignature floatConsSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, listTypeFloat);
	
	// signatures for the Append Operators
	private static FunctionSignature integerAppendSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER, listTypeInteger);
	private static FunctionSignature floatAppendSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT, listTypeFloat);
	
	// signatures for Head, Tail and Length
	private static FunctionSignature headIntegerSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	private static FunctionSignature headFloatSignature = new FunctionSignature(1, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
	private static FunctionSignature tailIntegerSignature = new FunctionSignature(1, PrimitiveType.INTEGER, listTypeInteger);
	private static FunctionSignature tailFloatSignature = new FunctionSignature(1, PrimitiveType.FLOAT,  listTypeFloat);
	private static FunctionSignature lengthIntegerSignature = new FunctionSignature(1, PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	private static FunctionSignature lengthFloatSignature = new FunctionSignature(1, PrimitiveType.FLOAT,  PrimitiveType.INTEGER);
	
	// signatures for TernaryExpression
	private static FunctionSignature ternaryIntegerSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.INTEGER , PrimitiveType.INTEGER, PrimitiveType.INTEGER);
	private static FunctionSignature ternaryFloatSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.FLOAT , PrimitiveType.FLOAT, PrimitiveType.FLOAT);
	private static FunctionSignature ternaryBooleanSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN , PrimitiveType.BOOLEAN, PrimitiveType.BOOLEAN);
	private static FunctionSignature ternaryListIntSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.INTEGER , PrimitiveType.INTEGER, new ListType(PrimitiveType.INTEGER));
	private static FunctionSignature ternaryListFloatSignature = new FunctionSignature(1, PrimitiveType.BOOLEAN, PrimitiveType.FLOAT , PrimitiveType.FLOAT,  new ListType(PrimitiveType.FLOAT));
	
	private static FunctionSignature neverMatchedSignature = new FunctionSignature(1, PrimitiveType.ERROR) {
		public boolean accepts(Type ...types) {
			return false;
		}
	};
	
	// the switch here is ugly compared to polymorphism.  This should perhaps be a method on Lextant.
	public static FunctionSignature signatureOf(Lextant lextant, Type primitiveType) {
		assert(lextant instanceof Punctuator);	
		Punctuator punctuator = (Punctuator)lextant;
		
		if(primitiveType == PrimitiveType.INTEGER){
			switch(punctuator) {
			case ADD:		return addSignature;
			case SUBTRACT:  return subtractSignature;
			case MULTIPLY:	return multiplySignature;
			case DIVISION:  return divideSignature;
			case REMAINDER: return remainderSignature;
		
			case LESS:		   return lessSignature;
			case LESSEQUAL:    return lessEqualSignature;
			case EQUAL:        return equalSignature;
			case NOTEQUAL:     return notEqualSignature;
			case GREATER:	   return greaterSignature;
			case GREATEREQUAL: return greaterEqualSignature;
			
			default:
				return neverMatchedSignature;
			}
		}
		else if(primitiveType == PrimitiveType.FLOAT){
			switch(punctuator) {
			case ADD:		return addFSignature;
			case SUBTRACT:  return subtractFSignature;
			case MULTIPLY:	return multiplyFSignature;
			case DIVISION:  return divideFSignature;
		
			case LESS:		   return lessFSignature;
			case LESSEQUAL:    return lessFEqualSignature;
			case EQUAL:        return equalFSignature;
			case NOTEQUAL:     return notEqualFSignature;
			case GREATER:	   return greaterFSignature;
			case GREATEREQUAL: return greaterFEqualSignature;
			
			default:
				return neverMatchedSignature;
			}
		}else if(primitiveType == PrimitiveType.BOOLEAN){
			switch(punctuator){
			case EQUAL: 	return notEqualBooleanSignature;
			case NOTEQUAL:  return equalBooleanSignature;
			case AND:		return andSignature;
			case OR:		return orSignature;
			case NOT:		return notSignature;
			
			default:
				return neverMatchedSignature;
			}
		}
		else{
			return neverMatchedSignature;
		}
	}
	
	public static FunctionSignature signatureConvertOf(Lextant lextant, Type primitiveType) {
		assert(lextant instanceof Keyword);	
		Keyword conversionKeyword = (Keyword)lextant;
		
			switch(conversionKeyword) {
			case FLOAT:  
				return convertIntegerSignature;
			case INTEGER: 
				return convertFloatSignature;
			default:
				return neverMatchedSignature;
			}
	}
	
	public static FunctionSignature signatureNegateOf(Lextant lextant, Type type) {
		assert(lextant instanceof Punctuator);
		
		if(type == PrimitiveType.BOOLEAN){
			return notSignature;
		}
		else{
			return neverMatchedSignature;
		}
	}
	
	public static FunctionSignature signatureNegationOf(Lextant operator, Type type) {
		Type negationType = type;
		
		if(negationType == PrimitiveType.INTEGER){
			return integerNegationSignature;
		}
		else if(negationType == PrimitiveType.FLOAT){
			return floatNegationSignature;
		}
		else{
			return neverMatchedSignature;
		}
	}
	
	public static FunctionSignature isMatchedByAny(Type type, Type type2) {
		Type left = type;
		ListType right = (ListType) type2;
		
		if( left == PrimitiveType.INTEGER && checkListTypeeAny(right)){
			return integerAnyConsSignature;
		}
		else if( left == PrimitiveType.FLOAT && checkListTypeeAny(right)){
			return floatAnyConsSignature;
		}
		else{
			return neverMatchedSignature;
		}
	}
	private static boolean checkListTypeeAny(ListType list){
		return (list instanceof ListType && list.getElementType() == PrimitiveType.ANY);
	}
	
	public static FunctionSignature isMatchedByList(Type type, Type type2) {
		
		Type left = type;
		Type right =  type2;
		if(right instanceof ListType){
			ListType rightType = (ListType) right;
			if( left == PrimitiveType.INTEGER && checkListTypeInteger(rightType)){
				return integerConsSignature;
			}
			else if( left == PrimitiveType.FLOAT && checkListTypeFloat(rightType)){
				return floatConsSignature;
			}
			else{
				return neverMatchedSignature;
			}
		}
		return neverMatchedSignature;
	}
	
	public static FunctionSignature isMatchedByAppend(Type type, Type type2) {
		Type left = type;
		Type right = type2;
		
		if( left instanceof ListType && right instanceof ListType){
			ListType leftList = (ListType) type;
			ListType rightList = (ListType) type2;

			if(checkListTypeInteger(leftList) && checkListTypeInteger(rightList)){
				return integerAppendSignature;
			}
			else if(checkListTypeFloat(leftList) && checkListTypeFloat(rightList) )
				return floatAppendSignature;
			else{
				return neverMatchedSignature;
			}
		}
		return neverMatchedSignature;
	}
	
	public static FunctionSignature isMatchedByHead(Type type) {
	
		Type nodeType = type;
		
		if( nodeType instanceof ListType){
			ListType nodeList = (ListType) type;
			
			if(checkListTypeInteger(nodeList)){
				return headIntegerSignature;
			}
			else if(checkListTypeFloat(nodeList)){
				return headFloatSignature;
			}
			else{
				return neverMatchedSignature;
			}
		}
		return neverMatchedSignature;
	}
	
	public static FunctionSignature isMatchedByTail(Type type) {
		Type nodeType = type;
		
		if( nodeType instanceof ListType){
			ListType nodeList = (ListType) type;
			
			if(checkListTypeInteger(nodeList)){
				return tailIntegerSignature;
			}
			else if(checkListTypeFloat(nodeList)){
				return tailFloatSignature;
			}
			else{
				return neverMatchedSignature;
			}
		}
		return neverMatchedSignature;
	}
	
	public static FunctionSignature isMatchedByLength(Type type) {
		Type nodeType = type;
		
		if( nodeType instanceof ListType){
			ListType nodeList = (ListType) type;
			
			if(checkListTypeInteger(nodeList)){
				return lengthIntegerSignature;
			}
			else if(checkListTypeFloat(nodeList)){
				return lengthFloatSignature;
			}
			else{
				return neverMatchedSignature;
			}
		}
		return neverMatchedSignature;
		
	}
	private static boolean checkListTypeInteger(ListType list){
		return (list instanceof ListType && list.getElementType() == PrimitiveType.INTEGER);
	}
	private static boolean checkListTypeFloat(ListType list){
		return (list instanceof ListType && list.getElementType() == PrimitiveType.FLOAT);
	}
	
	public static FunctionSignature isTernaryMatched(Type type1, Type type2, Type type3) {
		//Type booleanType = type1;
		Type bOperandType = type2;
		Type cOperandType = type3;
		
		if(bOperandType == PrimitiveType.INTEGER && cOperandType == PrimitiveType.INTEGER){
			return ternaryIntegerSignature;
		}
		else if(bOperandType == PrimitiveType.FLOAT && cOperandType ==  PrimitiveType.FLOAT){
			return ternaryFloatSignature;
		}
		else if(bOperandType ==  PrimitiveType.BOOLEAN && cOperandType ==  PrimitiveType.BOOLEAN){
			return ternaryBooleanSignature;
		}
		else if(bOperandType instanceof ListType && cOperandType instanceof ListType){
			ListType listType1 = (ListType) bOperandType;
			ListType listType2 = (ListType) cOperandType;
			if(listType1.getElementType() == listType2.getElementType()){
				Type element = listType1.getElementType();
				
				if(element == PrimitiveType.INTEGER){
					return ternaryListIntSignature;
				}
				else if(element == PrimitiveType.FLOAT){
					return ternaryListFloatSignature;
				}
				else{
					return neverMatchedSignature;
				}
			}
		}
		else{
			return neverMatchedSignature;
		}
		return neverMatchedSignature;
	}
	
	
}
