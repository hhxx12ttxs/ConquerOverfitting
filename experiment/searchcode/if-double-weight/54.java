import org.json.JSONObject;

public class BodyGoals {

private Double weight;
private Double fat;

public BodyGoals(JSONObject json) throws JSONException {
weight = json.getDouble(&quot;weight&quot;);
}
if (json.has(&quot;fat&quot;)) {
fat = json.getDouble(&quot;fat&quot;);

