
public abstract class Animal {


private String latinName;
private String friendlyName = &quot;no name&quot;;
public String getFriendlyName() {
return friendlyName;
}

public void setFriendlyName(String name) {
if (name == null){
name = &quot;no name&quot;;
}
this.friendlyName = name;
}



}

