import java.util.ArrayList;
class Writer {
private ArrayList<Text> elenco;

Writer() {
public String getText(int id) {
for(Text text:elenco) {
if(text.getId() == id) {
return text.getText();

