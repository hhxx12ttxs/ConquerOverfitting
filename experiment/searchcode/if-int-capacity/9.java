package matchmaker;

import engine.Student;


public class Group {
private int capacity;
private Student[] students;
public boolean setCapacity(int capacity) {
if (capacity < this.capacity)
return false;
else {
Student[] temp = students;

