if (o == null || getClass() != o.getClass()) return false;

ObjectKey objectKey = (ObjectKey) o;

if (keyIndex != objectKey.keyIndex) return false;
public int hashCode() {
return (int) keyIndex;
}

/**
* This is an index that uniquely identifies this key in the cluster.

