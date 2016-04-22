package io.logbase.collections.impl;

import io.logbase.collections.BatchList;
import io.logbase.collections.BatchListIterator;
import io.logbase.collections.BatchListReader;
import io.logbase.collections.BatchListWriter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: karthik
 */
public class ListBackedBatchList<E> implements BatchList<E>, BatchListReader<E>, BatchListWriter<E> {
  private final Class primitiveType;
  List<E> list = new ArrayList();
  private long memSize = 0;

  public ListBackedBatchList(Class primitiveType){
    this.primitiveType = primitiveType;
  }

  @Override
  public long size() {
    return list.size();
  }

  @Override
  public BatchListIterator<E> iterator(long maxIndex) {
    return new BatchListIteratorWrapper(list.iterator(), maxIndex, primitiveType);
  }

  @Override
  public BatchListReader<E> reader(long maxIndex) {
    return this;
  }

  @Override
  public BatchListWriter<E> writer() {
    return this;
  }

  @Override
  public Class<E> type() {
    return primitiveType;
  }

  @Override
  public long memSize(){
    return memSize;
  }

  @Override
  public E get(long index) {
    return list.get((int) index);
  }

  @Override
  public void add(Object values, int offset, int length) {
    for (int i = offset; i < length; i++) {
      list.add((E) Array.get(values, i));
    }
  }

  @Override
  public void add(E value) {
    list.add(value);

    // TODO - optimize this
    int bytes = 0;
    if (value instanceof Integer) {
      bytes = Integer.BYTES;
    } else if (value instanceof Long) {
      bytes = Long.BYTES;
    } else if (value instanceof Float) {
      bytes = Float.BYTES;
    } else if (value instanceof Double) {
      bytes = Double.BYTES;
    } else if (value instanceof String) {
      bytes = ((String)value).length();
    } else {
      bytes = 1;
    }
    memSize = memSize + bytes;
  }

  @Override
  public boolean close() {
    return false;
  }


}

