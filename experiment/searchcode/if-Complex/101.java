     * restriction<\/a> if simple types are involved.
/**
 * SchemaType is an interface implemented by all schema types: simple and complex types, built-in and
 * user-defined types.
 * <p>There is a hierarchy of interfaces that extend SchemaType, representing the top levels of the schema
 * type system: SimpleType and ComplexType, with SimpleType further subdivided into List, Union, and Atomic
 * types.<\/p>
 *
 * built-in types such as AnyType, AnySimpleType, and the built-in atomic types and list types; on the other
     * @return true if this SchemaType is a complex type
     */
     * Test whether this SchemaType is a complex type
     * , this constant represents the derivation by <a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/#key-typeRestriction'>
     * restriction<\/a> if complex types are involved, or a <a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/#element-restriction'>

