/**
 * 
 */
package uk.ac.lkl.migen.system.util.gwt;

import uk.ac.lkl.migen.system.expresser.model.AttributeHandle;
import uk.ac.lkl.migen.system.expresser.model.ExpressedObject;

/**
 * These are MiGen-specific utility functions that are common to the stand-alone and web versions.
 * 
 * @author Ken Kahn
 *
 */
public class SharedMigenUtilities {
    
    // needs to not occur in output from UUID.toString()
    // nor in ServerUtils.generateGUIDString()
    private static final String OBJECT_ID_ATTRIBUTE_NAME_SEPARATOR = "==";

    /**
     * @param id
     * @return   String[0] if no attribute -- contains the object id - otherwise contains the object id 
     *           String[1] the attribute name or null
     *           String[2] object id within value of attribute or null
     */
    public static String[] parseUniqueId(String id) {
        return id.split(OBJECT_ID_ATTRIBUTE_NAME_SEPARATOR, 3);
    }

    /**
     * @param expressedObject
     * @param attributeHandle
     * @return a string that can be broken into the expressedObject id and attributeHandle name
     */
    public static String createAttributeId(ExpressedObject<?> expressedObject,
	                                   AttributeHandle<?> attributeHandle) {
        return expressedObject.getUniqueId() + OBJECT_ID_ATTRIBUTE_NAME_SEPARATOR + attributeHandle.getName();
    }
    
    /**
     * @param expressedObject
     * @param attributeHandle
     * @return a string that can be broken into the expressedObject id,  attributeHandle name, and objectWithinAttributeId
     */
    public static String createAttributeId(ExpressedObject<?> expressedObject,
	                                   AttributeHandle<?> attributeHandle,
	                                   String objectWithinAttributeId) {
        return expressedObject.getUniqueId() + OBJECT_ID_ATTRIBUTE_NAME_SEPARATOR 
        	                             + attributeHandle.getName()
        	                             + OBJECT_ID_ATTRIBUTE_NAME_SEPARATOR 
        	                             + objectWithinAttributeId;
    }

}

