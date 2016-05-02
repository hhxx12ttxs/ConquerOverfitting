/*
 * Copyright 2011-2012 Jakob Flierl 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * 
 */
package lfapi.v2.services.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import com.google.gson.annotations.SerializedName;

import lfapi.v2.schema.SchemaEntity;

/**
 * The class SampleSchulzeMethodGraphs.
 * 
 * @author Jakob Flierl
 */
@SuppressWarnings("nls")
public class SampleSchulzeMethodGraphs {

    /** The Constant log. */
    final static Logger log = Logger.getLogger(SampleSchulzeMethodGraphs.class
            .getCanonicalName());

    /** The di-graph. */
    public DiGraph diGraph;

    /**
     * Instantiates a new sample schulze method graphs.
     */
    public SampleSchulzeMethodGraphs() {
        diGraph = new DiGraph();
    }
    
    /**
     * The Class GraphVizEntity.
     */
    public static class GraphVizEntity extends SchemaEntity {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -3925848684679012738L;

        /* (non-Javadoc)
         * @see lfapi.v2.schema.SchemaEntity#toString()
         */
        @Override
        public String toString() {
            String str = " ";

            Map<String, String> params = getParams();

            for (String s : params.keySet()) {
                String v = params.get(s);
                if (!s.isEmpty() && v != null && !v.isEmpty()) {
                    str += s + " = \"" + v + "\" ";
                }
            }

            return str;
        }
    }

    /**
     * The Class DiGraph.
     */
    public static class DiGraph extends GraphVizEntity {

        /** The name. */
        public String name;

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1318139822955008172L;

        /**
         * Instantiates a new di graph.
         */
        public DiGraph() {
            graphAttr = new GraphAttr();
            nodeAttr = new NodeAttr();
            edgeAttr = new EdgeAttr();

            nodes = new LinkedList<Node>();
            edges = new LinkedList<Edge>();
        }

        /** The graph attr. */
        @SerializedName("graphAttr")
        public SampleSchulzeMethodGraphs.GraphAttr graphAttr;

        /** The node attr. */
        @SerializedName("nodeAttr")
        public SampleSchulzeMethodGraphs.NodeAttr nodeAttr;

        /** The edge attr. */
        @SerializedName("edgeAttr")
        public SampleSchulzeMethodGraphs.EdgeAttr edgeAttr;

        /** The nodes. */
        public List<Node> nodes;
        
        /** The edges. */
        public List<Edge> edges;

        /**
         * To string.
         *
         * @param l the l
         * @return the string
         */
        public static String toString(List<? extends SchemaEntity> l) {
            StringBuilder sb = new StringBuilder();

            for (SchemaEntity se : l) {
                sb.append("  " + se.toString() + "\n");
            }

            return sb.toString();
        }

        /* (non-Javadoc)
         * @see lfapi.v2.services.example.SampleSchulzeMethodGraphs.GraphVizEntity#toString()
         */
        @Override
        public String toString() {
            return "digraph \"" + name + "\" {\n" + " graph "
                    + graphAttr.toString() + "\n" + " node "
                    + nodeAttr.toString() + "\n" + " edge "
                    + edgeAttr.toString() + "\n\n" + toString(nodes)
                    + toString(edges) + "}\n";
        }
    }

    /**
     * The Class GraphAttr.
     */
    public static class GraphAttr extends GraphVizEntity {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 3422378543703336419L;

        /** The K. */
        @SerializedName("K")
        public Double K;

        /** The start. */
        @SerializedName("start")
        public Integer start;

        /** The maxiter. */
        @SerializedName("maxiter")
        public Integer maxiter;

        /** The bgcolor. */
        @SerializedName("bgcolor")
        public String bgcolor;

        /** The overlap. */
        @SerializedName("overlap")
        public Boolean overlap;

        /** The splines. */
        @SerializedName("splines")
        public Boolean splines;
        
        /** The fixedsize. */
        @SerializedName("fixedsize")
        public Boolean fixedsize;
        
        /** The regular. */
        @SerializedName("regular")
        public Boolean regular;
        
        /** The outputorder. */
        @SerializedName("outputorder")
        public String outputorder;

        /* (non-Javadoc)
         * @see lfapi.v2.services.example.SampleSchulzeMethodGraphs.GraphVizEntity#toString()
         */
        @Override
        public String toString() {
            String txt = super.toString();
            return txt.length() == 1 ? "" : "[ " + super.toString() + " ]";
        }
    }

    /**
     * The Class NodeAttr.
     */
    public static class NodeAttr extends GraphVizEntity {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 3422378543703336419L;

        /** The style. */
        @SerializedName("style")
        public String style;

        /** The color. */
        @SerializedName("color")
        public String color;

        /** The fillcolor. */
        @SerializedName("fillcolor")
        public String fillcolor;

        /** The shape. */
        @SerializedName("shape")
        public String shape;

        /** The penwidth. */
        @SerializedName("penwidth")
        public Integer penwidth;

        /** The fontcolor. */
        @SerializedName("fontcolor")
        public String fontcolor;

        /** The fontsize. */
        @SerializedName("fontsize")
        public Integer fontsize;

        /** The fontname. */
        @SerializedName("fontname")
        public String fontname;

        /** The labelloc. */
        @SerializedName("labelloc")
        public String labelloc;

        /* (non-Javadoc)
         * @see lfapi.v2.services.example.SampleSchulzeMethodGraphs.GraphVizEntity#toString()
         */
        @Override
        public String toString() {
            String txt = super.toString();
            return txt.length() == 1 ? "" : "[" + super.toString() + "]";
        }
    }

    /**
     * The Class EdgeAttr.
     */
    public static class EdgeAttr extends GraphVizEntity {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 3422378543703336419L;

        /** The style. */
        @SerializedName("style")
        public String style;

        /** The color. */
        @SerializedName("color")
        public String color;

        /** The fillcolor. */
        @SerializedName("fillcolor")
        public String fillcolor;

        /** The shape. */
        @SerializedName("shape")
        public String shape;

