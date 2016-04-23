/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modelinglab.ocl.core.standard.iterators.collection;

import org.modelinglab.ocl.core.ast.expressions.IteratorExp;
import org.modelinglab.ocl.core.ast.types.Classifier;
import org.modelinglab.ocl.core.ast.types.CollectionType;
import org.modelinglab.ocl.core.ast.types.PrimitiveType;
import org.modelinglab.ocl.core.standard.iterators.exceptions.IllegalIteratorException;

/**
 *
 * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
 */
public class Exists extends IteratorExp {

    public Exists(CollectionType sourceType, Classifier bodyType) throws IllegalIteratorException {
        setName("exists");
        setType(PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.BOOLEAN));
        if (!bodyType.equals(PrimitiveType.getInstance(PrimitiveType.PrimitiveKind.BOOLEAN))) {
            throw new IllegalIteratorException("exists", sourceType, bodyType, 
                    "In 'exists' iterators, body type must be Boolean");
        }
    }

    public Exists(Exists other) {
        super(other);
    }

    @Override
    public Exists clone() {
        return new Exists(this);
    }
}

