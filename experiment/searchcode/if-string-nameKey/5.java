public static SimpleOption of(String id, String nameKey)
{
return of(id, nameKey, id.hashCode());
public static SimpleOption of(String id, String nameKey, int ordinal)
{
return new SimpleOption(id, nameKey, ordinal);

