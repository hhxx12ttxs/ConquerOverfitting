Copywrite (c) 2003 Zero Wait-State Inc. All rights reserved */

public class DefaultFieldname extends CriteriaModifierBase {
public String modify(String crit) {
if (-1 < crit.indexOf(&#39;=&#39;)) return crit;
return getFieldname() + &quot;=&quot; + crit.trim();

