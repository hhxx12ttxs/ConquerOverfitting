if(complexObs.contains(nodeName))
value = saveComplexObs(nodeName,value,formNode);
//Deal with complex obs first
if(complexObs.contains(nodeName)){
//Continue if complex obs has neither been replaced 
//with a another one nor cleared.
if(!dirtyComplexObs.contains(nodeName) && 
!( (newValue == null || newValue.trim().length() == 0) && 
(oldValue != null && oldValue.trim().length() > 0 ) ))
continue; //complex obs not modified.
String nodeName = node.getName();
if(complexObs.contains(nodeName)){
String key = getComplexDataKey(formNode.getAttributeValue(null, \"id\"),\"/form/obs/\" + node.getName() + \"/value\");

