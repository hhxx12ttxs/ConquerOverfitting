MeshVertex result = new MeshVertex();
result.id = id;
result.X = Float.parseFloat(strings[0+offset]);
result.Y = Float.parseFloat(strings[1+offset]);
ArrayList<Float> result = new ArrayList<Float>();
result.add((Float)X);
result.add((Float)Y);
result.add((Float)Z);
if(xyzw)
result.add((Float)W);
return result;
}
}

