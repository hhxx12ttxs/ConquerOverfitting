// {{{ prevent infinite recursion caused by complex types referencing themselves
if(typedef.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
{
if(complex != null && needToCalcContent)
if(seenComplexTypes.containsKey(complex)){
{
    following code is awkward :
    - if element is of complex type and this complex type is being
      expandend (ie is in seenComplexTypes)
if(same == null){
// same complextype but different names
ElementDecl sameContent = seenComplexTypes.get(complex).get(0);
// {{{ Expand complex type

