public class ExternalId extends AbstractId {

public ExternalId(String id) {
super(id);
}

@Override
public String getId() {

if (!id.startsWith(&quot;ext-&quot;)) {
id = &quot;ext-&quot; + id;
}
return id;
}
}

