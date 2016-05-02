/**
 * File: NodeSortArcsByQuality.java
 * Created by: mhaimel
 * Created on: Nov 25, 2010
 * CVS:  $Id: NodeSortArcsByQuality.java 1.0 Nov 25, 2010 10:50:46 AM mhaimel Exp $
 */
package uk.ac.ebi.curtain.model.graph.curtain.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.ac.ebi.curtain.model.graph.Arc;
import uk.ac.ebi.curtain.model.graph.Node;
import uk.ac.ebi.curtain.model.graph.GraphAccess.TouchEach;
import uk.ac.ebi.curtain.model.graph.curtain.CategoryReadInfo;
import uk.ac.ebi.curtain.model.graph.curtain.ContigInfo;
import uk.ac.ebi.curtain.model.graph.curtain.ReadWrapper;

/**
 * @author mhaimel
 *
 */
public class NodeSortArcsByQuality implements TouchEach<Node<ContigInfo, ReadWrapper>> {
	@Override
	public void touch(Node<ContigInfo, ReadWrapper> node) {
		final int cat = node.getValue().getCategories();
		{
			List<Arc<ContigInfo, ReadWrapper>> left = new ArrayList<Arc<ContigInfo,ReadWrapper>>(node.getLeft());
			for(Arc<ContigInfo, ReadWrapper> arc : left){
				node.removeArc(arc,true);
			}
			Collections.sort(left,getComparator(cat));
			for(Arc<ContigInfo, ReadWrapper> arc : left){
				node.registerArcLeft(arc);
			}		
		}
		{
			List<Arc<ContigInfo, ReadWrapper>> right = new ArrayList<Arc<ContigInfo,ReadWrapper>>(node.getRight());
			for(Arc<ContigInfo, ReadWrapper> arc : right){
				node.removeArc(arc,true);
			}
			Collections.sort(right,getComparator(cat));
			for(Arc<ContigInfo, ReadWrapper> arc : right){
				node.registerArcRight(arc);
			}
		}
	}

	private Comparator<Arc<ContigInfo, ReadWrapper>> getComparator(final int cat) {
		return new Comparator<Arc<ContigInfo, ReadWrapper>>() {
			@Override
			public int compare(Arc<ContigInfo, ReadWrapper> o1,Arc<ContigInfo, ReadWrapper> o2) {
				double q1 = 0;
				double q2 = 0;
				double c1 = 0;
				double c2 = 0;
				for(int i = 0; i < cat; ++i){
					CategoryReadInfo i1 = o1.getValue().getInfo(i);
					CategoryReadInfo i2 = o2.getValue().getInfo(i);
					if(null != i1){
						q1 += i1.getWeightSum().doubleValue();
						c1 += i1.getMultiplicity().doubleValue();
					}
					if(null != i2){
						q2 += i2.getWeightSum().doubleValue();
						c2 += i2.getMultiplicity().doubleValue();
					}
				}
				double v1 = c1 >0?q1/c1:c1;
				double v2 = c2 >0?q2/c2:c2;
				// highest one first 
				double res = (v1-v2)*-1;
				if(res == 0){
					/* Next -> multiplicity
					 */
					res = (c1-c2)*-1;
				}
				if(res == 0){
					long s1 = o1.getLeft().getValue().getLength() + o1.getRight().getValue().getLength();
					long s2 = o2.getLeft().getValue().getLength() + o2.getRight().getValue().getLength();
					/* Longest pair
					 */
					res = (s1-s2)*-1;
				}

				if(res == 0){
					long s1 = o1.getLeft().getId() + o1.getRight().getId();
					long s2 = o2.getLeft().getId() + o2.getRight().getId();
					/* lower other Node id: to get a sorting somehow (very unlikely event)
					 */
					res = (s1-s2);
				}
				if(res == 0){
					/* Based on hash code -> to get somehow a sorting order: does not matter at this point
					 * */
					res = o1.hashCode() - o2.hashCode();
				}
				if(res == 0){
					/* Should never reach this point really - but before ignoring a node ...
					 */
					res = 1;
				}
				if(res>0){
					res = 1;
				} else {
					res = -1;
				}
				return (int) res;
			}
		};
	}
	
	
}

