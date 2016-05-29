this.nh=n;
}
}

public static void ConnectStoD(int s,int d){

ver[s].nh=new Neighbour(d,1,ver[s].nh);
}

public static boolean BFS(int s,int d,int parent[],Vertex[] vr){

