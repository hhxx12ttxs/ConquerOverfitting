/* Licence 
 *  
 * This file is part of ccMesh. 
 *  
 * ccMesh is licensed under the Creative Commons 
 * Attribution-NonCommercial-ShareAlike 3.0 Unported License. To view a copy of 
 * this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ or send 
 * a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, 
 * California, 94041, USA. */ 
 
package net.copsey.ccmesh.primitive; 
 
public class Edge extends Line { 
 
    private Node[] nodes = new Node[3]; 
    private double ratio = 0.5; 
    private Faces dependencies = new Faces(); 
 
    public Edge(Node n1, Node n2) { 
        super(n1.getCoord(), n2.getCoord()); 
        this.nodes[0] = n1; 
        this.nodes[0].addDependency(this); 
        this.nodes[1] = n2; 
        this.nodes[1].addDependency(this); 
        update(); 
    } 
 
    public Node getNode(int id) { 
        return this.nodes[id-1]; 
    } 
 
    public void setNode(Node n, int id) { 
        this.nodes[id-1].removeDependency(this); 
        this.nodes[id-1] = n; 
        this.nodes[id-1].addDependency(this); 
        update(); 
    } 
 
    public void split() { 
        this.nodes[2] = new Node(new Coord(0.0, 0.0, 0.0)); 
        updateNode3(); 
    } 
 
    private void updateNode3() { 
        if (this.nodes != null && this.nodes[2] != null) { 
            double x = this.nodes[0].getCoord().getX() + (this.vector.getX() * this.ratio); 
            double y = this.nodes[0].getCoord().getY() + (this.vector.getY() * this.ratio); 
            double z = this.nodes[0].getCoord().getZ() + (this.vector.getZ() * this.ratio); 
            this.nodes[2].getCoord().setX(x); 
            this.nodes[2].getCoord().setY(y); 
            this.nodes[2].getCoord().setZ(z); 
        } 
    } 
 
    public double getRatio() { 
        return this.ratio; 
    } 
 
    public void setRatio(double ratio) { 
        this.ratio = ratio; 
        update(); 
    } 
 
    public void update() { 
        super.update(); 
        updateNode3(); 
        updateDependencies(); 
    } 
 
    public void addDependency(Face f) { 
        dependencies.add(f); 
    } 
 
    private void updateDependencies() { 
        if (dependencies != null) { 
            for (Face f: dependencies) { 
                f.update(); 
            } 
        } 
    } 
 
    public void removeDependency(Face f) { 
        dependencies.remove(f); 
    } 
 
    public void debug() { 
        System.out.println("Line: " + this.hashCode() + " ->"); 
        for(Node n: nodes) 
        { 
            if (n != null) { 
                System.out.println("  Node: " + n.hashCode()); 
            } 
        } 
    } 
 
}
