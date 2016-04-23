package edu.stanford.bmir.protege.web.client.ui.frame;

import com.google.common.collect.*;
import edu.stanford.bmir.protege.web.shared.PrimitiveType;

import java.util.*;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 03/12/2012
 */
public class PropertyValueGridGrammar {

    private Set<PrimitiveType> fillerTypes = Sets.newLinkedHashSet();

    private Multimap<PrimitiveType, PrimitiveType> productionMap = ArrayListMultimap.create();

    public void addProduction(PrimitiveType propertyType, PrimitiveType fillerType) {
        productionMap.put(propertyType, fillerType);
        fillerTypes.add(fillerType);
    }

    public Collection<PrimitiveType> getPropertyTypes() {
        return new LinkedHashSet<PrimitiveType>(productionMap.keySet());
    }

    public Collection<PrimitiveType> getValueTypes() {
        return fillerTypes;
    }

    public Collection<PrimitiveType> getValueTypesForPropertyType(PrimitiveType propertyType) {
        Collection<PrimitiveType> values =  productionMap.get(propertyType);
        if(values == null) {
            return Collections.emptyList();
        }
        return new LinkedHashSet<PrimitiveType>(values);
    }

    public static PropertyValueGridGrammar getAnnotationsGrammar() {
        PropertyValueGridGrammar grammar = new PropertyValueGridGrammar();
        grammar.addProduction(PrimitiveType.ANNOTATION_PROPERTY, PrimitiveType.LITERAL);
        grammar.addProduction(PrimitiveType.ANNOTATION_PROPERTY, PrimitiveType.IRI);
        return grammar;
    }

    public static PropertyValueGridGrammar getClassGrammar() {
        PropertyValueGridGrammar grammar = new PropertyValueGridGrammar();
        grammar.addProduction(PrimitiveType.OBJECT_PROPERTY, PrimitiveType.CLASS);
        grammar.addProduction(PrimitiveType.OBJECT_PROPERTY, PrimitiveType.NAMED_INDIVIDUAL);
        grammar.addProduction(PrimitiveType.DATA_PROPERTY, PrimitiveType.DATA_TYPE);
        grammar.addProduction(PrimitiveType.DATA_PROPERTY, PrimitiveType.LITERAL);
        return grammar;
    }

    public static PropertyValueGridGrammar getNamedIndividualGrammar() {
        PropertyValueGridGrammar grammar = new PropertyValueGridGrammar();
        grammar.addProduction(PrimitiveType.ANNOTATION_PROPERTY, PrimitiveType.LITERAL);
        grammar.addProduction(PrimitiveType.ANNOTATION_PROPERTY, PrimitiveType.IRI);
        grammar.addProduction(PrimitiveType.OBJECT_PROPERTY, PrimitiveType.NAMED_INDIVIDUAL);
        grammar.addProduction(PrimitiveType.OBJECT_PROPERTY, PrimitiveType.CLASS);
        grammar.addProduction(PrimitiveType.DATA_PROPERTY, PrimitiveType.LITERAL);
        grammar.addProduction(PrimitiveType.DATA_PROPERTY, PrimitiveType.DATA_TYPE);
        return grammar;
    }

//    public Set<PrimitiveType> getPropertyTypesForValueType(PrimitiveType valueType) {
//
//    }
}

