
public abstract class Descriptor implements Comparable<Descriptor> {
String desc;

public Descriptor(String desc) {
this.desc = desc;
}

public String toString() {
return desc;

