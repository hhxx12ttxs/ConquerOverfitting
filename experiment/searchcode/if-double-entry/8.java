TableEntry currentEntry = currentScope.firstEntry;

while(currentEntry != null)
{
if(currentEntry.name.equals(name))
TableEntry typeEntry = new TableEntry();
typeEntry.type = TableEntry.Type.DOUBLE;
typeEntry.kind = TableEntry.Kind.PROPERTY;
typeEntry.name = &quot;type&quot;;

