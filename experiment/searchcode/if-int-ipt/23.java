int bnds_len = bnds.Count();
for (int k = 0; k < bnds_len; k++) {
IptBnd itm_bnd = bnds.FetchAt(k);
if (del_by_key) {
}	void MakeList(IptEventType eventType) {regy[AryIdx(eventType)] = new IptBndHash(eventType);}
static int AryIdx(IptEventType eventType) {
int v = eventType.Val();
if		(v == IptEventType_.KeyDown.Val())		return 0;

