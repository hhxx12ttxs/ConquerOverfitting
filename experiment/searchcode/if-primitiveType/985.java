package com.cozilyworks.cozily.codedom.impl;

import java.util.ArrayList;
import java.util.List;
import com.cozilyworks.cozily.codedom.*;

public class Primary extends CodeDocument{
	public ParExpression parexpression;
	public void setParExpression(ParExpression t){
		this.parexpression=t;
	}
	public List<String> identifiers=new ArrayList<String>();
	public void addIDENTIFIER(String t){
		this.identifiers.add(t);
	}
	public IdentifierSuffix identifiersuffix;
	public void setIdentifierSuffix(IdentifierSuffix t){
		this.identifiersuffix=t;
	}
	public String identifierStr;
	public void setIDENTIFIER(String t){
		this.identifierStr=t;
	}
	public SuperSuffix supersuffix;
	public void setSuperSuffix(SuperSuffix t){
		this.supersuffix=t;
	}
	public Literal literal;
	public void setLiteral(Literal t){
		this.literal=t;
	}
	public Creator creator;
	public void setCreator(Creator t){
		this.creator=t;
	}
	public PrimitiveType primitivetype;
	public void setPrimitiveType(PrimitiveType t){
		this.primitivetype=t;
	}
	public List<String> bracketss=new ArrayList<String>();
	public void addBRACKETS(String t){
		this.bracketss.add(t);
	}
	public void visit(){
		if(coz==0){
			//"parExpression";
			add(parexpression);
		}
		if(coz==1){
			//"'this' ('.' IDENTIFIER )* identifierSuffix?";
			add("this");
			adds(".%s",this.identifiers);
			add(this.identifiersuffix);
		}
		if(coz==2){
			//"IDENTIFIER ('.' IDENTIFIER)* identifierSuffix?";
			add(this.identifierStr);
			adds(".%s",this.identifiers);
			add(this.identifiersuffix);
		}
		if(coz==3){
			//"'super' superSuffix";
			format("super %s",this.supersuffix);
		}
		if(coz==4){
			//"literal";
			add(literal);
		}
		if(coz==5){
			//"creator";
			add(creator);
		}
		if(coz==6){
			//"primitiveType BRACKETS* '.' 'class'";
			add(this.primitivetype);
			adds(this.bracketss);
			add(".class");
		}
		if(coz==7){
			//"'void' '.' 'class'";
			add("void . class");
		}
	}
}

