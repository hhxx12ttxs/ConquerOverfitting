public int maxNodesVisited(int[] parent, int L) {
n = parent.length;
depth(parent, 0, 0);
if (L <= depth)
depth = Math.max(depth, d);
for (int i = 0; i < n; i++)
if (parent[i] == pid)
depth(parent, i + 1, d + 1);
}

}

