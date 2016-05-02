/*
 * /*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.beach.metadata.generator;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author jfclere
 * Class description resulting of the parsing of the xsd files.
 */

public class Classes {
	QName name;
	String documentation;
	List<Property> properties;
	List<Property> attributes;
	List<QName> extensions;
	
	public Classes(QName name) {
		this.name = name;
	}
	
	/**
	 * Create a class description.
	 * @param name
	 * @param documentation
	 * @param properties
	 * @param attributes
	 * @param extensions
	 */
	public Classes(QName name, 	String documentation, List<Property> properties, List<Property> attributes, List<QName> extensions) {
		this.name = name;
		this.documentation = documentation;
		this.properties = properties;
		this.attributes = attributes;
		this.extensions = extensions;
	}
	
	   /* Build a list of export for a class */
	   private void buildListImports(QName name)
	   {
		   List <String>exports = new ArrayList<String>();
		   exports.add(Utils.getNamePackage(name) + "." + Utils.javaIdentifier(name));
		   
		   for (Property prop: properties) {
			   String parse = Utils.getNamePackage(prop.type);
			   if (parse != null)
				   parse = Utils.getNamePackage(prop.type) + "." + Utils.javaIdentifier(prop.type);
			   else
				   parse = Utils.javaIdentifier(prop.type);

			   if (parse.indexOf('.') != -1) {
				   // Test class we ignore / hack ?
				   if (parse.endsWith(".EmptyType"))
					   continue;
				   exports.add(parse);
			   }
		   }
		   for (QName ext: extensions) {
			   String parse = Utils.getNamePackage(ext);
			   if (parse != null) {
				   exports.add(parse + "." + Utils.javaIdentifier(ext));
			   }
		   }
		   for (Property prop: attributes) {
			   String parse = Utils.getNamePackage(prop.type);
			   if (parse != null) {
				   exports.add(parse + "." + Utils.javaIdentifier(prop.type));
			   }
		   }
		   if (!exports.isEmpty())
			   Utils.addExportList(name, exports);
	   }
	   
	   private static String hackJavaName(QName name, QName parent)
	   {
		   String namespaceURI = name.getNamespaceURI();
		   if(namespaceURI.equals("http://www.w3.org/2001/XMLSchema"))
		   {
			   String localPart = name.getLocalPart();
			   if(localPart.equals("boolean"))
				   return "Boolean";
			   if(localPart.equals("integer") || localPart.equals("nonNegativeInteger") || localPart.equals("positiveInteger"))
				   return "Integer";
			   if(localPart.equals("string") || localPart.equals("token"))
				   return "String";
			   if(localPart.equals("long"))
				   return "Long";
			   if(localPart.equals("anyURI"))
				   return URI.class.getName();
			   if(localPart.equals("QName"))
				   return QName.class.getName();
			   // throw new IllegalArgumentException("Can't handle " + name);
		   }
		   String t = name.getLocalPart();
		   if (t.equals("emptyType"))
			   t = "Boolean";
		   else if (t.equals("ordering-othersType"))
			   t = "Boolean";
		   else if (t.equals("ID"))
			   t = "String";
		   else if (t.equals("web-app-versionType"))
			   t = "String";
		   else if (t.equals("jboss-web-versionType"))
			   t = "String";
		   else if (t.equals("load-on-startupType"))
			   t = "Integer"; // <xsd:union memberTypes="javaee:null-charType xsd:integer"/>
		   else if (t.equals("protocol-bindingType"))
			   t = "java.net.URI"; // <xsd:union memberTypes="xsd:anyURI javaee:protocol-URIAliasType"/>
		   else {
			   String pkgp = Utils.getNamePackage(parent);
			   String pkg = Utils.getNamePackage(name);
			   t = Utils.javaIdentifier(name);
			   if (pkg != null) {
				   if (pkgp != null && !pkgp.equals(pkg))
					   t = pkg + "." + Utils.javaIdentifier(name);
			   }
		   }
		   return t;
	   }

