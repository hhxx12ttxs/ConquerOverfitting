/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.

    Copyright (C) 2006, by EADS CRC
    Copyright (C) 2007,2008, by EADS France

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.jcae.mesh.amibe.util;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Red-black binary trees to store quality factors.
 * Main ideas come from Ben Pfaff's <a href="http://adtinfo.org/">GNU libavl</a>.
 * These trees are used to sort vertices, edges, or triangles according
 * to their quality factors, and to process them in increasing or decreasing
 * order after they have been sorted.  See examples in algorithms from
 * {@link org.jcae.mesh.amibe.algos3d}.
 * A red-black tree has the following properties:
 * <ol>
 * <li>Null nodes are black.</li>
 * <li>A red node has no red child.</li>
 * <li>Paths from any node to external leaves contain the same number of black
 *     nodes.</li>
 * </ol>
 * By convention, root node is always black in order to simplify node insertion
 * and removal.  Node insertions and removals are explained in detail at
 * <a href="http://en.wikipedia.org/wiki/Red-Black_tree">wikipedia</a>.
 */
public class PRedBlackSortedTree<E> extends QSortedTree<E>
{
	private static final long serialVersionUID = 4767412412814775447L;
	private static final Logger logger=Logger.getLogger(PRedBlackSortedTree.class.getName());	
	private static class Node<E> extends QSortedTree.Node<E>
	{
		private static final long serialVersionUID = 4887055383474718211L;

		private boolean isRed;
		
		@SuppressWarnings("unchecked")
		@Override
		protected Node<E> [] newChilds()
		{
			return new Node[2];
		}

		private Node(E o, double v)
		{
			super(o, v);
			isRed = true;
		}
		
		@Override
		public void reset(double v)
		{
			super.reset(v);
			isRed = true;
		}

		@Override
		public final String toString()
		{
			return super.toString()+" "+(isRed ? "red" : "black");
		}
	}

	@Override
	final QSortedTree.Node<E> newNode(E o, double v)
	{
		return new Node<E>(o, v);
	}

	// Helper function
	private static <E> boolean isRedNode(QSortedTree.Node<E> x)
	{
		return (x != null) && ((Node<E>) x).isRed;
	}
	
	@Override
	final boolean insertNode(QSortedTree.Node<E> o)
	{
		Node<E> p = (Node<E>) o;
		Node<E> current = (Node<E>) root.child[0];
		Node<E> q = (Node<E>) root;
		int lastDir = 0;
		while (current != null)
		{
			if (p.compareTo(current) < 0)
				lastDir = 0;
			else
				lastDir = 1;
			q = current;
			current = (Node<E>) current.child[lastDir];
		}
		// Insert node
		q.child[lastDir] = p;
		p.parent = q;
		// Node color is red, so property 3 is preserved.
		// We must check if property 2 is violated, in which
		// case our tree has to be rebalanced and/or repainted.
		// Case I1: root node
		if (q == root)
		{
			// We enforce root node to be black, this eases
			// other cases below.
			logger.fine("Case I1");
			p.isRed = false;
			assert !((Node<E>) root.child[0]).isRed;
			return true;
		}
		while (p != root.child[0])
		{
			q = (Node<E>) p.parent;
			// If parent is black, property 2 is preserved,
			// everything is fine.
			// Case I2: parent is black
			if (!q.isRed)
			{
				logger.fine("Case I2");
				assert !((Node<E>) root.child[0]).isRed;
				return true;
			}
			// Parent is red, so it cannot be the root tree,
			// and grandparent is black.
			Node<E> grandparent = (Node<E>) q.parent;
			assert grandparent != root;
			if (grandparent.child[0] == q)
				lastDir = 0;
			else
				lastDir = 1;
			int sibDir = 1 - lastDir;
			Node<E> uncle = (Node<E>) grandparent.child[sibDir];
			if (isRedNode(uncle))
			{
				// Case I3: uncle is red
				/* Paint nodes and continue from grandparent
				     gB                gR
				    / \   ------>     / \
				   qR uR            qB  uB
				    \                 \
				    pR                pR
				*/
				logger.fine("Case I3");
				q.isRed = false;
				uncle.isRed = false;
				grandparent.isRed = true;
				p = grandparent;
			}
			else
			{
				assert !isRedNode(uncle);
				if (q.child[lastDir] != p)
				{
					/* Rotate to put red nodes on the
					   same side
					     gB            gB
					    / \   ---->   / \
					   qR uB        pR  uB
					    \           /
					    pR         qR
					*/
					logger.fine("Case I4");
					if (lastDir == 0)
						grandparent.child[0] = q.rotateL();
					else
						grandparent.child[1] = q.rotateR();
					p = q;
					q = (Node<E>) p.parent;
				}
				/* Rotate on opposite way and recolor.  Either
				   uncle is null, or we come from case 3 and
				   current node has 2 black children.
				        gB                qB
				       / \   ------>     /  \
				      qR uB            pR   gR
				     / \              / \   / \
				    pR zB            xB yB zB uB
				   / \
				  xB yB
				*/
				assert (uncle == null && q.child[sibDir] == null &&
				  p.child[0] == null && p.child[1] == null) ||
				 (uncle != null && q.child[sibDir] != null &&
				  p.child[0] != null && p.child[1] != null);
				logger.fine("Case I5");

				Node<E> greatgrandparent = (Node<E>) grandparent.parent;
				grandparent.isRed = true;
				q.isRed = false;
				if (greatgrandparent.child[0] == grandparent)
					lastDir = 0;
				else
					lastDir = 1;
				// lastDir has been modified, so use sibDir here
				if (sibDir == 1)
					greatgrandparent.child[lastDir] = grandparent.rotateR();
				else
					greatgrandparent.child[lastDir] = grandparent.rotateL();
				assert !((Node<E>) root.child[0]).isRed;
				return true;
			}
		}
		((Node<E>) root.child[0]).isRed = false;
		assert isValid();
		return true;
	}
	
