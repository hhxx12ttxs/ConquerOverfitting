public class Nod  implements Serializable{
public double element;
public Nod next;
public Nod prev;
Vectors.sort(this, incr);
}

public void addElement(double element){
if (size == 0){
Nod newElement = new Nod(element);

