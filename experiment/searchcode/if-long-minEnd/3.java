degree[destination]--;
minStart[destination] = Math.max(minStart[destination], minEnd[current]);
if (degree[destination] == 0)
if (minStart[i] == 0)
cGraph.addFlowEdge(2 * n, 2 * i, budget + 1);
if (minEnd[i] == answer)
cGraph.addFlowEdge(2 * i + 1, 2 * n + 1, budget + 1);

