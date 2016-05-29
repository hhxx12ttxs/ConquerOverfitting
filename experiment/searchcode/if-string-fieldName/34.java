this.fieldName = new Hashtable<>();
createTable();
}

public String getFieldName(String field) {
return fieldName.get(field);
return fieldName.keySet();
}

public boolean checkIfContais(String word){
return fieldName.containsKey(word);
}
public void createTable() {

