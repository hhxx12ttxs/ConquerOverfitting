public List<Address> address;

@Expose
public String name;

@Expose
public String nameKey;

public String toString() {
sb.append(&quot;    name: &quot;).append(name).append(&quot;\n&quot;);
}
if (StringUtils.isNotEmpty(nameKey)) {
sb.append(&quot;    nameKey: &quot;).append(nameKey).append(&quot;\n&quot;);

