public class Nod implements  Serializable{
public double element;
public Nod next;
public Nod prev;

public Nod() {
public void addElement(double value) {
size++;
if (size==1) {
head = new Nod (value);

