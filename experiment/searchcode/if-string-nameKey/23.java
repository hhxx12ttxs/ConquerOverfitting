public static ImplDefinition findByName(List<ImplDefinition> list, String name) {
for (ImplDefinition def : list) {
if (def.getName().equals(name))
private String name;
private String exportName;
private final String nameKey;
private final int[] supportedDataTypes;

