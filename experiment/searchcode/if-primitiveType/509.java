/**
 * 
 */
package com.ossys.classmaker.sourcegenerator.attributegenerator;

import com.ossys.classmaker.sourcegenerator.classgenerator.ClassGenerator;
import com.ossys.classmaker.sourcegenerator.classgenerator.ClassGenerator.NamingSyntaxType;

/**
 * @author Administrator
 *
 */
public class PhpAttributeGenerator extends AttributeGenerator {
	public static enum PrimitiveType {
		BOOLEAN,
		BYTE,
		NUM,
		STRING,
		OBJECT
	}
	private PrimitiveType primitiveType = null;
	
	public static enum AttributeType {
		DEFINITION,
		ARGUMENT
	}
	
	public PhpAttributeGenerator(String name) {
		super(name, NamingSyntaxType.LOWERCASE);
	}
	
	public PhpAttributeGenerator(String name, NamingSyntaxType namingSyntaxType) {
		super(name, namingSyntaxType);
	}
	
	public void setStatic() {
		this.stc = true;
	}
	
	public void setFinal() {
		this.fin = true;
	}
	
	public void setPrimitiveType(PrimitiveType primitiveType) {
		this.primitiveType = primitiveType;
	}
	
	public String name() {
		return "$" + ClassGenerator.getName(this.name,this.namingSyntaxType,false);
	}
	
	public String getSource(AttributeType type) {
		this.generate(type);
		return this.sb.toString();
	}
	
	private void generate(AttributeType type) {
		this.sb = new StringBuilder();
		
		if(type == AttributeType.ARGUMENT) {
			this.sb.append(this.name());
		} else if(type == AttributeType.DEFINITION) {
			if(this.cnst) { 
				sb.append("\t");
			} else {
				if(this.visibilityType == AttributeVisibilityType.PRIVATE) {
					sb.append("\tprivate ");
				} else if(this.visibilityType == AttributeVisibilityType.PROTECTED) {
					sb.append("\tprotected ");
				} else {
					sb.append("\tpublic ");
				}
			}
			
			if(this.stc) {
				sb.append("static ");
				
				if(this.fin) {
					sb.append("final ");
				}
			} else if(this.fin) {
				sb.append("final ");
			} else if(this.cnst) {
				sb.append("const ");
			}
			
			if(this.cnst) {
				sb.append(ClassGenerator.getName(this.name,this.namingSyntaxType,false));
			} else {
				sb.append(this.name());
			}
			
			if(this.dflt != null) {
				sb.append(" = ");
				
				if(this.primitiveType != null) {
					if(this.primitiveType == PrimitiveType.STRING) {
						sb.append("'" + this.dflt + "'");
					} else {
						sb.append(this.dflt);
					}
				}
			} else {
				sb.append(" = null");
			}
			sb.append(";\n");
		}
	}
}

