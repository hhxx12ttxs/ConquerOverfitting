package syntaxAnalyzer;

public class ArrayCreationExpression{

public static Object eval(Object o ){

	if( 	"new".equals((String)o) &&
 	((Boolean)PrimitiveType.eval(o)) &&
 	((Boolean)DimExprs.eval(o)) ){
		return true;
	}


	if( 	"new".equals((String)o) &&
 	((Boolean)PrimitiveType.eval(o)) &&
 	((Boolean)DimExprs.eval(o)) &&
 	((Boolean)Dims.eval(o)) ){
		return true;
	}


	if( 	"new".equals((String)o) &&
 	((Boolean)ClassOrInterfaceType.eval(o)) &&
 	((Boolean)DimExprs.eval(o)) ){
		return true;
	}


	if( 	"new".equals((String)o) &&
 	((Boolean)ClassOrInterfaceType.eval(o)) &&
 	((Boolean)DimExprs.eval(o)) &&
 	((Boolean)Dims.eval(o)) ){
		return true;
	}

	return false;
}
}

