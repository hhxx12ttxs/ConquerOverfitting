/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.common.collection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import com.orientechnologies.common.comparator.ODefaultComparator;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.common.profiler.OProfiler;

/**
 * Base abstract class of MVRB-Tree algorithm.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 * @param <K>
 *          Key type
 * @param <V>
 *          Value type
 */
@SuppressWarnings({ "unchecked", "serial" })
public abstract class OMVRBTree<K, V> extends AbstractMap<K, V> implements ONavigableMap<K, V>, Cloneable, java.io.Serializable {
  private static final OAlwaysLessKey      ALWAYS_LESS_KEY     = new OAlwaysLessKey();
  private static final OAlwaysGreaterKey   ALWAYS_GREATER_KEY  = new OAlwaysGreaterKey();

  boolean                                  pageItemFound       = false;
  protected int                            pageItemComparator  = 0;
  protected int                            pageIndex           = -1;

  protected float                          pageLoadFactor      = 0.7f;

  /**
   * The comparator used to maintain order in this tree map, or null if it uses the natural ordering of its keys.
   * 
   * @serial
   */
  protected final Comparator<? super K>    comparator;
  protected transient OMVRBTreeEntry<K, V> root                = null;

  /**
   * The number of structural modifications to the tree.
   */
  transient int                            modCount            = 0;
  protected transient boolean              runtimeCheckEnabled = false;
  protected transient boolean              debug               = false;

  protected Object                         lastSearchKey;
  protected OMVRBTreeEntry<K, V>           lastSearchNode;
  protected boolean                        lastSearchFound     = false;
  protected int                            lastSearchIndex     = -1;
  protected int                            keySize             = 1;

  /**
   * Indicates search behavior in case of {@link OCompositeKey} keys that have less amount of internal keys are used, whether lowest
   * or highest partially matched key should be used. Such keys is allowed to use only in
   * 
   * @link OMVRBTree#subMap(K, boolean, K, boolean)}, {@link OMVRBTree#tailMap(Object, boolean)} and
   *       {@link OMVRBTree#headMap(Object, boolean)} .
   */
  public static enum PartialSearchMode {
    /**
     * Any partially matched key will be used as search result.
     */
    NONE,
    /**
     * The biggest partially matched key will be used as search result.
     */
    HIGHEST_BOUNDARY,

    /**
     * The smallest partially matched key will be used as search result.
     */
    LOWEST_BOUNDARY
  }

  /**
   * Constructs a new, empty tree map, using the natural ordering of its keys. All keys inserted into the map must implement the
   * {@link Comparable} interface. Furthermore, all such keys must be <i>mutually comparable</i>: <tt>k1.compareTo(k2)</tt> must not
   * throw a <tt>ClassCastException</tt> for any keys <tt>k1</tt> and <tt>k2</tt> in the map. If the user attempts to put a key into
   * the map that violates this constraint (for example, the user attempts to put a string key into a map whose keys are integers),
   * the <tt>put(Object key, Object value)</tt> call will throw a <tt>ClassCastException</tt>.
   */
  public OMVRBTree() {
    this(1);
  }

  public OMVRBTree(int keySize) {
    comparator = ODefaultComparator.INSTANCE;

    init();
    this.keySize = keySize;
  }

  /**
   * Constructs a new, empty tree map, ordered according to the given comparator. All keys inserted into the map must be <i>mutually
   * comparable</i> by the given comparator: <tt>comparator.compare(k1,
   * k2)</tt> must not throw a <tt>ClassCastException</tt> for any keys <tt>k1</tt> and <tt>k2</tt> in the map. If the user attempts
   * to put a key into the map that violates this constraint, the <tt>put(Object
   * key, Object value)</tt> call will throw a <tt>ClassCastException</tt>.
   * 
   * @param iComparator
   *          the comparator that will be used to order this map. If <tt>null</tt>, the {@linkplain Comparable natural ordering} of
   *          the keys will be used.
   */
  public OMVRBTree(final Comparator<? super K> iComparator) {
    init();
    this.comparator = iComparator;
  }

  /**
   * Constructs a new tree map containing the same mappings as the given map, ordered according to the <i>natural ordering</i> of
   * its keys. All keys inserted into the new map must implement the {@link Comparable} interface. Furthermore, all such keys must
   * be <i>mutually comparable</i>: <tt>k1.compareTo(k2)</tt> must not throw a <tt>ClassCastException</tt> for any keys <tt>k1</tt>
   * and <tt>k2</tt> in the map. This method runs in n*log(n) time.
   * 
   * @param m
   *          the map whose mappings are to be placed in this map
   * @throws ClassCastException
   *           if the keys in m are not {@link Comparable}, or are not mutually comparable
   * @throws NullPointerException
   *           if the specified map is null
   */
  public OMVRBTree(final Map<? extends K, ? extends V> m) {
    comparator = ODefaultComparator.INSTANCE;

    init();
    putAll(m);
  }

  /**
   * Constructs a new tree map containing the same mappings and using the same ordering as the specified sorted map. This method
   * runs in linear time.
   * 
   * @param m
   *          the sorted map whose mappings are to be placed in this map, and whose comparator is to be used to sort this map
   * @throws NullPointerException
   *           if the specified map is null
   */
  public OMVRBTree(final SortedMap<K, ? extends V> m) {
    init();
    comparator = m.comparator();
    try {
      buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }
  }

  /**
   * Create a new entry with the first key/value to handle.
   */
  protected abstract OMVRBTreeEntry<K, V> createEntry(final K key, final V value);

  /**
   * Create a new node with the same parent of the node is splitting.
   */
  protected abstract OMVRBTreeEntry<K, V> createEntry(final OMVRBTreeEntry<K, V> parent);

  public int getNodes() {
    int counter = -1;

    OMVRBTreeEntry<K, V> entry = getFirstEntry();
    while (entry != null) {
      entry = successor(entry);
      counter++;
    }

    return counter;
  }

  protected abstract void setSize(int iSize);

  public abstract int getDefaultPageSize();

