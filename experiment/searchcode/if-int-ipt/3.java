public IptKey FetchByKeyPress(int charVal) {
if (literals == null) Init();
IptKey rv = charKeys[charVal];
return (rv == null) ? IptKey_.None : rv;
return rvObj == null ? String_.Empty : (String)rvObj;
}
public void XtoIptKeyAry(ListAdp list) {
if (literals == null) Init();
for (int i = 0; i < keys.Count(); i++)

