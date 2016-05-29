import com.fasterxml.jackson.annotation.ObjectIdGenerator.IdKey;
import java.util.HashMap;
import java.util.Map;

public class SimpleObjectIdResolver implements ObjectIdResolver {
public void bindItem(IdKey idKey, Object obj) {
if (this._items.containsKey(idKey)) {
throw new IllegalStateException(&quot;Already had POJO for id (&quot; + idKey.key.getClass().getName() + &quot;) [&quot; + idKey + &quot;]&quot;);

