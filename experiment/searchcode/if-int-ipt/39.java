public Xog_bnd_itm(String key, boolean sys, String cmd, int box, IptArg ipt) {
this.key = key; this.sys = sys; this.cmd = cmd; this.box = box; this.ipt = ipt;
public String Cmd() {return cmd;} public void Cmd_(String v) {cmd = v;} private String cmd;
public int Box() {return box;} private int box;
public IptArg Ipt() {return ipt;} public void Ipt_to_none() {ipt = IptKey_.None;} private IptArg ipt;

