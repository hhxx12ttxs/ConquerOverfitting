//Use 2 pointers p1 and p2. Move p2 by n-1 times. Now start moving p1 and p2 till p2 reaches end of list
for (int i = 0; i < n ; i++) { // make then n nodes apart.
if (p2 == null) {
return null;
}
p2 = p2.next;

