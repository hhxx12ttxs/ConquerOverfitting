package syntaxAnalyzer;

public class Type{

public static Object eval(Object o ){

	if( 	((Boolean)PrimitiveType.eval(o)) ){
		return true;
	}


	if( 	((Boolean)ReferenceType.eval(o)) ){
		return true;
	}

	return false;
}
}

