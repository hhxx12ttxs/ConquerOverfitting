package com.google.code.gaeom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify that a field on a class should be populated with either the Long or the String id.
 * 
 * NOTE: if the object specifies this annotation on a String field, then the ids must be provided (either in the field
 * or in the store command) at store time. If a Long value is used, the datastore will generate an id automatically.
 * 
 * @author Peter Murray <gaeom@pmurray.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id
{
}

