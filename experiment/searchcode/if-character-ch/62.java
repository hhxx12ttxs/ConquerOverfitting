public class Caractere implements CelulaPalavraCruzada {
private Character ch;

public Caractere(Character ch) {
this.ch = new Character(ch);
}

public String toString() {
return ch.toString();
}

public boolean equals(Object o) {
if (!(o instanceof Caractere)) {

