public int hashCode() {
final int prime = 31;
int result = 1;
result = prime * result + Float.floatToIntBits(u);
result = prime * result + Float.floatToIntBits(v);
return result;
}

@Override

