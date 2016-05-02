package mmo;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import mmo.world.Position;
import mmo.world.Spatial;

/**
 * Used for mapping Shapes to terrain regions, getting MapController
 * @author Alan Wang
 *
 * @param <V>
 */
public class SpatialMap<V extends Spatial> {
    // private SortedMap<Float, Set<V>> xMap = Collections.synchronizedSortedMap(new TreeMap<Float, Set<V>>());
    // private SortedMap<Float, Set<V>> yMap = Collections.synchronizedSortedMap(new TreeMap<Float, Set<V>>());
    private SortedMap<Double, Set<V>> xMap = new ConcurrentSkipListMap<Double, Set<V>>();
    private SortedMap<Double, Set<V>> yMap = new ConcurrentSkipListMap<Double, Set<V>>();
    private HashMap<V, Area> addPositionMap = new HashMap<V, Area>();
    
    ArrayList<V> heightsRanking = new ArrayList<V>();
    ArrayList<V> widthsRanking = new ArrayList<V>();
    private boolean isSorted = true;
    
    private static final Comparator<Spatial> heightComparator = new Comparator<Spatial>(){
            @Override
            public int compare(Spatial arg0, Spatial arg1) {
                return ((Double) arg0.getArea().getBounds2D().getHeight()).compareTo((Double) arg1.getArea().getBounds2D().getHeight());
            }
        };
    private static final Comparator<Spatial> widthComparator = new Comparator<Spatial>(){
            @Override
            public int compare(Spatial arg0, Spatial arg1) {
                return ((Double) arg0.getArea().getBounds2D().getWidth()).compareTo((Double) arg1.getArea().getBounds2D().getWidth());
            }
        };
    
    public void add(V value) {
        Area area = (Area) value.getArea().clone();
        addPositionMap.put(value, area);
        Rectangle2D rect = area.getBounds2D();
        Double minX = rect.getMinX();
        Double maxX = rect.getMaxX();
        Double minY = rect.getMinY();
        Double maxY = rect.getMaxY();
        
        // Store min and max X
        if(!xMap.containsKey(minX)) {
            HashSet<V> newSet = new HashSet<V>();
            newSet.add(value);
            xMap.put(minX, newSet);
        }
        else {
            xMap.get(minX).add(value);
        }
        if(!xMap.containsKey(maxX)) {
            HashSet<V> newSet = new HashSet<V>();
            newSet.add(value);
            xMap.put(maxX, newSet);
        }
        else {
            xMap.get(maxX).add(value);
        }
        
        // Store min and max Y        
        if(!yMap.containsKey(minY)) {
            HashSet<V> newSet = new HashSet<V>();
            newSet.add(value);
            yMap.put(minY, newSet);
        }
        else {
            yMap.get(minY).add(value);
        }
        if(!yMap.containsKey(maxY)) {
            HashSet<V> newSet = new HashSet<V>();
            newSet.add(value);
            yMap.put(maxY, newSet);
        }
        else {
            yMap.get(maxY).add(value);
        }
        
        heightsRanking.add(value);
        widthsRanking.add(value);
        isSorted = false;
    }
    
    public boolean remove(V value) {
        Rectangle2D rect = addPositionMap.remove(value).getBounds2D();
        Double minX = rect.getMinX();
        Double maxX = rect.getMaxX();
        Double minY = rect.getMinY();
        Double maxY = rect.getMaxY();
        
        if(!(heightsRanking.remove(value) && widthsRanking.remove(value))) {
            return false;
        }
        
        if(xMap.get(minX).size() == 1) {
            xMap.remove(minX);
        }
        else {
            xMap.get(minX).remove(value);
        }
        if(xMap.get(maxX).size() == 1) {
            xMap.remove(maxX);
        }
        else {
            xMap.get(maxX).remove(value);
        }
        
        if(yMap.get(minY).size() == 1) {
            yMap.remove(minY);
        }
        else {
            yMap.get(minY).remove(value);
        }
        if(yMap.get(maxY).size() == 1) {
            yMap.remove(maxY);
        }
        else {
            yMap.get(maxY).remove(value);
        }
        isSorted = false;
        
        return true;
    }
    
