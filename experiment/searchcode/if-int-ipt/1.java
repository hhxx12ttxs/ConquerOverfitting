public IptKey FetchByKeyPress(int charVal) {
if (literals == null) Init();
IptKey rv = charKeys[charVal];
return (rv == null) ? IptKey_.None : rv;
return rv == null ? String_.Empty : (String)rv;
}
public void XtoIptKeyAry(List_adp list) {
if (literals == null) Init();
for (int i = 0; i < keys.Count(); i++)