	 /* Generate the java interface file */
	   private static void generateSetGet(PrintStream out, QName parent, String name, String comment, QName type, boolean islist)
	   {
	 	  String s;
	 	 // first property @VALUE@ requires a "special handling".
		 if (name.equals("@VALUE@")) {
	 		 s = Utils.normalize(parent.getLocalPart());
	 	 } else {
	 		 s = Utils.normalize(name);
	 	 }
	      if(comment != null)
	         out.println(comment);
	      String t = hackJavaName(type, parent);
	      if (islist)
	    	  t = "java.util.List<" + t + ">";
	      out.println("   " + t + " get" + s + "();");
	      if (name.equals("@VALUE@")) {
	     	 out.println("   void set" + s + "(" + t + " " + Utils.normalize("value") + ");");
	      	// TODO really needed (if yes remove the comment :D)
	     	 out.println("   " + t + " value();");
	      } else 
	     	 out.println("   void set" + s + "(" + t + " " + Utils.normalize(name) + ");");
	      out.println();   
	   }
	   public static void generateInterface(QName name, String documentation, List<Property> properties, List<QName> extensions, List<Property> attributes, AllClasses all) throws IOException
	   {
		      System.err.println("generateInterface:" + name);
		      PrintStream out = Utils.openFileObject(name);
		      
		      Utils.printlnPackage(out, name);
		      
		      Utils.printlnImport(out, name);
		      
		      if(documentation != null)
		         out.println(Utils.comment(documentation));
		      out.println("public interface " + Utils.javaIdentifier(name) + (extensions.size() > 0 ? " extends " + Utils.comma(extensions) : "") + " {");     

		      for(Property property : properties)
		      {
		    	  generateSetGet(out, name, property.name, property.comment, property.type, property.isList);
		      }
		      for(Property property : attributes) {
		    	  generateSetGet(out, name, property.name, property.comment, property.type, property.isList);
		      }
		      out.println("}");

		      out.flush();
		      out.close();
	   }

	   /* Generate the java implementation file */
	   private static void generateSetGetImpl(PrintStream out, QName parent, String name, String comment, QName type, boolean islist)
	   {
		   String s;
		   if (name.equals("@VALUE@")) {
			   s = Utils.normalize(parent.getLocalPart());
		   } else {
			   s = Utils.normalize(name);
		   }
	       if(comment != null)
	          out.println(comment);
	       // TODO hack for EmptyType.java and OrderingOthersType.java and others....
	       String t = hackJavaName(type, parent);

	       if (islist)
	    	   t = "java.util.List<" + t + ">";
	       
	       /* field */
	       out.println("   private " + t + " " + s + ";");
	       /* getter */
	       out.println("   public " + t + " get" + s + "(){");
	       out.println("   return this." + s + ";");
	       out.println("   }");

	       out.println("");
	       /* setter */
	       if (name.equals("@VALUE@")) {
	      	 out.println("   public void set" + s + "(" + t + " " + Utils.normalize("value") + "){");
	      	 out.println("   this." + s + " = " + Utils.normalize("value") + ";");
	      	 out.println("   }");
	      	 // TODO really needed?
		     out.println("   public " + t + " value(){");
		     out.println("   return this." + s + ";");
		     out.println("   }");     	 
	       } else {
	    	   out.println("   public void set" + s + "(" + t + " " + Utils.normalize(name) + "){");
	    	   out.println("   this." + s + " = " + Utils.normalize(name) + ";");
	    	   out.println("   }");
	       }
	       out.println();   
	   }
	   public void addImplementation(PrintStream out, AllClasses all) throws IOException
	   {
		   for(Property property : properties)
		   {
			   generateSetGetImpl(out, name, property.name, property.comment, property.type, property.isList);
		   }
		   for(Property property : attributes)
		   {
			   generateSetGetImpl(out, name, property.name, property.comment, property.type, property.isList);
		   }	
		   if (extensions.size()>0) {
			   for (QName extension : extensions) {
				   all.addExtensionImpl(out, extension);
			   }
		   }
	   }
	   public static void generateImplementation(QName name, String documentation, List<Property> properties, List<QName> extensions, List<Property> attributes, AllClasses all) throws IOException
	   {
		   	  System.err.println("generateImplementation:" + name);
		      PrintStream out = Utils.openFileObjectImpl(name);

		      Utils.printlnPackage(out, name);

		      Utils.printlnImport(out, name, extensions);
		      if(documentation != null)
		         out.println(Utils.comment(documentation));
		      
		      out.println("public class " + Utils.javaIdentifier(name) + "Impl implements " + Utils.javaIdentifier(name) + " {");
		      out.println("// @@@_GENERATED_@@@");
		      for(Property property : properties)
		      {
		    	  generateSetGetImpl(out, name, property.name, property.comment, property.type, property.isList);
		      }
		      for(Property property : attributes)
		      {
		    	  generateSetGetImpl(out, name, property.name, property.comment, property.type, property.isList);
		      }
		      
		      // Process the extension in fact that looks like a hack but determineJavaType via isours has already created the Implementations
		      // we need.
		      if (extensions.size()>0) {
		    	  for (QName extension : extensions) {
		    		  all.addExtensionImpl(out, extension);
		    	  }
		      }
		      out.println("// @@@_GENERATED_@@@");
		      out.println("}");

		      out.flush();
		      out.close();
	   }

