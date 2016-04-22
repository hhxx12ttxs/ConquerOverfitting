package com.ossys.classmaker.sourcegenerator.attributegenerator;

import java.util.ArrayList;
import java.util.List;

import com.ossys.classmaker.sourcegenerator.classgenerator.ClassGenerator;

public class CppAttributeGenerator extends AttributeGenerator {
	public static enum PrimitiveType {
		BOOLEAN,
		BYTE,
		SHORT,
		INT,
		LONG,
		FLOAT,
		DOUBLE,
		CHAR,
		STRING,
		ENUM,
		DATE
	}
	private PrimitiveType primitiveType = null;
	
	public static enum AttributeType {
		DEFINITION,
		CLASS,
		ARGUMENT,
		GLOBAL,
		TYPEDEF
	}
	
	public static enum AttributeSignage {
		UNSIGNED,
		SIGNED
	}
	private AttributeSignage signage = null;
	
	private String complexType = null;
	
	private static ArrayList<CppAttributeGenerator> globals = new ArrayList<CppAttributeGenerator>();
	
	private String namespace = "";
	private String classname = "";
	private boolean instantiate_complex_type = false;
	private boolean pass_by_reference = false;
	private boolean pass_by_pointer = false;
	private boolean typedef = false;
	private boolean pointer = false;
	
	private List<String> generics = null;
	
	public CppAttributeGenerator(String name, ClassGenerator.NamingSyntaxType namingSyntaxType) {
		super(name, namingSyntaxType);

		this.generics = new ArrayList<String>();
	}
	
