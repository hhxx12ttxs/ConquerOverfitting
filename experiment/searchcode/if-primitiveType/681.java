/**
 * 
 */
package com.ossys.classmaker.sourcegenerator.attributegenerator;

import java.util.ArrayList;
import java.util.List;

import com.ossys.classmaker.sourcegenerator.classgenerator.ClassGenerator;

/**
 * @author Administrator
 *
 */
public class JavaAttributeGenerator extends AttributeGenerator {
	
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
		CLASS,
		INTERFACE,
		ARGUMENT
	}
	
	private String complexType = null;
	private String implementationType = null;
	private List<String> annotations = null;
	private List<String> generics = null;
	private List<String> instantiation_parameters = null;
	private List<String> default_list = null;
	
	private boolean instantiate_complex_type = false;
	private String complex_instantiator = null;
	
	public JavaAttributeGenerator(String name, ClassGenerator.NamingSyntaxType namingSyntaxType) {
		super(name, namingSyntaxType);
		
		this.annotations = new ArrayList<String>();
		this.generics = new ArrayList<String>();
		this.instantiation_parameters = new ArrayList<String>();
	}
	
	public void setType(PrimitiveType primitiveType) {
		this.primitiveType = primitiveType;
		if(this.primitiveType != PrimitiveType.ENUM) {
			this.complexType = null;
		}
	}
	
	public void setType(String complexType) {
		this.complexType = complexType;
	}
	
	public void setType(String complexType, String implementationType) {
		this.complexType = complexType;
		this.implementationType = implementationType;
	}
	
	public void instantiateComplexType() {
		this.instantiate_complex_type = true;
	}
	
	public void setComplexInstantiator(String complex_instantiator) {
		this.complex_instantiator = complex_instantiator;
	}
	
	public void setDefaultList(List<String> default_list) {
		this.default_list = default_list;
	}
	
	public void addAnnotation(String annotation) {
		this.annotations.add(annotation);
	}
	
	public void addGeneric(String generic) {
		this.generics.add(generic);
	}
	
	public void addInstantiationParameter(String instantiation_parameter) {
		this.instantiation_parameters.add(instantiation_parameter);
	}
	
	public String getSource(AttributeType type) {
		this.generate(type);
		return this.sb.toString();
	}
	
	private void generate(AttributeType type) {
		this.sb = new StringBuilder();

		if(type == AttributeType.CLASS) {
			for(String annotation : this.annotations) {
				sb.append("\t" + annotation + "\n");
			}
			
			if(this.visibilityType == AttributeVisibilityType.PRIVATE) {
				sb.append("\tprivate ");
			} else if(this.visibilityType == AttributeVisibilityType.PROTECTED) {
				sb.append("\tprotected ");
			} else {
				sb.append("\tpublic ");
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
		} else if(type == AttributeType.INTERFACE) {
			sb.append("\t");
		}
		
		if(this.primitiveType != null) {
			switch(this.primitiveType) {
				case BOOLEAN:
					sb.append("boolean");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case BYTE:
					sb.append("byte");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case SHORT:
					sb.append("short");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case INT:
					sb.append("int");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case LONG:
					sb.append("long");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case FLOAT:
					sb.append("float");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case DOUBLE:
					sb.append("double");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case CHAR:
					sb.append("char");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case STRING:
					sb.append("String");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case ENUM:
					sb.append(this.complexType);
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
				case DATE:
					sb.append("Date");
					if(this.is_array) {
						sb.append("[]");
					}
					sb.append(" ");
					break;
			}
		} else {
			sb.append(this.complexType);
			if(this.is_array) {
				sb.append("[]");
			} else {
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
			}
			sb.append(" ");
		}
		
		sb.append(this.name());
		
		if(type == AttributeType.CLASS || type == AttributeType.INTERFACE) {
			if(this.is_array) {
				if(this.array_size > 0 && this.primitiveType != null) {
					sb.append(" = new ");
					switch(this.primitiveType) {
						case BOOLEAN:
							sb.append("boolean");
							break;
						case BYTE:
							sb.append("byte");
							break;
						case CHAR:
							sb.append("char");
							break;
						case DATE:
							break;
						case DOUBLE:
							sb.append("double");
							break;
						case ENUM:
							break;
						case FLOAT:
							sb.append("float");
							break;
						case INT:
							sb.append("int");
							break;
						case LONG:
							sb.append("long");
							break;
						case SHORT:
							sb.append("short");
							break;
						case STRING:
							break;
					}
					sb.append("[" + this.array_size + "]");
				} else if(this.primitiveType != null && this.default_list != null) {
					sb.append(" = {");
					int cnt = 0;
					for(String dflt : this.default_list) {
						if(cnt > 0) {
							sb.append(", ");
						}
						if(this.primitiveType == PrimitiveType.CHAR) {
							sb.append("'" + dflt + "'");
						} else {
							sb.append(dflt);
						}
						cnt++;
					}
					sb.append("}");
				} else {
					sb.append(";");
				}
			} else if(this.dflt != null) {
				if(this.primitiveType != null) {
					if(this.primitiveType == PrimitiveType.STRING) {
						sb.append(" = \"" + this.dflt + "\"");
					} else if(this.primitiveType == PrimitiveType.CHAR) {
						sb.append(" = '" + this.dflt + "'");
					} else if(this.primitiveType == PrimitiveType.FLOAT) {
						sb.append(" = " + this.dflt + "F");
					} else if(this.primitiveType == PrimitiveType.LONG) {
						sb.append(" = " + this.dflt + "L");
					} else {
						sb.append(" = " + this.dflt);
					}
				} else {
					if(this.instantiate_complex_type) {
						sb.append(" = " + this.dflt);
					}
				}
			} else if(this.primitiveType != null) {
				switch(this.primitiveType) {
					case BYTE:
						if(!this.is_array) {
							sb.append(" = 0x00B");
						} else {
							sb.append(" = null");
						}
						break;
						
					case BOOLEAN:
						sb.append(" = false");
						break;
						
					case CHAR:
						sb.append(" = '\\u0000'");
						break;
						
					case SHORT:
						sb.append(" = 0");
						break;
	
					case INT:
						sb.append(" = 0");
						break;
	
					case LONG:
						sb.append(" = 0L");
						break;
	
					case FLOAT:
						sb.append(" = 0F");
						break;
	
					case DOUBLE:
						sb.append(" = 0D");
						break;
						
					default:
						if(this.instantiate_complex_type) {
							sb.append(" = new Date()");
						} else {
							sb.append(" = null");
						}
						break;
				}
			} else if(this.complexType != null) {
				if(this.complex_instantiator != null) {
					sb.append(" = " + this.complex_instantiator);
				} else if(this.instantiate_complex_type) {
					if(this.complexType != null && this.implementationType == null) {
						if(this.dflt == null) {
							sb.append(" = new " + this.complexType);
						}
					} else {
						sb.append(" = new " + this.implementationType);
					}

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
					
					sb.append("(");
					cnt = 0;
					for(String instantiation_parameter : this.instantiation_parameters) {
						if(cnt > 0) {
							sb.append(", ");
						}
						sb.append(instantiation_parameter);
						cnt++;
					}
					sb.append(")");
				} else {
					sb.append(" = null");
				}
			}
			sb.append(";\n");
			if(this.annotations.size() > 0) {
				sb.append("\n");
			}
		}
	}
	
}

