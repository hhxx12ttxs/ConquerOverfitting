while (reader.hasNext()) {
String nameKey = reader.nextName();
if(nameKey.equals(card.KEY_NAME)){
card.setName(reader.nextString());
}
else if(nameKey.equals(card.KEY_COST)){