	@Override
	final QSortedTree.Node<E> removeNode(QSortedTree.Node<E> o)
	{
		Node<E> p = (Node<E>) o;
		Node<E> ret = p;
		Node<E> q = (Node<E>) p.parent;
		int lastDir = 0;
		if (q.child[1] == p)
			lastDir = 1;
		if (p.child[1] == null)
		{
			q.child[lastDir] = p.child[0];
			if (q.child[lastDir] != null)
				q.child[lastDir].parent = q;
		}
		else
		{
			// p has a right child.  Replace p by its
			// successor, and update q and lastDir.
			Node<E> r = (Node<E>) p.nextNode();
			// Do not modify p's color!
			p.swap(r);
			p = r;
			q = (Node<E>) p.parent;
			if (q.child[0] == p)
				lastDir = 0;
			else
				lastDir = 1;
			assert p.child[0] == null;
			q.child[lastDir] = p.child[1];
			if (q.child[lastDir] != null)
				q.child[lastDir].parent = q;
			ret = r;
		}
		// p is the node to be removed, q its parent and
		// lastDir so that q.child[lastDir] == p;
		if (p.isRed)
			return ret;
		// p is a black node, so q is no more balanced, its
		// lastDir child of q has 1 black node less than its
		// sibling.
		while (true)
		{
			p = (Node<E>) q.child[lastDir];
			if (isRedNode(p))
			{
				p.isRed = false;
				logger.fine("Red node :-)");
				break;
			}
			// Case R1: root tree
			if (q == root)
			{
				// All paths have one less black node
				logger.fine("Case R1");
				break;
			}
			int sibDir = 1 - lastDir;
			Node<E> grandparent = (Node<E>) q.parent;
			int gLastDir = 0;
			if (grandparent.child[1] == q)
				gLastDir = 1;
			Node<E> sibling = (Node<E>) q.child[sibDir];
			// A black node is removed, so its sibling cannot
			// be null.
			assert sibling != null : q.toString();
			if (sibling.isRed)
			{
				// Case R2: sibling is red
				logger.fine("Case R2");
				sibling.isRed = false;
				q.isRed = true;
				assert sibling.child[0] != null;
				assert sibling.child[1] != null;

				/* Example with lastDir == 0
				     qB          qR             sB => gB
				     / \   ----> / \    ---->   / \
				    pB sR       pB sB          qR  bB
				       / \         / \        / \
				      aB bB       aB bB      pB aB => sB
				*/
				if (lastDir == 0)
					grandparent.child[gLastDir] = q.rotateL();
				else
					grandparent.child[gLastDir] = q.rotateR();
				grandparent = sibling;
				gLastDir = lastDir;
				sibling = (Node<E>) q.child[sibDir];
			}
			// Now sibling is black
			assert !sibling.isRed;
			if (!q.isRed && !isRedNode(sibling.child[0]) && !isRedNode(sibling.child[1]))
			{
				// Case R3: parent, sibling and sibling's
				// children are black
				logger.fine("Case R3");
				sibling.isRed = true;
			}
			else
			{
				if (!isRedNode(sibling.child[0]) && !isRedNode(sibling.child[1]))
				{
					// Case R4: sibling and sibling's
					// children are black, but parent is
					// red.
					assert q.isRed;
					logger.fine("Case R4");
					sibling.isRed = true;
					q.isRed = false;
					break;
				}
				if (isRedNode(sibling.child[lastDir]) && !isRedNode(sibling.child[sibDir]))
				{
					// Case R5: sibling is black, left child is
					// red and right child is black.
					// Rotate at sibling and paint nodes
					// so that sibling.child[sibDir] is red.
					logger.fine("Case R5");
					Node<E> y = (Node<E>) sibling.child[lastDir];
					y.isRed = false;
					sibling.isRed = true;
					if (lastDir == 0)
						q.child[sibDir] = sibling.rotateR();
					else
						q.child[sibDir] = sibling.rotateL();
					sibling = y;
					/* Example with lastDir == 0
					  q*       q*
					  / \ ---> / \
					 xB sB    xB yB
					   / \      / \
					  yR  zB   aB  sR
					 / \          / \
					aB bB        bB  zB
					*/
				}
				logger.fine("Case R6");
				// Case R6: sibling is black and its right child
				// is red.
				/*
				  q*       qB          s*
				 / \ ---> / \  --->   / \
				xB sB    xB s*      qB   yB
				  / \      / \     / \   / \
				 aB  yR   aB  yB  xB aB bB zB
				    / \      / \
				   bB  zB   bB  zB
				*/
				assert !isRedNode(sibling) && isRedNode(sibling.child[sibDir]);
				sibling.isRed = q.isRed;
				q.isRed = false;
				((Node<E>) sibling.child[sibDir]).isRed = false;
				if (lastDir == 0)
					grandparent.child[gLastDir] = q.rotateL();
				else
					grandparent.child[gLastDir] = q.rotateR();
				break;
			}
			if (q.parent.child[0] == q)
				lastDir = 0;
			else
				lastDir = 1;
			q = (Node<E>) q.parent;
		}
		assert isValid();
		return ret;
	}
	