	   /* Generate an element  of the parser code */
	   public boolean generateParserElement(PrintStream out, QName parent, Property property, boolean isfirst) throws IOException {
		      String s;
		      if (property.name.equals("@VALUE@")) {
		    	 // TODO that ignores any attributes for the moment.
		    	 s = Utils.normalize(name.getLocalPart());
		    	 if (!isfirst)
		    		 throw new IOException("@VALUE@ must be first property");
		    	 out.println("      mystuff.set" + s + "(reader.getElementText());");
			     out.println("      return mystuff;");
			     out.println("   }");
			     out.println("}");

			     out.flush();
			     out.close();
		    	 return false; // Don't set isfirst ... it is "special".
		     } else {
		    	 s = Utils.normalize(property.name);
		     }
	         if (isfirst) {
	   	      	 out.println("      while(reader.hasNext() && reader.nextTag() != javax.xml.stream.XMLStreamConstants.END_ELEMENT) {");
	   	      	 out.println("          final String localName = reader.getLocalName();");
		      	 out.println("// @@@_GENERATED_@@@");        	 
	        	 out.println("          if(localName.equals(\"" + property.name + "\")) {");
	        	 isfirst = false;
	         } else
	        	 out.println("          } else if(localName.equals(\"" + property.name + "\")) {");
	         
	         if (name.equals("absoluteOrderingType")) {
	        	 if (property.name.equals("name")) {
	        		 out.println("              AbsoluteOrderingTypeElementImpl order = new AbsoluteOrderingTypeElementImpl();");
	        		 out.println("              order.setName(reader.getElementText());");
	        		 out.println("              order.setOthers(false);");
	        		 out.println("              mystuff.getAbsoluteOrderingTypeElement().add(order);");
	        	 }
	        	 if (property.name.equals("others")) {
	        		 out.println("              AbsoluteOrderingTypeElementImpl order = new AbsoluteOrderingTypeElementImpl();");
	        		 out.println("              order.setName(null);");
	        		 out.println("              order.setOthers(true);");
	        		 out.println("              mystuff.getAbsoluteOrderingTypeElement().add(order);");
	        		 out.println("              order.setName(reader.getElementText()); // Ignores it");
	        	 }
	        	 return isfirst;
	         }
	         //String t = Utils.javaIdentifier(property.type);
	         String t = hackJavaName(property.type, parent);
	         if (property.isList)
	        	 t = "java.util.List<" + t + ">";

	         if (t.equals("String")) {
	        	 out.println("              mystuff.set" + s + "(reader.getElementText());");
	         } else if (t.equals("Integer")) {
	        	 out.println("              mystuff.set" + s + "(Integer.parseInt(reader.getElementText()));");
	         } else if (t.equals("Long")) {
	        	 out.println("              mystuff.set" + s + "(Long.parseLong(reader.getElementText()));");
	         } else if (t.equals("Boolean")) {
	        	 out.println("              mystuff.set" + s + "(Boolean.parseBoolean(reader.getElementText()));");
	         } else if (t.endsWith(".EmptyType")) {
	        	 // TODO Hack....
	        	 out.println("              mystuff.set" + s + "(Boolean.parseBoolean(reader.getElementText()));");
	         } else if (t.endsWith(".OrderingOthersType")) {
	        	 // TODO Hack...
	        	 out.println("              mystuff.set" + s + "(Boolean.parseBoolean(reader.getElementText()));");
	         } else if (t.equals("java.net.URI")) {
	        	 out.println("              try {");
	        	 out.println("              mystuff.set" + s + "(new java.net.URI(reader.getElementText()));");
	        	 out.println("              } catch (java.net.URISyntaxException ex) {");
	        	 out.println("              throw new XMLStreamException(\"Can't parse element java.net.URI\");");
	        	 out.println("              }");
	         } else if (t.equals("javax.xml.namespace.QName")) {
	        	 out.println("              mystuff.set" + s + "(new javax.xml.namespace.QName(reader.getElementText()));");
	         } else if (t.equals("java.util.List<String>")) {
	        	 out.println("              mystuff.get" + s + "().add(reader.getElementText());");
	         } else if (t.equals("java.util.List<javax.xml.namespace.QName>")) {
	        	 out.println("              mystuff.get" + s + "().add(new javax.xml.namespace.QName(reader.getElementText()));");
	         } else if (t.equals("java.util.List<java.net.URI>")) {
	        	 out.println("              try {");
	        	 out.println("              mystuff.get" + s + "().add(new java.net.URI(reader.getElementText()));");
	        	 out.println("              } catch (java.net.URISyntaxException ex) {");
	        	 out.println("              throw new XMLStreamException(\"Can't parse element List<java.net.URI>\");");
	        	 out.println("              }");	        	 
	         } else if (t.startsWith("java.util.List")) {
	        	 out.println(Utils.comment(t));
	        	 // String parse = Utils.javaIdentifier(property.type);
	        	 /// String parse = property.type;
	        	 String parse = Utils.listType(t);
	        	 out.println(Utils.comment(parse));
	        	 
	        	 out.println("              mystuff.get" + s + "().add(" + parse + "Parser.parse(reader));");
	         } else {
	        	 out.println(Utils.comment(t));
	        	 out.println("              mystuff.set" + s + "(" + t + "Parser.parse(reader));");     
	         }
	         return isfirst;
	   }
	   