    public Collection<V> get(final Area area) {
        Rectangle2D rect = area.getBounds2D();
        Spatial dummy = new Spatial() { 
                @Override
                public Area getArea() {
                    return area;
                }
            };
        
        if(!isSorted) {
            Collections.sort(heightsRanking, heightComparator);
            Collections.sort(widthsRanking, widthComparator);
            isSorted = true;
        }
        
        Set<V> xSet = new HashSet<V>();
        int widthIndex = Collections.binarySearch(widthsRanking, dummy, widthComparator);
        
        if(widthIndex < 0) {
            widthIndex = -(widthIndex + 1);
        }
        for(Collection<V> subSet : xMap.subMap(rect.getMinX(), rect.getMaxX()).values()) {
            xSet.addAll(subSet);
        }
        for(V value : widthsRanking.subList(widthIndex, widthsRanking.size())) {
            Double vMinX = value.getArea().getBounds2D().getMinX();
            Double vMaxX = value.getArea().getBounds2D().getMaxX();
            if(vMinX <= rect.getMinX() && vMaxX >= rect.getMaxX()) {
                xSet.add(value);
            }
        }
        
        Set<V> ySet = new HashSet<V>();
        int heightIndex = Collections.binarySearch(heightsRanking, dummy, heightComparator);
        
        if(heightIndex < 0) {
            heightIndex = -(heightIndex + 1);
        }
        for(Collection<V> subSet : yMap.subMap(rect.getMinY(), rect.getMaxY()).values()) {
            ySet.addAll(subSet);
        }
        for(V value : heightsRanking.subList(heightIndex, heightsRanking.size())) {
            Double vMinY = value.getArea().getBounds2D().getMinY();
            Double vMaxY = value.getArea().getBounds2D().getMaxY();
            if(vMinY <= rect.getMinY() && vMaxY >= rect.getMaxY()) {
                ySet.add(value);
            }
        }
        
        xSet.retainAll(ySet);
        return xSet;
    }
    
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        return (Collection<V>) heightsRanking.clone();
    }
    
    public boolean move(V value, Position newPos) {
        Rectangle2D rect = addPositionMap.remove(value).getBounds2D();
        Double minX = rect.getMinX();
        Double maxX = rect.getMaxX();
        Double minY = rect.getMinY();
        Double maxY = rect.getMaxY();
        
        if(xMap.get(minX).size() == 1) {
            xMap.remove(minX);
        }
        else {
            xMap.get(minX).remove(value);
        }
        if(xMap.get(maxX).size() == 1) {
            xMap.remove(maxX);
        }
        else {
            xMap.get(maxX).remove(value);
        }
        
        if(yMap.get(minY).size() == 1) {
            yMap.remove(minY);
        }
        else {
            yMap.get(minY).remove(value);
        }
        if(yMap.get(maxY).size() == 1) {
            yMap.remove(maxY);
        }
        else {
            yMap.get(maxY).remove(value);
        }
        
        Area newArea = (Area) value.getArea().clone();
        addPositionMap.put(value, newArea);
        rect = newArea.getBounds2D();
        minX = rect.getMinX();
        maxX = rect.getMaxX();
        minY = rect.getMinY();
        maxY = rect.getMaxY();
        if(!xMap.containsKey(minX)) {
            HashSet<V> newSet = new HashSet<V>();
            newSet.add(value);
            xMap.put(minX, newSet);
        }
        else {
            xMap.get(minX).add(value);
        }
        if(!xMap.containsKey(maxX)) {
            HashSet<V> newSet = new HashSet<V>();
            newSet.add(value);
            xMap.put(maxX, newSet);
        }
        else {
            xMap.get(maxX).add(value);
        }
                
        if(!yMap.containsKey(minY)) {
            HashSet<V> newSet = new HashSet<V>();
            newSet.add(value);
            yMap.put(minY, newSet);
        }
        else {
            yMap.get(minY).add(value);
        }
        if(!yMap.containsKey(maxY)) {
            HashSet<V> newSet = new HashSet<V>();
            newSet.add(value);
            yMap.put(maxY, newSet);
        }
        else {
            yMap.get(maxY).add(value);
        }
        
        return true;
    }
}

