public static Map<String, String> parseParameter(HierarchicalConfiguration config, String subKey, String nameKey, String valueKey)
{
Map<String, String> parameters = new HashMap<String, String>();
for(HierarchicalConfiguration subConfig : (List<HierarchicalConfiguration>)config.configurationsAt(subKey)){
String name = subConfig.getString(nameKey);
if(name.equals(nameValue)){