	public void setType(PrimitiveType primitiveType) {
		this.primitiveType = primitiveType;
		this.complexType = null;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public void setType(String complexType) {
		this.complexType = complexType;
	}
	
	public void setSignage(AttributeSignage signage) {
		this.signage = signage;
	}
	
	public void setPassByReference() {
		this.pass_by_reference = true;
	}
	
	public void setPassByPointer() {
		this.pass_by_pointer = true;
	}
	
	public void isTypedef() {
		this.typedef = true;
	}
	
	public void isPointer() {
		this.pointer = true;
	}
	
	@Override
	public void setFinal() {
		//In C++ Final is a C++11 extension for virtual methods only
	}
	
	public void setGlobal() {
		CppAttributeGenerator.globals.add(this);
	}
	
	public void setClassname(String classname) {
		this.classname = classname;
	}
	
	public void addGeneric(String generic) {
		this.generics.add(generic);
	}
	
	public static ArrayList<CppAttributeGenerator> getGlobals() {
		return CppAttributeGenerator.globals;
	}
	
	public void instantiateComplexType() {
		this.instantiate_complex_type = true;
	}
	
	public PrimitiveType getPrimitiveType() {
		return this.primitiveType;
	}
	
	public String getComplexType() {
		return this.complexType;
	}
	
	public String getSource(AttributeType type) {
		this.generate(type);
		return this.sb.toString();
	}
	
	private void generate(AttributeType type) {
		this.sb = new StringBuilder();
		
		// Setting static, final or const keywords
		if(type == AttributeType.DEFINITION ||
			type == AttributeType.CLASS) {
			if(this.stc && !(type == AttributeType.CLASS)) {
				this.sb.append("static ");
			}
			if(this.cnst) {
				this.sb.append("const ");
			}
			if(this.typedef) {
				this.sb.append("typedef ");
			}
		} else if(type == AttributeType.ARGUMENT) {
			if(this.cnst) {
				this.sb.append("const ");
			}
		}
		
		// Setting signage
		if(this.signage != null) {
			switch(this.signage) {
			case UNSIGNED:
				this.sb.append("unsigned ");
				break;
			case SIGNED:
				this.sb.append("signed ");
				break;
			default:
				break;	
			}
		}
		
		if(this.primitiveType != null) {
			switch(this.primitiveType) {
				case BOOLEAN:
					this.sb.append("bool");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case BYTE:
					this.sb.append("byte");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case SHORT:
					this.sb.append("short");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case INT:
					this.sb.append("int");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case LONG:
					this.sb.append("long long");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case FLOAT:
					this.sb.append("float");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case DOUBLE:
					this.sb.append("double");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case CHAR:
					this.sb.append("char");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case STRING:
					this.sb.append("std::string");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case ENUM:
					this.sb.append(this.complexType);
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
				case DATE:
					this.sb.append("std::time_t");
					if(this.is_array) {
						this.sb.append("[]");
					}
					this.sb.append(" ");
					break;
			}
		} else {
			this.sb.append(this.complexType);
			// Add generics if required
			if(this.generics.size() > 0) {
				sb.append("<");
			}
			int cnt = 0;
			for(String generic : this.generics) {
				if(cnt>0) {
					sb.append(", ");
				}
				sb.append(generic);
				cnt++;
			}
			if(this.generics.size() > 0) {
				sb.append(">");
			}
			this.sb.append(" ");
		}

		// Add fully-qualified class name
		if(type == AttributeType.GLOBAL || type == AttributeType.CLASS) {
			this.sb.append(this.namespace + ClassGenerator.getClassName(this.classname) + "::");
		}
		
		if(this.pass_by_reference) {
			this.sb.append("&");
		} else if(this.pass_by_pointer) {
			this.sb.append("*");
		}

		this.sb.append(this.name());
		
		if(type == AttributeType.CLASS ||
			type == AttributeType.GLOBAL
//			|| (type == AttributeType.DEFINITION && !this.cnst && this.primitiveType != null) // Un-comment this to initialize in-header
			) {
			if(this.is_array) {
				if(this.array_size > 0 && this.primitiveType != null) {
					this.sb.append(" = new ");
					switch(this.primitiveType) {
						case BOOLEAN:
							this.sb.append("bool");
							break;
						case BYTE:
							this.sb.append("byte");
							break;
						case CHAR:
							this.sb.append("char");
							break;
						case DATE:
							break;
						case DOUBLE:
							this.sb.append("double");
							break;
						case ENUM:
							break;
						case FLOAT:
							this.sb.append("float");
							break;
						case INT:
							this.sb.append("int");
							break;
						case LONG:
							this.sb.append("long long");
							break;
						case SHORT:
							this.sb.append("short");
							break;
						case STRING:
							break;
					}
					this.sb.append("[" + this.array_size + "]");
				} else {
					this.sb.append(";");
				}
			} else if(this.dflt != null) {
				this.sb.append(" = ");
				
				if(this.primitiveType != null) {
					if(this.primitiveType == PrimitiveType.STRING) {
						this.sb.append("\"" + this.dflt + "\"");
					} else if(this.primitiveType == PrimitiveType.CHAR) {
						this.sb.append("'" + this.dflt + "'");
					} else if(this.primitiveType == PrimitiveType.FLOAT) {
						this.sb.append(this.dflt + "F");
					} else {
						this.sb.append(this.dflt);
					}
				}
			} else if(this.primitiveType != null) {
				switch(this.primitiveType) {
					case BYTE:
						if(!this.is_array) {
							this.sb.append(" = 0x00B");
						} else {
							this.sb.append(" = NULL");
						}
						break;
						
					case BOOLEAN:
						this.sb.append(" = false");
						break;
						
					case CHAR:
						this.sb.append(" = '\\u0000'");
						break;
						
					case SHORT:
						this.sb.append(" = 0");
						break;
	
					case INT:
						this.sb.append(" = 0");
						break;
	
					case LONG:
						this.sb.append(" = 0L");
						break;
	
					case FLOAT:
						this.sb.append(" = 0F");
						break;
	
					case DOUBLE:
						this.sb.append(" = 0D");
						break;
						
					default:
						this.sb.append(" = NULL");
						break;
				}
			} else if(this.complexType != null) {
				if(this.instantiate_complex_type) {
					this.sb.append(" = new " + this.complexType + "()");
				} else {
					this.sb.append(" = NULL");
				}
			}
		}
		
		if(type == AttributeType.DEFINITION ||
			type == AttributeType.CLASS ||
			type == AttributeType.GLOBAL) {
			this.sb.append(";\n");
		}
	}
	
}

