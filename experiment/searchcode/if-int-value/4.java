public int intValue() {
return intValue;
}

@Override
public void setValue(Integer value) {
if (value == null) throw new IllegalArgumentException();
this.intValue = intValue;
if (oldValue != this.intValue) changed();
}

}

