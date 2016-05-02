package com.bagatelle.zplanner.workitem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.Session;
import org.joda.time.DateTime;

import com.bagatelle.zplanner.account.UserAccount;
import com.bagatelle.zplanner.estimate.Estimate;
import com.bagatelle.zplanner.estimate.EstimateUpdate;
import com.bagatelle.zplanner.estimate.TimeCard;
import com.bagatelle.zplanner.estimate.TimeEntry;

/**
 * This is the core class of Zplanner.  It is the abstract base class from which all the primary
 * domain objects are derived.  It abstractly represents the notion of a workitem, which can be thought
 * of as a node within a tree hierarchy, in whihc each level on the tree represents a more granular
 * breakdown of the items on the level above.  Every workitem has, at a minimum, a name, a description,
 * and an estimate.  These are members defined here since they are identical no matter what the 
 * implementation.  In contrast, every workitem also has children and a parent, but the class type
 * of these items depends on the item itself.  To give a concrete example, a project should only ever
 * have iterations as its children.  It cannot have stories as it's direct descendents.  So, we require
 * every implementor of workitem to implement methods to get and set the parent and children, but
 * these function definitions should correspond to the type of objects used by that particular class.
 * 
 * 
 * @author zacbol
 *
 */
@MappedSuperclass
public abstract class WorkItem {
	@Id @GeneratedValue private Long id; //Hibernate auto increment id	
	protected String name;
	protected String description;
	protected Estimate estimate;	
	protected TimeCard timeCard = new TimeCard();

	public TimeCard getTimeCard() {
		return timeCard;
	}

	public void setTimeCard(TimeCard timeCard) {
		this.timeCard = timeCard;
	}

	public WorkItem() {}
	
	public WorkItem(String name, String description, Double est) {
		this.name = name;
		this.description = description;
		this.estimate = new Estimate(est);
	}
	
	//Getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; } 

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	//the embedded estimate object abstracts away most of hte complication of how we roll up estimates
	//this is a convenience method for callers
	public double getEstimate() { return estimate.getEstimate(); }
	
	//we need to access/set the actual estimate object sometimes when we convert between types
	//of workitems so as to preserve estimate history
	public void setEstimateObject(Estimate estimate) { this.estimate = estimate; }
	public Estimate getEstimateObject() { return estimate; }
	
	//these methods are only here to faciliate testing, so we can create fake history of updates
	//These are NOT intended to be used anywhere within the actual application
	public List<EstimateUpdate> getEstimateUpdates() { return estimate.getEstimateUpdates(); }
    public void setEstimateUpdates(List<EstimateUpdate> estUpdates) { estimate.setEstimateUpdates(estUpdates); }
    
    //methods we require our subclasses to implement so we can manipulate them all the same way
	public abstract WorkItem getParent();
	public abstract void setParent(WorkItem newParent);
	
	public abstract List<? extends WorkItem> getChildren();
	
	//this will return the concrete class type of the child of any given workitem.  Used when
	//when we move an item around in the workitem tree so we cast it to the appropriate type to be
	//the child of its new parent
	@SuppressWarnings("unchecked") public abstract Class getChildClass();
    
	//this method is used for getting the data points when we graph the estimate history of this item
	public Map<DateTime, Double> getHistory(Session session) {
		return EstimateUpdate.getUpdates(session, this);
	}
	
	
	/**
	 * Initialize this workitem based on another workitem.  Used for converting different
	 * types of workitems amongst eachother so they can be freely moved to different levels within
	 * our hierarchical tree of projects/iterations/stories/tasks/subtasks
	 * 
	 * @param workitem
	 */
	@SuppressWarnings("unchecked")
	public void init(WorkItem workitem) {
		this.name = workitem.getName();
		this.description = workitem.getDescription();
		
		//need to convert any children
		for(WorkItem childItem : workitem.getChildren()) {
			WorkItem blessedChild = convertToChildType(childItem);
			blessedChild.setParent(this);
			
			 //FIXME;  we're kind of circumventing type safety here, but since we're converting the 
			//using the ChildType stored in the parent, we can be fairly certain it's okay
			((List)this.getChildren()).add(blessedChild); 
		}	
		
		//make sure to preserve the estimate history
		estimate = new Estimate(workitem.getEstimateObject());
	}
	
