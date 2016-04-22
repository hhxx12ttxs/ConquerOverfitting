package queryParser.vo;


public class PrimitiveType extends QueryComponentType{
	
	public static boolean isPrimitiveType(String value) throws Exception{
		return ColumnInfo.isColumnType(value) || ConstInfo.isConstType(value);
	}
	
	// String값을 PrimitiveType중 하나로 변환
	public static PrimitiveType convertStringToType(String value) throws Exception{
		PrimitiveType primitiveType = null;
		
		if(ColumnInfo.isColumnType(value)){
			primitiveType = ColumnInfo.convertStringToInfo(value);
			
		}else if(ConstInfo.isConstType(value)){
			primitiveType = ConstInfo.convertStringToInfo(value);
			
		}else{
			throw new Exception("잘못된 PrimitiveType 형식");
		}
		
		return primitiveType;
	}
}

