KVReportJni.IDKeyDataInfo localIDKeyDataInfo = (KVReportJni.IDKeyDataInfo)localIterator.next();
if (localIDKeyDataInfo == null)
{
u.e(&quot;!44@/B4Tb64lLpJlEqDd0Ubo4Jxu+CyGfot/sNGdExUpV40=&quot;, &quot;report idkeyGroupStat info == null return&quot;);
return;
}
if ((localIDKeyDataInfo.GetID() < 0L) || (localIDKeyDataInfo.GetKey() < 0L) || (localIDKeyDataInfo.GetValue() <= 0L))

