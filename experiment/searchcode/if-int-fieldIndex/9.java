public Group addGroup(int fieldIndex) {
SimpleGroup g = new SimpleGroup(schema.getType(fieldIndex).asGroupType());
add(fieldIndex, g);
return g;
}

@Override
public Group getGroup(int fieldIndex, int index) {