	   /* Add parser code from a sub element */
	   public void addParser(PrintStream out, QName name, AllClasses all) throws IOException
	   {
		   for(Property property : properties)
		   {
			   generateParserElement(out, name, property, false);
		   } 
		   if (extensions.size()>0) {
			   for (QName extension : extensions) {
				   all.addExtensionParser(out, name, extension);
			   }	    	  
		   }
	   }

	   /* Generate the parser java file */
	   public void generateParser(QName name, String documentation, List<Property> properties, List<QName> extensions,  List<Property> attrs, AllClasses all) throws IOException
	   {
		      System.err.println("generateParser: " + name);
		      PrintStream out = Utils.openFileObjectParser(name);
		      Utils.printlnPackage(out, name);
		      
		      out.println("import javax.xml.stream.XMLStreamException;");
		      out.println("import javax.xml.stream.XMLStreamReader;");
		      Utils.printlnImport(out, name);

		      if(documentation != null)
		         out.println(Utils.comment(documentation));
		      out.println("public class " + Utils.javaIdentifier(name) + "Parser {");
		      out.println("   public static " + Utils.javaIdentifier(name) + " parse(XMLStreamReader reader) throws XMLStreamException {");
		      out.println("      " + Utils.javaIdentifier(name) + "Impl mystuff = new " + Utils.javaIdentifier(name) + "Impl();");
		      
		      // Process attributes.
		      if (!attrs.isEmpty()) {
		    	  out.println("// @@@_ATTRIBUTES");
		    	  out.println("     // attributes...");
		    	  out.println("     final int count = reader.getAttributeCount();");
		    	  out.println("     for (int i = 0; i < count; i ++) {");
		    	  out.println("           final String value = reader.getAttributeValue(i);");
		    	  out.println("           if (reader.getAttributeNamespace(i) != null) {");
		    	  out.println("                continue;");
		    	  out.println("           }");
		    	  out.println("            final String attribute = reader.getAttributeLocalName(i);");
		    	  boolean isfirst = true;
		    	  for(Property property : attrs) {
		    		  // the setters and a Bunch of ifs...
		    		  String s = Utils.normalize(property.name);
		    		  if (isfirst) {
		    			  out.println("            if (attribute.equals(\"" + property.name +  "\")) {");
		    			  isfirst = false;
		    		  } else
		    			  out.println("            } else if (attribute.equals(\"" +  property.name + "\")) {");
		    		  out.println("                // - " + property.name + " - " + property.type);
		    		  if (property.type.getLocalPart().equals("boolean"))
		    			  out.println("                mystuff.set" + s +   "(Boolean.valueOf(value));");
		    		  else
		    			  out.println("                mystuff.set" + s +   "(value);");
		    		  
		    	  }
	    		  out.println("            }");
		    	  out.println("     }");
		    	  out.println("// @@@_ATTRIBUTES");
		      }
		      
		      // Bunch of ifs ....
		      boolean isfirst = true;
		      for(Property property : properties)
		      {
		    	  isfirst = generateParserElement(out, name, property, isfirst);
		      } 
		      if (extensions.size()>0) {
		    	  for (QName extension : extensions) {
		    		  all.addExtensionParser(out, name, extension);
		    	  }	    	  
		      }
		      if (extensions.size()>0 || !properties.isEmpty()) {
		    	  out.println("// @@@_GENERATED_@@@");
		    	  out.println("          }");
		    	  out.println("      }");
		      }
		      out.println("      return mystuff;");
		      out.println("   }");
		      out.println("}");

		      out.flush();
		      out.close();
	   }
	   
	   /* generate the java file */
	   public void generate(AllClasses all) throws IOException {

		   generateInterface(name, documentation, properties, extensions, attributes, all);

		   // TODO add extension to allow stuff like public class JBossServletMetaData extends ServletMetaData
		   /* QName sextends = null; // Utils.getSpecQName(name);
		   if (sextends != null) {
			   List<QName> exts = new ArrayList<QName>(extensions);
			   exts.add(sextends);
			   generateImplementation(name, documentation, properties, exts, attributes, all);
		   } else {
		    */
		   generateImplementation(name, documentation, properties, extensions, attributes, all);
		   generateParser(name, documentation, properties, extensions,attributes, all);
	   }

	   public void buildListImports(QName name, AllClasses allclass) {
		   for (QName ext: extensions) {
			   Classes extclass = allclass.get(ext);
			   extclass.buildListImports(name);
			   extclass.buildListImports(name, allclass);
		   }
	   }
	   public void buildImports(AllClasses allclass) {
		   buildListImports(name);
		   buildListImports(name, allclass);
	   }

}
