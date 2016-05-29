import com.google.common.base.Splitter;
import lombok.Value;

@Value
public class MailId {

private final String backendId;
List<String> idStringParts = Splitter.on(&quot;:&quot;).splitToList(idString);
if (idStringParts.size() == 2) {
return new MailId(idStringParts.get(0), idStringParts.get(1));

