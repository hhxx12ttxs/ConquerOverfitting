import java.util.*;
import java.io.*;

public class A {
    FastScanner in;
    PrintWriter out;

    class Point {
        int x;
        int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    class Edge {
        int u;
        int v;
        int distance;

        Edge(int u, int v, int distance) {
            this.u = u;
            this.v = v;
            this.distance = distance;
        }
    }

    class Pair implements Comparable<Pair> {
        int vertex;
        int priority;

        Pair(int vertex, int priority) {
            this.vertex = vertex;
            this.priority = priority;
        }

        @Override
        public int compareTo(Pair o) {
            return -Integer.compare(priority, o.priority);
        }
    }

    public static final int INF = Integer.MAX_VALUE / 2;

    Point[] points;
    int[][] weight;
    boolean[] used;
    int[] priority;
    int[] selfEdge;
    int n;

    public void solve() throws IOException {
        n = in.nextInt();
        used = new boolean[n];
        points = new Point[n];
        priority = new int[n];
        selfEdge = new int[n];
        weight = new int[n][n];
        Arrays.fill(priority, INF);
        Arrays.fill(selfEdge, -1);
        priority[0] = 0;

        for (int i = 0; i < n; i++) {
            points[i] = new Point(in.nextInt(), in.nextInt());
        }

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                weight[u][v] = (points[u].x - points[v].x) * (points[u].x - points[v].x) + (points[u].y - points[v].y) * (points[u].y - points[v].y);
            }
        }

        out.println(primMST());
    }

    public double primMST() {
        for (int i = 0; i < n; i++) {
            int from = -1;
            for (int to = 0; to < n; to++) {
                if (!used[to] && (from == -1 || priority[to] < priority[from])) {
                    from = to;
                }
            }
            used[from] = true;
            for (int to = 0; to < n; to++) {
                if (priority[to] > weight[from][to] && from != to && !used[to]) {
                    priority[to] = weight[from][to];
                    selfEdge[to] = from;
                }
            }
        }

        double answer = 0;
        for (int i = 0; i < n; i++) {
            answer += Math.sqrt(priority[i]);
        }
        return answer;
    }

    public void run() {
        try {
            in = new FastScanner(new File("spantree.in"));
            out = new PrintWriter(new File("spantree.out"));

            solve();

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class FastScanner {
        BufferedReader br;
        StringTokenizer st;

        FastScanner(File f) {
            try {
                br = new BufferedReader(new FileReader(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }
    }

    public static void main(String[] arg) {
        new A().run();
    }
}
