public static Port fromId(String id) {
return new Port(id);
}

private String id;

private Port(String id) {
setId(id);
}

public String getId() {
return id;
}

public void setId(String id) {
this.id = id;

