cs.getDouble(&quot;spawns.&quot; + k + &quot;.x&quot;),
cs.getDouble(&quot;spawns.&quot; + k + &quot;.y&quot;),
cs.getDouble(&quot;spawns.&quot; + k + &quot;.z&quot;)
);
if (cs.isSet(k + &quot;.pitch&quot;)) {
l.setPitch((float)cs.getDouble(cs.getCurrentPath() + &quot;.spawns.&quot; + k + &quot;.pitch&quot;));
}
if (cs.isSet(k + &quot;.yaw&quot;)) {
l.setYaw((float)cs.getDouble(cs.getCurrentPath() + &quot;.spawns.&quot; + k + &quot;.yaw&quot;));

