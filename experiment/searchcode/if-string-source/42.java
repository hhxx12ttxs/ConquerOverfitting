private static final long serialVersionUID = 51906769556727320L;

protected String source;

public OAuthToken() {
}

public OAuthToken(String source) {
public String getSource() {
return source;
}

public void setSource(String source) {
if (source != null)
source = source.trim();

