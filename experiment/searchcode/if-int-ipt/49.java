public IptCfgItm GetOrDefaultArgs(String bndKey, GfoMsg defaultMsg, IptArg[] defaultArgs) {
IptCfgItm rv = (IptCfgItm)hash.Get_by(bndKey);
if (rv == null) {	// no cfg
rv = IptCfgItm.new_().Key_(bndKey).Ipt_(List_adp_.many_((Object[])defaultArgs)).Msg_(defaultMsg);
List_adp list = (List_adp)owners.Get_by(bndKey);
if (list == null) return;
for (int i = 0; i < list.Count(); i++) {
IptBndsOwner owner = (IptBndsOwner)list.Get_at(i);

