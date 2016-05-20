package zinger.wbj.gae;

import com.google.common.base.*;

import java.util.*;

import javax.jdo.*;
import javax.jdo.annotations.*;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PoolTally
{
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private com.google.appengine.api.datastore.Key key;
	
	@Persistent
	private String user;
	
	@Persistent
	private String location;
	
	@Persistent
	private String pool;
	
	@Persistent
	private int dayOfWeek;
	
	@Persistent
	private float tally;
	
	public PoolTally(final String user, final String location, final String pool, final int dayOfWeek, final float tally)
	{
		this.user = user;
		this.pool = pool;
		this.location = location;
		this.dayOfWeek = dayOfWeek;
		this.tally = tally;
	}
	
	public com.google.appengine.api.datastore.Key getKey() { return key; }
	
	public String getUser() { return user; }
	
	public String getLocation() { return location; }
	
	public String getPool() { return pool; }
	
	public int getDayOfWeek() { return dayOfWeek; }
	
	public float getTally() { return tally; }
	
	public int hashCode()
	{
		return Objects.hashCode(getUser(), getLocation(), getPool(), getDayOfWeek(), getTally());
	}
	
	public boolean equals(final Object obj)
	{
		if(!(obj instanceof PoolTally))
			return false;
		final PoolTally other = (PoolTally)obj;
		return
			Objects.equal(this.getUser(), other.getUser()) &&
			Objects.equal(this.getLocation(), other.getLocation()) &&
			Objects.equal(this.getPool(), other.getPool()) &&
			this.getDayOfWeek() == other.getDayOfWeek() &&
			this.getTally() == other.getTally();
	}
	
	public String toString()
	{
		return Objects.toStringHelper(this)
			.add("user", getUser())
			.add("location", getLocation())
			.add("pool", getPool())
			.add("dayOfWeek", getDayOfWeek())
			.add("tally", getTally())
			.toString();
	}
}