        /** The penwidth. */
        @SerializedName("penwidth")
        public Integer penwidth;

        /** The fontcolor. */
        @SerializedName("fontcolor")
        public String fontcolor;

        /** The fontsize. */
        @SerializedName("fontsize")
        public Integer fontsize;

        /** The fontname. */
        @SerializedName("fontname")
        public String fontname;

        /** The arrowsize. */
        @SerializedName("arrowsize")
        public Double arrowsize;

        /** The arrowhead. */
        @SerializedName("arrowhead")
        public String arrowhead;

        /* (non-Javadoc)
         * @see lfapi.v2.services.example.SampleSchulzeMethodGraphs.GraphVizEntity#toString()
         */
        @Override
        public String toString() {
            String txt = super.toString();
            return txt.length() == 1 ? "" : "[" + super.toString() + "]";
        }
    }

    /**
     * The Class Node.
     */
    public static class Node extends NodeAttr {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 7453140022187823220L;

        /** The name. */
        @SerializedName("")
        public String name;

        /** The label. */
        @SerializedName("label")
        public String label;

        /** The pos. */
        @SerializedName("pos")
        public String pos;

        /** The width. */
        @SerializedName("width")
        public Double width;

        /** The height. */
        @SerializedName("height")
        public Double height;

        /* (non-Javadoc)
         * @see lfapi.v2.services.example.SampleSchulzeMethodGraphs.NodeAttr#toString()
         */
        @Override
        public String toString() {
            return "\"" + name + "\" " + super.toString();
        }
    }

    /**
     * The Class Edge.
     */
    public static class Edge extends EdgeAttr {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -2434424670170371329L;

        /** The from. */
        @SerializedName("")
        public String from;

        /** The to. */
        @SerializedName("")
        public String to;

        /* (non-Javadoc)
         * @see lfapi.v2.services.example.SampleSchulzeMethodGraphs.EdgeAttr#toString()
         */
        @Override
        public String toString() {
            return "\"" + from + "\" -> " + "\"" + to + "\"" + super.toString();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return diGraph.toString();
    }

