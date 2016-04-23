package com.dennisjonsson.annotation.processor.parser;

import com.dennisjonsson.annotation.log.ast.LogUtils;
import com.dennisjonsson.annotation.markup.DataStructure;
import java.util.ArrayList;
import java.util.Arrays;

public class MethodsSource{
	
        final String className;
	ArrayList<String> types;
        private ArrayList<String> primitives = 
                new ArrayList<>();
        private ArrayList<String> looseTypes = 
                new ArrayList<>(Arrays.asList("int", "java.lang.String", "boolean", "char", "double", "float", "java.lang.Object"));
	public MethodsSource(String className){
            
            this.className = className;
            types = new ArrayList<String>();

	}
        
        
        public boolean isPrimitive(String primitive){
            for(String str : primitives){
               if(str.equalsIgnoreCase(primitive)){
                   return true;
               }
            }
            return false;
        }
	
	public String getMethods(DataStructure dStruct){
		
            
            String originalType = dStruct.getType();
            String cleanType = fixClassTypes(originalType);
            String primitiveType = cleanType;
            // check type already exists as method
            for(String type : types){
                    if(type.equalsIgnoreCase(primitiveType)){
                            return "";
                    }
            }
            
            String result = null;
            
             
            if(dStruct.getType().contains("[")){

                primitiveType = primitiveType.replaceAll("(\\[|\\])", "");

                result = "\n"+getArrayEvalsAndWrites(
                            countDimension(dStruct.getType()),
                            primitiveType
                        );
            }else{
                result = 
                     "\n"+getWriteMethod(primitiveType,0)
                    + "\n"+getEval(primitiveType,0);
                    //+ "\n"+getPrimitiveEvals();
            }
            
            types.add(primitiveType);
            types.add(cleanType);
            types.add(originalType);
            
            return result;
            

	}
        
        public String fixClassTypes(String type){
            if(!looseTypes.contains(type.replaceAll("(\\[|\\])", "")) || type.contains(".")){
                int i = type.lastIndexOf(".") + 1;
                String object = type.substring(i,i+1).toUpperCase()+type.substring(i+1,type.length());
                return type.substring(0,i)+object;
            }
            return type;
            
        }
	// logg(String op, String id, String uuid ,int index , int dimension){
	public String getReadMethod(){
            
            return "public static int read("
                    + "String name,"
                    + "int dimension, "
                    + "int index ){ "
                    + "\nlogger.read(\""+className+"\", name ,index ,dimension);\n"

                    + "return index; \n}\n";
	}
	
	public String getWriteMethod(String primitiveType, int dimension){
            return "public static "+primitiveType+" write(String name, "+primitiveType+" value, int sourceType, int targetType ){\n"
                       
                        + "logger.write(\""+className+"\", name, "+getValue(dimension,primitiveType,"value")+", sourceType, targetType);\n"
                        + "return value;\n"
                        + "}\n";
	}
        
        public String getEval(String primitiveType, int dimension){
            return "public static "+primitiveType+" eval( String targetId, "+primitiveType+" value, int expressionType, int [] line){"
                    + "\n"
                    + "logger.eval(\""+className+"\", targetId, "+getValue(dimension,primitiveType,"value")+", expressionType, line);\n"
                    + "return value;\n"
                    + "}\n";
        }
        
        public String getValue(int dimension, String type, String name){
            String value = name;
            if(dimension > 1){
                 value = "new "+LogUtils.class.getName()+"<"+type+">()."+LogUtils.COPY+"("+name+")";
            }
            if(dimension == 1){
                value = "java.util.Arrays.copyOf("+name+","+name+".length)";
            }
            return value;
        }
        
        
        
          public int countDimension(String type){
            int i = 0; 
            int j = 0;
            
            while(i != -1){
                i = type.indexOf("[", i+1);
                j++;
            }
            return j;
        }
          
        public String getArrayEvalsAndWrites(int dimensions, String primitiveType){
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i <= dimensions; i++){
                if(!isPrimitive(primitiveType)){
                    builder.append(getEval(primitiveType, i));
                    builder.append(getWriteMethod(primitiveType, i));
                    primitives.add(primitiveType);
                    types.add(primitiveType);
                }
                primitiveType = primitiveType + "[]";
            }
             
            return builder.toString();
        }
        
        
        public String getPrimitiveEvals(){
            StringBuilder builder = new StringBuilder();
         
            for(String str : looseTypes){
                if(!isPrimitive(str)){
                    builder.append(getWriteMethod(str,0));
                    builder.append(getEval(str,0));
                }
            }
            return builder.toString();
        }
        
        
}
