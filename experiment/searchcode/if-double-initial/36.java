public static final String name = &quot;double&quot;;

public JavaTypeDouble() {
super(name);
}

@Override
public String formatInitialExpression(String initial, String key,
String value, String varName) {
if (null == initial)
return &quot;0.0&quot;;

