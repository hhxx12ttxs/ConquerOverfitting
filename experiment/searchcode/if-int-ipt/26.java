public void Add(IptBnd bnd) {
for (IptBndHash list : regy)
if (IptEventType_.Has(bnd.EventTypes(), list.EventType()))
list.Add(bnd);
for (int i = 0; i < bnd.Ipts().Count(); i++) {
IptArg arg = (IptArg)bnd.Ipts().Get_at(i);

