private boolean hasCycle;

public Cycle (Graph G) {
seen = new boolean[G.V()];
for (int i = 0; i < G.V(); ++i) if (!seen[i])
Dfs(G,i);
}

public boolean HasCycle() { return hasCycle;}

