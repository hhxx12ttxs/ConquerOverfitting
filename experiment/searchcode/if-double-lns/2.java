@DependsOn(parameter=&quot;search&quot;,enumClass={SearchType.class}, allowedValues={&quot;LNS&quot;})
public Double lnsProb;

@DependsOn(parameter=&quot;search&quot;,enumClass={SearchType.class}, allowedValues={&quot;LNS&quot;})
sb.append(&quot;, lnsIter: &quot;+lnsIter);
return sb.toString();
}

@Override
public boolean equals(Object obj) {
if (obj == null || !(obj instanceof MiniBrassConfig))

