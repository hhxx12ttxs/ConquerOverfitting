package component;

import java.util.LinkedList;
import java.awt.geom.Rectangle2D.Double;


/**
 * A node of the QuadTree
 *
 * @author Edmund Qiu
 * @version March 10, 2014
 */

public class QuadTreeNode implements Rectangular
{

    public final int UL = 0;
    public final int UR = 1;
    public final int BL = 2;
    public final int BR = 3;
    public final int LEAF_SIZE_LIMIT = 5;

    /** Dimensions of the boundaries of this node in space */
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * Child nodes
     */
    private QuadTreeNode[] children;

    /**
     * When QuadTreeNode is a leaf, stores the objects which
     * fall comfortably in one rectangle.
     * When QuadTreeNode is a stem, stores the objects which
     * overlap between boundaries within this node.
     */
    private LinkedList<Rectangular> contents;

    /** How far is this node from the root? Not used in this demo */
    private int level;

    /** Whether this node is a leaf of the graph */
    boolean isLeaf;

    /**
     * For the root of the tree
     */
    public QuadTreeNode(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        contents = new LinkedList<>();
        children = null;
        isLeaf = true;
        level = 0;
    }

    /**
     * For children nodes in the tree
     */
    public QuadTreeNode(int x, int y, int width, int height, int level)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        contents = new LinkedList<>();
        children = null;
        isLeaf = true;
        this.level = level;
    }

    /**
     * Creates children nodes for this QuadTreeNode
     */
    public void split()
    {
        int hfWidth = width/2;
        int hfHeight = height/2;
        children = new QuadTreeNode[4];
        children[UL] = new QuadTreeNode(x, y, hfWidth, hfHeight, level + 1);
        children[UR] = new QuadTreeNode(x + hfWidth, y, hfWidth, hfHeight,
                level + 1);
        children[BL] = new QuadTreeNode(x, y + hfHeight, hfWidth, hfHeight,
                level + 1);
        children[BR] = new QuadTreeNode(x + hfWidth, y + hfHeight, hfWidth,
                hfHeight, level + 1);
        isLeaf = false;
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }

    /**
     * Recursively queries the size of this node and its children
     */
    public int getSize()
    {
        int thisNodeSize = contents.size();
        if (isLeaf)
        {
            return thisNodeSize;
        }
        else
        {
            int childSize = 0;
            for (QuadTreeNode n : children) childSize += n.getSize();
            return thisNodeSize + childSize;
        }
    }

    /**
     * Returns all of the objects in this node
     */
    public LinkedList<Rectangular> getAllObjects()
    {
        LinkedList<Rectangular> returns = new LinkedList<>();

        //Add everything in the list
        returns.addAll(contents);

        //If it has child nodes, add their contents too
        if (!isLeaf)
        {
            for (QuadTreeNode n : children)
            {
                returns.addAll(n.getAllObjects());
            }
        }

        return returns;
    }

    /**
     * Deletes a specified Rectangular
     */
    public void remove(Rectangular object)
    {
        if (isLeaf())
        {
            contents.remove(object);
        }
        else
        {
            //If this is one of those overlapping things, remove it
            QuadTreeNode fit = fitsInWhichNode(object);
            if (fit == null)
            {
                contents.remove(object);
            }
            else
            {
                fit.remove(object);
            }

            //Have we fallen under the size limit? If so, merge children
            if (this.getSize() < LEAF_SIZE_LIMIT)
            {
                LinkedList<Rectangular> allObj = getAllObjects();
                contents = allObj;
                children = null;
                isLeaf = true;
            }

        }

    }

    /**
     * Adds a new rectangular object to this QuadTree
     */
    public void add(Rectangular object)
    {
        if (isLeaf())
        {
            //Does adding an object exceed the limit?
            if (this.getSize() + 1 == LEAF_SIZE_LIMIT)
            {
                //This leaf is now an internal node
                split();

                //distribute each object into the child nodes
                contents.add(object);
                LinkedList<Rectangular> newContents = new LinkedList<>();
                for (Rectangular r : contents)
                {
                    QuadTreeNode fit = fitsInWhichNode(r);
                    if (fit == null)
                    {
                        newContents.add(r);
                    }
                    else
                    {
                        fit.add(r);
                    }
                }

                //Make "contents" point to "newContents" now.
                contents = newContents;

            }
            else
            {
                //If it doesn't, simply add to this node's list
                contents.add(object);
            }

        }
        else
        {
            //If this node is not a leaf, we either add it to the child
            //node it belongs in, or if it doesn't fit cleanly, we add
            //it to this list
            QuadTreeNode fit = fitsInWhichNode(object);
            if (fit == null)
            {
                contents.add(object);
            }
            else
            {
                fit.add(object);
            }

        }
    }

    /**
     * Here's where the usefulness comes in. Given a Rectangular object,
     * combs through the tree to see where it belongs. Based on this, derives
     * a list of Rectangular objects which are close enough to collide with
     * our object
     */
    public LinkedList<Rectangular> getCollisionCandidates(Rectangular object)
    {
        Double rect = new Double(object.getX(), object.getY(),
                object.getWidth(), object.getHeight());
        LinkedList<Rectangular> empty = new LinkedList<>();
        getCandidatesInRegion(empty, rect);
        return empty;
    }

    /**
     * Similar to getCollisionCandidates, but based on any designated region
     * Fills the given LinkedList<Rectangular> with the candidates
     */
    public void getCandidatesInRegion(LinkedList<Rectangular> empty, Double rect)
    {
        if (isLeaf())
        {
            empty.addAll(contents);
            return;
        }
        else
        {
            //Query each child to see if it intersects with this region
            for (QuadTreeNode n : children)
            {
                if (QuadTreeNode.rectIntersects(n, rect))
                {
                    n.getCandidatesInRegion(empty, rect);
                }
            }
            return;
        }
    }

    /**
     * Returns every QuadTreeNode, including this one.
     * Useful for a visual representation of how this works
     */
    public LinkedList<QuadTreeNode> getAllNodes()
    {
        LinkedList<QuadTreeNode> allNodes = new LinkedList<>();
        allNodes.add(this);
        if (isLeaf())
        {
            return allNodes;
        }
        else
        {
            for (QuadTreeNode n : children)
            {
                allNodes.addAll(n.getAllNodes());
            }
            return allNodes;
        }
    }

    /**
     * If the object Rectangular fits perfectly within one of the
     * children, then returns the child node it fits in. Else, it returns
     * null.
     */
    private QuadTreeNode fitsInWhichNode(Rectangular object)
    {
        for (QuadTreeNode n : children)
        {
            if (QuadTreeNode.rectContains(n, object)) return n;
        }
        return null;
    }

    //Getters for the boundaries of this QuadTreeNode
    public double getWidth() { return width;}
    public double getHeight() {return height;}
    public double getX() { return x; }
    public double getY() { return y; }

    /**
     * Utility method. Returns true if all of r2 falls within r1
     * Positive widths and heights, please.
     */
    public static boolean rectContains(Rectangular r1, Rectangular r2)
    {
        double r1x = r1.getX();
        double r2x = r2.getX();

        if (!((r1x <= r2x) &&
              (r2x + r2.getWidth() <= r1x + r1.getWidth()))) return false;

        double r1y = r1.getY();
        double r2y = r2.getY();

        return ((r1y <= r2y) &&
                (r2y + r2.getHeight() <= r1y + r1.getHeight()));
    }

    /**
     * Utility method. Returns true if all of r2 falls within r1
     * Positive widths and heights, please.
     */
    public static boolean rectContains(Rectangular r1, Double r2)
    {
        Double rect1 = new Double(r1.getX(), r1.getY(),
                r1.getWidth(), r1.getHeight());
        return rect1.contains(r2);
    }

    /**
     * Utility method. Returns true r1 intersects r2
     */
    public static boolean rectIntersects(Rectangular r1, Rectangular r2)
    {
        Double rect1 = new Double(r1.getX(), r1.getY(),
                r1.getWidth(), r1.getHeight());
        Double rect2 = new Double(r2.getX(), r2.getY(),
                r2.getWidth(), r2.getHeight());
        return rect1.intersects(rect2);
    }

    /**
     * Utility method. Returns true r1 intersects r2
     */
    public static boolean rectIntersects(Rectangular r1, Double r2)
    {
        Double rect1 = new Double(r1.getX(), r1.getY(),
                r1.getWidth(), r1.getHeight());
        return rect1.intersects(r2);
    }

}
