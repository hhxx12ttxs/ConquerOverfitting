private String _name;
private String _lowerName;
private String _nativeName;

protected EntityItem(String name, String nativeName)
{
_name = name;
_lowerName = name.toLowerCase();

if(nativeName != null)

