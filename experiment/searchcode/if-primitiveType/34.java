package com.cozilyworks.cozily.codedom.impl;

import java.util.ArrayList;
import java.util.List;
import com.cozilyworks.cozily.codedom.*;

public class CreatedName extends CodeDocument{
	public ClassOrInterfaceType classorinterfacetype;
	public void setClassOrInterfaceType(ClassOrInterfaceType t){
		this.classorinterfacetype=t;
	}
	public PrimitiveType primitivetype;
	public void setPrimitiveType(PrimitiveType t){
		this.primitivetype=t;
	}
	public void visit(){
		if(coz==0){
			//"classOrInterfaceType";
			add(classorinterfacetype);
		}
		if(coz==1){
			//"primitiveType";
			add(primitivetype);
		}
	}
}

