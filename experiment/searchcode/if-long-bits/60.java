hash = 79 * hash + (int) (Double.doubleToLongBits(a) ^ (Double.doubleToLongBits(a) >>> 32));
hash = 79 * hash + (int) (Double.doubleToLongBits(b) ^ (Double.doubleToLongBits(b) >>> 32));
return hash;
}

// public int hashCode() {

