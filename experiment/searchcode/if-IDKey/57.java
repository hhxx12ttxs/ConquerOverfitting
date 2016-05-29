implements ObjectIdResolver {
private Map<ObjectIdGenerator.IdKey, Object> _items = new HashMap<ObjectIdGenerator.IdKey, Object>();
public void bindItem(ObjectIdGenerator.IdKey idKey, Object object) {
if (this._items.containsKey(idKey)) {
throw new IllegalStateException(&quot;Already had POJO for id (&quot; + idKey.key.getClass().getName() + &quot;) [&quot; + idKey + &quot;]&quot;);

