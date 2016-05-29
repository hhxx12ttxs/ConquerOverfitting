* @param clazz The type of the object
* @return null if no class is found
*/
public MappingStrategy getStrategy(Class<?> clazz) {
clazz = JpaClientUtils.fixReturnType(clazz);
for (Class<?> c : list) {
if(child.isAssignableFrom(c)) {
child = c;
}
}
mappingStrategy = getMapping().get(child);

