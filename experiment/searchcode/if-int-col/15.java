/**
 * Copyright (C) 2009 Mathieu Carbou <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package euler;

import com.mycila.Matrix;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import static org.junit.Assert.*;

import java.util.Scanner;

/**
 * http://projecteuler.net/index.php?section=problems&id=18
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Problem018 {

    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();

        // We can represent the table as a DAG (http://en.wikipedia.org/wiki/Directed_acyclic_graph)
        // The root will be a virtual node 'start' connected to eah number at the bottom line
        // The end will be the node '75'. The weigth of the edges will be the numbers
        // The Bellman-Ford algorithm is used to find the longuest path, so we need to negate the values. This alorithm is the only one working
        // with negative values. Dijkstra does not work.

        String triangle = "75\n" +
                "95 64\n" +
                "17 47 82\n" +
                "18 35 87 10\n" +
                "20 04 82 47 65\n" +
                "19 01 23 75 03 34\n" +
                "88 02 77 73 07 63 67\n" +
                "99 65 04 28 06 16 70 92\n" +
                "41 41 26 56 83 40 80 70 33\n" +
                "41 48 72 33 47 32 37 16 94 29\n" +
                "53 71 44 65 25 43 91 52 97 51 14\n" +
                "70 11 33 28 77 73 17 78 39 68 17 57\n" +
                "91 71 52 38 17 14 91 43 58 50 27 29 48\n" +
                "63 66 04 68 89 53 67 30 73 16 69 87 40 31\n" +
                "04 62 98 27 23 09 70 98 73 93 38 53 60 04 23\n";

        // create the matrix from the triangle
        Matrix<Integer> matrix = readTriangle(triangle);
        //System.out.println(matrix);

        // create the graph
        DirectedAcyclicGraph<String, Edge> dag = createDAG(matrix);

        // Export the graph to show it in YED.
        //GraphMLExporter<String, Edge> graphmlExporter = new GraphMLExporter<String, Edge>();
        //GmlExporter<String, Edge> gmlExporter = new GmlExporter<String, Edge>();
        //graphmlExporter.export(new FileWriter("dag.graphml"), dag);
        //gmlExporter.export(new FileWriter("dag.gml"), dag);

        // get the shortest path, wich will be the longest path since we negated the weights
        BellmanFordShortestPath<String, Edge> path = new BellmanFordShortestPath<String, Edge>(dag, "start");
        System.out.println("SUM: " + (int) -path.getCost("(0,0)") + " in " + (System.currentTimeMillis() - time) + "ms");

        // display the path
        StringBuilder sb = new StringBuilder();
        for (Edge edge : path.getPathEdgeList("(0,0)")) {
            sb.append((int) -edge.getWeight()).append(" ");
        }
        System.out.println("PATH: " + sb);
        assertEquals("93 73 58 78 91 32 83 28 73 75 82 87 82 64 75 ", sb.toString());
    }

    private static DirectedAcyclicGraph<String, Edge> createDAG(Matrix<Integer> matrix) throws DirectedAcyclicGraph.CycleFoundException {
        DirectedAcyclicGraph<String, Edge> dag = new DirectedAcyclicGraph<String, Edge>(Edge.class);
        dag.addVertex("start");
        for (int row = matrix.rowCount() - 1; row >= 0; row--)
            for (int col = 0; col < matrix.columnCount() && matrix.isSet(row, col); col++) {
                String vertex = "(" + row + "," + col + ")";
                dag.addVertex(vertex);
                if (row == matrix.rowCount() - 1)
                    dag.setEdgeWeight(dag.addDagEdge("start", vertex), -matrix.get(row, col).doubleValue());
                else {
                    dag.setEdgeWeight(dag.addDagEdge("(" + (row + 1) + "," + col + ")", vertex), -matrix.get(row, col).doubleValue());
                    dag.setEdgeWeight(dag.addDagEdge("(" + (row + 1) + "," + (col + 1) + ")", vertex), -matrix.get(row, col).doubleValue());
                }
            }
        return dag;
    }

    private static Matrix<Integer> readTriangle(String triangle) {
        Matrix<Integer> matrix = Matrix.create(15, 15);
        Scanner lines = new Scanner(triangle);
        lines.useDelimiter("\\n");
        for (int row = 0; lines.hasNext(); row++) {
            Scanner numbers = new Scanner(lines.next());
            numbers.useDelimiter("\\s");
            for (int col = 0; numbers.hasNext(); col++) {
                matrix.set(row, col, numbers.nextInt());
            }
        }
        return matrix;
    }

    public static class Edge extends DefaultWeightedEdge {
        @Override
        public double getWeight() {
            return super.getWeight();
        }

        @Override
        public String getSource() {
            return (String) super.getSource();
        }

        @Override
        public String getTarget() {
            return (String) super.getTarget();
        }
    }
}

