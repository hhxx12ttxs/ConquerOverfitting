package com.google.android.apps.unveil.env;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RegionQuadTree<T>
{
  private static final int LEAF_SPLIT_THRESHOLD = 16;
  private int count;
  private final int height;
  private final RegionQuadTree<T>.Link northEastChild;
  private final RegionQuadTree<T>.Link northWestChild;
  private final RegionQuadTree<T>.Link southEastChild;
  private final RegionQuadTree<T>.Link southWestChild;
  private final int width;
  private final int xOrigin;
  private final int yOrigin;

  public RegionQuadTree(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramInt3 <= 0) || (paramInt4 <= 0))
      throw new IllegalArgumentException("Invalid dimensions: " + paramInt3 + "x" + paramInt4);
    this.xOrigin = paramInt1;
    this.yOrigin = paramInt2;
    this.width = paramInt3;
    this.height = paramInt4;
    this.northWestChild = new Link(Quadrant.NW_QUADRANT, this);
    this.northEastChild = new Link(Quadrant.NE_QUADRANT, this);
    this.southWestChild = new Link(Quadrant.SW_QUADRANT, this);
    this.southEastChild = new Link(Quadrant.SE_QUADRANT, this);
  }

  private void addItem(Item<T> paramItem)
  {
    int i = paramItem.getX();
    int j = paramItem.getY();
    if ((i < this.xOrigin) || (i >= this.xOrigin + this.width) || (j < this.yOrigin) || (j >= this.yOrigin + this.height))
      throw new IllegalArgumentException("Item " + paramItem.toString() + " outside tree bounds: " + toString());
    getLink(i, j).addItem(paramItem);
    this.count = (1 + this.count);
  }

  private void appendItemsInQuadrant(RegionQuadTree<T>.Link paramRegionQuadTree, int paramInt1, int paramInt2, int paramInt3, int paramInt4, List<Item<T>> paramList, List<RegionQuadTree<T>> paramList1)
  {
    List localList = paramRegionQuadTree.getItemsInRegion(paramInt1, paramInt2, paramInt3, paramInt4, paramList1);
    if (localList != null)
      paramList.addAll(localList);
  }

  private int getCenterX()
  {
    return this.xOrigin + this.width / 2;
  }

  private int getCenterY()
  {
    return this.yOrigin + this.height / 2;
  }

  private RegionQuadTree<T>.Link getLink(int paramInt1, int paramInt2)
  {
    if ((paramInt1 < getCenterX()) && (paramInt2 < getCenterY()))
      return this.northWestChild;
    if ((paramInt1 >= getCenterX()) && (paramInt2 < getCenterY()))
      return this.northEastChild;
    if (paramInt1 < getCenterX())
      return this.southWestChild;
    return this.southEastChild;
  }

  public void add(int paramInt1, int paramInt2, T paramT)
  {
    addItem(new Item(paramInt1, paramInt2, paramT));
  }

  public int getCount()
  {
    return this.count;
  }

  public List<Item<T>> getItemsInRegion(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList();
    localArrayList2.add(this);
    if (!localArrayList2.isEmpty())
    {
      int i = -1 + localArrayList2.size();
      RegionQuadTree localRegionQuadTree = (RegionQuadTree)localArrayList2.get(i);
      localArrayList2.remove(i);
      int j;
      label74: int k;
      label87: int m;
      if (paramInt2 < localRegionQuadTree.getCenterY())
      {
        j = 1;
        if (paramInt4 < localRegionQuadTree.getCenterY())
          break label226;
        k = 1;
        if (paramInt1 >= localRegionQuadTree.getCenterX())
          break label232;
        m = 1;
        label99: if (paramInt3 < localRegionQuadTree.getCenterX())
          break label238;
      }
      label226: label232: label238: for (int n = 1; ; n = 0)
      {
        if (j != 0)
        {
          if (m != 0)
            localRegionQuadTree.appendItemsInQuadrant(localRegionQuadTree.northWestChild, paramInt1, paramInt2, paramInt3, paramInt4, localArrayList1, localArrayList2);
          if (n != 0)
            localRegionQuadTree.appendItemsInQuadrant(localRegionQuadTree.northEastChild, paramInt1, paramInt2, paramInt3, paramInt4, localArrayList1, localArrayList2);
        }
        if (k == 0)
          break;
        if (m != 0)
          localRegionQuadTree.appendItemsInQuadrant(localRegionQuadTree.southWestChild, paramInt1, paramInt2, paramInt3, paramInt4, localArrayList1, localArrayList2);
        if (n == 0)
          break;
        localRegionQuadTree.appendItemsInQuadrant(localRegionQuadTree.southEastChild, paramInt1, paramInt2, paramInt3, paramInt4, localArrayList1, localArrayList2);
        break;
        j = 0;
        break label74;
        k = 0;
        break label87;
        m = 0;
        break label99;
      }
    }
    return localArrayList1;
  }

  public String toString()
  {
    Object[] arrayOfObject = new Object[10];
    arrayOfObject[0] = super.toString();
    arrayOfObject[1] = Integer.valueOf(this.xOrigin);
    arrayOfObject[2] = Integer.valueOf(this.yOrigin);
    arrayOfObject[3] = Integer.valueOf(this.width);
    arrayOfObject[4] = Integer.valueOf(this.height);
    arrayOfObject[5] = Integer.valueOf(this.count);
    arrayOfObject[6] = this.northEastChild.toString();
    arrayOfObject[7] = this.northWestChild.toString();
    arrayOfObject[8] = this.southWestChild.toString();
    arrayOfObject[9] = this.southEastChild.toString();
    return String.format("[tree=%s origin=(%d, %d) size=%dx%d count=%d NE=%s NW=%s SW=%s SE=%s]", arrayOfObject);
  }

  public static class Item<T>
  {
    private final T data;
    private final int x;
    private final int y;

    public Item(int paramInt1, int paramInt2, T paramT)
    {
      this.x = paramInt1;
      this.y = paramInt2;
      this.data = paramT;
    }

    public T getData()
    {
      return this.data;
    }

    public int getX()
    {
      return this.x;
    }

    public int getY()
    {
      return this.y;
    }

    public boolean inRegion(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      return (this.x >= paramInt1) && (this.y >= paramInt2) && (this.x <= paramInt3) && (this.y <= paramInt4);
    }

    public String toString()
    {
      Object[] arrayOfObject = new Object[3];
      arrayOfObject[0] = Integer.valueOf(this.x);
      arrayOfObject[1] = Integer.valueOf(this.y);
      arrayOfObject[2] = this.data.toString();
      return String.format("(%d,%d)=\"%s\"", arrayOfObject);
    }
  }

  private class Link
  {
    private RegionQuadTree<T> intermediateNode;
    private ArrayList<RegionQuadTree.Item<T>> leafNodeItems;
    private final RegionQuadTree<T> parent;
    private final RegionQuadTree.Quadrant quadrant;

    public Link(RegionQuadTree<T> arg2)
    {
      Object localObject1;
      this.quadrant = localObject1;
      Object localObject2;
      this.parent = localObject2;
    }

    private void addItem(RegionQuadTree.Item<T> paramItem)
    {
      if (this.intermediateNode != null)
      {
        this.intermediateNode.addItem(paramItem);
        return;
      }
      if (this.leafNodeItems != null)
      {
        if (splitLeaf())
        {
          this.intermediateNode.addItem(paramItem);
          return;
        }
        this.leafNodeItems.add(paramItem);
        return;
      }
      this.leafNodeItems = new ArrayList();
      this.leafNodeItems.add(paramItem);
    }

    private boolean canSplit()
    {
      return (this.leafNodeItems.size() >= 16) && (this.parent.width > 1) && (this.parent.height > 1);
    }

    private int getHeight()
    {
      switch (RegionQuadTree.1.$SwitchMap$com$google$android$apps$unveil$env$RegionQuadTree$Quadrant[this.quadrant.ordinal()])
      {
      default:
        throw new AssertionError("Unknown quadrant: " + this.quadrant.toString());
      case 1:
      case 3:
        return this.parent.height / 2;
      case 2:
      case 4:
      }
      return (1 + this.parent.height) / 2;
    }

    private int getMinX()
    {
      switch (RegionQuadTree.1.$SwitchMap$com$google$android$apps$unveil$env$RegionQuadTree$Quadrant[this.quadrant.ordinal()])
      {
      default:
        throw new AssertionError("Unknown quadrant: " + this.quadrant.toString());
      case 1:
      case 2:
        return this.parent.getCenterX();
      case 3:
      case 4:
      }
      return this.parent.xOrigin;
    }

    private int getMinY()
    {
      switch (RegionQuadTree.1.$SwitchMap$com$google$android$apps$unveil$env$RegionQuadTree$Quadrant[this.quadrant.ordinal()])
      {
      default:
        throw new AssertionError("Unknown quadrant: " + this.quadrant.toString());
      case 1:
      case 3:
        return this.parent.yOrigin;
      case 2:
      case 4:
      }
      return this.parent.getCenterY();
    }

    private int getWidth()
    {
      switch (RegionQuadTree.1.$SwitchMap$com$google$android$apps$unveil$env$RegionQuadTree$Quadrant[this.quadrant.ordinal()])
      {
      default:
        throw new AssertionError("Unknown quadrant: " + this.quadrant.toString());
      case 1:
      case 2:
        return (1 + this.parent.width) / 2;
      case 3:
      case 4:
      }
      return this.parent.width / 2;
    }

    private boolean splitLeaf()
    {
      if (canSplit())
      {
        this.intermediateNode = new RegionQuadTree(getMinX(), getMinY(), getWidth(), getHeight());
        Iterator localIterator = this.leafNodeItems.iterator();
        while (localIterator.hasNext())
        {
          RegionQuadTree.Item localItem = (RegionQuadTree.Item)localIterator.next();
          this.intermediateNode.addItem(localItem);
        }
        this.leafNodeItems = null;
        return true;
      }
      return false;
    }

    List<RegionQuadTree.Item<T>> getItemsInRegion(int paramInt1, int paramInt2, int paramInt3, int paramInt4, List<RegionQuadTree<T>> paramList)
    {
      if (this.leafNodeItems != null)
      {
        localArrayList = new ArrayList();
        Iterator localIterator = this.leafNodeItems.iterator();
        while (localIterator.hasNext())
        {
          RegionQuadTree.Item localItem = (RegionQuadTree.Item)localIterator.next();
          if (localItem.inRegion(paramInt1, paramInt2, paramInt3, paramInt4))
            localArrayList.add(localItem);
        }
      }
      RegionQuadTree localRegionQuadTree = this.intermediateNode;
      ArrayList localArrayList = null;
      if (localRegionQuadTree != null)
        paramList.add(this.intermediateNode);
      return localArrayList;
    }

    public String toString()
    {
      if (this.intermediateNode != null)
        return this.intermediateNode.toString();
      if (this.leafNodeItems != null)
      {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("( ");
        Iterator localIterator = this.leafNodeItems.iterator();
        while (localIterator.hasNext())
        {
          localStringBuilder.append(((RegionQuadTree.Item)localIterator.next()).toString());
          localStringBuilder.append(' ');
        }
        localStringBuilder.append(')');
        return localStringBuilder.toString();
      }
      return "<NIL>";
    }
  }

  public static enum Quadrant
  {
    static
    {
      NE_QUADRANT = new Quadrant("NE_QUADRANT", 2);
      SE_QUADRANT = new Quadrant("SE_QUADRANT", 3);
      Quadrant[] arrayOfQuadrant = new Quadrant[4];
      arrayOfQuadrant[0] = NW_QUADRANT;
      arrayOfQuadrant[1] = SW_QUADRANT;
      arrayOfQuadrant[2] = NE_QUADRANT;
      arrayOfQuadrant[3] = SE_QUADRANT;
    }
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\com.google.android.apps.translate-141\classes_dex2jar.jar
 * Qualified Name:     com.google.android.apps.unveil.env.RegionQuadTree
 * JD-Core Version:    0.6.2
 */
