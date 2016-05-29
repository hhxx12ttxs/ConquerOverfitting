private static final long serialVersionUID = -2234285177676920926L;

private String fieldName;

public IndexField(String fieldName) {
this.fieldName = fieldName;
}

public String getName() {
return fieldName;
}



@Override
public int compareTo(IndexField o) {

