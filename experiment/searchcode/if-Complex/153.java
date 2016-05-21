     * restriction<\/a> if complex types are involved, or a <a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/#element-restriction'>
     * restriction<\/a> if simple types are involved. 
     * , this constant represents the derivation by <a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/#key-typeRestriction'>
 *  The <code>TypeInfo<\/code> interface represents a type referenced from 
 * <code>Element<\/code> or <code>Attr<\/code> nodes, specified in the schemas 
 * associated with the document. The type is a pair of a namespace URI and 
 * name properties, and depends on the document's schema. 
 * <p> If the document's schema is an XML DTD [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0<\/a>], the values 
 * are computed as follows: 
 * <ul>
 * <li> If this type is referenced from an 
 * <code>Attr<\/code> node, <code>typeNamespace<\/code> is 
 * represents the <b>[attribute type]<\/b> property in the [<a href='http://www.w3.org/TR/2004/REC-xml-infoset-20040204/'>XML Information Set<\/a>]
 * . If there is no declaration for the attribute, <code>typeNamespace<\/code>
 *  and <code>typeName<\/code> are <code>null<\/code>. 

