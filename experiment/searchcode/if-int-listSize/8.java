add(value);
}
}

@Override
public E set(int index, E value) {
if (index >= listSize || index < 0) {
return old;
}

@Override
public E remove(int index) {
if (index > listSize || index < 0) {

