package uconnocalypse.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class BoxTree<B extends Box> implements Iterable<B> {
    private static final XYComparator xyComparator = new XYComparator();
    
    private final IntervalTree<B> xTree;
    private final IntervalTree<B> yTree;
    
    public BoxTree() {
        xTree = new IntervalTree<>();
        yTree = new IntervalTree<>();
    }
    
    public void insert(B box) {
        xTree.insert(box.getXMin(), box.getXMax(), box);
        yTree.insert(box.getYMin(), box.getYMax(), box);
    }
    
    public void remove(B box) {
        xTree.remove(box.getXMin(), box.getXMax(), box);
        yTree.remove(box.getYMin(), box.getYMax(), box);
    }
    
    public void clear() {
        xTree.clear();
        yTree.clear();
    }
    
    public List<B> intersect(Box box) {
        List<B> xOverlap = xTree.intersect(box.getXMin(), box.getXMax());
        List<B> yOverlap = yTree.intersect(box.getYMin(), box.getYMax());
        Collections.sort(xOverlap, xyComparator);
        Collections.sort(yOverlap, xyComparator);
        return mergeResults(xOverlap, yOverlap);
    }
    
    public List<B> intersect(double x, double y) {
        List<B> xOverlap = xTree.intersect(x);
        List<B> yOverlap = yTree.intersect(y);
        Collections.sort(xOverlap, xyComparator);
        Collections.sort(yOverlap, xyComparator);
        return mergeResults(xOverlap, yOverlap);
    }
    
    @Override
    public Iterator<B> iterator() {
        return xTree.iterator();
    }
    
    private List<B> mergeResults(List<B> a, List<B> b) {
        List<B> matches = new ArrayList<>();
        int xIndex = 0;
        int yIndex = 0;
        while (a.size() > xIndex && b.size() > yIndex) {
            if (a.get(xIndex) == b.get(yIndex)) {
                matches.add(a.get(xIndex));
                xIndex++;
            } else if (a.get(xIndex).getXMin() < b.get(yIndex).getXMin()) {
                xIndex++;
            } else if (a.get(xIndex).getXMin() > b.get(yIndex).getXMin()) {
                yIndex++;
            } else if (a.get(xIndex).getYMin() < b.get(yIndex).getYMin()) {
                xIndex++;
            } else if (a.get(xIndex).getYMin() > b.get(yIndex).getYMin()) {
                yIndex++;
            } else {
                // tricky case here - there can be runs with equal values of xmin
                // and ymin in each list.
                // All such boxes MUST intersect, so just add all of them.
                double xmin = a.get(xIndex).getXMin();
                double ymin = a.get(xIndex).getYMin();
                Set<B> subMatches = new HashSet<>();
                do {
                    subMatches.add(a.get(xIndex));
                    xIndex++;
                } while (a.size() > xIndex && a.get(xIndex).getXMin() == xmin && a.get(xIndex).getYMin() == ymin);
                do {
                    subMatches.add(b.get(yIndex));
                    yIndex++;
                } while (b.size() > yIndex && b.get(yIndex).getXMin() == xmin && b.get(yIndex).getYMin() == ymin);
                for (B box : subMatches) {
                    matches.add(box);
                }
            }
        }
        return matches;
    }
    
    private static final class XYComparator implements Comparator<Box> {
        @Override
        public int compare(Box a, Box b) {
            double residual = a.getXMin() - b.getXMin();
            if (residual == 0)
                residual = a.getYMin() - b.getYMin();
            if (residual == 0)
                return 0;
            else if (residual > 0)
                return 1;
            else
                return -1;
        }
    }
}