  /**
   * Returns <tt>true</tt> if this map contains a mapping for the specified key.
   * 
   * @param key
   *          key whose presence in this map is to be tested
   * @return <tt>true</tt> if this map contains a mapping for the specified key
   * @throws ClassCastException
   *           if the specified key cannot be compared with the keys currently in the map
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   */
  @Override
  public boolean containsKey(final Object key) {
    return getEntry(key, PartialSearchMode.NONE) != null;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the specified value. More formally, returns <tt>true</tt> if and
   * only if this map contains at least one mapping to a value <tt>v</tt> such that
   * <tt>(value==null ? v==null : value.equals(v))</tt>. This operation will probably require time linear in the map size for most
   * implementations.
   * 
   * @param value
   *          value whose presence in this map is to be tested
   * @return <tt>true</tt> if a mapping to <tt>value</tt> exists; <tt>false</tt> otherwise
   * @since 1.2
   */
  @Override
  public boolean containsValue(final Object value) {
    for (OMVRBTreeEntry<K, V> e = getFirstEntry(); e != null; e = next(e))
      if (valEquals(value, e.getValue()))
        return true;
    return false;
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
   * 
   * <p>
   * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such that {@code key} compares equal to
   * {@code k} according to the map's ordering, then this method returns {@code v}; otherwise it returns {@code null}. (There can be
   * at most one such mapping.)
   * 
   * <p>
   * A return value of {@code null} does not <i>necessarily</i> indicate that the map contains no mapping for the key; it's also
   * possible that the map explicitly maps the key to {@code null}. The {@link #containsKey containsKey} operation may be used to
   * distinguish these two cases.
   * 
   * @throws ClassCastException
   *           if the specified key cannot be compared with the keys currently in the map
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   */
  @Override
  public V get(final Object key) {
    if (size() == 0)
      return null;

    OMVRBTreeEntry<K, V> entry = null;

    final long timer = OProfiler.getInstance().startChrono();

    try {
      // TRY TO GET LATEST SEARCH
      final OMVRBTreeEntry<K, V> node = getLastSearchNodeForSameKey(key);
      if (node != null) {
        // SAME SEARCH OF PREVIOUS ONE: REUSE LAST RESULT?
        if (lastSearchFound)
          // REUSE LAST RESULT, OTHERWISE THE KEY NOT EXISTS
          return node.getValue(lastSearchIndex);
      } else
        // SEARCH THE ITEM
        entry = getEntry(key, PartialSearchMode.NONE);

      return entry == null ? null : entry.getValue();

    } finally {
      OProfiler.getInstance().stopChrono("OMVRBTree.get", timer);
    }
  }

  public Comparator<? super K> comparator() {
    return comparator;
  }

  /**
   * @throws NoSuchElementException
   *           {@inheritDoc}
   */
  public K firstKey() {
    return key(getFirstEntry());
  }

  /**
   * @throws NoSuchElementException
   *           {@inheritDoc}
   */
  public K lastKey() {
    return key(getLastEntry());
  }

  /**
   * Copies all of the mappings from the specified map to this map. These mappings replace any mappings that this map had for any of
   * the keys currently in the specified map.
   * 
   * @param map
   *          mappings to be stored in this map
   * @throws ClassCastException
   *           if the class of a key or value in the specified map prevents it from being stored in this map
   * @throws NullPointerException
   *           if the specified map is null or the specified map contains a null key and this map does not permit null keys
   */
  @Override
  public void putAll(final Map<? extends K, ? extends V> map) {
    int mapSize = map.size();
    if (size() == 0 && mapSize != 0 && map instanceof SortedMap) {
      Comparator<?> c = ((SortedMap<? extends K, ? extends V>) map).comparator();
      if (c == comparator || (c != null && c.equals(comparator))) {
        ++modCount;
        try {
          buildFromSorted(mapSize, map.entrySet().iterator(), null, null);
        } catch (java.io.IOException cannotHappen) {
        } catch (ClassNotFoundException cannotHappen) {
        }
        return;
      }
    }
    super.putAll(map);
  }

  /**
   * Returns this map's entry for the given key, or <tt>null</tt> if the map does not contain an entry for the key.
   * 
   * In case of {@link OCompositeKey} keys you can specify which key can be used: lowest, highest, any.
   * 
   * @param key
   *          Key to search.
   * @param partialSearchMode
   *          Which key can be used in case of {@link OCompositeKey} key is passed in.
   * 
   * @return this map's entry for the given key, or <tt>null</tt> if the map does not contain an entry for the key
   * @throws ClassCastException
   *           if the specified key cannot be compared with the keys currently in the map
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   */
  public final OMVRBTreeEntry<K, V> getEntry(final Object key, final PartialSearchMode partialSearchMode) {
    return getEntry(key, false, partialSearchMode);
  }

  final OMVRBTreeEntry<K, V> getEntry(final Object key, final boolean iGetContainer, final PartialSearchMode partialSearchMode) {
    if (key == null)
      return setLastSearchNode(null, null);

    pageItemFound = false;

    if (size() == 0) {
      pageIndex = 0;
      return iGetContainer ? root : null;
    }

    final K k;

    if (keySize == 1)
      k = (K) key;
    else if (((OCompositeKey) key).getKeys().size() == keySize)
      k = (K) key;
    else if (partialSearchMode.equals(PartialSearchMode.NONE))
      k = (K) key;
    else {
      final OCompositeKey fullKey = new OCompositeKey((Comparable<? super K>) key);
      int itemsToAdd = keySize - fullKey.getKeys().size();

      final Comparable<?> keyItem;
      if (partialSearchMode.equals(PartialSearchMode.HIGHEST_BOUNDARY))
        keyItem = ALWAYS_GREATER_KEY;
      else
        keyItem = ALWAYS_LESS_KEY;

      for (int i = 0; i < itemsToAdd; i++)
        fullKey.addKey(keyItem);

      k = (K) fullKey;
    }

    OMVRBTreeEntry<K, V> p = getBestEntryPoint(k);

    checkTreeStructure(p);

    if (p == null)
      return setLastSearchNode(key, null);

    OMVRBTreeEntry<K, V> lastNode = p;
    OMVRBTreeEntry<K, V> prevNode = null;
    OMVRBTreeEntry<K, V> tmpNode;
    int beginKey = -1;
    int steps = -1;

    try {
      while (p != null && p.getSize() > 0) {
        searchNodeCallback();
        steps++;

        lastNode = p;

        beginKey = compare(k, p.getFirstKey());

        if (beginKey == 0) {
          // EXACT MATCH, YOU'RE VERY LUCKY: RETURN THE FIRST KEY WITHOUT SEARCH INSIDE THE NODE
          pageIndex = 0;
          pageItemFound = true;
          pageItemComparator = 0;

          return setLastSearchNode(key, p);
        }

        pageItemComparator = compare(k, p.getLastKey());

        if (beginKey < 0) {
          if (pageItemComparator < 0) {
            tmpNode = predecessor(p);
            if (tmpNode != null && tmpNode != prevNode) {
              // MINOR THAN THE CURRENT: GET THE LEFT NODE
              prevNode = p;
              p = tmpNode;
              continue;
            }
          }
        } else if (beginKey > 0) {
          if (pageItemComparator > 0) {
            tmpNode = successor(p);
            if (tmpNode != null && tmpNode != prevNode) {
              // MAJOR THAN THE CURRENT: GET THE RIGHT NODE
              prevNode = p;
              p = tmpNode;
              continue;
            }
          }
        }

        // SEARCH INSIDE THE NODE
        final V value = lastNode.search(k);

        // PROBABLY PARTIAL KEY IS FOUND USE SEARCH MODE TO FIND PREFERRED ONE
        if (key instanceof OCompositeKey) {
          final OCompositeKey compositeKey = (OCompositeKey) key;

          if (value != null && compositeKey.getKeys().size() == keySize) {
            return setLastSearchNode(key, lastNode);
          }

          if (partialSearchMode.equals(PartialSearchMode.NONE)) {
            if (value != null || iGetContainer)
              return lastNode;
            else
              return null;
          }

          if (partialSearchMode.equals(PartialSearchMode.HIGHEST_BOUNDARY)) {
            // FOUNDED ENTRY EITHER GREATER THAN EXISTING ITEM OR ITEM DOES NOT EXIST
            return adjustHighestPartialSearchResult(iGetContainer, lastNode, compositeKey);
          }

          if (partialSearchMode.equals(PartialSearchMode.LOWEST_BOUNDARY)) {
            return adjustLowestPartialSearchResult(iGetContainer, lastNode, compositeKey);
          }
        }

        if (value != null) {
          setLastSearchNode(key, lastNode);
        }

        if (value != null || iGetContainer)
          // FOUND: RETURN CURRENT NODE OR AT LEAST THE CONTAINER NODE
          return lastNode;

        // NOT FOUND
        return null;
      }
    } finally {
      checkTreeStructure(p);

      OProfiler.getInstance().updateStat("[OMVRBTree.getEntry] Steps of search", steps);
    }

    return setLastSearchNode(key, null);
  }

  private OMVRBTreeEntry<K, V> adjustHighestPartialSearchResult(final boolean iGetContainer, final OMVRBTreeEntry<K, V> lastNode,
      final OCompositeKey compositeKey) {
    final int oldPageIndex = pageIndex;

    final OMVRBTreeEntry<K, V> prevNd = previous(lastNode);

    if (prevNd == null) {
      pageIndex = oldPageIndex;
      pageItemFound = false;

      if (iGetContainer)
        return lastNode;

      return null;
    }

    pageItemComparator = compare(prevNd.getKey(), compositeKey);

    if (pageItemComparator == 0) {
      pageItemFound = true;
      return prevNd;
    } else if (pageItemComparator > 1) {
      pageItemFound = false;

      if (iGetContainer)
        return prevNd;

      return null;
    } else {
      pageIndex = oldPageIndex;
      pageItemFound = false;

      if (iGetContainer)
        return lastNode;

      return null;
    }
  }

  private OMVRBTreeEntry<K, V> adjustLowestPartialSearchResult(final boolean iGetContainer, OMVRBTreeEntry<K, V> lastNode,
      final OCompositeKey compositeKey) {

    // RARE CASE WHEN NODE ITSELF DOES CONTAIN KEY, BUT ALL KEYS LESS THAN GIVEN ONE

    final int oldPageIndex = pageIndex;
    final OMVRBTreeEntry<K, V> oldNode = lastNode;

    if (pageIndex >= lastNode.getSize()) {
      lastNode = next(lastNode);

      if (lastNode == null) {
        lastNode = oldNode;
        pageIndex = oldPageIndex;

        pageItemFound = false;

        if (iGetContainer)
          return lastNode;

        return null;
      }

    }

    pageItemComparator = compare(lastNode.getKey(), compositeKey);

    if (pageItemComparator == 0) {
      pageItemFound = true;
      return lastNode;
    } else {
      pageItemFound = false;

      if (iGetContainer)
        return lastNode;

      return null;
    }
  }

  /**
   * Basic implementation that returns the root node.
   */
  protected OMVRBTreeEntry<K, V> getBestEntryPoint(final K key) {
    return root;
  }

  /**
   * Gets the entry corresponding to the specified key; if no such entry exists, returns the entry for the least key greater than
   * the specified key; if no such entry exists (i.e., the greatest key in the Tree is less than the specified key), returns
   * <tt>null</tt>.
   * 
   * @param key
   *          Key to search.
   * @param partialSearchMode
   *          In case of {@link OCompositeKey} key is passed in this parameter will be used to find preferred one.
   */
  public OMVRBTreeEntry<K, V> getCeilingEntry(final K key, final PartialSearchMode partialSearchMode) {
    OMVRBTreeEntry<K, V> p = getEntry(key, true, partialSearchMode);

    if (p == null)
      return null;

    if (pageItemFound)
      return p;
    // NOT MATCHED, POSITION IS ALREADY TO THE NEXT ONE
    else if (pageIndex < p.getSize()) {
      if (key instanceof OCompositeKey)
        return adjustSearchResult((OCompositeKey) key, partialSearchMode, p);
      else
        return p;
    }

    return null;
  }

  /**
   * Gets the entry corresponding to the specified key; if no such entry exists, returns the entry for the greatest key less than
   * the specified key; if no such entry exists, returns <tt>null</tt>.
   * 
   * @param key
   *          Key to search.
   * @param partialSearchMode
   *          In case of {@link OCompositeKey} composite key is passed in this parameter will be used to find preferred one.
   */
  public OMVRBTreeEntry<K, V> getFloorEntry(final K key, final PartialSearchMode partialSearchMode) {
    OMVRBTreeEntry<K, V> p = getEntry(key, true, partialSearchMode);

    if (p == null)
      return null;

    if (pageItemFound)
      return p;

    final OMVRBTreeEntry<K, V> adjacentEntry = previous(p);
    if (key instanceof OCompositeKey) {
      return adjustSearchResult((OCompositeKey) key, partialSearchMode, adjacentEntry);
    }
    return adjacentEntry;
  }

  private OMVRBTreeEntry<K, V> adjustSearchResult(final OCompositeKey key, final PartialSearchMode partialSearchMode,
      final OMVRBTreeEntry<K, V> foundEntry) {
    if (partialSearchMode.equals(PartialSearchMode.NONE))
      return foundEntry;

    final OCompositeKey keyToSearch = key;
    final OCompositeKey foundKey = (OCompositeKey) foundEntry.getKey();

    if (keyToSearch.getKeys().size() < keySize) {
      final OCompositeKey borderKey = new OCompositeKey();
      final OCompositeKey keyToCompare = new OCompositeKey();

      final List<Object> keyItems = foundKey.getKeys();

      for (int i = 0; i < keySize - 1; i++) {
        final Object keyItem = keyItems.get(i);
        borderKey.addKey(keyItem);

        if (i < keyToSearch.getKeys().size())
          keyToCompare.addKey(keyItem);
      }

      if (partialSearchMode.equals(PartialSearchMode.HIGHEST_BOUNDARY))
        borderKey.addKey(ALWAYS_GREATER_KEY);
      else
        borderKey.addKey(ALWAYS_LESS_KEY);

      final OMVRBTreeEntry<K, V> adjustedNode = getEntry(borderKey, true, PartialSearchMode.NONE);

      if (partialSearchMode.equals(PartialSearchMode.HIGHEST_BOUNDARY))
        return adjustHighestPartialSearchResult(false, adjustedNode, keyToCompare);
      else
        return adjustLowestPartialSearchResult(false, adjustedNode, keyToCompare);

    }
    return foundEntry;
  }

  /**
   * Gets the entry for the least key greater than the specified key; if no such entry exists, returns the entry for the least key
   * greater than the specified key; if no such entry exists returns <tt>null</tt>.
   */
  public OMVRBTreeEntry<K, V> getHigherEntry(final K key) {
    final OMVRBTreeEntry<K, V> p = getEntry(key, true, PartialSearchMode.HIGHEST_BOUNDARY);

    if (p == null)
      return null;

    if (pageItemFound)
      // MATCH, RETURN THE NEXT ONE
      return next(p);
    else if (pageIndex < p.getSize())
      // NOT MATCHED, POSITION IS ALREADY TO THE NEXT ONE
      return p;

    return null;
  }

  /**
   * Returns the entry for the greatest key less than the specified key; if no such entry exists (i.e., the least key in the Tree is
   * greater than the specified key), returns <tt>null</tt>.
   */
  public OMVRBTreeEntry<K, V> getLowerEntry(final K key) {
    final OMVRBTreeEntry<K, V> p = getEntry(key, true, PartialSearchMode.LOWEST_BOUNDARY);

    if (p == null)
      return null;

    return previous(p);
  }

  /**
   * Associates the specified value with the specified key in this map. If the map previously contained a mapping for the key, the
   * old value is replaced.
   * 
   * @param key
   *          key with which the specified value is to be associated
   * @param value
   *          value to be associated with the specified key
   * 
   * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>. (A
   *         <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt> with <tt>key</tt>.)
   * @throws ClassCastException
   *           if the specified key cannot be compared with the keys currently in the map
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   */
  @Override
  public V put(final K key, final V value) {
    OMVRBTreeEntry<K, V> parentNode = null;

    try {
      if (root == null) {
        root = createEntry(key, value);
        root.setColor(BLACK);

        setSize(1);
        modCount++;
        return null;
      }

      // TRY TO GET LATEST SEARCH
      parentNode = getLastSearchNodeForSameKey(key);
      if (parentNode != null) {
        if (lastSearchFound) {
          // EXACT MATCH: UPDATE THE VALUE
          pageIndex = lastSearchIndex;
          modCount++;
          return parentNode.setValue(value);
        }
      }

      // SEARCH THE ITEM
      parentNode = getEntry(key, true, PartialSearchMode.NONE);

      if (pageItemFound) {
        modCount++;
        // EXACT MATCH: UPDATE THE VALUE
        return parentNode.setValue(value);
      }

      setLastSearchNode(null, null);

      if (parentNode == null) {
        parentNode = root;
        pageIndex = 0;
      }

      if (parentNode.getFreeSpace() > 0) {
        // INSERT INTO THE PAGE
        parentNode.insert(pageIndex, key, value);
      } else {
        // CREATE NEW NODE AND COPY HALF OF VALUES FROM THE ORIGIN TO THE NEW ONE IN ORDER TO GET VALUES BALANCED
        final OMVRBTreeEntry<K, V> newNode = createEntry(parentNode);

        if (pageIndex < parentNode.getPageSplitItems())
          // INSERT IN THE ORIGINAL NODE
          parentNode.insert(pageIndex, key, value);
        else
          // INSERT IN THE NEW NODE
          newNode.insert(pageIndex - parentNode.getPageSplitItems(), key, value);

        OMVRBTreeEntry<K, V> node = parentNode.getRight();
        OMVRBTreeEntry<K, V> prevNode = parentNode;
        int cmp = 0;
        if (comparator != null)
          while (node != null) {
            cmp = comparator.compare(newNode.getFirstKey(), node.getFirstKey());
            if (cmp < 0) {
              prevNode = node;
              node = node.getLeft();
            } else if (cmp > 0) {
              prevNode = node;
              node = node.getRight();
            } else {
              throw new IllegalStateException("Duplicated keys were found in OMVRBTree.");
            }
          }
        else
          while (node != null) {
            cmp = compare(newNode.getFirstKey(), node.getFirstKey());
            if (cmp < 0) {
              prevNode = node;
              node = node.getLeft();
            } else if (cmp > 0) {
              prevNode = node;
              node = node.getRight();
            } else {
              throw new IllegalStateException("Duplicated keys were found in OMVRBTree.");
            }
          }

        if (prevNode == parentNode)
          parentNode.setRight(newNode);
        else if (cmp < 0)
          prevNode.setLeft(newNode);
        else if (cmp > 0)
          prevNode.setRight(newNode);
        else
          throw new IllegalStateException("Duplicated keys were found in OMVRBTree.");

        fixAfterInsertion(newNode);
      }

      modCount++;
      setSizeDelta(+1);

    } finally {
      checkTreeStructure(parentNode);
    }

    return null;
  }

  /**
   * Removes the mapping for this key from this OMVRBTree if present.
   * 
   * @param key
   *          key for which mapping should be removed
   * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>. (A
   *         <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt> with <tt>key</tt>.)
   * @throws ClassCastException
   *           if the specified key cannot be compared with the keys currently in the map
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   */
  @Override
  public V remove(final Object key) {
    OMVRBTreeEntry<K, V> p = getEntry(key, PartialSearchMode.NONE);
    setLastSearchNode(null, null);
    if (p == null)
      return null;

    V oldValue = p.getValue();
    deleteEntry(p);
    return oldValue;
  }

  /**
   * Removes all of the mappings from this map. The map will be empty after this call returns.
   */
  @Override
  public void clear() {
    modCount++;
    setSize(0);
    setLastSearchNode(null, null);
    setRoot(null);
  }

  /**
   * Returns a shallow copy of this <tt>OMVRBTree</tt> instance. (The keys and values themselves are not cloned.)
   * 
   * @return a shallow copy of this map
   */
  @Override
  public Object clone() {
    OMVRBTree<K, V> clone = null;
    try {
      clone = (OMVRBTree<K, V>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }

    // Put clone into "virgin" state (except for comparator)
    clone.pageIndex = pageIndex;
    clone.pageItemFound = pageItemFound;
    clone.pageLoadFactor = pageLoadFactor;

    clone.root = null;
    clone.setSize(0);
    clone.modCount = 0;
    clone.entrySet = null;
    clone.navigableKeySet = null;
    clone.descendingMap = null;

    // Initialize clone with our mappings
    try {
      clone.buildFromSorted(size(), entrySet().iterator(), null, null);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }

    return clone;
  }

  // ONavigableMap API methods

  /**
   * @since 1.6
   */
  public Map.Entry<K, V> firstEntry() {
    return exportEntry(getFirstEntry());
  }

  /**
   * @since 1.6
   */
  public Map.Entry<K, V> lastEntry() {
    return exportEntry(getLastEntry());
  }

  /**
   * @since 1.6
   */
  public Entry<K, V> pollFirstEntry() {
    OMVRBTreeEntry<K, V> p = getFirstEntry();
    Map.Entry<K, V> result = exportEntry(p);
    if (p != null)
      deleteEntry(p);
    return result;
  }

  /**
   * @since 1.6
   */
  public Entry<K, V> pollLastEntry() {
    OMVRBTreeEntry<K, V> p = getLastEntry();
    Map.Entry<K, V> result = exportEntry(p);
    if (p != null)
      deleteEntry(p);
    return result;
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   * @since 1.6
   */
  public Map.Entry<K, V> lowerEntry(final K key) {
    return exportEntry(getLowerEntry(key));
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   * @since 1.6
   */
  public K lowerKey(final K key) {
    return keyOrNull(getLowerEntry(key));
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   * @since 1.6
   */
  public Map.Entry<K, V> floorEntry(final K key) {
    return exportEntry(getFloorEntry(key, PartialSearchMode.NONE));
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   * @since 1.6
   */
  public K floorKey(final K key) {
    return keyOrNull(getFloorEntry(key, PartialSearchMode.NONE));
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   * @since 1.6
   */
  public Map.Entry<K, V> ceilingEntry(final K key) {
    return exportEntry(getCeilingEntry(key, PartialSearchMode.NONE));
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   * @since 1.6
   */
  public K ceilingKey(final K key) {
    return keyOrNull(getCeilingEntry(key, PartialSearchMode.NONE));
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   * @since 1.6
   */
  public Map.Entry<K, V> higherEntry(final K key) {
    return exportEntry(getHigherEntry(key));
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
   * @since 1.6
   */
  public K higherKey(final K key) {
    return keyOrNull(getHigherEntry(key));
  }

  // Views

  /**
   * Fields initialized to contain an instance of the entry set view the first time this view is requested. Views are stateless, so
   * there's no reason to create more than one.
   */
  private transient EntrySet            entrySet        = null;
  private transient KeySet<K>           navigableKeySet = null;
  private transient ONavigableMap<K, V> descendingMap   = null;

  /**
   * Returns a {@link Set} view of the keys contained in this map. The set's iterator returns the keys in ascending order. The set
   * is backed by the map, so changes to the map are reflected in the set, and vice-versa. If the map is modified while an iteration
   * over the set is in progress (except through the iterator's own <tt>remove</tt> operation), the results of the iteration are
   * undefined. The set supports element removal, which removes the corresponding mapping from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It does
   * not support the <tt>add</tt> or <tt>addAll</tt> operations.
   */
  @Override
  public Set<K> keySet() {
    return navigableKeySet();
  }

  /**
   * @since 1.6
   */
  public ONavigableSet<K> navigableKeySet() {
    final KeySet<K> nks = navigableKeySet;
    return (nks != null) ? nks : (navigableKeySet = (KeySet<K>) new KeySet<Object>((ONavigableMap<Object, Object>) this));
  }

  /**
   * @since 1.6
   */
  public ONavigableSet<K> descendingKeySet() {
    return descendingMap().navigableKeySet();
  }

  /**
   * Returns a {@link Collection} view of the values contained in this map. The collection's iterator returns the values in
   * ascending order of the corresponding keys. The collection is backed by the map, so changes to the map are reflected in the
   * collection, and vice-versa. If the map is modified while an iteration over the collection is in progress (except through the
   * iterator's own <tt>remove</tt> operation), the results of the iteration are undefined. The collection supports element removal,
   * which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
   * operations.
   */
  @Override
  public Collection<V> values() {
    final Collection<V> vs = super.values();
    return (vs != null) ? vs : null;
  }

  /**
   * Returns a {@link Set} view of the mappings contained in this map. The set's iterator returns the entries in ascending key
   * order. The set is backed by the map, so changes to the map are reflected in the set, and vice-versa. If the map is modified
   * while an iteration over the set is in progress (except through the iterator's own <tt>remove</tt> operation, or through the
   * <tt>setValue</tt> operation on a map entry returned by the iterator) the results of the iteration are undefined. The set
   * supports element removal, which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support the <tt>add</tt>
   * or <tt>addAll</tt> operations.
   */
  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    final EntrySet es = entrySet;
    return (es != null) ? es : (entrySet = new EntrySet());
  }

  /**
   * @since 1.6
   */
  public ONavigableMap<K, V> descendingMap() {
    final ONavigableMap<K, V> km = descendingMap;
    return (km != null) ? km : (descendingMap = new DescendingSubMap<K, V>(this, true, null, true, true, null, true));
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if <tt>fromKey</tt> or <tt>toKey</tt> is null and this map uses natural ordering, or its comparator does not permit
   *           null keys
   * @throws IllegalArgumentException
   *           {@inheritDoc}
   * @since 1.6
   */
  public ONavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive) {
    return new AscendingSubMap<K, V>(this, false, fromKey, fromInclusive, false, toKey, toInclusive);
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if <tt>toKey</tt> is null and this map uses natural ordering, or its comparator does not permit null keys
   * @throws IllegalArgumentException
   *           {@inheritDoc}
   * @since 1.6
   */
  public ONavigableMap<K, V> headMap(final K toKey, final boolean inclusive) {
    return new AscendingSubMap<K, V>(this, true, null, true, false, toKey, inclusive);
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if <tt>fromKey</tt> is null and this map uses natural ordering, or its comparator does not permit null keys
   * @throws IllegalArgumentException
   *           {@inheritDoc}
   * @since 1.6
   */
  public ONavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive) {
    return new AscendingSubMap<K, V>(this, false, fromKey, inclusive, true, null, true);
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if <tt>fromKey</tt> or <tt>toKey</tt> is null and this map uses natural ordering, or its comparator does not permit
   *           null keys
   * @throws IllegalArgumentException
   *           {@inheritDoc}
   */
  public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
    return subMap(fromKey, true, toKey, false);
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if <tt>toKey</tt> is null and this map uses natural ordering, or its comparator does not permit null keys
   * @throws IllegalArgumentException
   *           {@inheritDoc}
   */
  public SortedMap<K, V> headMap(final K toKey) {
    return headMap(toKey, false);
  }

  /**
   * @throws ClassCastException
   *           {@inheritDoc}
   * @throws NullPointerException
   *           if <tt>fromKey</tt> is null and this map uses natural ordering, or its comparator does not permit null keys
   * @throws IllegalArgumentException
   *           {@inheritDoc}
   */
  public SortedMap<K, V> tailMap(final K fromKey) {
    return tailMap(fromKey, true);
  }

  // View class support

  class Values extends AbstractCollection<V> {
    @Override
    public Iterator<V> iterator() {
      return new ValueIterator(getFirstEntry());
    }

    @Override
    public int size() {
      return OMVRBTree.this.size();
    }

    @Override
    public boolean contains(final Object o) {
      return OMVRBTree.this.containsValue(o);
    }

    @Override
    public boolean remove(final Object o) {
      for (OMVRBTreeEntry<K, V> e = getFirstEntry(); e != null; e = next(e)) {
        if (valEquals(e.getValue(), o)) {
          deleteEntry(e);
          return true;
        }
      }
      return false;
    }

    @Override
    public void clear() {
      OMVRBTree.this.clear();
    }
  }

  class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator(getFirstEntry());
    }

    @Override
    public boolean contains(final Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      OMVRBTreeEntry<K, V> entry = (OMVRBTreeEntry<K, V>) o;
      final V value = entry.getValue();
      final V p = get(entry.getKey());
      return p != null && valEquals(p, value);
    }

    @Override
    public boolean remove(final Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      final OMVRBTreeEntry<K, V> entry = (OMVRBTreeEntry<K, V>) o;
      final V value = entry.getValue();
      OMVRBTreeEntry<K, V> p = getEntry(entry.getKey(), PartialSearchMode.NONE);
      if (p != null && valEquals(p.getValue(), value)) {
        deleteEntry(p);
        return true;
      }
      return false;
    }

    @Override
    public int size() {
      return OMVRBTree.this.size();
    }

    @Override
    public void clear() {
      OMVRBTree.this.clear();
    }
  }

  /*
   * Unlike Values and EntrySet, the KeySet class is static, delegating to a ONavigableMap to allow use by SubMaps, which outweighs
   * the ugliness of needing type-tests for the following Iterator methods that are defined appropriately in main versus submap
   * classes.
   */

  OLazyIterator<K> keyIterator() {
    return new KeyIterator(getFirstEntry());
  }

  OLazyIterator<K> descendingKeyIterator() {
    return new DescendingKeyIterator(getLastEntry());
  }

  @SuppressWarnings("rawtypes")
  static final class KeySet<E> extends AbstractSet<E> implements ONavigableSet<E> {
    private final ONavigableMap<E, Object> m;

    KeySet(ONavigableMap<E, Object> map) {
      m = map;
    }

    @Override
    public OLazyIterator<E> iterator() {
      if (m instanceof OMVRBTree)
        return ((OMVRBTree<E, Object>) m).keyIterator();
      else
        return (((OMVRBTree.NavigableSubMap) m).keyIterator());
    }

    public OLazyIterator<E> descendingIterator() {
      if (m instanceof OMVRBTree)
        return ((OMVRBTree<E, Object>) m).descendingKeyIterator();
      else
        return (((OMVRBTree.NavigableSubMap) m).descendingKeyIterator());
    }

    @Override
    public int size() {
      return m.size();
    }

    @Override
    public boolean isEmpty() {
      return m.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
      return m.containsKey(o);
    }

    @Override
    public void clear() {
      m.clear();
    }

    public E lower(final E e) {
      return m.lowerKey(e);
    }

    public E floor(final E e) {
      return m.floorKey(e);
    }

    public E ceiling(final E e) {
      return m.ceilingKey(e);
    }

    public E higher(final E e) {
      return m.higherKey(e);
    }

    public E first() {
      return m.firstKey();
    }

    public E last() {
      return m.lastKey();
    }

    public Comparator<? super E> comparator() {
      return m.comparator();
    }

    public E pollFirst() {
      final Map.Entry<E, Object> e = m.pollFirstEntry();
      return e == null ? null : e.getKey();
    }

    public E pollLast() {
      final Map.Entry<E, Object> e = m.pollLastEntry();
      return e == null ? null : e.getKey();
    }

    @Override
    public boolean remove(final Object o) {
      final int oldSize = size();
      m.remove(o);
      return size() != oldSize;
    }

    public ONavigableSet<E> subSet(final E fromElement, final boolean fromInclusive, final E toElement, final boolean toInclusive) {
      return new OMVRBTreeSet<E>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
    }

    public ONavigableSet<E> headSet(final E toElement, final boolean inclusive) {
      return new OMVRBTreeSet<E>(m.headMap(toElement, inclusive));
    }

    public ONavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
      return new OMVRBTreeSet<E>(m.tailMap(fromElement, inclusive));
    }

    public SortedSet<E> subSet(final E fromElement, final E toElement) {
      return subSet(fromElement, true, toElement, false);
    }

    public SortedSet<E> headSet(final E toElement) {
      return headSet(toElement, false);
    }

    public SortedSet<E> tailSet(final E fromElement) {
      return tailSet(fromElement, true);
    }

    public ONavigableSet<E> descendingSet() {
      return new OMVRBTreeSet<E>(m.descendingMap());
    }
  }

  final class EntryIterator extends AbstractEntryIterator<K, V, Map.Entry<K, V>> {
    EntryIterator(final OMVRBTreeEntry<K, V> first) {
      super(first);
    }

    public Map.Entry<K, V> next() {
      return nextEntry();
    }
  }

  final class ValueIterator extends AbstractEntryIterator<K, V, V> {
    ValueIterator(final OMVRBTreeEntry<K, V> first) {
      super(first);
    }

    public V next() {
      return nextValue();
    }
  }

  final class KeyIterator extends AbstractEntryIterator<K, V, K> {
    KeyIterator(final OMVRBTreeEntry<K, V> first) {
      super(first);
    }

    public K next() {
      return nextKey();
    }
  }

  final class DescendingKeyIterator extends AbstractEntryIterator<K, V, K> {
    DescendingKeyIterator(final OMVRBTreeEntry<K, V> first) {
      super(first);
    }

    public K next() {
      return prevEntry().getKey();
    }
  }

  // Little utilities

  /**
   * Compares two keys using the correct comparison method for this OMVRBTree.
   */
  final int compare(final Object k1, final Object k2) {
    return comparator == null ? ((Comparable<? super K>) k1).compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
  }

  /**
   * Test two values for equality. Differs from o1.equals(o2) only in that it copes with <tt>null</tt> o1 properly.
   */
  final static boolean valEquals(final Object o1, final Object o2) {
    return (o1 == null ? o2 == null : o1.equals(o2));
  }

  /**
   * Return SimpleImmutableEntry for entry, or null if null
   */
  static <K, V> Map.Entry<K, V> exportEntry(final OMVRBTreeEntry<K, V> omvrbTreeEntryPosition) {
    return omvrbTreeEntryPosition == null ? null : new OSimpleImmutableEntry<K, V>(omvrbTreeEntryPosition);
  }

  /**
   * Return SimpleImmutableEntry for entry, or null if null
   */
  static <K, V> Map.Entry<K, V> exportEntry(final OMVRBTreeEntryPosition<K, V> omvrbTreeEntryPosition) {
    return omvrbTreeEntryPosition == null ? null : new OSimpleImmutableEntry<K, V>(omvrbTreeEntryPosition.entry);
  }

  /**
   * Return key for entry, or null if null
   */
  static <K, V> K keyOrNull(final OMVRBTreeEntry<K, V> e) {
    return e == null ? null : e.getKey();
  }

  /**
   * Return key for entry, or null if null
   */
  static <K, V> K keyOrNull(OMVRBTreeEntryPosition<K, V> e) {
    return e == null ? null : e.getKey();
  }

  /**
   * Returns the key corresponding to the specified Entry.
   * 
   * @throws NoSuchElementException
   *           if the Entry is null
   */
  static <K> K key(OMVRBTreeEntry<K, ?> e) {
    if (e == null)
      throw new NoSuchElementException();
    return e.getKey();
  }

  // SubMaps

  /**
   * @serial include
   */
  static abstract class NavigableSubMap<K, V> extends AbstractMap<K, V> implements ONavigableMap<K, V>, java.io.Serializable {
    /**
     * The backing map.
     */
    final OMVRBTree<K, V> m;

    /**
     * Endpoints are represented as triples (fromStart, lo, loInclusive) and (toEnd, hi, hiInclusive). If fromStart is true, then
     * the low (absolute) bound is the start of the backing map, and the other values are ignored. Otherwise, if loInclusive is
     * true, lo is the inclusive bound, else lo is the exclusive bound. Similarly for the upper bound.
     */
    final K               lo, hi;
    final boolean         fromStart, toEnd;
    final boolean         loInclusive, hiInclusive;

    NavigableSubMap(final OMVRBTree<K, V> m, final boolean fromStart, K lo, final boolean loInclusive, final boolean toEnd, K hi,
        final boolean hiInclusive) {
      if (!fromStart && !toEnd) {
        if (m.compare(lo, hi) > 0)
          throw new IllegalArgumentException("fromKey > toKey");
      } else {
        if (!fromStart) // type check
          m.compare(lo, lo);
        if (!toEnd)
          m.compare(hi, hi);
      }

      this.m = m;
      this.fromStart = fromStart;
      this.lo = lo;
      this.loInclusive = loInclusive;
      this.toEnd = toEnd;
      this.hi = hi;
      this.hiInclusive = hiInclusive;
    }

    // internal utilities

    final boolean tooLow(final Object key) {
      if (!fromStart) {
        int c = m.compare(key, lo);
        if (c < 0 || (c == 0 && !loInclusive))
          return true;
      }
      return false;
    }

    final boolean tooHigh(final Object key) {
      if (!toEnd) {
        int c = m.compare(key, hi);
        if (c > 0 || (c == 0 && !hiInclusive))
          return true;
      }
      return false;
    }

    final boolean inRange(final Object key) {
      return !tooLow(key) && !tooHigh(key);
    }

    final boolean inClosedRange(final Object key) {
      return (fromStart || m.compare(key, lo) >= 0) && (toEnd || m.compare(hi, key) >= 0);
    }

    final boolean inRange(final Object key, final boolean inclusive) {
      return inclusive ? inRange(key) : inClosedRange(key);
    }

    /*
     * Absolute versions of relation operations. Subclasses map to these using like-named "sub" versions that invert senses for
     * descending maps
     */

    final OMVRBTreeEntryPosition<K, V> absLowest() {
      OMVRBTreeEntry<K, V> e = (fromStart ? m.getFirstEntry() : (loInclusive ? m.getCeilingEntry(lo,
          PartialSearchMode.LOWEST_BOUNDARY) : m.getHigherEntry(lo)));
      return (e == null || tooHigh(e.getKey())) ? null : new OMVRBTreeEntryPosition<K, V>(e);
    }

    final OMVRBTreeEntryPosition<K, V> absHighest() {
      OMVRBTreeEntry<K, V> e = (toEnd ? m.getLastEntry() : (hiInclusive ? m.getFloorEntry(hi, PartialSearchMode.HIGHEST_BOUNDARY)
          : m.getLowerEntry(hi)));
      return (e == null || tooLow(e.getKey())) ? null : new OMVRBTreeEntryPosition<K, V>(e);
    }

    final OMVRBTreeEntryPosition<K, V> absCeiling(K key) {
      if (tooLow(key))
        return absLowest();
      OMVRBTreeEntry<K, V> e = m.getCeilingEntry(key, PartialSearchMode.NONE);
      return (e == null || tooHigh(e.getKey())) ? null : new OMVRBTreeEntryPosition<K, V>(e);
    }

    final OMVRBTreeEntryPosition<K, V> absHigher(K key) {
      if (tooLow(key))
        return absLowest();
      OMVRBTreeEntry<K, V> e = m.getHigherEntry(key);
      return (e == null || tooHigh(e.getKey())) ? null : new OMVRBTreeEntryPosition<K, V>(e);
    }

    final OMVRBTreeEntryPosition<K, V> absFloor(K key) {
      if (tooHigh(key))
        return absHighest();
      OMVRBTreeEntry<K, V> e = m.getFloorEntry(key, PartialSearchMode.NONE);
      return (e == null || tooLow(e.getKey())) ? null : new OMVRBTreeEntryPosition<K, V>(e);
    }

    final OMVRBTreeEntryPosition<K, V> absLower(K key) {
      if (tooHigh(key))
        return absHighest();
      OMVRBTreeEntry<K, V> e = m.getLowerEntry(key);
      return (e == null || tooLow(e.getKey())) ? null : new OMVRBTreeEntryPosition<K, V>(e);
    }

    /** Returns the absolute high fence for ascending traversal */
    final OMVRBTreeEntryPosition<K, V> absHighFence() {
      return (toEnd ? null : new OMVRBTreeEntryPosition<K, V>(hiInclusive ? m.getHigherEntry(hi) : m.getCeilingEntry(hi,
          PartialSearchMode.LOWEST_BOUNDARY)));
    }

    /** Return the absolute low fence for descending traversal */
    final OMVRBTreeEntryPosition<K, V> absLowFence() {
      return (fromStart ? null : new OMVRBTreeEntryPosition<K, V>(loInclusive ? m.getLowerEntry(lo) : m.getFloorEntry(lo,
          PartialSearchMode.HIGHEST_BOUNDARY)));
    }

    // Abstract methods defined in ascending vs descending classes
    // These relay to the appropriate absolute versions

    abstract OMVRBTreeEntry<K, V> subLowest();

    abstract OMVRBTreeEntry<K, V> subHighest();

    abstract OMVRBTreeEntry<K, V> subCeiling(K key);

    abstract OMVRBTreeEntry<K, V> subHigher(K key);

    abstract OMVRBTreeEntry<K, V> subFloor(K key);

    abstract OMVRBTreeEntry<K, V> subLower(K key);

    /** Returns ascending iterator from the perspective of this submap */
    abstract OLazyIterator<K> keyIterator();

    /** Returns descending iterator from the perspective of this submap */
    abstract OLazyIterator<K> descendingKeyIterator();

    // public methods

    @Override
    public boolean isEmpty() {
      return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
    }

    @Override
    public int size() {
      return (fromStart && toEnd) ? m.size() : entrySet().size();
    }

    @Override
    public final boolean containsKey(Object key) {
      return inRange(key) && m.containsKey(key);
    }

    @Override
    public final V put(K key, V value) {
      if (!inRange(key))
        throw new IllegalArgumentException("key out of range");
      return m.put(key, value);
    }

    @Override
    public final V get(Object key) {
      return !inRange(key) ? null : m.get(key);
    }

    @Override
    public final V remove(Object key) {
      return !inRange(key) ? null : m.remove(key);
    }

    public final Map.Entry<K, V> ceilingEntry(K key) {
      return exportEntry(subCeiling(key));
    }

    public final K ceilingKey(K key) {
      return keyOrNull(subCeiling(key));
    }

    public final Map.Entry<K, V> higherEntry(K key) {
      return exportEntry(subHigher(key));
    }

    public final K higherKey(K key) {
      return keyOrNull(subHigher(key));
    }

    public final Map.Entry<K, V> floorEntry(K key) {
      return exportEntry(subFloor(key));
    }

    public final K floorKey(K key) {
      return keyOrNull(subFloor(key));
    }

    public final Map.Entry<K, V> lowerEntry(K key) {
      return exportEntry(subLower(key));
    }

    public final K lowerKey(K key) {
      return keyOrNull(subLower(key));
    }

    public final K firstKey() {
      return key(subLowest());
    }

    public final K lastKey() {
      return key(subHighest());
    }

    public final Map.Entry<K, V> firstEntry() {
      return exportEntry(subLowest());
    }

    public final Map.Entry<K, V> lastEntry() {
      return exportEntry(subHighest());
    }

    public final Map.Entry<K, V> pollFirstEntry() {
      OMVRBTreeEntry<K, V> e = subLowest();
      Map.Entry<K, V> result = exportEntry(e);
      if (e != null)
        m.deleteEntry(e);
      return result;
    }

    public final Map.Entry<K, V> pollLastEntry() {
      OMVRBTreeEntry<K, V> e = subHighest();
      Map.Entry<K, V> result = exportEntry(e);
      if (e != null)
        m.deleteEntry(e);
      return result;
    }

    // Views
    transient ONavigableMap<K, V> descendingMapView   = null;
    transient EntrySetView        entrySetView        = null;
    transient KeySet<K>           navigableKeySetView = null;

    @SuppressWarnings("rawtypes")
    public final ONavigableSet<K> navigableKeySet() {
      KeySet<K> nksv = navigableKeySetView;
      return (nksv != null) ? nksv : (navigableKeySetView = new OMVRBTree.KeySet(this));
    }

    @Override
    public final Set<K> keySet() {
      return navigableKeySet();
    }

    public ONavigableSet<K> descendingKeySet() {
      return descendingMap().navigableKeySet();
    }

    public final SortedMap<K, V> subMap(final K fromKey, final K toKey) {
      return subMap(fromKey, true, toKey, false);
    }

    public final SortedMap<K, V> headMap(final K toKey) {
      return headMap(toKey, false);
    }

    public final SortedMap<K, V> tailMap(final K fromKey) {
      return tailMap(fromKey, true);
    }

    // View classes

    abstract class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
      private transient int size = -1, sizeModCount;

      @Override
      public int size() {
        if (fromStart && toEnd)
          return m.size();
        if (size == -1 || sizeModCount != m.modCount) {
          sizeModCount = m.modCount;
          size = 0;
          Iterator<?> i = iterator();
          while (i.hasNext()) {
            size++;
            i.next();
          }
        }
        return size;
      }

      @Override
      public boolean isEmpty() {
        OMVRBTreeEntryPosition<K, V> n = absLowest();
        return n == null || tooHigh(n.getKey());
      }

      @Override
      public boolean contains(final Object o) {
        if (!(o instanceof OMVRBTreeEntry))
          return false;
        final OMVRBTreeEntry<K, V> entry = (OMVRBTreeEntry<K, V>) o;
        final K key = entry.getKey();
        if (!inRange(key))
          return false;
        V nodeValue = m.get(key);
        return nodeValue != null && valEquals(nodeValue, entry.getValue());
      }

      @Override
      public boolean remove(final Object o) {
        if (!(o instanceof OMVRBTreeEntry))
          return false;
        final OMVRBTreeEntry<K, V> entry = (OMVRBTreeEntry<K, V>) o;
        final K key = entry.getKey();
        if (!inRange(key))
          return false;
        final OMVRBTreeEntry<K, V> node = m.getEntry(key, PartialSearchMode.NONE);
        if (node != null && valEquals(node.getValue(), entry.getValue())) {
          m.deleteEntry(node);
          return true;
        }
        return false;
      }
    }

    /**
     * Iterators for SubMaps
     */
    abstract class SubMapIterator<T> implements OLazyIterator<T> {
      OMVRBTreeEntryPosition<K, V> lastReturned;
      OMVRBTreeEntryPosition<K, V> next;
      final K                      fenceKey;
      int                          expectedModCount;

      SubMapIterator(final OMVRBTreeEntryPosition<K, V> first, final OMVRBTreeEntryPosition<K, V> fence) {
        expectedModCount = m.modCount;
        lastReturned = null;
        next = first;
        fenceKey = fence == null ? null : fence.getKey();
      }

      public final boolean hasNext() {
        if (next != null) {
          final K k = next.getKey();
          return k != fenceKey && !k.equals(fenceKey);
        }
        return false;
      }

      final OMVRBTreeEntryPosition<K, V> nextEntry() {
        final OMVRBTreeEntryPosition<K, V> e;
        if (next != null)
          e = new OMVRBTreeEntryPosition<K, V>(next);
        else
          e = null;
        if (e == null || e.entry == null)
          throw new NoSuchElementException();

        final K k = e.getKey();
        if (k == fenceKey || k.equals(fenceKey))
          throw new NoSuchElementException();

        if (m.modCount != expectedModCount)
          throw new ConcurrentModificationException();
        next.assign(OMVRBTree.next(e));
        lastReturned = e;
        return e;
      }

      final OMVRBTreeEntryPosition<K, V> prevEntry() {
        final OMVRBTreeEntryPosition<K, V> e;
        if (next != null)
          e = new OMVRBTreeEntryPosition<K, V>(next);
        else
          e = null;

        if (e == null || e.entry == null)
          throw new NoSuchElementException();

        final K k = e.getKey();
        if (k == fenceKey || k.equals(fenceKey))
          throw new NoSuchElementException();

        if (m.modCount != expectedModCount)
          throw new ConcurrentModificationException();
        next.assign(OMVRBTree.previous(e));
        lastReturned = e;
        return e;
      }

      final public T update(final T iValue) {
        if (lastReturned == null)
          throw new IllegalStateException();
        if (m.modCount != expectedModCount)
          throw new ConcurrentModificationException();
        return (T) lastReturned.entry.setValue((V) iValue);
      }

      final void removeAscending() {
        if (lastReturned == null)
          throw new IllegalStateException();
        if (m.modCount != expectedModCount)
          throw new ConcurrentModificationException();
        // deleted entries are replaced by their successors
        if (lastReturned.entry.getLeft() != null && lastReturned.entry.getRight() != null)
          next = lastReturned;
        m.deleteEntry(lastReturned.entry);
        lastReturned = null;
        expectedModCount = m.modCount;
      }

      final void removeDescending() {
        if (lastReturned == null)
          throw new IllegalStateException();
        if (m.modCount != expectedModCount)
          throw new ConcurrentModificationException();
        m.deleteEntry(lastReturned.entry);
        lastReturned = null;
        expectedModCount = m.modCount;
      }

    }

    final class SubMapEntryIterator extends SubMapIterator<Map.Entry<K, V>> {
      SubMapEntryIterator(final OMVRBTreeEntryPosition<K, V> first, final OMVRBTreeEntryPosition<K, V> fence) {
        super(first, fence);
      }

      public Map.Entry<K, V> next() {
        final Map.Entry<K, V> e = OMVRBTree.exportEntry(next);
        nextEntry();
        return e;
      }

      public void remove() {
        removeAscending();
      }
    }

    final class SubMapKeyIterator extends SubMapIterator<K> {
      SubMapKeyIterator(final OMVRBTreeEntryPosition<K, V> first, final OMVRBTreeEntryPosition<K, V> fence) {
        super(first, fence);
      }

      public K next() {
        return nextEntry().getKey();
      }

      public void remove() {
        removeAscending();
      }
    }

    final class DescendingSubMapEntryIterator extends SubMapIterator<Map.Entry<K, V>> {
      DescendingSubMapEntryIterator(final OMVRBTreeEntryPosition<K, V> last, final OMVRBTreeEntryPosition<K, V> fence) {
        super(last, fence);
      }

      public Map.Entry<K, V> next() {
        final Map.Entry<K, V> e = OMVRBTree.exportEntry(next);
        prevEntry();
        return e;
      }

      public void remove() {
        removeDescending();
      }
    }

    final class DescendingSubMapKeyIterator extends SubMapIterator<K> {
      DescendingSubMapKeyIterator(final OMVRBTreeEntryPosition<K, V> last, final OMVRBTreeEntryPosition<K, V> fence) {
        super(last, fence);
      }

      public K next() {
        return prevEntry().getKey();
      }

      public void remove() {
        removeDescending();
      }
    }
  }

  /**
   * @serial include
   */
  static final class AscendingSubMap<K, V> extends NavigableSubMap<K, V> {
    private static final long serialVersionUID = 912986545866124060L;

    AscendingSubMap(final OMVRBTree<K, V> m, final boolean fromStart, final K lo, final boolean loInclusive, final boolean toEnd,
        K hi, final boolean hiInclusive) {
      super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
    }

    public Comparator<? super K> comparator() {
      return m.comparator();
    }

    public ONavigableMap<K, V> subMap(final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive) {
      if (!inRange(fromKey, fromInclusive))
        throw new IllegalArgumentExcep
