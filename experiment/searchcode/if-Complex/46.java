package au.com.aapt.lsdjpa.attribute.builder;

import java.util.HashSet;
import java.util.Set;

public class ComplexAttribute
{
	private String name;
	private Set<Attribute> attributes;
	private Set<ComplexAttribute> complexAttributes;
	
	public ComplexAttribute(){};
	
	public ComplexAttribute(String name, Set<Attribute> attributes, Set<ComplexAttribute> complexAttributes)
	{
		this.name = name;
		this.attributes = attributes;
		this.complexAttributes = complexAttributes;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Set<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public void addAttribute(Attribute attribute) {
		
		if (this.attributes == null)
			attributes = new HashSet<Attribute>();
		
		attributes.add(attribute);
	}

	public Set<ComplexAttribute> getComplexAttributes() {
		return complexAttributes;
	}

	public void setComplexAttributes(Set<ComplexAttribute> complexAttributes) {
		this.complexAttributes = complexAttributes;
	}	
	

	public void addComplexAttribute(ComplexAttribute complexAttribute) {
		if (this.complexAttributes == null)
			complexAttributes = new HashSet<ComplexAttribute>();
		
		complexAttributes.add(complexAttribute);
	}
}