	private boolean isValid()
	{
		// Call debugIsValid() only when debugging, otherwise
		// tree manipulations are way too slow.
		return true;
		//return debugIsValid();
		//return checkParentPointers();
	}

	public boolean checkParentPointers()
	{
		if (root.child[0] == null)
			return true;
		for (Iterator<QSortedTree.Node<E>> it = iterator(); it.hasNext(); )
		{
			QSortedTree.Node<E> current = it.next();
			if (current.child[0] != null && current.child[0].parent != current)
				return false;
			if (current.child[1] != null && current.child[1].parent != current)
				return false;
		}
		return true;
	}

	public boolean debugIsValid()
	{
		Node<E> current = (Node<E>) root.child[0];
		if (isRedNode(current))
			return false;
		if (current == null)
			return true;
		while (current.child[0] != null)
			current = (Node<E>) current.child[0];
		// Now traverse the tree
		while (current != root)
		{
			if (isRedNode(current) && (isRedNode(current.child[0]) || isRedNode(current.child[1])))
				return false;
			if (current.child[0] != null && current.child[0].compareTo(current) > 0)
				return false;
			if (current.child[1] != null && current.child[1].compareTo(current) < 0)
				return false;
			if (current.child[1] != null)
			{
				current = (Node<E>) current.child[1];
				if (isRedNode(current) && (isRedNode(current.child[0]) || isRedNode(current.child[1])))
					return false;
				while (current.child[0] != null)
				{
					current = (Node<E>) current.child[0];
					if (isRedNode(current) && (isRedNode(current.child[0]) || isRedNode(current.child[1])))
						return false;
				}
			}
			else
			{
				// Walk upwards
				while (current.parent.child[0] != current)
				{
					if (isRedNode(current) && (isRedNode(current.child[0]) || isRedNode(current.child[1])))
						return false;
					current = (Node<E>) current.parent;
				}
				current = (Node<E>) current.parent;
			}
		}
		return true;
	}

}

