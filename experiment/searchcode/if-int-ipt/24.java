public void run_Set_msg(String key, int i, IptArg... ary)	{cfg.Set(key, make_(key, i), ary);}
GfoMsg make_(String key, int i) {return GfoMsg_.new_cast_(key).Add(&quot;val&quot;, i);}
public IptCfg_mok() {cfg = (IptCfg_base)IptCfg_.new_(&quot;cfg&quot;);} IptCfg_base cfg;

