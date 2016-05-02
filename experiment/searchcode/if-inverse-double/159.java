/*
 * Copyright (C) 2012, EADS France
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package xowl.gmi.view.diagram;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Laurent WOUTERS
 */
public class Hull {
    private static final int thresholdMerge = 100;
    private static final int thresholdSplit = 625;
    
    private List<Couple> vertices;
    private List<Couple> ports;
    private List<Couple> normals;
    
    public List<Couple> getVertices() { return vertices; }
    public List<Couple> getPorts() { return ports; }
    public List<Couple> getNormals() { return normals; }
    
    public Hull(List<Couple> vertices) {
        buildVertices(vertices);
        buildPorts();
        buildNormals();
    }
    
    private void buildVertices(List<Couple> input) {
        // Split edges long enough
        this.vertices = new ArrayList<>(input.size()*2);
        Couple vp = input.get(input.size()-1);
        for (Couple v : input) {
            Couple vector = new Couple(vp, v);
            if (vector.length2() >= thresholdSplit)
                this.vertices.add(new Couple((v.x+vp.x)/2, (v.y+vp.y)/2));
            this.vertices.add(v);
            vp = v;
        }
    }
    
    private void buildPorts() {
        // Merge close vertices
        this.ports = new ArrayList<>(vertices.size()*2);
        Couple v0 = this.vertices.get(0);
        Couple v1 = this.vertices.get(1);
        ports.add(v0);
        for (Couple v2 : this.vertices) {
            Couple o = new Couple(v2, v0);
            v0 = v1;
            v1 = v2;
            if (o.length2() > thresholdMerge)
                ports.add(v0);
        }
        ports.add(this.vertices.get(this.vertices.size()-1));
    }
    
    private void buildNormals() {
        this.normals = new ArrayList<>();
        Couple b = ports.get(ports.size()-1);
        for (int i=0; i!=ports.size(); i++) {
            Couple a = ports.get(i);
            Couple c = null;
            if (i == ports.size()-1) c = ports.get(0);
            else c = ports.get(i+1);
            normals.add(getNormal(a, b, c));
            b = a;
        }
    }
    
    private Couple getNormal(Couple a, Couple b, Couple c) {
        Couple ab = new Couple(a, b);
        Couple ac = new Couple(a, c);
        if (ab.x == -ac.x && ab.y == -ac.y) {
            // Points are aligned
            Couple bc = new Couple(b, c);
            return bc.orthogonal(1).inverse();
        }
        double tx = ac.x*ab.length() + ab.x*ac.length();
        double ty = ab.y*ac.length() + ac.y*ab.length();
        double xy = Math.signum(ty) * java.lang.Math.sqrt(ty*ty/(tx*tx+ty*ty));
        double xx = Math.signum(tx) * java.lang.Math.sqrt(1-xy*xy);
        Couple ax = new Couple(xx, xy);
        return ax.inverse();
    }
}

