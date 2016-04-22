/**
 * 
 */
package com.ossys.classmaker.sourcegenerator.attributegenerator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ossys.classmaker.sourcegenerator.classgenerator.ClassGenerator;
import com.ossys.classmaker.sourcegenerator.classgenerator.JavascriptClassGenerator;
import com.ossys.classmaker.sourcegenerator.methodgenerator.JavascriptMethodGenerator;

/**
 * @author Administrator
 *
 */
public class JavascriptAttributeGenerator extends AttributeGenerator {
	
	public static enum PrimitiveType {
		BOOLEAN,
		BYTE,
		NUM,
		CHAR,
		STRING,
		ENUM,
		DATE,
		OBJECT,
		ARRAY
	}
	private PrimitiveType primitiveType = null;
	
	public static enum AttributeType {
		VALUE,
		ARGUMENT,
		MEMBER
	}
	
	private int array_cnt = 0;
	private int tab_level = 0;
	private boolean tabbed = false;
	private StringBuilder sb = new StringBuilder();
	private Map<String, String> values = new LinkedHashMap<String, String>();
	
	private JavascriptMethodGenerator parent = null;
	
	public JavascriptAttributeGenerator() {
		super(null);
	}
	
	public JavascriptAttributeGenerator(String name) {
		super(name);
	}
	
	public JavascriptAttributeGenerator(long name) {
		super(String.valueOf(name));
	}
	
	public JavascriptAttributeGenerator(double name) {
		super(String.valueOf(name));
	}
	
	public JavascriptAttributeGenerator(String name, ClassGenerator.NamingSyntaxType namingSyntaxType) {
		super(name, namingSyntaxType);
	}
	
	public void setTabbed(boolean tabbed) {
		this.tabbed = tabbed;
	}
	
	public void setTabLevel(int tab_level) {
		this.tab_level = tab_level;
	}
	
	public int getTabLevel() {
		return this.tab_level;
	}
	
	public void setType(PrimitiveType primitiveType) {
		this.primitiveType = primitiveType;
	}
	
	public void setDefault(JavascriptAttributeGenerator jsag) {
		this.dflt = jsag.generate(AttributeType.ARGUMENT);
	}
	
	public void setParent(JavascriptMethodGenerator parent) {
		this.parent = parent;
	}
	
	public PrimitiveType getType() {
		return this.primitiveType;
	}
	
	public void add(String name, String value) {
		if(this.primitiveType == PrimitiveType.OBJECT) {
			this.values.put(name, value);
		}
	}
	
	public void add(String name, long value) {
		if(this.primitiveType == PrimitiveType.OBJECT) {
			this.values.put(name, String.valueOf(value));
		}
	}
	
	public void add(String name, double value) {
		if(this.primitiveType == PrimitiveType.OBJECT) {
			this.values.put(name, String.valueOf(value));
		}
	}
	
	public void add(String name, JavascriptAttributeGenerator value) {
		if(this.primitiveType == PrimitiveType.OBJECT) {
			this.values.put(name, value.generate(AttributeType.VALUE));
		}
	}
	
	public void add(JavascriptAttributeGenerator value) {
		if(this.primitiveType == PrimitiveType.ARRAY) {
			this.values.put(String.valueOf(this.array_cnt++), value.generate(AttributeType.VALUE));
		}
	}
	
	public String generate(AttributeType type) {
		if(this.name != null && type == AttributeType.ARGUMENT && this.primitiveType == PrimitiveType.STRING) {
			this.sb.append("'" + this.name + "'");
		} else if(this.name != null && type == AttributeType.ARGUMENT) {
			this.sb.append(this.name);
		} else if((type == AttributeType.VALUE || type == AttributeType.ARGUMENT) && (this.primitiveType == PrimitiveType.OBJECT || this.primitiveType == PrimitiveType.ARRAY)) {
			if(this.name != null) {
				this.sb.append("var " + this.name + " = ");
			}
			
			if(this.primitiveType == PrimitiveType.ARRAY) {
				this.sb.append("[");
			} else if(this.primitiveType == PrimitiveType.OBJECT) {
				this.sb.append("{");
			}
			
			if(this.tabbed) {
				this.sb.append("\n");
			} else {
				this.sb.append(" ");
			}
		
			int cnt=0;
		    Iterator<Map.Entry<String, String>> it = values.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next();
		        if(cnt > 0) {
		        	this.sb.append(",");
					if(this.tabbed) {
						this.sb.append("\n");
					} else {
						this.sb.append(" ");
					}
		        }
		        for(int i=0; i < this.tab_level; i++) {
		        	this.sb.append("\t");
		        }
		        if(this.primitiveType == PrimitiveType.ARRAY) {
		        	this.sb.append(pair.getValue());
		        } else if(this.primitiveType == PrimitiveType.OBJECT) {
		        	this.sb.append(pair.getKey() + " : " + pair.getValue());
		        }
				cnt++;
		    }
		    if(this.tabbed) {
		    	this.sb.append("\n");
		    } else {
		    	this.sb.append(" ");
		    }
		    for(int i=0; i<this.tab_level-1; i++) {
		    	this.sb.append("\t");
		    }
		    
			if(this.primitiveType == PrimitiveType.ARRAY) {
				this.sb.append("]");
			} else if(this.primitiveType == PrimitiveType.OBJECT) {
				this.sb.append("}");
			}
		} else if(type == AttributeType.VALUE && (this.primitiveType == PrimitiveType.BOOLEAN || this.primitiveType == PrimitiveType.BYTE || this.primitiveType == PrimitiveType.CHAR || this.primitiveType == PrimitiveType.DATE || this.primitiveType == PrimitiveType.NUM || this.primitiveType == PrimitiveType.STRING)) {
			this.sb.append(this.dflt);
		} else if(type == AttributeType.MEMBER) {
			if(this.visibilityType == AttributeVisibilityType.PRIVATE) {
				this.sb.append("var ");
			} else if(this.visibilityType == AttributeVisibilityType.PUBLIC) {
				if(this.stc && this.parent != null) {
					this.sb.append(this.parent.getName() + ".");
				} else {
					this.sb.append("this.");
				}
			}
			this.sb.append(this.name);
			
			if(this.dflt != null) {
				if(this.primitiveType == PrimitiveType.STRING) {
					this.sb.append(" = '" + this.dflt + "';");
				} else if(this.primitiveType == PrimitiveType.ENUM) {
					this.sb.append(" = Object.freeze(" + this.dflt + ");");
				} else if(!this.dflt.equals("")) {
					this.sb.append(" = " + this.dflt + ";");
				} else {
					this.sb.append(";");
				}
			} else {
				this.sb.append(" = null;");
			}
		}
		
		String ret = this.sb.toString();
		if(this.sb.length() > 0) {
			this.sb.delete(0, this.sb.length()-1);
		}
		return ret;
	}
	
}

