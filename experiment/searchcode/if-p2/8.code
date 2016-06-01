public static Node kth(Node n, int k) {
if(k <= 0) return null;

Node p1 = n, p2 = n;

for(int i = 0; i < k - 1; i++) {
if(p2 == null) return null;
p2 = p2.next();
}

while(p2.next != null) {
p2 = p2.next;
p1 = p1.next;
}

return p1;
}

