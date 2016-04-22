package syntaxAnalyzer;

public class PrimitiveType{

public static Object eval(Object o ){

	if( 	((Boolean)NumericType.eval(o)) ){
		return true;
	}


	if( 	"boolean".equals((String)o) ){
		return true;
	}

	return false;
}
}

