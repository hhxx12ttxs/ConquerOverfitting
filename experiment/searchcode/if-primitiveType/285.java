/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modelinglab.ocl.utils.environments.java;

import org.modelinglab.ocl.core.ast.Element;
import org.modelinglab.ocl.core.ast.Namespace;
import org.modelinglab.ocl.core.ast.UmlClass;
import org.modelinglab.ocl.core.ast.types.Classifier;
import org.modelinglab.ocl.core.ast.types.PrimitiveType;
import org.modelinglab.ocl.utils.environments.StandardNamespace;
import org.modelinglab.utils.m2m.ModelTransformation;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
 */
public class JavaToUmlTask implements Callable<Namespace> {

    final JavaToUmlTaskContext context;

    public JavaToUmlTask(JavaToUmlTaskContext context) {
        this.context = context;
    }
    
    @Override
    public Namespace call() {
        context.validate(this);
        
        StandardNamespace standardNS = context.getStandardNS();
        
        Map<Class, Classifier> basicMap = createBasicMap(standardNS);
        
        JavaToUmlTransformationContext transformationContext = new JavaToUmlTransformationContext();
        transformationContext.setBasicMap(basicMap);
        
        JavaToUmlObjectFactory objectFactory = new JavaToUmlObjectFactory(context.getFilter());
        JavaToUmlTranslatorFactory translatorFactory = new JavaToUmlTranslatorFactory();
        
        ModelTransformation<Class, JavaToUmlTransformationContext> m2m 
                = new ModelTransformation<>(translatorFactory, objectFactory);
        
        for (Class basicClass : basicMap.keySet()) {
            m2m.getTranslatedObjects().put(basicClass, basicMap.get(basicClass));
        }
        
        Namespace result = new Namespace();
        
        for (Class classToTranslate : context.getClassesToMap()) {
            m2m.getTranslation(classToTranslate, transformationContext);            
        }
        Map<Class, Object> translatedObjects = m2m.getTranslatedObjects();
        for (Class classToTranslate : translatedObjects.keySet()) {
            final Classifier translation = (Classifier) translatedObjects.get(classToTranslate);
            
            Element inStandard = standardNS.lookup(translation.getName());
            if (inStandard != null) { //translation is already in standard namespace
                if (inStandard != translation) { //translation is different than element contained in standard namespace
                    throw new RuntimeException("Class "+ classToTranslate + " is different but "
                            + "has the same name as " + inStandard + ", which is declared in "
                            + "standard namespace."); //TODO: add new user exception
                }
            }
            else {
                Element alreadyMapped = result.lookup(translation.getName());
                if (alreadyMapped != null) { //translation is already mapped before
                    if (alreadyMapped != translation) { //translation is different than previously mapped element
                        throw new RuntimeException("Class "+ classToTranslate + " is different but has "
                                + "the same name as " + alreadyMapped + ", which was previously "
                                + "mapped."); //TODO: add new user exception
                    }
                }
                result.addMember(translation);
            }
        }
        
        //TODO: translate attributes to associations
        
        return result;        
    }
    
    /**
     * Create a map that associates standard Java classes with standard UML types.
     * @param standardNS
     * @return 
     */
    private Map<Class, Classifier> createBasicMap(StandardNamespace standardNS) {
        Map<Class, Classifier> basicTypes = new HashMap<Class, Classifier>();
        
        basicTypes.put(boolean.class,   PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.BOOLEAN));
        basicTypes.put(Boolean.class,   PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.BOOLEAN));
        basicTypes.put(int.class,       PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.INTEGER));
        basicTypes.put(Integer.class,   PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.INTEGER));
        basicTypes.put(float.class,     PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.REAL));
        basicTypes.put(Float.class,     PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.REAL));
        basicTypes.put(double.class,    PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.REAL));
        basicTypes.put(Double.class,    PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.REAL));
        basicTypes.put(String.class,    PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.STRING));
        basicTypes.put(char.class,      PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.STRING));
        basicTypes.put(Character.class, PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.STRING));
        
        basicTypes.put(File.class, (UmlClass) standardNS.lookup("File"));
        assert basicTypes.get(File.class) != null;
        
        basicTypes.put(Date.class, (UmlClass) standardNS.lookup("Date"));
        assert basicTypes.get(Date.class) != null;
        
        return basicTypes;
    }
    
}

