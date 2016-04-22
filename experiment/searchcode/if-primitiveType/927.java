
public class VarDec extends VarDecList 
{
	Type type;
	IdList idList;
	
	public VarDec(Type t, IdList il) 
	{
		type = t;
		idList = il;
	}
	
	void printParseTree(String indent)
	{
		String indent1 = indent + " ";
		String indent2 = indent1 + " ";
				
		IO.displayln(indent + indent.length() + " <var dec>");
		type.printParseTree(indent1);
//		if ( type instanceof PrimitiveType && ((PrimitiveType) type).type.equals("boolean") ) 
//		{
//			idList.printParseTree(indent1);
//		}
//		else
			idList.printParseTree(indent1);
	}
	
	
}

