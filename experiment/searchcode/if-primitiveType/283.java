package com.cozilyworks.cozily.codedom.impl;

import java.util.ArrayList;
import java.util.List;
import com.cozilyworks.cozily.codedom.*;

public class Type extends CodeDocument{
	public ClassOrInterfaceType classorinterfacetype;
	public void setClassOrInterfaceType(ClassOrInterfaceType t){
		this.classorinterfacetype=t;
	}
	public List<String> bracketss=new ArrayList<String>();
	public void addBRACKETS(String t){
		this.bracketss.add(t);
	}
	public PrimitiveType primitivetype;
	public void setPrimitiveType(PrimitiveType t){
		this.primitivetype=t;
	}
	public void visit(){
		if(coz==0){
			//"classOrInterfaceType BRACKETS*";
			String classOrInterfaceType=this.classorinterfacetype.toString();
			source.addSymbols(classOrInterfaceType);
			//
			add(classOrInterfaceType);
			adds(this.bracketss);
		}
		if(coz==1){
			//"primitiveType BRACKETS*";
			add(this.primitivetype);
			adds(this.bracketss);
		}
	}
}

