PersistentType jpt = it.next();
PersistentType superclass = fp.getFirstSuperclassBelongingToTheDiagram(jpt);
if (superclass == null)
for (int i = 0; i < 10000000; i++) {
if (!hasAttributeNamed(jpt, genName))
return genName;

