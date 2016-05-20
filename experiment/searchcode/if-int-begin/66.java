package net.moraleboost.junsai.doublearray;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.moraleboost.junsai.util.ByteBufferUtil;
import net.moraleboost.junsai.util.Size;

/**
 * double-array trie???
 * 
 * @author taketa
 *
 */
public class TrieBuilder
{
    private static class Node
    {
        public int code;
        public int depth;
        public int left;
        public int right;
    }
    
    private static class Unit
    {
        public int base;
        public int check;
    }
    
    private int size;
    private int allocSize;
    private Unit[] array;
    private byte[] used;
    private String[] key;
    private int keySize;
    private int[] length;
    private int[] value;
    private int progress;
    private int nextCheckPos;
    private int error;
    
    private void resize(int newSize)
    {
        if (array == null) {
            array = new Unit[newSize];
            for (int i=0; i<newSize; ++i) {
                array[i] = new Unit();
            }
        } else {
            Unit[] newArray = Arrays.copyOf(array, newSize);
            for (int i=array.length; i<newSize; ++i) {
                newArray[i] = new Unit();
            }
            array = newArray;
        }
        
        if (used == null) {
            used = new byte[newSize];
        } else {
            used = Arrays.copyOf(used, newSize);
        }
        
        allocSize = newSize;
    }
    
    private int fetch(Node parent, List<Node> siblings)
    {
        if (error < 0) {
            return 0;
        }
        
        int prev = 0;
        
        for (int i=parent.left; i<parent.right; ++i) {
            int len = (length != null) ? length[i] : key[i].length();
            if (len < parent.depth) {
                continue;
            }
            
            String tmp = key[i];
            int cur = 0;
            if (len != parent.depth) {
                cur = (int)tmp.charAt(parent.depth) + 1;
            }
            
            if (prev > cur) {
                error = -3;
                return 0;
            }
            
            if (cur != prev || siblings.isEmpty()) {
                Node tmpNode = new Node();
                tmpNode.depth = parent.depth + 1;
                tmpNode.code = cur;
                tmpNode.left = i;
                if (!siblings.isEmpty()) {
                    siblings.get(siblings.size()-1).right = i;
                }
                siblings.add(tmpNode);
            }
            
            prev = cur;
        }
        
        if (!siblings.isEmpty()) {
            siblings.get(siblings.size()-1).right = parent.right;
        }
        
        return siblings.size();
    }
    
    private int insert(List<Node> siblings)
    {
        if (error < 0) {
            return 0;
        }
        
        int begin = 0;
        int pos = Math.max(siblings.get(0).code+1, nextCheckPos) - 1;
        int nonzeroNum = 0;
        boolean first = true;
        
        if (allocSize <= pos) {
            resize(pos + 1);
        }
        
        next:
        while (true) {
            ++pos;
            
            if (allocSize <= pos) {
                resize(pos + 1);
            }
            
            if (array[pos].check != 0) {
                ++nonzeroNum;
                continue;
            } else if (first) {
                nextCheckPos = pos;
                first = false;
            }
            
            begin = pos - siblings.get(0).code;
            if (allocSize <= (begin + siblings.get(siblings.size() - 1).code)) {
                resize((int)(allocSize * Math.max(1.05, 1.0 * keySize
                        / progress)));
            }
            
            if (used[begin] != 0) {
                continue;
            }
            
            for (int i=1; i<siblings.size(); ++i) {
                if (array[begin+siblings.get(i).code].check != 0) {
                    continue next;
                }
            }
            
            break;
        }
        
        if (1.0 * nonzeroNum / (pos - nextCheckPos + 1) >= 0.95) {
            nextCheckPos = pos;
        }
        
        used[begin] = 1;
        size = Math.max(size, begin+siblings.get(siblings.size()-1).code + 1);
        
        for (int i=0; i<siblings.size(); ++i) {
            array[begin+siblings.get(i).code].check = begin;
        }
        
        for (int i=0; i<siblings.size(); ++i) {
            ArrayList<Node> newSiblings = new ArrayList<Node>();
            int ret = fetch(siblings.get(i), newSiblings);
            if (ret == 0) {
                array[begin + siblings.get(i).code].base =
                    (value != null) ?
                            (-value[siblings.get(i).left] - 1) :
                                (-siblings.get(i).left - 1);
                if (value != null && (-value[siblings.get(i).left]-1) >= 0) {
                    error = -2;
                    return 0;
                }
                ++progress;
            } else {
                int h = insert(newSiblings);
                array[begin+siblings.get(i).code].base = h;
            }
        }
        
        return begin;
    }
    
    public int build(int keySize, String[] key, int[] length, int[] value)
    {
        if (keySize <= 0 || key == null) {
            return 0;
        }
        
        this.key = key;
        this.length = length;
        this.keySize = keySize;
        this.value = value;
        this.progress = 0;
        
        resize(65536 * 2);
        
        this.array[0].base = 1;
        this.nextCheckPos = 0;
        
        Node rootNode = new Node();
        rootNode.left = 0;
        rootNode.right = keySize;
        rootNode.depth = 0;
        
        ArrayList<Node> siblings = new ArrayList<Node>();
        fetch(rootNode, siblings);
        insert(siblings);
        
        this.size += (1 << 16) + 1;
        if (this.size >= this.allocSize) {
            resize(this.size);
        }
        
        this.used = null;
        
        return this.error;
    }
    
    public ByteBuffer toByteBuffer()
    {
        // Unit1?????8byte(=int???)???
        ByteBuffer b = ByteBufferUtil.allocate(this.size * Size.INT * 2);
        
        // Unit?????
        for (int i=0; i<this.size; ++i) {
            b.putInt(array[i].base);
            b.putInt(array[i].check);
        }
        
        b.rewind();
        return b;
    }
}

