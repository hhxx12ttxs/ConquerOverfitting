package by.q64.promo.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * The persistent class for the shipment database table.
 * 
 */
@Entity
@Table(name="activities")
@NamedQuery(name="Activity.findAll", query="SELECT a FROM Activity a")
public class Activity implements Serializable {
	
    private static final long serialVersionUID = 5L;
    
    public static final int NO_ACTIVITY = 5;
    
    public static final int NO_FORM=0;
    public static final int WIN_FORM=1;
    public static final int HP_FORM=2;
    public static final int INTEL_FORM=3;
    

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    
    
    @JsonProperty("unitRegion")
    private int unitRegion;
    
    @JsonProperty("project")
    private int project;
    
    @JsonProperty("activity")
    private int activity;
    
    private int promoForm;
    

    @JsonIgnore
    private Timestamp started;

    @JsonIgnore
    private Timestamp ended;
    
    public int getUnitRegion() {
        return unitRegion;
    }
    public void setUnitRegion(int unitRegion) {
        this.unitRegion = unitRegion;
    }
    public int getActivity() {
        return activity;
    }
    public void setActivity(int unitActivity) {
        this.activity = unitActivity;
    }
    
    
    public int getPromoForm() {
		return promoForm;
	}
	public void setPromoForm(int promoForm) {
		this.promoForm = promoForm;
	}
	public int getProject() {
        return project;
    }
    public void setProject(int project) {
        this.project = project;
    }

    public int getId() {
        return id;
    }
    public Activity setId(int id) {
        this.id = id;
        return this;
    }
    public LocalDateTime getStarted() {
        return started.toLocalDateTime();
    }
    public void setStarted(LocalDateTime started) {
        this.started = Timestamp.valueOf(started);
    }
    public LocalDateTime getEnded() {
        return ended.toLocalDateTime();
    }
    public void setEnded(LocalDateTime ended) {
        this.ended = Timestamp.valueOf(ended);
    }
    
    public static Activity getActivity(int unitRegion, int project, int activity,
    		LocalDateTime started, LocalDateTime ended,int promoForm) {
    	Timestamp timestampStarted;
    	Timestamp timestampEnded = null;
    	if (started != null) {
    		timestampStarted = Timestamp.valueOf(started);
    	} else {
    		timestampStarted = new Timestamp(System.currentTimeMillis());
    	}
    	if (ended != null) {
    		timestampEnded = Timestamp.valueOf(ended);
    	}
    	return new Activity(unitRegion, project, activity, timestampStarted, timestampEnded,promoForm);
    }
    
	private Activity(int unitRegion, int project, int activity,
			Timestamp started, Timestamp ended,int promoForm) {
		super();
		this.unitRegion = unitRegion;
		this.project = project;
		this.activity = activity;
		this.started = started;
		this.ended = ended;
		this.promoForm=promoForm;
	}
	
	
	public Activity() {
		super();
		org.slf4j.LoggerFactory.getLogger(getClass()).warn("DEFAULT ACTIVITY CONSTRUCTOR");
	}
    

}
