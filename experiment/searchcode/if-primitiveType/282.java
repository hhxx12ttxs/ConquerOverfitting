package com.cozilyworks.cozily.codedom.impl;

import java.util.ArrayList;
import java.util.List;
import com.cozilyworks.cozily.codedom.*;

public class CastExpression extends CodeDocument{
	public PrimitiveType primitivetype;
	public void setPrimitiveType(PrimitiveType t){
		this.primitivetype=t;
	}
	public UnaryExpression unaryexpression;
	public void setUnaryExpression(UnaryExpression t){
		this.unaryexpression=t;
	}
	public Type type;
	public void setType(Type t){
		this.type=t;
	}
	public UnaryExpressionNotPlusMinus unaryexpressionnotplusminus;
	public void setUnaryExpressionNotPlusMinus(UnaryExpressionNotPlusMinus t){
		this.unaryexpressionnotplusminus=t;
	}
	public void visit(){
		if(coz==0){
			//"'(' primitiveType ')' unaryExpression";
			format("( %s ) %s",this.primitivetype,this.unaryexpression);
		}
		if(coz==1){
			//"'(' type ')' unaryExpressionNotPlusMinus";
			format("( %s ) %s",this.type,this.unaryexpressionnotplusminus);
		}
	}
}