    /**
     * Prints the.
     *
     * @param m the m
     */
    public static void print(int[][] m) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                if (m[i][j] != 0) {
                    print(String.format("%2d", m[i][j]) + " ");
                } else {
                    print(" -  ");
                }
            }
            println();
        }
        println();
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        example1(); example2(); example3(); example4(); example6(); example7();
        example8(); example8Files();
    }
    
    // Schulze method paper example 3 missing.
    // Schulze method paper example 4 missing.
    // Schulze method paper example 5 missing.
    // Schulze method paper example 6 missing.
    
    // Wikipedia example 5 missing.
    // Wikipedia example 8 missing.

    /**
     * Wikipedia example one.
     *
     * @throws Exception the exception
     */
    public static void example1() throws Exception {
        VotePreference[] vps = { new VotePreference(5, "A,C,B,E,D"),
                new VotePreference(5, "A,D,E,C,B"),
                new VotePreference(8, "B,E,D,A,C"),
                new VotePreference(3, "C,A,B,E,D"),
                new VotePreference(7, "C,A,E,B,D"),
                new VotePreference(2, "C,B,A,D,E"),
                new VotePreference(7, "D,C,E,B,A"),
                new VotePreference(8, "E,B,A,D,C"), };

        generateAllGraphs(vps, "Schulze_method_example1", "doc/EX1/", 0.217, false,
                null);

    }

    /**
     * Wikipedia example two.
     *
     * @throws Exception the exception
     */
    public static void example2() throws Exception {
        VotePreference[] vps = { new VotePreference(5, "A,C,B,D"),
                new VotePreference(2, "A,C,D,B"),
                new VotePreference(3, "A,D,C,B"),
                new VotePreference(4, "B,A,C,D"),
                new VotePreference(3, "C,B,D,A"),
                new VotePreference(3, "C,D,B,A"),
                new VotePreference(1, "D,A,C,B"),
                new VotePreference(5, "D,B,A,C"),
                new VotePreference(4, "D,C,B,A"), };

        Map<String, String> edgePos = new HashMap<String, String>();
        edgePos.put("AC", "0.60,0.90!");
        edgePos.put("DB", "1.15,0.90!");

        generateAllGraphs(vps, "Schulze_method_example2", "doc/EX2/", 0.3, false,
                edgePos);
    }

    /**
     * Wikipedia example three.
     *
     * @throws Exception the exception
     */
    public static void example3() throws Exception {
        VotePreference[] vps = { new VotePreference(3, "A,B,D,E,C"),
                new VotePreference(5, "A,D,E,B,C"),
                new VotePreference(1, "A,D,E,C,B"),
                new VotePreference(2, "B,A,D,E,C"),
                new VotePreference(2, "B,D,E,C,A"),
                new VotePreference(4, "C,A,B,D,E"),
                new VotePreference(6, "C,B,A,D,E"),
                new VotePreference(2, "D,B,E,C,A"),
                new VotePreference(5, "D,E,C,A,B"), };

        generateAllGraphs(vps, "Schulze_method_example3", "doc/EX3/", 0.3, false, null);
    }

    /**
     * Wikipedia example 4, schulze method paper example 2.
     *
     * @throws Exception the exception
     */
    public static void example4() throws Exception {
        VotePreference[] vps = { new VotePreference(3, "A,B,C,D"),
                new VotePreference(2, "D,A,B,C"),
                new VotePreference(2, "D,B,C,A"),
                new VotePreference(2, "C,B,D,A"), };

        Map<String, String> ep = new HashMap<String, String>();
        ep.put("AC", "0.60,0.90!");
        ep.put("BD", "1.15,0.90!");

        generateAllGraphs(vps, "Schulze_method_example4", "doc/EX4/", 0.3, false, ep);
    }

    /**
     * Wikipedia example six.
     *
     * @throws Exception the exception
     */
    public static void example6() throws Exception {
        SchulzeMethodNode A = new SchulzeMethodNode("A", "-0.35,  2.05!", false);
        SchulzeMethodNode B = new SchulzeMethodNode("B", " 2.05,  2.05!", false);
        SchulzeMethodNode C = new SchulzeMethodNode("C", " 2.05, -0.35!", false);
        SchulzeMethodNode D = new SchulzeMethodNode("D", "-0.35, -0.35!", false);

        SchulzeMethodNode[] n = { A, B, C, D };

        SchulzeMethodEdge AB = new SchulzeMethodEdge("A", "B", 16, false);
        SchulzeMethodEdge AC = new SchulzeMethodEdge("A", "C", 17, false,
                "0.60,0.90!");
        SchulzeMethodEdge BC = new SchulzeMethodEdge("B", "C", 19, false);
        SchulzeMethodEdge CD = new SchulzeMethodEdge("C", "D", 20, false);
        SchulzeMethodEdge DA = new SchulzeMethodEdge("D", "A", 18, false);
        SchulzeMethodEdge DB = new SchulzeMethodEdge("D", "B", 21, false,
                "1.15,0.90!");

        String name = "Schulze method example 6";
        String fName = "doc/EX6/" + name.replaceAll(" ", "_");

        toDot(name + "a", fName + "a", n, new SchulzeMethodEdge[] { AB, AC, BC,
                CD, DA, DB }, 0.3, false);
        
        toDot(name + "b", fName + "b", n, new SchulzeMethodEdge[] { AC, BC,
                CD, DA, DB }, 0.3, false);
        
        DB.pos = null;
        
        toDot(name + "c", fName + "c", n, new SchulzeMethodEdge[] { BC,
                CD, DA, DB }, 0.3, false);
        
        toDot(name + "d", fName + "d",
                new SchulzeMethodNode[] { B, C, D},
                new SchulzeMethodEdge[] { BC, CD, DB }, 0.3, false);

        toDot(name + "e", fName + "e",
                new SchulzeMethodNode[] { B, C, D},
                new SchulzeMethodEdge[] { CD, DB }, 0.3, false);
    }

    /**
     * Wikipedia example 7, schulze method paper example 1.
     *
     * @throws Exception the exception
     */
    public static void example7() throws Exception {
        VotePreference[] vps = {
                new VotePreference(8, "A,C,D,B"),
                new VotePreference(2, "B,A,D,C"),
                new VotePreference(4, "C,D,B,A"),
                new VotePreference(4, "D,B,A,C"),
                new VotePreference(3, "D,C,B,A") };

        Map<String, String> ep = new HashMap<String, String>();
        ep.put("DB", "0.60,0.90!");
        ep.put("AC", "1.15,0.90!");

        generateAllGraphs(vps, "Schulze_method_example7", "doc/EX7/", 0.3, false, ep);
    }

    /**
     * Wikipedia example eight.
     *
     * @throws Exception the exception
     */
    public static void example8() throws Exception {
        double r = 0.39;
        
        boolean f = false;
        
        @SuppressWarnings("unused")
        boolean t = true;

        SchulzeMethodNode JH = new SchulzeMethodNode("JH", "-0.35,  2.05!", f, r);
        SchulzeMethodNode RP = new SchulzeMethodNode("RP", " 2.05,  2.05!", f, r);
        SchulzeMethodNode SS = new SchulzeMethodNode("SS", " 2.05, -0.35!", f, r);
        SchulzeMethodNode RS = new SchulzeMethodNode("RS", "-0.35, -0.35!", f, r);

        SchulzeMethodNode[] n = { JH, RP, SS, RS };

        SchulzeMethodEdge JH_RP = new SchulzeMethodEdge("JH", "RP", 841, f);
        SchulzeMethodEdge JH_SS = new SchulzeMethodEdge("JH", "SS", 798, f, "0.60,0.90!");
        SchulzeMethodEdge RP_RS = new SchulzeMethodEdge("RP", "RS", 797, f, "1.15,0.90!");
        SchulzeMethodEdge RP_SS = new SchulzeMethodEdge("RP", "SS", 755, f);
        SchulzeMethodEdge SS_RS = new SchulzeMethodEdge("SS", "RS", 778, f);
        SchulzeMethodEdge RS_JH = new SchulzeMethodEdge("RS", "JH", 745, f);

        String name = "Schulze method example 8";
        String fName = "doc/EX8/" + name.replaceAll(" ", "_");

        toDot(name, fName, n, new SchulzeMethodEdge[] { JH_RP, JH_SS, RP_RS,
                RP_SS, SS_RS, RS_JH }, 0.3, false);
    }
    
    /**
     * Example8 files.
     *
     * @throws Exception the exception
     */
    public static void example8Files() throws Exception {
        Map<String, VoteCandidate> vcm = isVoteCandidates(SampleSchulzeMethodGraphs.class.getResourceAsStream("wp-2008-candidates.txt"));
        
        ArrayList<VoteCandidate> vcsl = toSortedList(vcm);
        
        println(vcsl.toString());
        
        ArrayList<Vote> vl = isVotes(SampleSchulzeMethodGraphs.class.getResourceAsStream("wp-2008-votes.txt"));
        
        // println(vl.toString());
        
        print("calculating p .. ");
        
        int[][] d = new int[vcsl.size()][vcsl.size()];
        
        for (int i = 0; i < vcsl.size(); i++) {
            for (int j = 0; j < vcsl.size(); j++) {
                for (int k = 0; k < vl.size(); k++) {
                    if (i != j) {
                        Vote v = vl.get(k);
                    
                        VoteCandidate a = vcsl.get(i);
                        VoteCandidate b = vcsl.get(j);
                        
                        int pref = v.getPreference(a, b);
                        
                        if (pref > 0) {
                            d[i][j]++;
                        }
                    }
                }
            }
        }
        
        println("ok. WP 2008 p:");
        print(d);
        
        int[][] p = computeStrongestPath(d);
        
        println("p:");
        print(p);
        
        String name = "Schulze method example WP 2008";
        String dir = "doc/EX-WP-2008/";

        toDot(dir, name, vcsl, d, p, null, 0.2, true, 3.5, null);
    }
    
    /**
     * Test vote class.
     */
    public static void testVoteClass() {
        VoteCandidate a = new VoteCandidate("A", "A");
        VoteCandidate b = new VoteCandidate("B", "B");
        VoteCandidate c = new VoteCandidate("C", "C");
        
        Vote v = new Vote("A,B");
        
        println("V (A,B): ? A:A = " + v.getPreference(a, a));
        println("V (A,B): ? A:B = " + v.getPreference(a, b));
        println("V (A,B): ? B:A = " + v.getPreference(b, a));
        println("V (A,B): ? B:B = " + v.getPreference(b, b));
        
        println();
        
        v = new Vote("C,AB");
        
        println("V (C,AB): ? A:B = " + v.getPreference(a, b));
        println("V (C,AB): ? B:A = " + v.getPreference(b, a));
        println("V (C,AB): ? A:C = " + v.getPreference(a, c));
        println("V (C,AB): ? C:A = " + v.getPreference(c, a));
        println("V (C,AB): ? C:B = " + v.getPreference(c, b));
        println("V (C,AB): ? B:C = " + v.getPreference(b, c));
    }
    
    /**
     * Generate all graphs.
     *
     * @param wpEx the wp ex
     * @param fileName the file name
     * @param outputDir the output dir
     * @param K the k
     * @param overlap the overlap
     * @param edgePos the edge pos
     * @throws Exception the exception
     */
    public static void generateAllGraphs(VotePreference[] wpEx,
            String fileName, String outputDir, double K, boolean overlap,
            Map<String, String> edgePos) throws Exception {

        List<String> cands = getAllCandidates(wpEx);

        int[][] d = computePairwiseDefeats(wpEx);
        int[][] p = getStrongestNext(d);

        toDot(outputDir, fileName, wpEx, null, K, overlap, edgePos);

        println("* strongest paths:");
        for (int from = 0; from < p.length; from++) {
            for (int to = 0; to < p.length; to++) {
                if (from != to) {

                    List<Path> paths = getStrongestPaths(p, from, to);

                    for (int i = 0; i < paths.size(); i++) {
                        println("  * " + paths.get(i).toString(p, cands));
                    }

                    for (int i = 0; i < paths.size(); i++) {
                        String name = fileName + "_" + cands.get(from)
                                + cands.get(to);

                        if (paths.size() > 1) {
                            name += "_" + (i + 1);
                        }

                        toDot(outputDir, name, wpEx, paths.get(i), K, overlap, edgePos);
                    }
                }
            }
        }
    }

    /**
     * The Class SchulzeMethodNode.
     */
    public static class SchulzeMethodNode {
        
        /** The name. */
        public String name;
        
        /** The pos. */
        public String pos;
        
        /** The selected. */
        public Boolean selected;
        
        /** The radius. */
        public Double radius;
        
        /**
         * Instantiates a new schulze method node.
         *
         * @param name the name
         * @param pos the pos
         * @param selected the selected
         * @param radius the radius
         */
        public SchulzeMethodNode(String name, String pos, Boolean selected, Double radius) {
            this(name, pos, selected);
            this.radius = radius;
        }

        /**
         * Instantiates a new schulze method node.
         *
         * @param name the name
         * @param pos the pos
         * @param selected the selected
         */
        public SchulzeMethodNode(String name, String pos, Boolean selected) {
            this.name = name;
            this.pos = pos;
            this.selected = selected;
            this.radius = 0.33;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "name = " + name + " pos = \"" + pos + "\" sel = "
                    + selected;
        }
    }

    /**
     * The Class SchulzeMethodEdge.
     */
    public static class SchulzeMethodEdge {
        
        /** The from. */
        public String from;
        
        /** The to. */
        public String to;
        
        /** The weight. */
        public Integer weight;
        
        /** The selected. */
        public Boolean selected;
        
        /** The pos. */
        public String pos;

        /**
         * Instantiates a new schulze method edge.
         *
         * @param from the from
         * @param to the to
         * @param weight the weight
         * @param selected the selected
         */
        public SchulzeMethodEdge(String from, String to, Integer weight,
                Boolean selected) {
            this(from, to, weight, selected, null);
        }

        /**
         * Instantiates a new schulze method edge.
         *
         * @param from the from
         * @param to the to
         * @param weight the weight
         * @param selected the selected
         * @param pos the pos
         */
        public SchulzeMethodEdge(String from, String to, Integer weight,
                Boolean selected, String pos) {
            this.from = from;
            this.to = to;
            this.weight = weight;
            this.selected = selected;
            this.pos = pos;
        }
    }

    /**
     * The Class VotePreference.
     */
    public static class VotePreference {
        
        /** The weight. */
        public Integer weight;
        
        /** The order. */
        public String order;

        /**
         * Instantiates a new vote preference.
         *
         * @param weight the weight
         * @param order the order
         */
        public VotePreference(Integer weight, String order) {
            this.weight = weight;
            this.order = order;
        }
    }
    
    /**
     * The Class VoteCandidate.
     */
    public static class VoteCandidate implements Comparable<VoteCandidate> {
        
        /** The id. */
        public String id;
        
        /** The name. */
        public String name;
        
        /**
         * Instantiates a new vote candidate.
         *
         * @param id the id
         * @param name the name
         */
        public VoteCandidate(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return id + ": " + name;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(VoteCandidate o) {
            return id.compareTo(o.id);
        }
    }
    
    /**
     * The Class Vote.
     */
    public static class Vote {
        
        /** The preference. */
        public String preference;
        
        /**
         * Instantiates a new vote.
         *
         * @param preference the preference
         */
        public Vote(String preference) {
            this.preference = preference;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return preference;
        }
        
        /**
         * Gets the preference.
         *
         * @return the preference
         */
        public String[] getPreference() {
            return preference.split(",");
        }
        
        /**
         * Gets the preference.
         *
         * @param a the a
         * @param b the b
         * @return the preference
         */
        public int getPreference(VoteCandidate a, VoteCandidate b) {
            String aId = a.id;
            String bId = b.id;
            
            List<String> l = Arrays.asList(preference.split(","));
            
            int aIdx = l.indexOf(aId);
            int bIdx = l.indexOf(bId);
            
            if (aIdx == -1) {
                boolean found = false;
                for (int i = 0; i < l.size() && !found; i++) {
                    List<String> subList = Arrays.asList(l.get(i).split(""));
                
                    if (subList.indexOf(aId) != -1) {
                        found = true;
                        aIdx = i;
                    }
                }
            }
            
            if (bIdx == -1) {
                boolean found = false;
                for (int i = 0; i < l.size() && !found; i++) {
                    List<String> subList = Arrays.asList(l.get(i).split(""));
                
                    if (subList.indexOf(bId) != -1) {
                        found = true;
                        bIdx = i;
                    }
                }
            }
            
            return aIdx > bIdx ? -1 : (aIdx < bIdx ? 1 : 0);
        }
    }
    
    /**
     * To sorted list.
     *
     * @param m the m
     * @return the array list
     */
    public static ArrayList<VoteCandidate> toSortedList(Map<String, VoteCandidate> m) {
        ArrayList<VoteCandidate> l = new ArrayList<VoteCandidate>();
        
        l.addAll(m.values());
        Collections.sort(l);
        
        return l;
    }
    
    /**
     * Checks if is vote candidates.
     *
     * @param is the is
     * @return the map
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Map<String, VoteCandidate> isVoteCandidates(InputStream is) throws IOException {
        Map<String, VoteCandidate> vc = new HashMap<String, VoteCandidate>();
        
        Scanner scanner = new Scanner(is);
        
        try {
            while (scanner.hasNextLine()){
                String l = scanner.nextLine();
                String[] s = l.split(",");
                
                String id = s[0];
                String name = s[1];
                
                vc.put(id, new VoteCandidate(id, name));
            }
        } finally{
            scanner.close();
        }
          
        return vc;
    }
    
    /**
     * Checks if is votes.
     *
     * @param is the is
     * @return the array list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ArrayList<Vote> isVotes(InputStream is) throws IOException {
        ArrayList<Vote> v = new ArrayList<Vote>();
        
        Scanner s = new Scanner(is);
        
        try {
            while (s.hasNextLine()){
                v.add(new Vote(s.nextLine()));
            }
        } finally{
            s.close();
        }

        return v;
    }

    /**
     * Split preference2 list.
     *
     * @param wp the wp
     * @return the list
     */
    public static List<String> splitPreference2List(VotePreference wp) {
        String[] l = wp.order.split(",");

        List<String> res = new ArrayList<String>();

        for (int i = 0; i < l.length; i++) {
            res.add(l[i]);
        }

        return res;
    }

    /**
     * Split preference.
     *
     * @param wp the wp
     * @return the string[]
     */
    public static String[] splitPreference(VotePreference wp) {
        return wp.order.split(",");
    }

    /**
     * Gets the all candidates.
     *
     * @param wp the wp
     * @return the all candidates
     */
    public static List<String> getAllCandidates(VotePreference[] wp) {
        Map<String, String> cand = new HashMap<String, String>();

        for (int i = 0; i < wp.length; i++) {
            String[] cands = splitPreference(wp[i]);

            for (int j = 0; j < cands.length; j++) {
                String c = cands[j];

                if (!cand.containsKey(c)) {
                    cand.put(c, c);
                }
            }
        }

        List<String> cList = new ArrayList<String>();

        for (String string : cand.values()) {
            cList.add(string);
        }
        Collections.sort(cList);

        return cList;
    }

    /**
     * Compute pairwise defeats.
     *
     * @param wp the wp
     * @return the int[][]
     */
    public static int[][] computePairwiseDefeats(VotePreference[] wp) {
        List<String> cands = getAllCandidates(wp);

        // println("candidates: " + cands);

        int candCount = cands.size();

        int[][] m = new int[candCount][candCount];

        for (int i = 0; i < candCount; i++) {
            for (int j = 0; j < candCount; j++) {
                int max = 0;

                if (i != j) {
                    for (int k = 0; k < wp.length; k++) {

                        List<String> p = splitPreference2List(wp[k]);

                        int fstIdx = p.indexOf(cands.get(i));
                        int sndIdx = p.indexOf(cands.get(j));

                        if (fstIdx < sndIdx) {
                            max += wp[k].weight;
                        }
                    }

                    m[i][j] = max;
                }
            }
        }

        return m;
    }

    /**
     * Gets the strongest next.
     *
     * @param d the d
     * @return the strongest next
     */
    public static int[][] getStrongestNext(int[][] d) {
        int c = d.length;
        int[][] p = new int[c][c];

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < c; j++) {
                if (i != j) {
                    if (d[i][j] > d[j][i]) {
                        p[i][j] = d[i][j];
                    } else {
                        p[i][j] = 0;
                    }
                }
            }
        }

        print(p);

        return p;
    }

    /**
     * Gets the strongest paths.
     *
     * @param p the p
     * @param from the from
     * @param to the to
     * @return the strongest paths
     */
    public static List<Path> getStrongestPaths(int[][] p, int from, int to) {
        DFS dfs = new DFS();

        List<Path> paths = dfs.enumerate(p, from, to);
        // print(from + "," + to + ": " + paths.toString());

        int weightMax = Integer.MIN_VALUE;

        // find maximum weight
        for (int i = 0; i < paths.size(); i++) {
            Path link = getWeakestLink(p, paths.get(i));

            int weight = p[link.path.get(0)][link.path.get(1)];

            if (weight > weightMax) {
                weightMax = weight;
            }
        }

        List<Path> strongest = new LinkedList<Path>();

        // add all paths with maximum weight
        for (int i = 0; i < paths.size(); i++) {
            Path link = getWeakestLink(p, paths.get(i));

            int weight = p[link.path.get(0)][link.path.get(1)];

            if (weight == weightMax) {
                strongest.add(paths.get(i));
            }
        }

        return strongest;
    }

    /**
     * Gets the weakest link.
     *
     * @param p the p
     * @param path the path
     * @return the weakest link
     */
    public static Path getWeakestLink(int[][] p, Path path) {
        Path link = new Path();

        int weightMin = Integer.MAX_VALUE;

        int idxFrom = 0, idxTo = 0;

        for (int i = 0; i < path.path.size() - 1; i++) {
            int from = path.path.get(i);
            int to = path.path.get(i + 1);

            if (p[from][to] > 0 && p[from][to] < weightMin) {
                weightMin = p[from][to];
                idxFrom = from;
                idxTo = to;
            }
        }

        link.path.add(idxFrom);
        link.path.add(idxTo);

        return link;
    }

    /**
     * The Class Path.
     */
    public static class Path implements Cloneable {
        
        /** The path. */
        Stack<Integer> path;

        /**
         * Instantiates a new path.
         */
        public Path() {
            path = new Stack<Integer>();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @SuppressWarnings("unchecked")
        @Override
        public Object clone() {
            try {
                Path p = (Path) super.clone();
                p.path = (Stack<Integer>) path.clone();
                return p;
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return path.toString();
        }

        /**
         * Contains.
         *
         * @param i the i
         * @return true, if successful
         */
        public boolean contains(int i) {
            return path.contains(i);
        }

        /**
         * Contains.
         *
         * @param i the i
         * @param j the j
         * @return true, if successful
         */
        public boolean contains(int i, int j) {
            int idxFrom = path.indexOf(i);
            int idxTo = path.indexOf(j);

            return (idxFrom >= 0 && idxTo >= 0 && (idxTo - idxFrom == 1 || idxTo
                    - idxFrom == -1));
        }

        /**
         * To string.
         *
         * @param p the p
         * @param cands the cands
         * @return the string
         */
        public String toString(int[][] p, List<String> cands) {
            String s = "";

            for (int i = 0; i < path.size() - 1; i++) {
                int from = path.get(i);
                int to = path.get(i + 1);

                s += cands.get(from);

                int weight = p[from][to];

                if (p[to][from] > weight) {
                    weight = p[to][from];
                }

                if (i < path.size() - 1) {
                    s += "-(" + weight + ")-";
                }
            }

            s += cands.get(path.get(path.size() - 1));

            return s;
        }
    }

    /**
     * The Class DFS.
     */
    public static class DFS {

        /** The path. */
        private Path path = new Path();
        
        /** The on path. */
        private Set<Integer> onPath = new HashSet<Integer>();
        
        /** The paths. */
        private LinkedList<Path> paths = new LinkedList<Path>();

        /**
         * Enumerate.
         *
         * @param p the p
         * @param from the from
         * @param to the to
         * @return the list
         */
        public List<Path> enumerate(int[][] p, int from, int to) {
            path = new Path();
            paths.clear();

            dfs(p, from, to);

            return paths;
        }

        /**
         * Dfs.
         *
         * @param p the p
         * @param from the from
         * @param to the to
         */
        private void dfs(int[][] p, int from, int to) {
            path.path.push(from);
            onPath.add(from);

            if (from == to) {
                paths.addLast((Path) path.clone());
            } else {
                for (int i = 0; i < p.length; i++) {
                    if (p[from][i] != 0 && !onPath.contains(i)) {
                        dfs(p, i, to);
                    }
                }
            }

            path.path.pop();
            onPath.remove(from);
        }

    }
    
    /**
     * To dot.
     *
     * @param dir the dir
     * @param name the name
     * @param vc the vc
     * @param d the d
     * @param p the p
     * @param strongPath the strong path
     * @param K the k
     * @param overlap the overlap
     * @param radius the radius
     * @param edgePos the edge pos
     * @throws Exception the exception
     */
    public static void toDot(String dir, String name, ArrayList<VoteCandidate> vc,
            int[][] d, int[][]p, Path strongPath, double K, boolean overlap, double radius, Map<String, String> edgePos) throws Exception {
        
        SchulzeMethodNode[] smn = new SchulzeMethodNode[p.length];

        DecimalFormat f = new DecimalFormat("#0.00");

        for (int i = 0; i < vc.size(); i++) {
            double ang = ((double) (i - 0.5) / vc.size()) * Math.PI * 2.0;

            double x = Math.sin(ang) * radius * 2.0 + radius;
            double y = Math.cos(ang) * radius * 2.0 + radius;
            String pos = f.format(x) + "," + f.format(y) + "!";

            boolean selected = false;

            if (strongPath != null && strongPath.contains(i)) {
                selected = true;
            }

            smn[i] = new SchulzeMethodNode(vc.get(i).id, pos, selected);

            // print(smn[i].toString());
        }

        SchulzeMethodEdge[] sme = new SchulzeMethodEdge[p.length
                * (p.length - 1) / 2];
        int idx = 0;

        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < i; j++) {

                boolean selected = false;

                if (strongPath != null && strongPath.contains(i, j)) {
                    selected = true;
                }

                String ep = null;

                if (d[i][j] > d[j][i]) {

                    if (edgePos != null
                            && edgePos.containsKey(vc.get(i).id + vc.get(j).id)) {
                        ep = edgePos.get(vc.get(i).id + vc.get(j).id);
                    }

                    sme[idx++] = new SchulzeMethodEdge(vc.get(i).id, vc
                            .get(j).id, d[i][j], selected, ep);
                } else {
                    if (edgePos != null
                            && edgePos.containsKey(vc.get(j).id + vc.get(i).id)) {
                        ep = edgePos.get(vc.get(j).id + vc.get(i).id);
                    }

                    sme[idx++] = new SchulzeMethodEdge(vc.get(j).id, vc
                            .get(i).id, d[j][i], selected, ep);
                }
            }
        }

        // print("idx = " + idx);
        // print("len = " + sme.length);

        toDot(name, dir + name.replaceAll(" ", "_"), smn, sme, K, overlap);        
    }

    /**
     * To dot.
     *
     * @param dir the dir
     * @param name the name
     * @param wpEx the wp ex
     * @param strongPath the strong path
     * @param K the k
     * @param overlap the overlap
     * @param edgePos the edge pos
     * @throws Exception the exception
     */
    public static void toDot(String dir, String name, VotePreference[] wpEx,
            Path strongPath, double K, boolean overlap, Map<String, String> edgePos)
            throws Exception {
        List<String> cands = getAllCandidates(wpEx);

        int[][] d = computePairwiseDefeats(wpEx);
        int[][] p = computeStrongestPath(d);

        SchulzeMethodNode[] smn = new SchulzeMethodNode[p.length];

        DecimalFormat f = new DecimalFormat("#0.00");

        double radius = 0.85;

        for (int i = 0; i < cands.size(); i++) {
            double ang = ((double) (i - 0.5) / cands.size()) * Math.PI * 2.0;

            double x = Math.sin(ang) * radius * 2.0 + radius;
            double y = Math.cos(ang) * radius * 2.0 + radius;
            String pos = f.format(x) + "," + f.format(y) + "!";

            boolean selected = false;

            if (strongPath != null && strongPath.contains(i)) {
                selected = true;
            }

            smn[i] = new SchulzeMethodNode(cands.get(i), pos, selected);

            // print(smn[i].toString());
        }

        SchulzeMethodEdge[] sme = new SchulzeMethodEdge[p.length
                * (p.length - 1) / 2];
        int idx = 0;

        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < i; j++) {

                boolean selected = false;

                if (strongPath != null && strongPath.contains(i, j)) {
                    selected = true;
                }

                String ep = null;

                if (d[i][j] > d[j][i]) {

                    if (edgePos != null
                            && edgePos.containsKey(cands.get(i) + cands.get(j))) {
                        ep = edgePos.get(cands.get(i) + cands.get(j));
                    }

                    sme[idx++] = new SchulzeMethodEdge(cands.get(i), cands
                            .get(j), d[i][j], selected, ep);
                } else {
                    if (edgePos != null
                            && edgePos.containsKey(cands.get(j) + cands.get(i))) {
                        ep = edgePos.get(cands.get(j) + cands.get(i));
                    }

                    sme[idx++] = new SchulzeMethodEdge(cands.get(j), cands
                            .get(i), d[j][i], selected, ep);
                }
            }
        }

        toDot(name, dir + name, smn, sme, K, overlap);
    }

    /**
     * Compute strongest path.
     *
     * @param d the d
     * @return the int[][]
     */
    public static int[][] computeStrongestPath(int[][] d) {

        int c = d.length;
        int[][] p = new int[c][c];

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < c; j++) {
                if (i != j) {
                    if (d[i][j] > d[j][i]) {
                        p[i][j] = d[i][j];
                    } else {
                        p[i][j] = 0;
                    }
                }
            }
        }

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < c; j++) {
                if (i != j) {
                    for (int k = 0; k < c; k++) {
                        if (i != k && j != k) {
                            p[j][k] = Math.max(p[j][k], (Math.min(p[j][i],
                                    p[i][k])));
                        }
                    }
                }
            }
        }

        return p;
    }

    /**
     * To dot.
     *
     * @param name the name
     * @param fName the f name
     * @param smNodes the sm nodes
     * @param smEdges the sm edges
     * @param K the k
     * @param overlap the overlap
     * @throws Exception the exception
     */
    public static void toDot(String name, String fName,
            SchulzeMethodNode[] smNodes, SchulzeMethodEdge[] smEdges, double K, boolean overlap)
            throws Exception {

        BufferedWriter o = openFile(fName + ".gv");
        print(o, toDot(name, smNodes, smEdges, K, overlap).toString());
        closeFile(o);

        exec("dot", "-Kfdp -Tsvg -o" + fName + ".svg " + fName + ".gv");
        exec("rsvg", "-f png -w 1024 -h 1024 " + fName + ".svg " + fName + ".png");
    }

    /**
     * To dot.
     *
     * @param name the name
     * @param smNodes the sm nodes
     * @param smEdges the sm edges
     * @param K the k
     * @param overlap the overlap
     * @return the sample schulze method graphs
     * @throws Exception the exception
     */
    public static SampleSchulzeMethodGraphs toDot(String name,
            SchulzeMethodNode[] smNodes, SchulzeMethodEdge[] smEdges, double K,
            boolean overlap)
            throws Exception {

        SampleSchulzeMethodGraphs gv = new SampleSchulzeMethodGraphs();

        gv.diGraph.name = name;

        GraphAttr g = gv.diGraph.graphAttr;

        g.bgcolor = "transparent";
        g.K = K;
        g.start = 1;
        g.maxiter = 1000;
        g.overlap = overlap;
        g.splines = true;
        g.fixedsize = true;
        g.regular = true;
        g.outputorder = "edgesfirst";

        NodeAttr n = gv.diGraph.nodeAttr;

        n.style = "filled";
        n.color = "black";
        n.fillcolor = "snow";
        n.shape = "circle";
        n.penwidth = 3;
        n.fontsize = 20;
        n.fontname = "Helvetica-Normal";
        n.labelloc = "c";

        EdgeAttr e = gv.diGraph.edgeAttr;
        e.style = "filled";
        e.color = "black";
        e.fillcolor = "snow";
        e.shape = "box";
        e.penwidth = 3;
        e.fontsize = 20;
        e.arrowsize = 1.3;

        gv.diGraph.nodeAttr.style = "filled";

        List<Node> nl = gv.diGraph.nodes;

        SampleSchulzeMethodGraphs.Node gvnNodeTemplate = new SampleSchulzeMethodGraphs.Node();
        gvnNodeTemplate.fillcolor = "snow";
        gvnNodeTemplate.penwidth = 1;
        gvnNodeTemplate.shape = "ellipse";

        SampleSchulzeMethodGraphs.Node gvnNodeTemplateSelected = new SampleSchulzeMethodGraphs.Node();
        gvnNodeTemplateSelected.fillcolor = "gray85";
        gvnNodeTemplateSelected.color = "red4";
        gvnNodeTemplateSelected.fontcolor = "black";
        gvnNodeTemplateSelected.penwidth = 4;
        gvnNodeTemplateSelected.shape = "ellipse";

        SampleSchulzeMethodGraphs.Node gvnEdgeNodeTemplate = new SampleSchulzeMethodGraphs.Node();
        gvnEdgeNodeTemplate.shape = "box";
        gvnEdgeNodeTemplate.width = 0.5;
        gvnEdgeNodeTemplate.height = 0.4;
        gvnEdgeNodeTemplate.fillcolor = "snow";
        gvnEdgeNodeTemplate.penwidth = 1;
        gvnEdgeNodeTemplate.fontsize = 16;

        SampleSchulzeMethodGraphs.Node gvnEdgeNodeTemplateSelected = (SampleSchulzeMethodGraphs.Node) gvnEdgeNodeTemplate
                .clone();
        gvnEdgeNodeTemplateSelected.color = "red4";
        gvnEdgeNodeTemplateSelected.fontcolor = "black";
        gvnEdgeNodeTemplateSelected.fillcolor = "gray85";
        gvnEdgeNodeTemplateSelected.penwidth = 4;

        for (int i = 0; i < smNodes.length; i++) {
            SchulzeMethodNode smn = smNodes[i];

            SampleSchulzeMethodGraphs.Node gn;

            if (smn.selected) {
                gn = (SampleSchulzeMethodGraphs.Node) gvnNodeTemplateSelected
                        .clone();
            } else {
                gn = (SampleSchulzeMethodGraphs.Node) gvnNodeTemplate.clone();
            }

            gn.name = smn.name;
            gn.pos = smn.pos;
            gn.width = smn.radius * 2.0;
            gn.height = smn.radius * 2.0;

            nl.add(gn);
        }

        // real graphviz edges
        List<Edge> el = gv.diGraph.edges;

        SampleSchulzeMethodGraphs.Edge gveEdgeTemplate1 = new SampleSchulzeMethodGraphs.Edge();
        gveEdgeTemplate1.arrowhead = "none";

        SampleSchulzeMethodGraphs.Edge gveEdgeTemplate2 = new SampleSchulzeMethodGraphs.Edge();

        SampleSchulzeMethodGraphs.Edge gveEdgeTemplate1Selected = (SampleSchulzeMethodGraphs.Edge) gveEdgeTemplate1
                .clone();
        gveEdgeTemplate1Selected.color = "red4";
        gveEdgeTemplate1Selected.penwidth = 4;

        SampleSchulzeMethodGraphs.Edge gveEdgeTemplate2Selected = (SampleSchulzeMethodGraphs.Edge) gveEdgeTemplate1Selected
                .clone();
        gveEdgeTemplate2Selected.arrowhead = null;

        // edge nodes
        for (int i = 0; i < smEdges.length; i++) {
            SchulzeMethodEdge sme = smEdges[i];

            SampleSchulzeMethodGraphs.Node gn;

            if (sme.selected) {
                gn = (SampleSchulzeMethodGraphs.Node) gvnEdgeNodeTemplateSelected
                        .clone();
            } else {
                gn = (SampleSchulzeMethodGraphs.Node) gvnEdgeNodeTemplate
                        .clone();
            }

            gn.name = "E_" + sme.from + sme.to;
            gn.label = Integer.toString(sme.weight);
            gn.pos = sme.pos;

            nl.add(gn);
        }

        for (int i = 0; i < smEdges.length; i++) {
            SchulzeMethodEdge sme = smEdges[i];

            SampleSchulzeMethodGraphs.Edge ge1;

            if (sme.selected) {
                ge1 = (SampleSchulzeMethodGraphs.Edge) gveEdgeTemplate1Selected
                        .clone();
            } else {
                ge1 = (SampleSchulzeMethodGraphs.Edge) gveEdgeTemplate1.clone();
            }

            ge1.from = sme.from;
            ge1.to = "E_" + sme.from + sme.to;

            el.add(ge1);

            SampleSchulzeMethodGraphs.Edge ge2;

            if (sme.selected) {
                ge2 = (SampleSchulzeMethodGraphs.Edge) gveEdgeTemplate2Selected
                        .clone();
            } else {
                ge2 = (SampleSchulzeMethodGraphs.Edge) gveEdgeTemplate2.clone();
            }

            ge2.from = "E_" + sme.from + sme.to;
            ge2.to = sme.to;

            el.add(ge2);
        }

        return gv;
    }

    /**
     * Println.
     *
     * @param out the out
     * @param txt the txt
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void println(Writer out, String txt) throws IOException {
        out.append(txt + "\n");
    }

    /**
     * Prints the.
     *
     * @param out the out
     * @param txt the txt
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void print(Writer out, String txt) throws IOException {
        out.append(txt);
    }

    /**
     * Opens the given <tt>fileName</tt> and returns a.
     *
     * @param fileName the name of the file to open.
     * @return a {@link java.io.BufferedWriter} in case of success.
     * @throws IOException in case of an error.
     * {@link java.io.BufferedWriter}.
     */
    static BufferedWriter openFile(String fileName) throws IOException {
        // print("generating " + fileName + ".. ");
        return new BufferedWriter(new FileWriter(fileName));
    }
    
    /**
     * Closes the given writer stream and prints <tt>ok.</tt> to stdout.
     * 
     * @param o
     *            the stream to close.
     * 
     * @throws IOException
     *             in case of an error.
     */
    static void closeFile(Writer o) throws IOException {
        o.close();
        // print("ok.");
    }

    /**
     * Exec.
     *
     * @param executable the executable
     * @param arguments the arguments
     */
    static void exec(String executable, String arguments) {
        try {
            String line;
            String cmd = executable + " " + arguments;
            println("Running " + cmd + ".. ");
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
            // log.fine("ok.");
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    /**
     * Prints the.
     *
     * @param msg the msg
     */
    static void print(String msg) {
        // log.info(msg);
        System.out.print(msg);
    }

    /**
     * Println.
     *
     * @param msg the msg
     */
    static void println(String msg) {
        // log.info(msg);
        System.out.println(msg);
    }

    /**
     * Println.
     */
    static void println() {
        System.out.println();
    }
}

