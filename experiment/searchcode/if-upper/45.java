{  //All comparison are done in UpperCase to avoid problems
DataType aux=null;
if (isIntegerDataType(name)) aux= new DT_Integer(name);
//Removing the (idl) suffix of some Visio datatypes
if(nameUpper.length()>5)
{if (nameUpper.substring(nameUpper.length()-5).equals(&quot;(IDL)&quot;))