	/**
	 * Add a new child to this item.  We first have to disassociate the item from any current parent
	 * it may have, then to its new parents.  Then both branches of the tree have to recalculate
	 * any estimates higher in the branch which will have been affected
	 * 
	 * @param child
	 * @return
	 */
	protected List<WorkItem> addChild(WorkItem child) {
		if(child == null) { throw new RuntimeException("Cannot add a null child to a workitem!"); }
		
		List<WorkItem> updatedNodes = new ArrayList<WorkItem>();
		
		WorkItem currentParent = child.getParent();
		if(currentParent != null) {
			currentParent.getChildren().remove(child);
			
			//current parent needs to recalc its estimate
			updatedNodes = currentParent.updateEstimate(); 
		}

		//associate this item to its new parent, same for an existing or newly created item 
		child.setParent(this);		
		
		//FIXME:  bad!!!  Circumventing generics type safety
		((List)this.getChildren()).add(child);
		
		updatedNodes.addAll(this.updateEstimate());
		
		return updatedNodes;
	}
	
	/**
	 * This method is called anytime a node is added or removed from a node within our project
	 * tree.  We recalculate the estimate for the node, record an event in the audit history (even
	 * if the estimate didn't change), and then call updateEstimate on the parent since having
	 * recalculated *our* estimate we've also affected it
	 * 
	 * @return
	 */
	protected List<WorkItem> updateEstimate() {
		//track all the nodes that were in this branch of our project tree and affected by update
		List<WorkItem> updatedNodes = new ArrayList<WorkItem>(); 
		
		//calculate the estimate for this node in the tree, rolling up estimates under it
		Double newEstimate = CommonUtil.calcEstimate(this); 
		
		//add an update.  We don't care if the estimate stayed the same (Note: in the original 
		//implementation we did) because we want an audit change of any material change to the item
		//even if the net effect on the estimate is zero
		estimate.update(this.getChildren().size(), newEstimate);
		updatedNodes.add(this);

		//update parent as well, which may update others & returns list of all affected nodes
		WorkItem parent = getParent();
		if(parent != null) {
			updatedNodes.addAll(parent.updateEstimate());
		}
		
		return updatedNodes;
	}
	
	public List<WorkItem> logWork(TimeEntry timeEntry) {
		List<WorkItem> updatedNodes = new ArrayList<WorkItem>(); 
		
		//update the work done for this item
		getTimeCard().addEntry(timeEntry);
		updatedNodes.add(this);
		
		WorkItem parent = getParent();
		if(parent != null) {
			//if parent is not null, cascade the work done updward (since it also applies to all parents)
			updatedNodes.addAll(parent.logWork(timeEntry));
		}
		
		return updatedNodes;
	}
	
	public List<WorkItem> logWork2(TimeEntry timeEntry) {
		List<WorkItem> updatedNodes = new ArrayList<WorkItem>(); 
		
		//update the work done for this item
		getTimeCard().addEntry(timeEntry);
		updatedNodes.add(this);
		
		WorkItem parent = getParent();
		if(parent != null) {
			//if parent is not null, cascade the work done updward (since it also applies to all parents)
			updatedNodes.addAll(parent.logWork(timeEntry));
		}
		
		return updatedNodes;
	}
	
	public double getRemainingWork() {	
		return getEstimate()-getTimeCard().getTotalWorkDone();
	}
	
	/**
	 * Method that given an concrete workitem class and another workitem, will convert the workitem
	 * to the appropriate type to be a child of hte provided parent
	 * 
	 * @return
	 */
	protected WorkItem convertToChildType(WorkItem childToBe) {
		Class childClass = this.getChildClass();
		WorkItem convertedChild = null;
		try {
			convertedChild = (WorkItem) childClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
		
		convertedChild.init(childToBe);
		
		return convertedChild;
	}
}

