package com.bagatelle.zplanner.estimate;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Estimate encapsulates the notion of an estimate and it's associate change history (i.e EstimateUpdates).
 * It's primary purpose is to hide the mechanics of how an estimate is internally represented as generally
 * as estimate can either be associated directly to an item or it can be derived as the sum of all children
 * of a given item (which override any estimate directly assigned to that item), if the item does in fact have
 * children.  It is used as a component within WorkItem
 * 
 * @author zacbol
 *
 */
@Embeddable
public class Estimate {
	@Id @GeneratedValue private Long id;
	private double itemEstimate; 
    private double sumEstimate;
    private int numChildren = 0;
    //@Transient private double estimate; 
    @OneToMany(cascade=CascadeType.ALL) private List<EstimateUpdate> estimateUpdates = new ArrayList<EstimateUpdate>();
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public double getEstimate() {
		return (numChildren > 0) ? sumEstimate : itemEstimate;    	
	}
	private void setEstimate() {}
	
	public double getItemEstimate() { return itemEstimate; }
	public void setItemEstimate(double estimate) {
		//if the item has a sumEstimate, you can't explicitly set its Itemestimate...
		if(sumEstimate != 0.0) {
			return;
		}
		
		this.itemEstimate = estimate;
	}

	//TODO: make setter private?
    public double getSumEstimate() { return sumEstimate; }
    public void setSumEstimate(double sumEstimate) { this.sumEstimate = sumEstimate; }
    
    public List<EstimateUpdate> getEstimateUpdates() { return estimateUpdates; }
	public void setEstimateUpdates(List<EstimateUpdate> estimateUpdates) {
		this.estimateUpdates = estimateUpdates;
	}

	@SuppressWarnings("unused") private Estimate() {}
	
	/**
	 * We need to be able to construct a new Estimate based on an old one in the cases where we
	 * transform a workitem into another type of workitem (i.e. a story becomase a task), so that
	 * we can preserve the estimate history of the item
	 * 
	 * @param estimate
	 */
	public Estimate(Estimate estimate) {
		itemEstimate = estimate.getItemEstimate();
		
		for(EstimateUpdate update : estimate.getEstimateUpdates()) {
			EstimateUpdate copiedUpdate = new EstimateUpdate(update);
			getEstimateUpdates().add(copiedUpdate);
		}
	}
	
	public Estimate(double estimate) { 
		itemEstimate = estimate;
		
		addUpdate(estimate);
	}
	
	public void update(int numChildren, double estimate) {
		this.numChildren = numChildren; 
		
		if(numChildren > 0) {
			sumEstimate = estimate;
		} else {
			itemEstimate = estimate;
		}
		
		addUpdate(estimate);
	}
	
	
	private void addUpdate(double newEstimate) {
		EstimateUpdate estTx = new EstimateUpdate(newEstimate);
		getEstimateUpdates().add(estTx);
	}

}

